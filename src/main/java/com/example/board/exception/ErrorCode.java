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
    // 401 Unauthorized
    INVALID_TOKEN(401, "INVALID_TOKEN", "유효하지 않거나 만료된 토큰입니다."),

    // 403 Forbidden
    FORBIDDEN(403, "FORBIDDEN", "권한이 없습니다."),

    // 404 Not Found
    NOT_FOUND(404, "NOT_FOUND", "게시글을 찾을 수 없습니다."),
    PARENT_NOT_FOUND(404, "PARENT_NOT_FOUND", "상위 댓글을 찾을 수 없습니다."),

    // 500 Internal Server Error
    DB_ERROR(500, "DB_ERROR", "데이터베이스 조회 중 오류가 발생했습니다."),

    //이미지 업로드 시 에러 코드 ------------------------------------------------------------------------

    // 400 Bad Request (이미지 관련 추가)
    EMPTY_FILE(400, "EMPTY_FILE", "첨부된 파일이 없습니다."),
    INVALID_FILE_EXTENSION(400, "INVALID_FILE_EXTENSION", "지원하지 않는 이미지 형식입니다."),

    // 400 Bad Request (이미지 유효성 및 정책 위반)
    IMAGE_LIMIT_EXCEEDED(400, "IMAGE_LIMIT_EXCEEDED", "게시글 1개당 최대 10개의 이미지만 등록할 수 있습니다."),
    ALREADY_MAPPED_IMAGE(400, "ALREADY_MAPPED_IMAGE", "이미 다른 게시글에 등록된 이미지입니다."),

    // 403 Forbidden (권한 위반)
    IMAGE_ACCESS_DENIED(403, "IMAGE_ACCESS_DENIED", "본인이 업로드한 이미지만 사용할 수 있습니다."),

    // 404 Not Found (데이터 불일치 및 미존재)
    IMAGE_NOT_FOUND(404, "IMAGE_NOT_FOUND", "요청한 이미지를 찾을 수 없거나 유효하지 않습니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
