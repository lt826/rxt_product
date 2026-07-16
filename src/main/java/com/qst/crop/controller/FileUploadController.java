package com.qst.crop.controller;

import com.qst.crop.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传控制器
 * 农户/买家发布商品时上传图片，返回可直接访问的图片 URL。
 * 上传的文件保存到本地 {app.upload.dir}/goods 目录，通过 /uploads/goods/** 访问。
 */
@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_EXT = new HashSet<>(
            Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "上传文件为空");
        }

        // 1. 校验 ContentType
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只支持上传图片文件");
        }

        // 2. 校验扩展名
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        if (ext.isEmpty() || !ALLOWED_EXT.contains(ext)) {
            return Result.error(400, "不支持的图片格式（仅支持 jpg/jpeg/png/gif/webp）");
        }

        // 3. 保存到 {uploadDir}/goods，目录不存在则自动创建
        try {
            Path dir = Paths.get(uploadDir, "goods");
            Files.createDirectories(dir);
            // 时间戳 + 随机串，避免重名覆盖
            String filename = System.currentTimeMillis() + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + ext;
            Path target = dir.resolve(filename);
            // 用 InputStream 拷贝，规避 MultipartFile.transferTo 对相对路径的歧义
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target);
            }

            // 4. 拼接可直接访问的完整 URL（根据当前请求动态获取协议+主机+端口）
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            String url = baseUrl + "/uploads/goods/" + filename;
            return Result.success("上传成功", url);
        } catch (IOException e) {
            return Result.error(500, "图片保存失败：" + e.getMessage());
        }
    }
}
