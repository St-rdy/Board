package com.example.board.dto.image.response;

public record ImageUploadResponse(
        Long id,
        String imageUrl
) {
    public static ImageUploadResponse of(Long id, String imageUrl) {
        return new ImageUploadResponse(id, imageUrl);
    }
}
