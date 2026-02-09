package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.BusinessException;
import com.heritage.common.Result;
import com.heritage.dto.AiImageGenerateResponse;
import com.heritage.dto.AiImageRecordVO;
import com.heritage.dto.AiTextToImageRequest;
import com.heritage.dto.PageQuery;
import com.heritage.service.AiImageService;
import com.heritage.service.AiImageRecordService;
import com.heritage.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customize/ai-image")
@RequiredArgsConstructor
@Tag(name = "智能定制-AI生图", description = "腾讯混元AI生图接口")
public class AiImageController {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final AiImageService aiImageService;

    private final AiImageRecordService aiImageRecordService;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @PostMapping("/text-to-image")
    @Operation(summary = "文生图")
    public Result<AiImageGenerateResponse> textToImage(@Valid @RequestBody AiTextToImageRequest request) {
        Long userId = currentUserId();
        AiImageGenerateResponse response = aiImageService.textToImage(request);
        aiImageRecordService.saveRecords(userId, null, request.getPrompt(), buildHistoryResultUrls(normalizeResultImages(response)));
        return Result.success(response);
    }

    @GetMapping("/records")
    @Operation(summary = "历史生图记录分页（当前用户）")
    public Result<Page<AiImageRecordVO>> pageRecords(PageQuery query) {
        Long userId = currentUserId();
        return Result.success(aiImageRecordService.pageUserRecords(userId, query));
    }

    @PostMapping("/image-to-image")
    @Operation(summary = "图生图（上传原图）")
    public Result<AiImageGenerateResponse> imageToImage(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("prompt") String prompt,
                                                        @RequestParam(value = "negativePrompt", required = false) String negativePrompt,
                                                        @RequestParam(value = "strength", required = false) Float strength,
                                                        @RequestParam(value = "resolution", required = false) String resolution,
                                                        @RequestParam(value = "enhanceImage", required = false) Boolean enhanceImage,
                                                        @RequestParam(value = "restoreFace", required = false) Boolean restoreFace,
                                                        @RequestParam(value = "styles", required = false) String[] styles,
                                                        @RequestParam(value = "count", required = false) Integer count,
                                                        @RequestParam(value = "rspImgType", required = false) String rspImgType) {
        Long userId = currentUserId();
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new BusinessException("读取图片失败");
        }
        String originalImageUrl = saveHistoryReferenceImage(bytes, file.getOriginalFilename());
        AiImageGenerateResponse response = aiImageService.imageToImage(bytes, prompt, negativePrompt, strength, resolution, enhanceImage, restoreFace, styles, count, rspImgType);
        aiImageRecordService.saveRecords(userId, originalImageUrl, prompt, buildHistoryResultUrls(normalizeResultImages(response)));
        return Result.success(response);
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new BusinessException("未登录");
        }
        return userDetails.getUserId();
    }

    private List<String> normalizeResultImages(AiImageGenerateResponse response) {
        if (response == null) {
            return List.of();
        }
        if (response.getResultImages() != null && !response.getResultImages().isEmpty()) {
            return response.getResultImages();
        }
        if (StringUtils.hasText(response.getResultImage())) {
            return List.of(response.getResultImage());
        }
        return List.of();
    }

    private List<String> buildHistoryResultUrls(List<String> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        try {
            return persistImagesToLocalUploads(images);
        } catch (Exception e) {
            return images.stream()
                    .filter(StringUtils::hasText)
                    .filter(s -> s.length() <= 255)
                    .toList();
        }
    }

    private String saveHistoryReferenceImage(byte[] imageBytes, String originalFilename) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }

        String ext = ".png";
        if (originalFilename != null) {
            int idx = originalFilename.lastIndexOf('.');
            if (idx >= 0 && idx < originalFilename.length() - 1) {
                String candidate = originalFilename.substring(idx).toLowerCase();
                if (candidate.length() <= 10) {
                    ext = candidate;
                }
            }
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().resolve("ai-history-ref");
        try {
            Files.createDirectories(uploadDir);
            Files.write(uploadDir.resolve(filename), imageBytes);
        } catch (Exception e) {
            throw new BusinessException("保存参考图失败");
        }
        return "/uploads/ai-history-ref/" + filename;
    }

    private List<String> persistImagesToLocalUploads(List<String> images) {
        List<String> out = new ArrayList<>();
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().resolve("ai-history-result");
        try {
            Files.createDirectories(uploadDir);
        } catch (Exception e) {
            return images.stream()
                    .filter(StringUtils::hasText)
                    .filter(s -> s.length() <= 255)
                    .toList();
        }

        for (String img : images) {
            if (!StringUtils.hasText(img)) {
                continue;
            }
            String trimmed = img.trim();
            if (trimmed.startsWith("/uploads/")) {
                if (trimmed.length() <= 255) {
                    out.add(trimmed);
                }
                continue;
            }

            try {
                String persisted = persistSingleImage(trimmed, uploadDir);
                if (StringUtils.hasText(persisted) && persisted.length() <= 255) {
                    out.add(persisted);
                }
            } catch (Exception ignored) {
                if (trimmed.length() <= 255) {
                    out.add(trimmed);
                }
            }
        }
        return out;
    }

    private String persistSingleImage(String img, Path uploadDir) throws Exception {
        if (img.startsWith("http://") || img.startsWith("https://")) {
            return persistFromRemoteUrl(img, uploadDir);
        }
        if (img.startsWith("data:")) {
            return persistFromDataUrl(img, uploadDir);
        }
        return persistFromBase64(img, uploadDir);
    }

    private String persistFromRemoteUrl(String url, Path uploadDir) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();
        HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("图片下载失败");
        }
        byte[] bytes = response.body();
        if (bytes == null || bytes.length == 0) {
            throw new IllegalStateException("图片内容为空");
        }

        String contentType = response.headers().firstValue("content-type").orElse("");
        String ext = extFromContentType(contentType);
        if (ext == null) {
            ext = extFromUrl(url);
        }
        if (ext == null) {
            ext = ".png";
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Files.write(uploadDir.resolve(filename), bytes);
        return "/uploads/ai-history-result/" + filename;
    }

    private String persistFromDataUrl(String dataUrl, Path uploadDir) throws Exception {
        int comma = dataUrl.indexOf(',');
        if (comma <= 0) {
            throw new IllegalArgumentException("data url格式错误");
        }
        String header = dataUrl.substring(0, comma);
        String data = dataUrl.substring(comma + 1);
        if (!header.contains("base64")) {
            throw new IllegalArgumentException("仅支持base64 data url");
        }

        String ext = ".png";
        int idx = header.indexOf(':');
        int sep = header.indexOf(';');
        if (idx >= 0 && sep > idx) {
            String mime = header.substring(idx + 1, sep).trim().toLowerCase();
            String mimeExt = extFromMime(mime);
            if (mimeExt != null) {
                ext = mimeExt;
            }
        }

        byte[] bytes = Base64.getDecoder().decode(data);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Files.write(uploadDir.resolve(filename), bytes);
        return "/uploads/ai-history-result/" + filename;
    }

    private String persistFromBase64(String base64, Path uploadDir) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(base64);
        String ext = ".png";
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Files.write(uploadDir.resolve(filename), bytes);
        return "/uploads/ai-history-result/" + filename;
    }

    private String extFromContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        String lowered = contentType.toLowerCase();
        int semicolon = lowered.indexOf(';');
        if (semicolon > 0) {
            lowered = lowered.substring(0, semicolon).trim();
        }
        return extFromMime(lowered);
    }

    private String extFromMime(String mime) {
        if (!StringUtils.hasText(mime)) {
            return null;
        }
        return switch (mime) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            case "image/gif" -> ".gif";
            default -> null;
        };
    }

    private String extFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        int q = url.indexOf('?');
        String base = q > 0 ? url.substring(0, q) : url;
        int dot = base.lastIndexOf('.');
        if (dot < 0 || dot == base.length() - 1) {
            return null;
        }
        String ext = base.substring(dot).toLowerCase();
        if (ext.length() > 10) {
            return null;
        }
        if (ext.equals(".jpeg")) {
            return ".jpg";
        }
        return ext;
    }
}
