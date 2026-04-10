package com.example.board.dto;

public record JwtUserInfo(
        Long userId,
        String nickname,
        String profileUrl
) {
}
