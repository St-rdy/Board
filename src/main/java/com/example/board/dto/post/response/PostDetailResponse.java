package com.example.board.dto.post.response;

import com.example.board.dto.comment.response.CommentResponse;
import com.example.board.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PostDetailResponse(
        Long id,
        Map<String, Object> category,
        String title,
        String content,
        Long userId,
        int likeCount,
        int viewCount,
        int commentCount,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        Instant updatedAt,
        String thumbnail,
        List<String> imageUrls,
        Page<CommentResponse> comments
) {
    public static PostDetailResponse of(Post post, List<String> imageUrls, Page<CommentResponse> comments) {
        return new PostDetailResponse(
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
                post.getThumbnail(),
                imageUrls,
                comments
        );
    }
}
