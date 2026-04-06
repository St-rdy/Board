package com.example.board.controller;

import com.example.board.dto.ApiResponse;
import com.example.board.dto.JwtUserInfo;
import com.example.board.dto.comment.request.CommentCreateRequest;
import com.example.board.dto.comment.response.CommentResponse;
import com.example.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/post/{postId}/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal JwtUserInfo userInfo,
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request) {

        CommentResponse response = commentService.createComment(userInfo, postId, request);
        return ResponseEntity.ok(ApiResponse.success("댓글 작성 성공", response));
    }
}
