package com.example.board.dto.comment.response;

import com.example.board.entity.Comment;
import java.time.Instant;

public record CommentResponse(
        Long id,
        Long userId,
        String nickname,
        String profileUrl,
        String content,
        String status,
        Instant createdAt
) {
    public static CommentResponse from(Comment comment, String nickname, String profileUrl) {
        return new CommentResponse(
                comment.getId(),
                comment.getUserId(),
                nickname,
                profileUrl,
                comment.getContent(),
                comment.getStatus(),
                comment.getCreatedAt()
        );
    }
}
