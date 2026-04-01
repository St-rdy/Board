package com.example.board.service;

import com.example.board.dto.post.request.PostCreateRequest;
import com.example.board.dto.post.response.PostResponse;
import com.example.board.entity.Image;
import com.example.board.exception.BusinessException;
import com.example.board.exception.ErrorCode;
import com.example.board.repository.ImageRepository;
import com.example.board.repository.PostRepository;
import com.example.board.entity.Post;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ImageRepository imageRepository;

    // 게시글 목록 조회 (필터링, 페이징)
    @Transactional(readOnly = true)
    public Page<PostResponse> getPosts(String category, String keyword, Pageable pageable) {
        Page<Post> postPage = postRepository.findAllByFilters(category, keyword, pageable);
        return postPage.map(PostResponse::from);
    }

    // 게시글 생성
    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest postCreateRequest) {
        validatePostRequest(postCreateRequest);

        List<Image> images = validateImagesForCreate(userId, postCreateRequest.imageIds());

        Post post = postCreateRequest.toEntity(userId);
        Post savedPost = postRepository.save(post);

        if (images != null) {
            images.forEach(image -> image.mapToPost(savedPost));
        }

        return PostResponse.from(savedPost);
    }

    // 제목, 내용 무결성 검사
    private void validatePostRequest(PostCreateRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new BusinessException(ErrorCode.TITLE_REQUIRED);
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new BusinessException(ErrorCode.CONTENT_REQUIRED);
        }
    }

    // 이미지 검사
    private List<Image> validateImagesForCreate(Long userId, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return null;
        }

        
        // 최대 이미지 수량
        if (imageIds.size() > 10) {
            throw new BusinessException(ErrorCode.IMAGE_LIMIT_EXCEEDED);
        }

        List<Image> images = imageRepository.findAllById(imageIds);

        // 요청 ID들이 모두 DB에 존재하는지 확인
        if (images.size() != imageIds.size()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        //이미지 소유권 및 점유 여부 검사
        for (Image image : images) {
            if (!image.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.IMAGE_ACCESS_DENIED);
            }
            if (image.getPost() != null) {
                throw new BusinessException(ErrorCode.ALREADY_MAPPED_IMAGE);
            }
        }

        return images;
    }
}
