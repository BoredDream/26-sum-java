package com.training.system.info.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件上传校验工具
 */
public class FileValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "zip", "rar", "jpg", "jpeg", "png", "gif"
    ));

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public static String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) return "文件为空";
        if (file.getSize() > MAX_FILE_SIZE) return "文件大小超出限制（最大10MB）";
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) return "不支持的文件类型：" + ext;
        return null;
    }

    private static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
