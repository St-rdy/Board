package com.example.board.dto.post.request;

import com.example.board.entity.Post;

import java.util.List;
import java.util.Map;

public record PostCreateRequest(
        Map<String, Object> category,
        String title,
        String content,
        List<Long> imageIds
) {
    public Post toEntity(Long Id) {
        return Post.builder().
                userId(Id).
                categoryJson(category).
                title(title).
                content(content).
                build();
    }
}
