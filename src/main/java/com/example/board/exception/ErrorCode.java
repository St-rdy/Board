package com.example.board.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    MISSING_FIELDS(400, "MISSING_FIELDS", "필수 필드가 누락되었습니다."),
    INVALID_FIELD(400, "INVALID_FIELD", "유효하지 않은 필드값입니다."),
    TITLE_REQUIRED(400, "TITLE_REQUIRED", "제목을 입력해주세요."),
    CONTENT_REQUIRED(400,"CONTENT_REQUIRED","내용을 입력해주세요."),
    INVALID_CATEGORY(400, "INVALID_CATEGORY", "유효하지 않은 카테고리입니다."),
    INVALID_PARENT(400, "INVALID_PARENT", "대댓글에는 답글을 달 수 없습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(401, "UNAUTHORIZED", "인증이 필요합니다."),

    // 403 Forbidden
    FORBIDDEN(403, "FORBIDDEN", "권한이 없습니다."),

    // 404 Not Found
    NOT_FOUND(404, "NOT_FOUND", "게시글을 찾을 수 없습니다."),
    PARENT_NOT_FOUND(404, "PARENT_NOT_FOUND", "상위 댓글을 찾을 수 없습니다."),

    // 500 Internal Server Error
    DB_ERROR(500, "DB_ERROR", "데이터베이스 조회 중 오류가 발생했습니다.");


    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
