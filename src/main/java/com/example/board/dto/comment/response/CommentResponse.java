package com.example.board.dto.comment.response;

import com.example.board.entity.Comment;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record CommentResponse(
        Long id,
        Long userId,
        String nickname,
        String profileUrl,
        String content,
        String status,
        Instant createdAt,
        List<CommentResponse> replies
) {
    public static CommentResponse from(Comment comment, String nickname, String profileUrl) {
        String displayContent = "DELETED".equals(comment.getStatus()) 
                ? "삭제된 댓글입니다." 
                : comment.getContent();

        return new CommentResponse(
                comment.getId(),
                comment.getUserId(),
                nickname,
                profileUrl,
                displayContent,
                comment.getStatus(),
                comment.getCreatedAt(),
                new ArrayList<>()
        );
    }
}
