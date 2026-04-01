package com.example.board.support;

import com.example.board.dto.image.response.ImageUploadResponse;
import com.example.board.entity.Image;
import com.example.board.entity.Post;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

public class ImageFixture {

    // 1. 정상적인 이미지 파일 데이터
    public static MockMultipartFile createMockImageFile() {
        return new MockMultipartFile(
                "file",                     // 파라미터 이름 (Controller에서 @RequestParam("file")로 받음)
                "test-image.png",           // 원본 파일명
                MediaType.IMAGE_PNG_VALUE,  // 파일 타입
                "dummy image content".getBytes() // 가짜 파일 데이터
        );
    }

    // 2. 비어있는(Empty) 파일 데이터 (에러 테스트용)
    public static MockMultipartFile createEmptyMockImageFile() {
        return new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]                 // 0바이트 데이터
        );
    }

    //3.

    // 업로드 성공 응답 데이터
    public static ImageUploadResponse createImageUploadResponse() {
        return new ImageUploadResponse(10L, "https://test.test.../uuid.png");
    }

    //테스트 이미지 생성 로직
    public static Image createImageWithUserId(Long userId) {
        return Image.builder()
                .userId(userId)
                .imageUrl("https://s3.aws.../other-user-image.png")
                .build();
    }

    public static Image createMappedImage(Long userId, Long mappedPostId){
        Image image = Image.builder()
                .userId(userId)
                .imageUrl("https://s3.aws.../other-user-image.png")
                .build();
        Post dummyPost = Post.builder()
                .userId(userId)
                .title("제목")
                .content("내용")
                .build();
        ReflectionTestUtils.setField(dummyPost, "id", mappedPostId);

        image.mapToPost(dummyPost);
        return image;
    }

    // 지원하지 않는 확장자(txt) 파일 픽스처
    public static MockMultipartFile createInvalidExtensionMockFile() {
        return new MockMultipartFile(
                "file",
                "test-document.txt", // 확장자가 txt
                "text/plain",
                "dummy content".getBytes()
        );
    }
}