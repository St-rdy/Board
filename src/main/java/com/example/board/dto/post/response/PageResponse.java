package com.example.board.dto.post.response;

import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class PageResponse<T> {
    private final List<T> content;        // 실제 데이터 목록
    private final int currentPage;        // 현재 페이지 번호
    private final int totalPages;         // 전체 페이지 수
    private final long totalElements;     // 전체 데이터 개수
    private final boolean hasNext;        // 다음 페이지 존재 여부

    // Page 인터페이스를 받아서 DTO로 변환해주는 생성자
    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber() + 1; // 0-based index를 1-based로 변경하는 등 커스텀 가능
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.hasNext = page.hasNext();
    }
}
