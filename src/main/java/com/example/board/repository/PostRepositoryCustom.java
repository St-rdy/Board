package com.example.board.repository;

import com.example.board.dto.post.request.PostSearchRequest;
import com.example.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> findAllByFilters(PostSearchRequest request, Pageable pageable);
}
