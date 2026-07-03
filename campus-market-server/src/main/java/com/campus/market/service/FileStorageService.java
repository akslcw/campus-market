package com.campus.market.service;

import com.campus.market.config.UploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地文件存储服务
 * @author 成员二
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final UploadProperties uploadProperties;

    /**
     * 保存上传的图片，返回访问 URL
     * 文件名格式: goods/yyyyMM/dd/uuid.扩展名
     */
    public String save(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        // 生成相对路径：goods/yyyyMM/dd/uuid.ext
        String dateDir = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMM/dd"));
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relativePath = "goods/" + dateDir + "/" + filename;

        // 写入磁盘
        Path fullPath = Paths.get(uploadProperties.getPath(), relativePath);
        try {
            Files.createDirectories(fullPath.getParent());
            // 用 Files.copy 而非 MultipartFile.transferTo：后者对相对路径会解析到
            // Tomcat 临时工作目录，与 createDirectories 的基准不一致，导致 FileNotFound。
            try (java.io.InputStream in = file.getInputStream()) {
                Files.copy(in, fullPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("文件保存成功: {}", fullPath);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new RuntimeException("文件保存失败", e);
        }

        // 返回访问 URL
        return uploadProperties.getUrlPrefix() + "/" + relativePath.replace("\\", "/");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
