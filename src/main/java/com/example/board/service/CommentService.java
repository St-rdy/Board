package com.example.board.service;

import com.example.board.dto.ApiResponse;
import com.example.board.dto.JwtUserInfo;
import com.example.board.dto.comment.request.CommentCreateRequest;
import com.example.board.dto.comment.request.CommentUpdateRequest;
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

    @Transactional
    public void deleteComment(JwtUserInfo userInfo, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        
        // 소유권 확인
        if (!comment.getUserId().equals(userInfo.userId())){
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if ("DELETED".equals(comment.getStatus())){
            return; //예외처리를 할까..
        }

        comment.delete();
        comment.getPost().decreaseCommentCount();
    }

    @Transactional
    public CommentResponse updateComment(
            JwtUserInfo userInfo,
            Long commentId,
            CommentUpdateRequest request) {
        
        //내용 비어있을시 다른 동작 안하도록 최상단에 배치
        if (request.content() == null || request.content().isBlank()){
            throw new BusinessException(ErrorCode.CONTENT_REQUIRED);
        }

        //댓글 존재여부
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        //소유권 확인
        if (!userInfo.userId().equals(comment.getUserId())){
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comment.update(request.content());

        return CommentResponse.from(comment, userInfo.nickname(), userInfo.profileUrl());
    }
}
