package com.example.board.service;

import com.example.board.dto.JwtUserInfo;
import com.example.board.dto.comment.request.CommentCreateRequest;
import com.example.board.dto.comment.response.CommentResponse;
import com.example.board.entity.Comment;
import com.example.board.entity.Post;
import com.example.board.exception.BusinessException;
import com.example.board.exception.ErrorCode;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentResponse createComment(JwtUserInfo userInfo, Long postId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARENT_NOT_FOUND));

            if (parent.getParent() != null) {
                throw new BusinessException(ErrorCode.INVALID_PARENT);
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .userId(userInfo.userId())
                .parent(parent)
                .content(request.content())
                .build();

        Comment savedComment = commentRepository.save(comment);
        post.increaseCommentCount();

        return CommentResponse.from(savedComment, userInfo.nickname(), userInfo.profileUrl());
    }
}
