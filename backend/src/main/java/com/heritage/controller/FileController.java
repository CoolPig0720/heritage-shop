package com.heritage.controller;

import com.heritage.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传相关接口")
public class FileController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("收到文件上传请求，文件名：{}", file.getOriginalFilename());
        log.info("文件大小：{} bytes", file.getSize());
        log.info("文件类型：{}", file.getContentType());
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            Path uploadDir = Paths.get(uploadPath).toAbsolutePath();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(filename).toAbsolutePath();
            file.transferTo(filePath.toFile());

            String fileUrl = "/uploads/" + filename;
            log.info("文件上传成功，URL：{}", fileUrl);
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }
}
