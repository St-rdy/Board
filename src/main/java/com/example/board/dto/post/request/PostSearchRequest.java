package com.example.board.dto.post.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostSearchRequest {
    private String region;
    private String subject;
    private String keyword;

    @Builder
    public PostSearchRequest(String region, String subject, String keyword) {
        this.region = region;
        this.subject = subject;
        this.keyword = keyword;
    }
}
