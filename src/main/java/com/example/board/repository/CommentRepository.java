package com.example.board.repository;

import com.example.board.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostId(Long postId);

    // 루트 댓글(부모가 없는 댓글)만 페이징 조회
    Page<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId, Pageable pageable);

    // 특정 부모 댓글들에 속하는 대댓글들만 조회
    List<Comment> findByParentIdInOrderByCreatedAtAsc(List<Long> parentIds);
}
