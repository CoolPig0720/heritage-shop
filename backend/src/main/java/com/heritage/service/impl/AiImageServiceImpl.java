package com.heritage.service.impl;

import com.heritage.common.BusinessException;
import com.heritage.config.AiTencentCloudProperties;
import com.heritage.dto.AiImageGenerateResponse;
import com.heritage.dto.AiTextToImageRequest;
import com.heritage.service.AiImageService;
import com.tencentcloudapi.aiart.v20221229.AiartClient;
import com.tencentcloudapi.aiart.v20221229.models.ImageToImageRequest;
import com.tencentcloudapi.aiart.v20221229.models.ImageToImageResponse;
import com.tencentcloudapi.aiart.v20221229.models.QueryTextToImageJobRequest;
import com.tencentcloudapi.aiart.v20221229.models.QueryTextToImageJobResponse;
import com.tencentcloudapi.aiart.v20221229.models.ResultConfig;
import com.tencentcloudapi.aiart.v20221229.models.SubmitTextToImageJobRequest;
import com.tencentcloudapi.aiart.v20221229.models.SubmitTextToImageJobResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiImageServiceImpl implements AiImageService {

    private static final String DEFAULT_REGION = "ap-guangzhou";
    private static final String DEFAULT_RSP_IMG_TYPE = "url";
    private static final String DEFAULT_RESOLUTION = "1024:1024";
    private static final int MAX_BATCH_COUNT = 4;
    private static final int DEFAULT_HTTP_TIMEOUT_MS = 180_000;
    private static final long TEXT_TO_IMAGE_JOB_MAX_WAIT_MS = 600_000;
    private static final long TEXT_TO_IMAGE_JOB_POLL_INTERVAL_MS = 1_000;
    private static final long TEXT_TO_IMAGE_JOB_POLL_MAX_INTERVAL_MS = 5_000;
    private static final String DEFAULT_UPLOAD_PATH = "uploads";

    private final AiTencentCloudProperties tencentCloudProperties;
    private final Environment environment;

    @Override
    public AiImageGenerateResponse textToImage(AiTextToImageRequest request) {
        String prompt = normalizeRequired(request.getPrompt(), "Prompt不能为空");
        String rspImgType = normalizeRspImgType(request.getRspImgType());
        if (!"url".equals(rspImgType)) {
            throw new BusinessException("混元生图3.0暂仅支持返回url");
        }

        AiartClient client = createClient();
        SubmitTextToImageJobRequest apiRequest = new SubmitTextToImageJobRequest();
        apiRequest.setPrompt(prompt);

        String resolution = normalizeOptional(request.getResolution());
        if (resolution != null) {
            apiRequest.setResolution(resolution);
        } else {
            apiRequest.setResolution(DEFAULT_RESOLUTION);
        }

        Long seed = request.getSeed();
        if (seed != null) {
            apiRequest.setSeed(seed);
        }

        SubmitTextToImageJobResponse submitResponse;
        try {
            submitResponse = client.SubmitTextToImageJob(apiRequest);
        } catch (TencentCloudSDKException e) {
            throw new BusinessException(normalizeTencentError(e));
        }

        QueryTextToImageJobResponse queryResponse = waitTextToImageJobDone(client, submitResponse.getJobId());

        AiImageGenerateResponse response = new AiImageGenerateResponse();
        response.setRequestId(queryResponse.getRequestId());
        List<String> images = queryResponse.getResultImage() == null ? List.of() : List.of(queryResponse.getResultImage());
        response.setResultImages(images);
        response.setResultImage(images.isEmpty() ? null : images.get(0));
        return response;
    }

    @Override
    public AiImageGenerateResponse imageToImage(byte[] imageBytes,
                                                String prompt,
                                                String negativePrompt,
                                                Float strength,
                                                String resolution,
                                                Boolean enhanceImage,
                                                Boolean restoreFace,
                                                String[] styles,
                                                Integer count,
                                                String rspImgType) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new BusinessException("图片不能为空");
        }
        String normalizedPrompt = normalizeRequired(prompt, "Prompt不能为空");
        String normalizedRspImgType = normalizeRspImgType(rspImgType);
        int normalizedCount = normalizeCount(count);

        AiartClient client = createClient();
        List<String> images = new ArrayList<>(normalizedCount);
        String requestId = null;
        if (shouldUseTextToImageJobForImg2Img()) {
            if (!"url".equals(normalizedRspImgType)) {
                throw new BusinessException("混元生图3.0暂仅支持返回url");
            }

            String referenceImageUrl = saveReferenceImageAndGetPublicUrl(imageBytes);
            for (int i = 0; i < normalizedCount; i++) {
                SubmitTextToImageJobRequest apiRequest = new SubmitTextToImageJobRequest();
                apiRequest.setPrompt(buildImageToImagePrompt(normalizedPrompt, negativePrompt, strength, enhanceImage, restoreFace, styles));
                apiRequest.setImages(new String[]{referenceImageUrl});

                String normalizedResolution = normalizeOptional(resolution);
                if (normalizedResolution != null) {
                    apiRequest.setResolution(normalizedResolution);
                } else {
                    apiRequest.setResolution(DEFAULT_RESOLUTION);
                }

                SubmitTextToImageJobResponse submitResponse;
                try {
                    submitResponse = client.SubmitTextToImageJob(apiRequest);
                } catch (TencentCloudSDKException e) {
                    throw new BusinessException(normalizeTencentError(e));
                }

                QueryTextToImageJobResponse queryResponse = waitTextToImageJobDone(client, submitResponse.getJobId());
                if (requestId == null) {
                    requestId = queryResponse.getRequestId();
                }
                String[] result = queryResponse.getResultImage();
                if (result != null && result.length > 0 && result[0] != null && !result[0].isBlank()) {
                    images.add(result[0]);
                }
            }
        } else {
            String inputBase64 = Base64.getEncoder().encodeToString(imageBytes);
            for (int i = 0; i < normalizedCount; i++) {
                ImageToImageRequest apiRequest = new ImageToImageRequest();
                apiRequest.setInputImage(inputBase64);
                apiRequest.setPrompt(normalizedPrompt);
                apiRequest.setRspImgType(normalizedRspImgType);

                String normalizedNegativePrompt = normalizeOptional(negativePrompt);
                if (normalizedNegativePrompt != null) {
                    apiRequest.setNegativePrompt(normalizedNegativePrompt);
                }

                if (styles != null && styles.length > 0) {
                    apiRequest.setStyles(styles);
                }

                if (strength != null) {
                    if (strength < 0 || strength > 1) {
                        throw new BusinessException("Strength范围应为0~1");
                    }
                    apiRequest.setStrength(strength);
                }

                String normalizedResolution = normalizeOptional(resolution);
                if (normalizedResolution != null) {
                    ResultConfig cfg = new ResultConfig();
                    cfg.setResolution(normalizedResolution);
                    setSdkField(apiRequest, "ResultConfig", cfg);
                }

                if (Boolean.TRUE.equals(enhanceImage)) {
                    apiRequest.setEnhanceImage(1L);
                }
                if (Boolean.TRUE.equals(restoreFace)) {
                    apiRequest.setRestoreFace(1L);
                }

                ImageToImageResponse apiResponse;
                try {
                    apiResponse = client.ImageToImage(apiRequest);
                } catch (TencentCloudSDKException e) {
                    throw new BusinessException(normalizeTencentError(e));
                }
                if (requestId == null) {
                    requestId = apiResponse.getRequestId();
                }
                if (apiResponse.getResultImage() != null && !apiResponse.getResultImage().isBlank()) {
                    images.add(apiResponse.getResultImage());
                }
            }
        }

        AiImageGenerateResponse response = new AiImageGenerateResponse();
        response.setRequestId(requestId);
        response.setResultImage(images.isEmpty() ? null : images.get(0));
        response.setResultImages(images);
        return response;
    }

    private AiartClient createClient() {
        String secretId = normalizeOptional(tencentCloudProperties.getSecretId());
        String secretKey = normalizeOptional(tencentCloudProperties.getSecretKey());
        String region = normalizeOptional(tencentCloudProperties.getRegion());
        String endpoint = normalizeOptional(tencentCloudProperties.getEndpoint());

        if (secretId == null || secretKey == null) {
            throw new BusinessException("请配置ai.tencentcloud.secret-id与ai.tencentcloud.secret-key");
        }

        Credential cred = new Credential(secretId, secretKey);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(endpoint == null ? "aiart.tencentcloudapi.com" : endpoint);
        httpProfile.setConnTimeout(DEFAULT_HTTP_TIMEOUT_MS);
        httpProfile.setReadTimeout(DEFAULT_HTTP_TIMEOUT_MS);
        httpProfile.setWriteTimeout(DEFAULT_HTTP_TIMEOUT_MS);
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new AiartClient(cred, region == null ? DEFAULT_REGION : region, clientProfile);
    }

    private String normalizeRspImgType(String v) {
        String normalized = normalizeOptional(v);
        if (normalized == null) {
            return DEFAULT_RSP_IMG_TYPE;
        }
        if (!"base64".equalsIgnoreCase(normalized) && !"url".equalsIgnoreCase(normalized)) {
            throw new BusinessException("RspImgType仅支持base64或url");
        }
        return normalized.toLowerCase();
    }

    private int normalizeCount(Integer count) {
        if (count == null) {
            return 1;
        }
        if (count < 1) {
            return 1;
        }
        return Math.min(count, MAX_BATCH_COUNT);
    }

    private String normalizeRequired(String v, String errorMsg) {
        String normalized = normalizeOptional(v);
        if (normalized == null) {
            throw new BusinessException(errorMsg);
        }
        return normalized;
    }

    private String normalizeOptional(String v) {
        if (v == null) {
            return null;
        }
        String trimmed = v.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeTencentError(TencentCloudSDKException e) {
        String msg = e.getMessage();
        if (msg == null) {
            return "腾讯云接口调用失败";
        }
        String trimmed = msg.trim();
        if (trimmed.isEmpty()) {
            return "腾讯云接口调用失败";
        }
        byte[] bytes = trimmed.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 300) {
            return trimmed;
        }
        return new String(bytes, 0, 300, StandardCharsets.UTF_8);
    }

    private String buildImageToImagePrompt(String prompt,
                                          String negativePrompt,
                                          Float strength,
                                          Boolean enhanceImage,
                                          Boolean restoreFace,
                                          String[] styles) {
        StringBuilder sb = new StringBuilder(prompt);
        String normalizedNegativePrompt = normalizeOptional(negativePrompt);
        if (normalizedNegativePrompt != null) {
            sb.append("\n负面描述：").append(normalizedNegativePrompt);
        }
        if (strength != null) {
            if (strength < 0 || strength > 1) {
                throw new BusinessException("Strength范围应为0~1");
            }
            sb.append("\n生成自由度：").append(String.format("%.2f", strength));
        }
        if (Boolean.TRUE.equals(enhanceImage)) {
            sb.append("\n画质增强：开启");
        }
        if (Boolean.TRUE.equals(restoreFace)) {
            sb.append("\n面部优化：开启");
        }
        if (styles != null && styles.length > 0) {
            List<String> normalized = new ArrayList<>();
            for (String style : styles) {
                String s = normalizeOptional(style);
                if (s != null) {
                    normalized.add(s);
                }
            }
            if (!normalized.isEmpty()) {
                sb.append("\n风格：").append(String.join(",", normalized));
            }
        }
        return sb.toString();
    }

    private boolean shouldUseTextToImageJobForImg2Img() {
        String baseUrl = normalizeOptional(environment.getProperty("file.public-base-url"));
        if (baseUrl == null) {
            return false;
        }
        String lowered = baseUrl.toLowerCase();
        if (lowered.contains("localhost") || lowered.contains("127.0.0.1")) {
            return false;
        }
        return lowered.startsWith("http://") || lowered.startsWith("https://");
    }

    private String saveReferenceImageAndGetPublicUrl(byte[] imageBytes) {
        String baseUrl = normalizeOptional(environment.getProperty("file.public-base-url"));
        if (baseUrl == null) {
            throw new BusinessException("图生图走混元生图3.0需要配置file.public-base-url为可公网访问的后端地址");
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            throw new BusinessException("file.public-base-url必须以http://或https://开头");
        }

        String uploadPath = normalizeOptional(environment.getProperty("file.upload.path"));
        if (uploadPath == null) {
            uploadPath = DEFAULT_UPLOAD_PATH;
        }

        String ext = sniffImageExtension(imageBytes);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().resolve("ai-ref");
        try {
            Files.createDirectories(uploadDir);
            Files.write(uploadDir.resolve(filename), imageBytes);
        } catch (IOException e) {
            throw new BusinessException("保存参考图失败");
        }

        return baseUrl + "/uploads/ai-ref/" + filename;
    }

    private String sniffImageExtension(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return ".jpg";
        }
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
            return ".jpg";
        }
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return ".png";
        }
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return ".webp";
        }
        return ".jpg";
    }

    private QueryTextToImageJobResponse waitTextToImageJobDone(AiartClient client, String jobId) {
        String normalizedJobId = normalizeRequired(jobId, "腾讯云返回的JobId为空");
        long start = System.currentTimeMillis();
        long interval = TEXT_TO_IMAGE_JOB_POLL_INTERVAL_MS;

        while (true) {
            QueryTextToImageJobRequest request = new QueryTextToImageJobRequest();
            request.setJobId(normalizedJobId);
            QueryTextToImageJobResponse response;
            try {
                response = client.QueryTextToImageJob(request);
            } catch (TencentCloudSDKException e) {
                throw new BusinessException(normalizeTencentError(e));
            }

            String statusCode = normalizeOptional(response.getJobStatusCode());
            if ("5".equals(statusCode)) {
                return response;
            }
            if ("4".equals(statusCode)) {
                String errorCode = normalizeOptional(response.getJobErrorCode());
                String errorMsg = normalizeOptional(response.getJobErrorMsg());
                throw new BusinessException((errorCode == null ? "任务失败" : errorCode) + (errorMsg == null ? "" : (": " + errorMsg)));
            }

            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > TEXT_TO_IMAGE_JOB_MAX_WAIT_MS) {
                throw new BusinessException("混元生图任务超时，请稍后重试");
            }

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("任务被中断");
            }
            interval = Math.min(interval * 2, TEXT_TO_IMAGE_JOB_POLL_MAX_INTERVAL_MS);
        }
    }

    private void setSdkField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("SDK字段赋值失败: " + target.getClass().getName() + "." + fieldName, e);
        }
    }
}
