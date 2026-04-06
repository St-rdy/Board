package com.example.board.repository;

import com.example.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 필터링 및 페이징 기반 게시글 목록 조회
    @Query("SELECT p FROM Post p " +
            "WHERE (:category IS NULL OR CAST(FUNCTION('jsonb_extract_path_text', p.categoryJson, 'name') AS string) = :category) " +
            "AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> findAllByFilters(@Param("category") String category, 
                                @Param("keyword") String keyword, 
                                Pageable pageable);
}
