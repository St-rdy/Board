package com.example.board.service;

import com.example.board.dto.image.response.ImageUploadResponse;
import com.example.board.entity.Image;
import com.example.board.exception.BusinessException;
import com.example.board.exception.ErrorCode;
import com.example.board.repository.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    @Transactional
    public ImageUploadResponse uploadImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }

        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        String storageFileName = UUID.randomUUID().toString();
        String uploadUrl = uploadFile(file, storageFileName);

        Image image = Image.builder()
                .imageUrl(uploadUrl)
                .userId(userId)
                .build();

        Image savedImage = imageRepository.save(image);

        return ImageUploadResponse.of(savedImage.getId(), savedImage.getImageUrl());
    }

    private String uploadFile(MultipartFile file, String storageFileName) {
        return "https://추후.오브젝트스토리지.연동후/개발/"+storageFileName;
    }

    //확장자 따오기
    private String getFileExtension(String fileName) {
        if(fileName == null || !fileName.contains(".")){
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
