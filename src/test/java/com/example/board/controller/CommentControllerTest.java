package com.example.board.controller;

import com.example.board.dto.JwtUserInfo;
import com.example.board.dto.comment.request.CommentCreateRequest;
import com.example.board.dto.comment.request.CommentUpdateRequest;
import com.example.board.dto.comment.response.CommentResponse;
import com.example.board.security.JwtFilter;
import com.example.board.security.JwtUtil;
import com.example.board.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("댓글 작성 API 성공 테스트")
    @WithMockUser
    void createComment_Success() throws Exception {
        // given
        Long postId = 1L;
        Long userId = 1L;
        String nickname = "테스트닉네임";
        String profileUrl = "http://profile.com";
        
        JwtUserInfo userInfo = new JwtUserInfo(userId, nickname, profileUrl);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userInfo, null, Collections.emptyList())
        );

        CommentCreateRequest request = new CommentCreateRequest("댓글 테스트 내용", null);
        CommentResponse response = new CommentResponse(100L, userId, nickname, profileUrl, "댓글 테스트 내용", "ALIVE", Instant.now());

        given(commentService.createComment(eq(userInfo), eq(postId), any(CommentCreateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/post/{postId}/comment", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 작성 성공"))
                .andExpect(jsonPath("$.data.content").value("댓글 테스트 내용"))
                .andExpect(jsonPath("$.data.nickname").value(nickname));
    }

    @Test
    @DisplayName("댓글 삭제 API 성공 테스트")
    @WithMockUser
    void deleteComment_Success() throws Exception {
        // given
        Long commentId = 100L;
        Long userId = 1L;
        JwtUserInfo userInfo = new JwtUserInfo(userId, "nickname", "profileUrl");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userInfo, null, Collections.emptyList())
        );

        // when & then
        mockMvc.perform(delete("/api/v1/comment/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."));

        verify(commentService).deleteComment(eq(userInfo), eq(commentId));
    }

    @Test
    @DisplayName("댓글 수정 API 성공 테스트")
    @WithMockUser
    void updateComment_Success() throws Exception {
        // given
        Long commentId = 100L;
        Long userId = 1L;
        String nickname = "테스트닉네임";
        String profileUrl = "http://profile.com";
        JwtUserInfo userInfo = new JwtUserInfo(userId, nickname, profileUrl);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userInfo, null, Collections.emptyList())
        );

        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글 내용");
        CommentResponse response = new CommentResponse(commentId, userId, nickname, profileUrl, "수정된 댓글 내용", "ALIVE", Instant.now());

        given(commentService.updateComment(eq(userInfo), eq(commentId), any(CommentUpdateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/comment/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 수정 성공"))
                .andExpect(jsonPath("$.data.content").value("수정된 댓글 내용"));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
