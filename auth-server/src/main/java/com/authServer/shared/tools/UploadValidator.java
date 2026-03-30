package com.authServer.shared.tools;

import com.authServer.exceptions.InvalidInsertDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UploadValidator {

    @Value("${upload.max-file-size:104857600}") // 100MB default
    private long maxFileSize;

    public void validateUpload(MultipartFile file) throws InvalidInsertDetails {
        if (file.getSize() > maxFileSize) {
            throw new InvalidInsertDetails(
                "File too large. Maximum size is " + (maxFileSize / 1024 / 1024) + "MB"
            );
        }

        // Validate file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
            throw new InvalidInsertDetails("Only ZIP files are allowed");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (!"application/zip".equals(contentType) &&
            !"application/x-zip-compressed".equals(contentType)) {
            throw new InvalidInsertDetails("Invalid file type");
        }
    }
}