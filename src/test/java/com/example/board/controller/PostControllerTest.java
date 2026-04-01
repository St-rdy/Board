package com.example.board.controller;

import com.example.board.dto.post.response.PostResponse;
import com.example.board.service.PostService;
import com.example.board.security.JwtUtil;
import com.example.board.security.JwtFilter;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtFilter jwtFilter;

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
                OffsetDateTime.now(),
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
                OffsetDateTime.now(),
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
}
