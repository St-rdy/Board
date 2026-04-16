package com.example.board.service;

import com.example.board.dto.comment.response.CommentResponse;
import com.example.board.dto.post.request.PostCreateRequest;
import com.example.board.dto.post.request.PostUpdateRequest;
import com.example.board.dto.post.response.PageResponse;
import com.example.board.dto.post.response.PostDetailResponse;
import com.example.board.dto.post.response.PostResponse;
import com.example.board.entity.*;
import com.example.board.exception.BusinessException;
import com.example.board.exception.ErrorCode;
import com.example.board.repository.ImageRepository;
import com.example.board.repository.PostLikeRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.PostScrapRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final CommentService commentService;
    private final PostLikeRepository postLikeRepository;
    private final PostScrapRepository postScrapRepository;

    // 게시글 상세 조회
    @Transactional
    public PostDetailResponse getPostDetail(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 조회수 증가
        post.increaseViewCount();

        // 이미지 URL 리스트 조회
        List<String> imageUrls = imageRepository.findAllByPostId(postId).stream()
                .map(Image::getImageUrl)
                .toList();

        // 댓글 페이징 조회
        Page<CommentResponse> comments = commentService.getCommentsByPost(postId, pageable);

        return PostDetailResponse.of(post, imageUrls, comments);
    }

    // 게시글 목록 조회 (필터링, 페이징)
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getPosts(String category, String keyword, Pageable pageable) {
        Page<Post> postPage = postRepository.findAllByFilters(category, keyword, pageable);
        Page<PostResponse> postResponsePage = postPage.map(PostResponse::from);
        return new PageResponse<>(postResponsePage);
    }

    // 게시글 생성
    @Transactional
    public PostResponse createPost(Long userId, PostCreateRequest postCreateRequest) {
        validatePostRequest(postCreateRequest.title(), postCreateRequest.content());

        List<Image> images = validateImagesForPost(userId, null, postCreateRequest.imageIds());

        Post post = postCreateRequest.toEntity(userId);
        Post savedPost = postRepository.save(post);

        if (images != null) {
            images.forEach(image -> image.mapToPost(savedPost));
        }

        return PostResponse.from(savedPost);
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long userId, Long postId, PostUpdateRequest postUpdateRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 소유권 확인
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        validatePostRequest(postUpdateRequest.title(), postUpdateRequest.content());

        // 이미지 업데이트 (요청에 포함된 경우만 처리)
        if (postUpdateRequest.imageIds() != null) {
            // 기존 이미지 매핑 해제
            List<Image> currentImages = imageRepository.findAllByPostId(postId);
            currentImages.forEach(image -> image.mapToPost(null));

            // 새 이미지 검증 및 매핑
            List<Image> newImages = validateImagesForPost(userId, post, postUpdateRequest.imageIds());
            if (newImages != null) {
                newImages.forEach(image -> image.mapToPost(post));
            }
        }

        // 게시글 업데이트 (썸네일 포함)
        // 이미지가 있다면 첫번째 이미지 없다면 기존 이미지로 지정
        String thumbnail = (postUpdateRequest.imageIds() != null && !postUpdateRequest.imageIds().isEmpty())
                ? imageRepository.findById(postUpdateRequest.imageIds().getFirst())
                .map(Image::getImageUrl).orElse(null) : post.getThumbnail();

        post.update(postUpdateRequest.category(), postUpdateRequest.title(), postUpdateRequest.content(), thumbnail);

        return PostResponse.from(post);
    }

    //게시글 좋아요
    @Transactional
    public void togglePostLike(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        PostLikeId postLikeId = new PostLikeId();
        postLikeId.setUserId(userId);
        postLikeId.setPostId(postId);

        if (postLikeRepository.existsById(postLikeId)) {
            postLikeRepository.deleteById(postLikeId);
            post.decreaseLikeCount();
        } else {
            PostLike postLike = new PostLike();
            postLike.setId(postLikeId);
            postLike.setPost(post);
            postLikeRepository.save(postLike);
            post.increaseLikeCount();
        }
    }

    //게시글 스크랩
    @Transactional
    public void togglePostScrap(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        PostScrapId postScrapId = new PostScrapId();
        postScrapId.setUserId(userId);
        postScrapId.setPostId(postId);

        if (postScrapRepository.existsById(postScrapId)) {
            postScrapRepository.deleteById(postScrapId);
            post.decreaseScrapCount();
        } else {
            PostScrap postScrap = new PostScrap();
            postScrap.setId(postScrapId);
            postScrap.setPost(post);
            postScrapRepository.save(postScrap);
            post.increaseScrapCount();
        }
    }

    // 제목, 내용 무결성 검사
    private void validatePostRequest(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.TITLE_REQUIRED);
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.CONTENT_REQUIRED);
        }
    }

    // 이미지 검사 (생성/수정 공통)
    private List<Image> validateImagesForPost(Long userId, Post post, List<Long> imageIds) {
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

        // 이미지 소유권 및 점유 여부 검사
        for (Image image : images) {
            if (!image.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.IMAGE_ACCESS_DENIED);
            }
            // 이미 다른 게시물에 매핑되어 있는 경우 (자기 자신은 제외)
            if (image.getPost() != null && (post == null || !image.getPost().getId().equals(post.getId()))) {
                throw new BusinessException(ErrorCode.ALREADY_MAPPED_IMAGE);
            }
        }

        return images;
    }
}
