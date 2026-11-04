package com.integrity.assignment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 原文文件存储。
 *
 * <p>P0 用本地目录替代 MinIO（对应手册 submission-files bucket）。
 * 返回的 fileKey 写到 submissions.file_key；P1 换成 MinIO 实现时键格式保持兼容。
 */
@Service
public class FileStorageService {

    @Value("${integrity.storage.local-dir:./data/files}")
    private String localDir;

    /** 落盘并返回对象键，形如 202610/&lt;uuid&gt;.docx */
    public String store(byte[] bytes, String originalName) {
        String ext = "";
        int dot = originalName == null ? -1 : originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot);
        }
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = month + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Path target = Path.of(localDir, key);
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
            return key;
        } catch (IOException e) {
            throw new IllegalStateException("文件落盘失败: " + e.getMessage(), e);
        }
    }
}
