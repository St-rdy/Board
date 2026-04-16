package com.example.board.controller;

import com.example.board.dto.ApiResponse;
import com.example.board.dto.JwtUserInfo;
import com.example.board.dto.post.request.PostCreateRequest;
import com.example.board.dto.post.request.PostUpdateRequest;
import com.example.board.dto.post.response.PageResponse;
import com.example.board.dto.post.response.PostDetailResponse;
import com.example.board.dto.post.response.PostResponse;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 상세 조회 API
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
            @PathVariable Long postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        PostDetailResponse response = postService.getPostDetail(postId, pageable);
        return ResponseEntity.ok(ApiResponse.success("게시글 상세 조회 성공", response));
    }

    // 게시글 목록 조회 API
    @GetMapping()
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {

        PageResponse<PostResponse> response = postService.getPosts(category, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 성공", response));
    }

    // 게시글 생성 API
    @PostMapping()
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal JwtUserInfo userInfo,
            @RequestBody PostCreateRequest postCreateRequest) {

        PostResponse response = postService.createPost(userInfo.userId(), postCreateRequest);
        return ResponseEntity.ok(ApiResponse.success("게시글 작성 성공", response));
    }

    // 게시글 수정 API
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @AuthenticationPrincipal JwtUserInfo userInfo,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest postUpdateRequest) {

        PostResponse response = postService.updatePost(userInfo.userId(), postId, postUpdateRequest);
        return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공", response));
    }

    // 게시글 좋아요 토글 API
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> togglePostLike(
            @AuthenticationPrincipal JwtUserInfo userInfo,
            @PathVariable Long postId) {

        postService.togglePostLike(userInfo.userId(), postId);
        return ResponseEntity.ok(ApiResponse.success("게시글 좋아요 토글 성공", null));
    }

    // 게시글 스크랩 토글 API
    @PostMapping("/{postId}/scrap")
    public ResponseEntity<ApiResponse<Void>> togglePostScrap(
            @AuthenticationPrincipal JwtUserInfo userInfo,
            @PathVariable Long postId) {

        postService.togglePostScrap(userInfo.userId(), postId);
        return ResponseEntity.ok(ApiResponse.success("게시글 스크랩 토글 성공", null));
    }
}
