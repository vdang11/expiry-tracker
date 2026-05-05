package com.expiry.infrastructure.image;

import com.expiry.exception.InvalidImageException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Component
public class ImageValidator {

    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5MB per file
    private static final int MAX_IMAGES = 2;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    public void validateAll(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new InvalidImageException("IMAGES_EMPTY", "At least 1 image is required.");
        }
        if (files.size() > MAX_IMAGES) {
            throw new InvalidImageException("TOO_MANY_IMAGES", "Max " + MAX_IMAGES + " images are allowed.");
        }
        for (MultipartFile f : files) {
            validateOne(f);
        }
    }

    public void validateOne(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("IMAGE_EMPTY", "Image file is required.");
        }

        if (file.getSize() > MAX_BYTES) {
            throw new InvalidImageException("IMAGE_TOO_LARGE", "Image must be <= 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new InvalidImageException("IMAGE_UNSUPPORTED_TYPE", "Only JPG, PNG, WEBP are supported.");
        }
    }
}