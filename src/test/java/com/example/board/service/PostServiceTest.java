package com.example.board.service;

import com.example.board.dto.post.request.PostCreateRequest;
import com.example.board.dto.post.response.PostResponse;
import com.example.board.exception.BusinessException;
import com.example.board.exception.ErrorCode;
import com.example.board.repository.ImageRepository;
import com.example.board.repository.PostRepository;
import com.example.board.entity.Post;
import com.example.board.entity.Image;
import com.example.board.support.ImageFixture;
import com.example.board.support.PostFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("게시글 생성 성공 테스트")
    void createPost_success() {
        // given
        Long userId = 1L; // 작성자 ID
        Long expectedPostId = 100L; // 생성될 게시글 ID

        PostCreateRequest postCreateRequest = PostFixture.createPostRequest();

        // 요청 개수(3개)와 동일하게 3개의 이미지를 반환하도록 수정
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

        // 3개를 반환하되 하나는 다른 유저 소유로 설정
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

        // 3개를 반환하되 하나는 이미 매핑된 상태로 설정
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