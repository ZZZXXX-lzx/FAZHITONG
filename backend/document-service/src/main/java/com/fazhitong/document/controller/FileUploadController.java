package com.fazhitong.document.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/document/file")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.access-url:/uploads}")
    private String accessUrl;

    @PostMapping("/upload")
    public ApiResult<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new BusinessException("文件名不能为空");
        }

        // Generate date-based subdirectory
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String suffix = "";
        int dotIdx = originalName.lastIndexOf(".");
        if (dotIdx > 0) {
            suffix = originalName.substring(dotIdx);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // Create directory if not exists
        File dir = new File(uploadDir + File.separator + datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建上传目录失败");
        }

        // Save file
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        // Return accessible URL
        String url = accessUrl + "/" + datePath + "/" + fileName;
        return ApiResult.success(url);
    }
}
