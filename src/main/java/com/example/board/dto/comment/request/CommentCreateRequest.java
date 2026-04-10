package com.example.board.dto.comment.request;

public record CommentCreateRequest(
        String content,
        Long parentId
) {
}
