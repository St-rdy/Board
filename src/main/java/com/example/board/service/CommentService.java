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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable) {
        // 루트 댓글 페이징 조회
        Page<Comment> rootComments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId, pageable);
        
        //댓글 없음
        if (rootComments.isEmpty()) {
            return Page.empty(pageable);
        }

        // 현재 페이지의 루트 댓글 ID 추출
        List<Long> rootIds = rootComments.getContent().stream()
                .map(Comment::getId)
                .toList();

        // 추출된 루트 댓글들에 속하는 대댓글만 조회
        List<Comment> replies = commentRepository.findByParentIdInOrderByCreatedAtAsc(rootIds);
        
        // 대댓글을 부모 ID별로 그룹화
        Map<Long, List<CommentResponse>> repliesByParentId = replies.stream()
                .collect(Collectors.groupingBy(
                        reply -> reply.getParent().getId(),
                        Collectors.mapping(
                                reply -> CommentResponse.from(reply, "익명", null), // TODO: 실제 닉네임 연동 필요
                                Collectors.toList()
                        )
                ));

        // 루트 댓글에 대댓글 매핑
        List<CommentResponse> content = rootComments.getContent().stream()
                .map(root -> {
                    CommentResponse response = CommentResponse.from(root, "익명", null); // TODO: 실제 닉네임 연동 필요
                    List<CommentResponse> childReplies = repliesByParentId.getOrDefault(root.getId(), List.of());
                    response.replies().addAll(childReplies);
                    return response;
                })
                .toList();

        return new PageImpl<>(content, pageable, rootComments.getTotalElements());
    }

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
