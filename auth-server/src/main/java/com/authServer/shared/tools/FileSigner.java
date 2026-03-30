package com.authServer.shared.tools;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class FileSigner {

    private final Logger logger = LoggerFactory.getLogger(FileSigner.class);

    @Value("${file.signature.secret}")
    private String secret;

    @PostConstruct
    public void validateConfiguration() {
        if (secret == null || secret.trim().isEmpty() || secret.equals("changeme-in-production")) {
            logger.error("FILE_SIGNATURE_SECRET is not properly configured!");
            throw new IllegalStateException(
                "file.signature.secret must be set to a secure value in production"
            );
        }
        if (secret.length() < 32) {
            logger.warn("file.signature.secret should be at least 32 characters for optimal security");
        }
    }

    public String sign(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error generating file signature: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating file signature", e);
        }
    }
}
