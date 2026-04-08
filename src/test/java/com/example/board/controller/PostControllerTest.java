package com.example.board.controller;

import com.example.board.dto.JwtUserInfo;
import com.example.board.dto.post.response.PostDetailResponse;
import com.example.board.dto.post.request.PostUpdateRequest;
import com.example.board.dto.post.response.PostResponse;
import com.example.board.dto.comment.response.CommentResponse;
import com.example.board.service.PostService;
import com.example.board.security.JwtUtil;
import com.example.board.security.JwtFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    @DisplayName("게시글 상세 조회 API 성공 테스트")
    void getPostDetail_success() throws Exception {
        // given
        Long postId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        
        // Mock 댓글 데이터
        CommentResponse commentResponse = new CommentResponse(
                101L, 2L, "익명", null, "댓글 내용", "ALIVE", Instant.now(), new ArrayList<>()
        );
        Page<CommentResponse> commentPage = new PageImpl<>(List.of(commentResponse), pageable, 1);

        PostDetailResponse postDetailResponse = new PostDetailResponse(
                postId,
                Map.of("name", "공시생 잡담"),
                "상세 제목",
                "상세 내용",
                1L,
                0, 1, 1,
                Instant.now(),
                null,
                null,
                List.of("http://image1.com", "http://image2.com"),
                commentPage
        );

        when(postService.getPostDetail(eq(postId), any(Pageable.class))).thenReturn(postDetailResponse);

        // when and then
        mockMvc.perform(get("/api/v1/post/{postId}", postId)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("상세 제목"))
                .andExpect(jsonPath("$.data.viewCount").value(1))
                .andExpect(jsonPath("$.data.imageUrls[0]").value("http://image1.com"))
                .andExpect(jsonPath("$.data.comments.content[0].content").value("댓글 내용"));
    }

    @Test
    @DisplayName("게시글 목록 조회 API 성공 테스트")
    void getPosts_success() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        PostResponse postResponse = new PostResponse(
                1L,
                Map.of("name", "공시생 잡담"),
                "테스트 제목",
                "테스트 내용",
                1L,
                0, 0, 0,
                Instant.now(),
                null,
                null
        );
        Page<PostResponse> page = new PageImpl<>(List.of(postResponse), pageable, 1);

        when(postService.getPosts(any(), any(), any(Pageable.class))).thenReturn(page);

        // when and then
        mockMvc.perform(get("/api/v1/post")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].title").value("테스트 제목"))
                .andExpect(jsonPath("$.data.content[0].category.name").value("공시생 잡담"));
    }

    @Test
    @DisplayName("게시글 필터링 조회 API 성공 테스트")
    void getPosts_withFilters_success() throws Exception {
        // given
        String category = "개발 질문";
        String keyword = "자바";
        Pageable pageable = PageRequest.of(0, 10);
        PostResponse postResponse = new PostResponse(
                1L,
                Map.of("name", category),
                "자바 질문입니다",
                "내용",
                1L,
                0, 0, 0,
                Instant.now(),
                null,
                null
        );
        Page<PostResponse> page = new PageImpl<>(List.of(postResponse), pageable, 1);

        when(postService.getPosts(eq(category), eq(keyword), any(Pageable.class))).thenReturn(page);

        // when and then
        mockMvc.perform(get("/api/v1/post")
                        .param("category", category)
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("자바 질문입니다"))
                .andExpect(jsonPath("$.data.content[0].category.name").value(category));
    }

    @Test
    @DisplayName("게시글 수정 API 성공 테스트")
    void updatePost_success() throws Exception {
        // given
        Long postId = 1L;

        // SecurityContext에 Mock 유저 주입
        JwtUserInfo mockUser = new JwtUserInfo(1L, "nickname", "profileUrl");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList())
        );

        PostUpdateRequest updateRequest = new PostUpdateRequest(
                Map.of("name", "수정된 카테고리"),
                "수정된 제목",
                "수정된 내용",
                List.of(1L, 2L)
        );
        PostResponse postResponse = new PostResponse(
                postId,
                updateRequest.category(),
                updateRequest.title(),
                updateRequest.content(),
                1L,
                0, 0, 0,
                Instant.now(),
                Instant.now(),
                null
        );

        when(postService.updatePost(any(), eq(postId), any(PostUpdateRequest.class))).thenReturn(postResponse);

        // when and then
        mockMvc.perform(patch("/api/v1/post/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.message").value("게시글 수정 성공"));
    }
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
