package com.example.board.service;

import com.example.board.dto.post.request.PostCreateRequest;
import com.example.board.dto.post.response.PostResponse;
import com.example.board.entity.Image;
import com.example.board.entity.Post;
import com.example.board.exception.BusinessException;
import com.example.board.exception.ErrorCode;
import com.example.board.repository.ImageRepository;
import com.example.board.repository.PostRepository;
import com.example.board.support.ImageFixture;
import com.example.board.support.PostFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("게시글 목록 조회 성공 - 페이징 처리")
    void getPosts_success_paging() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = Arrays.asList(
                PostFixture.createPost(1L, 101L, "제목1", "내용1"),
                PostFixture.createPost(1L, 102L, "제목2", "내용2")
        );
        Page<Post> postPage = new PageImpl<>(posts, pageable, posts.size());

        when(postRepository.findAllByFilters(null, null, pageable)).thenReturn(postPage);

        // when
        Page<PostResponse> result = postService.getPosts(null, null, pageable);

        // then
        Assertions.assertThat(result.getContent()).hasSize(2);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(2);
        verify(postRepository, times(1)).findAllByFilters(null, null, pageable);
    }

    @Test
    @DisplayName("게시글 목록 조회 성공 - 카테고리 필터링")
    void getPosts_success_categoryFilter() {
        // given
        String category = "공시생 잡담";
        Pageable pageable = PageRequest.of(0, 10);
        Post post = PostFixture.createPostWithCategory(1L, 101L, category, "제목", "내용");
        Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findAllByFilters(eq(category), any(), any(Pageable.class))).thenReturn(postPage);

        // when
        Page<PostResponse> result = postService.getPosts(category, null, pageable);

        // then
        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent().getFirst().category().get("name")).isEqualTo(category);
    }

    @Test
    @DisplayName("게시글 목록 조회 성공 - 키워드 검색")
    void getPosts_success_searchKeyword() {
        // given
        String keyword = "개발";
        Pageable pageable = PageRequest.of(0, 10);
        Post post = PostFixture.createPost(1L, 101L, "개발 질문입니다", "내용");
        Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

        when(postRepository.findAllByFilters(any(), eq(keyword), any(Pageable.class))).thenReturn(postPage);

        // when
        Page<PostResponse> result = postService.getPosts(null, keyword, pageable);

        // then
        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent().getFirst().title()).contains(keyword);
    }

    @Test
    @DisplayName("게시글 목록 조회 성공 - 검색 결과가 없는 경우 빈 페이지 반환")
    void getPosts_success_emptyResult() {
        // given
        String keyword = "존재하지않는키워드";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(postRepository.findAllByFilters(any(), eq(keyword), any(Pageable.class))).thenReturn(emptyPage);

        // when
        Page<PostResponse> result = postService.getPosts(null, keyword, pageable);

        // then
        Assertions.assertThat(result.getContent()).isEmpty();
        Assertions.assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("게시글 생성 성공 테스트")
    void createPost_success() {
        // given
        Long userId = 1L; // 작성자 ID
        Long expectedPostId = 100L; // 생성될 게시글 ID

        PostCreateRequest postCreateRequest = PostFixture.createPostRequest();

        List<Image> mockImages = List.of(
                ImageFixture.createImageWithUserId(userId),
                ImageFixture.createImageWithUserId(userId),
                ImageFixture.createImageWithUserId(userId)
        );
        when(imageRepository.findAllById(postCreateRequest.imageIds())).thenReturn(mockImages);

        Post savedPost = postCreateRequest.toEntity(userId);
        ReflectionTestUtils.setField(savedPost, "id", expectedPostId);

        // Mockito
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // when
        PostResponse result = postService.createPost(userId, postCreateRequest);

        // then
        Assertions.assertThat(result.id()).isEqualTo(expectedPostId);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 비어있는 제목을 입력하면 예외")
    void createPost_fail_emptyTitle() {
        // given
        PostCreateRequest badRequest = PostFixture.createPostRequestWithoutTitle();

        // when & then
        Assertions.assertThatThrownBy(() -> postService.createPost(1L, badRequest))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TITLE_REQUIRED);
    }

    @Test
    @DisplayName("게시글 생성 실패 - 비어있는 내용을 입력하면 예외가 발생한다")
    void createPost_fail_emptyContent() {
        // given
        PostCreateRequest badRequest = PostFixture.createPostRequestWithoutContent();

        // when & then
        Assertions.assertThatThrownBy(() -> postService.createPost(1L, badRequest))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONTENT_REQUIRED);
    }

    @Test
    @DisplayName("게시글 생성 실패 - 존재하지 않는 이미지ID를 요청한 경우")
    void createPost_fail_imageNotFound(){
        //given
        PostCreateRequest badRequest = PostFixture.createPostRequest();

        //when & then
        // 요청 개수보다 적은 개수를 반환하게 하여 IMAGE_NOT_FOUND 유도
        when(imageRepository.findAllById(badRequest.imageIds())).thenReturn(Collections.emptyList());

        Assertions.assertThatThrownBy(() -> postService.createPost(1L, badRequest))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글 생성 실패 - 다른 사용자가 올린 이미지를 사용할 경우")
    void createPost_fail_imageAccessDenied(){
        Long currentUserId = 1L;
        Long otherUserId = 2L;

        //given
        PostCreateRequest badRequest = PostFixture.createPostRequest();

        List<Image> mockImages = List.of(
                ImageFixture.createImageWithUserId(currentUserId),
                ImageFixture.createImageWithUserId(otherUserId),
                ImageFixture.createImageWithUserId(currentUserId)
        );

        //when
        when(imageRepository.findAllById(badRequest.imageIds())).thenReturn(mockImages);

        Assertions.assertThatThrownBy(() -> postService.createPost(currentUserId, badRequest))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("게시글 생성 실패 - 이미 게시글이 있는 이미지를 사용했을 경우")
    void createPost_fail_alreadyMappedImage(){
        Long userId = 1L;

        //given
        PostCreateRequest badRequest = PostFixture.createPostRequest();

        List<Image> mockImages = List.of(
                ImageFixture.createImageWithUserId(userId),
                ImageFixture.createMappedImage(userId, 100L),
                ImageFixture.createImageWithUserId(userId)
        );

        // when
        when(imageRepository.findAllById(badRequest.imageIds())).thenReturn(mockImages);

        Assertions.assertThatThrownBy(() -> postService.createPost(userId,badRequest))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_MAPPED_IMAGE);
    }
}
