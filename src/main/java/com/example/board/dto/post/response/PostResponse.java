package com.example.board.dto.post.response;

import com.example.board.entity.Post;

import java.time.OffsetDateTime;
import java.util.Map;

public record PostResponse(
        Long id,
        Map<String, Object> category,
        String title,
        String content,
        Long userId,
        int likeCount,
        int viewCount,
        int commentCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String thumbnail
) {

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getCategoryJson(),
                post.getTitle(),
                post.getContent(),
                post.getUserId(),
                post.getLikeCount(),
                post.getViewCount(),
                post.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getThumbnail()
        );
    }
}
