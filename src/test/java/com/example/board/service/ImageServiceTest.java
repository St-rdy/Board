package com.example.board.service;

import com.example.board.dto.image.response.ImageUploadResponse;
import com.example.board.entity.Image;
import com.example.board.exception.BusinessException;
import com.example.board.exception.ErrorCode;
import com.example.board.repository.ImageRepository;
import com.example.board.support.ImageFixture; // 픽스처 사용
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    @Test
    @DisplayName("이미지 업로드 성공 - 정상적인 파일을 올리면 DB에 저장하고 응답을 반환한다")
    void uploadImage_success() {
        // given
        Long userId = 1L;
        Long expectedImageId = 10L;
        MockMultipartFile validFile = ImageFixture.createMockImageFile();

        // 저장될 엔티티 가짜(Mock) 세팅
        Image savedImage = Image.builder()
                .userId(userId)
                .imageUrl("https://s3.aws.../uuid_test-image.png")
                .build();
        ReflectionTestUtils.setField(savedImage, "id", expectedImageId); // PK 강제 주입

        // Mockito: repository.save()가 호출되면 가짜 엔티티를 반환하도록 설정
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);

        // when
        ImageUploadResponse result = imageService.uploadImage(userId, validFile);

        // then
        Assertions.assertThat(result.id()).isEqualTo(expectedImageId);
        Assertions.assertThat(result.imageUrl()).contains("uuid_test-image.png");
        verify(imageRepository, times(1)).save(any(Image.class)); // save가 1번 호출되었는지 검증
    }

    @Test
    @DisplayName("이미지 업로드 실패 - 빈 파일을 올리면 예외가 발생한다")
    void uploadImage_fail_emptyFile() {
        // given
        Long userId = 1L;
        MockMultipartFile emptyFile = ImageFixture.createEmptyMockImageFile();

        // when & then
        Assertions.assertThatThrownBy(() -> imageService.uploadImage(userId, emptyFile))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMPTY_FILE);

        // 빈 파일이면 DB 저장이 절대 일어나면 안 됨을 검증
        verify(imageRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미지 업로드 실패 - 지원하지 않는 확장자를 올리면 예외가 발생한다")
    void uploadImage_fail_invalid_file(){
        Long userId = 1L;
        MockMultipartFile invalidExtensionFile = ImageFixture.createInvalidExtensionMockFile();

        Assertions.assertThatThrownBy(() -> imageService.uploadImage(userId, invalidExtensionFile))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_FILE_EXTENSION);
    }
}