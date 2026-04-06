package com.example.board.dto.post.request;

import java.util.List;
import java.util.Map;

public record PostUpdateRequest(
        Map<String, Object> category,
        String title,
        String content,
        List<Long> imageIds
) {
}
