package com.example.board.controller;

import com.example.board.dto.ApiResponse;
import com.example.board.dto.JwtUserInfo;
import com.example.board.dto.post.request.PostCreateRequest;
import com.example.board.dto.post.response.PostResponse;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping()
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal JwtUserInfo userInfo,
            @RequestBody PostCreateRequest postCreateRequest) {

        PostResponse response = postService.createPost(userInfo.userId(), postCreateRequest);
        return ResponseEntity.ok(ApiResponse.success("게시글 작성 성공", response));
    }
}
