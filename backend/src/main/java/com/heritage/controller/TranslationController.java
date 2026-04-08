package com.heritage.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j

/**
 * 翻译接口控制器
 * 代理百度翻译API，避免前端暴露密钥
 */
@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    @Value("${baidu.translate.appid}")
    private String appId;

    @Value("${baidu.translate.secret}")
    private String secret;

    private static final String BAIDU_API_URL = "https://api.fanyi.baidu.com/api/trans/vip/translate";
    // 百度翻译API单次最大字符数
    private static final int MAX_LENGTH = 2000;

    /**
     * 测试翻译 - 用于调试签名问题
     */
    @GetMapping("/test")
    public Map<String, Object> testTranslate() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 使用固定文本测试
            String testText = "hello";
            String salt = String.valueOf(System.currentTimeMillis());

            // 签名
            String signStr = appId + testText + salt + secret;
            String sign = DigestUtils.md5DigestAsHex(signStr.getBytes(StandardCharsets.UTF_8));

            // 构建URL
            String encodedQ = URLEncoder.encode(testText, "UTF-8");
            String url = BAIDU_API_URL +
                    "?q=" + encodedQ +
                    "&from=en" +
                    "&to=zh" +
                    "&appid=" + appId +
                    "&salt=" + salt +
                    "&sign=" + sign;

            // 完整调试信息
            result.put("step1_appid", appId);
            result.put("step2_q", testText);
            result.put("step3_salt", salt);
            result.put("step4_secret_full", secret);
            result.put("step5_signString", signStr);
            result.put("step6_sign", sign);
            result.put("step7_url", url);

            // 发送请求
            RestTemplate restTemplate = new RestTemplate();
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            result.put("response", response);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 单条翻译
     * 
     * @param q    要翻译的文本
     * @param from 源语言（默认zh）
     * @param to   目标语言（默认en）
     */
    @GetMapping
    public Map<String, Object> translate(
            @RequestParam String q,
            @RequestParam(defaultValue = "zh") String from,
            @RequestParam(defaultValue = "en") String to) {

        return doTranslate(q, from, to);
    }

    /**
     * 批量翻译 - 多段文本合并优化版
     * 利用百度API的多段文本功能，单次调用翻译多条文本
     * 多段文本用换行符分隔，大幅减少API调用次数
     */
    @PostMapping("/batch")
    public Map<String, Object> translateBatch(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<String> texts = (List<String>) request.get("texts");
            String from = (String) request.getOrDefault("from", "zh");
            String to = (String) request.getOrDefault("to", "en");

            if (texts == null || texts.isEmpty()) {
                result.put("code", 200);
                result.put("data", new ArrayList<String>());
                return result;
            }

            // 初始化结果列表，保持与输入相同的长度
            List<String> translatedTexts = new ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                translatedTexts.add("");
            }

            // 记录非空文本的索引和内容
            List<Integer> validIndices = new ArrayList<>();
            List<String> validTexts = new ArrayList<>();

            for (int i = 0; i < texts.size(); i++) {
                String text = texts.get(i);
                if (text != null && !text.trim().isEmpty()) {
                    validIndices.add(i);
                    // 将原文中的换行符替换为特殊占位符，避免与分隔符混淆
                    String processedText = text.trim()
                            .replace("\r\n", " ") // Windows换行符替换为空格
                            .replace("\n", " ") // Unix换行符替换为空格
                            .replace("\r", " "); // 旧Mac换行符替换为空格
                    validTexts.add(processedText);
                }
            }

            if (validTexts.isEmpty()) {
                // 全部为空文本
                result.put("code", 200);
                result.put("data", translatedTexts);
                return result;
            }

            log.info("批量翻译：{} 条文本待翻译", validTexts.size());

            // 分批翻译（考虑单次请求字符数限制）
            List<String> allTranslations = translateInBatches(validTexts, from, to);

            // 按原始索引填充结果
            for (int i = 0; i < validIndices.size() && i < allTranslations.size(); i++) {
                int originalIndex = validIndices.get(i);
                String translation = allTranslations.get(i);
                translatedTexts.set(originalIndex, translation);
            }

            result.put("code", 200);
            result.put("data", translatedTexts);

        } catch (Exception e) {
            log.error("批量翻译异常", e);
            result.put("code", 500);
            result.put("message", "批量翻译失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 分批翻译文本列表
     * 自动按字符数限制分批，确保单次API调用不超过限制
     */
    private List<String> translateInBatches(List<String> texts, String from, String to) {
        List<String> allTranslations = new ArrayList<>();

        // 单次请求安全字符数限制（预留100字符余量）
        final int BATCH_CHAR_LIMIT = MAX_LENGTH - 100;

        List<String> currentBatch = new ArrayList<>();
        int currentBatchLength = 0;

        for (String text : texts) {
            int textLength = text.length();

            // 如果单条文本就超过限制，需要截断
            if (textLength > BATCH_CHAR_LIMIT) {
                // 先处理当前批次
                if (!currentBatch.isEmpty()) {
                    allTranslations.addAll(translateCombinedTexts(currentBatch, from, to));
                    currentBatch.clear();
                    currentBatchLength = 0;
                }

                // 单独处理超长文本（截断）
                String truncated = text.substring(0, BATCH_CHAR_LIMIT);
                List<String> singleResult = translateCombinedTexts(List.of(truncated), from, to);
                allTranslations.add(singleResult.get(0));
                continue;
            }

            // 检查加入当前文本是否会超限
            // +1 是换行符的长度
            if (currentBatchLength + textLength + 1 > BATCH_CHAR_LIMIT && !currentBatch.isEmpty()) {
                // 当前批次已满，先翻译
                allTranslations.addAll(translateCombinedTexts(currentBatch, from, to));
                currentBatch.clear();
                currentBatchLength = 0;
            }

            currentBatch.add(text);
            currentBatchLength += textLength + 1; // +1 for separator
        }

        // 处理最后一批
        if (!currentBatch.isEmpty()) {
            allTranslations.addAll(translateCombinedTexts(currentBatch, from, to));
        }

        return allTranslations;
    }

    /**
     * 翻译合并后的文本列表（单次API调用）
     * 多段文本用换行符连接，一次翻译
     * 
     * 注意：百度API对于多段文本（换行符分隔），返回的 trans_result 是数组，
     * 每段文本对应一个元素，格式如下：
     * [
     * {"src": "第一段原文", "dst": "First paragraph"},
     * {"src": "第二段原文", "dst": "Second paragraph"}
     * ]
     */
    private List<String> translateCombinedTexts(List<String> texts, String from, String to) {
        List<String> translations = new ArrayList<>();

        if (texts.isEmpty()) {
            return translations;
        }

        // 用换行符连接所有文本
        String combinedText = String.join("\n", texts);

        log.info("合并翻译：{} 条文本，总字符数 {}", texts.size(), combinedText.length());

        // 调用翻译API
        Map<String, Object> translateResult = doTranslate(combinedText, from, to);

        if ((Integer) translateResult.get("code") == 200) {
            Map<String, Object> data = (Map<String, Object>) translateResult.get("data");
            List<Map<String, String>> transResult = (List<Map<String, String>>) data.get("trans_result");

            if (transResult != null && !transResult.isEmpty()) {
                log.info("翻译返回 {} 条结果", transResult.size());

                // 百度API返回的trans_result是数组，每段文本对应一个元素
                for (int i = 0; i < texts.size(); i++) {
                    if (i < transResult.size()) {
                        // 获取对应的翻译结果
                        String translated = transResult.get(i).get("dst");
                        translations.add(translated != null ? translated : texts.get(i));
                    } else {
                        // 如果返回结果少于输入数量，使用原文
                        translations.add(texts.get(i));
                    }
                }
            } else {
                // 翻译结果为空，返回原文
                log.warn("翻译结果为空，返回原文");
                translations.addAll(texts);
            }
        } else {
            // 翻译失败，返回原文
            log.warn("批量翻译失败，返回原文: {}", translateResult.get("message"));
            translations.addAll(texts);
        }

        return translations;
    }

    /**
     * 执行翻译请求
     * 支持多段文本翻译（换行符分隔）
     */
    private Map<String, Object> doTranslate(String q, String from, String to) {
        Map<String, Object> result = new HashMap<>();

        // 空文本直接返回
        if (q == null || q.trim().isEmpty()) {
            result.put("code", 200);
            result.put("data", Map.of("trans_result", List.of()));
            return result;
        }

        // 预处理文本：合并多余空格，但保留换行符（用于多段文本翻译）
        String textToTranslate = q
                .replaceAll("[ \\t]+", " ") // 合并多个空格和制表符
                .trim();

        // 如果超过最大长度，需要截断
        if (textToTranslate.length() > MAX_LENGTH) {
            textToTranslate = textToTranslate.substring(0, MAX_LENGTH);
            log.warn("文本超过最大长度限制，已截断至 {} 字符", MAX_LENGTH);
        }

        try {
            // 生成10位随机数作为salt（符合百度API文档要求）
            String salt = String.format("%010d", (long) (Math.random() * 10000000000L));

            // 清理空格（关键：防止配置文件末尾的空格或换行）
            String appIdClean = appId.trim();
            String secretClean = secret.trim();

            // 百度翻译API签名规则：MD5(appid + query + salt + 密钥)
            // 注意：签名必须使用原始文本，不能有任何编码
            String signStr = appIdClean + textToTranslate + salt + secretClean;
            String sign = DigestUtils.md5DigestAsHex(signStr.getBytes(StandardCharsets.UTF_8));

            // 打印详细调试信息
            System.out.println("=== 翻译调试信息 ===");
            System.out.println("APPID: [" + appIdClean + "] (长度:" + appIdClean.length() + ")");
            System.out.println("原文本: [" + textToTranslate + "]");
            System.out.println("文本长度: " + textToTranslate.length());
            System.out.println("Salt: [" + salt + "] (长度: " + salt.length() + ")");
            System.out.println("密钥: [" + secretClean + "] (长度:" + secretClean.length() + ")");
            System.out.println("签名字符串: [" + signStr + "]");
            System.out.println("签名字符串长度: " + signStr.length());
            System.out.println("MD5签名: " + sign);
            System.out.println("签名长度: " + sign.length());
            System.out.println("==================");

            // 构建请求参数 - 改为POST请求（推荐）
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("q", textToTranslate);
            params.add("from", from);
            params.add("to", to);
            params.add("appid", appIdClean);
            params.add("salt", salt);
            params.add("sign", sign);

            // 发送POST请求
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(BAIDU_API_URL, request, Map.class);

            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("trans_result")) {
                result.put("code", 200);
                result.put("data", response);
                result.put("message", "success");
            } else {
                // 返回详细错误信息
                Object errorCode = response != null ? response.get("error_code") : null;
                Object errorMsg = response != null ? response.get("error_msg") : "翻译服务无响应";
                result.put("code", 500);
                result.put("message", "翻译失败: " + errorMsg);
                result.put("error_code", errorCode);
                System.err.println("Translation API error: " + errorCode + " - " + errorMsg);
            }

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "翻译异常: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}
