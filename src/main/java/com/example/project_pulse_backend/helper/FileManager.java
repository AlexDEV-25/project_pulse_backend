package com.example.project_pulse_backend.helper;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileManager {

    private final Cloudinary cloudinary;

    public void deleteFile(String url) throws Exception {
        String publicId = extractPublicId(url);

        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
    }

    private String fileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "file";
        }

        return originalFilename.toLowerCase();
    }

    public Map<?, ?> uploadImage(MultipartFile file) throws Exception {
        String avatarName = this.fileName(file);
        if (avatarName.endsWith(".jpg") || avatarName.endsWith(".jpeg") || avatarName.endsWith(".png")
                || avatarName.endsWith(".webp")) {
            String folder = "tailieu/image";
            String publicId = folder + "/" + UUID.randomUUID();

            return uploadToCloudinary(file, publicId);
        } else {
            throw AppException.builder().appError(AppError.INVALID_IMAGE_FORMAT).build();
        }

    }

    private Map<?, ?> uploadToCloudinary(MultipartFile file, String publicId) throws Exception {

        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "image",
                "public_id", publicId, "type", "upload", "access_mode", "public"));
    }

    private String extractPublicId(String url) {

        // Lấy phần sau "/upload/"
        int index = url.indexOf("/upload/");
        String path = url.substring(index + 8);

        // Bỏ version (v123456/)
        path = path.replaceFirst("v\\d+/", "");

        // Bỏ extension (.pdf, .jpg, .zip...)
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex != -1) {
            path = path.substring(0, dotIndex);
        }

        return path;
    }

}
