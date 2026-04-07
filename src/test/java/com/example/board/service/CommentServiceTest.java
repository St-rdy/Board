package com.example.board.service;

import com.example.board.dto.JwtUserInfo;
import com.example.board.dto.comment.request.CommentCreateRequest;
import com.example.board.dto.comment.request.CommentUpdateRequest;
import com.example.board.dto.comment.response.CommentResponse;
import com.example.board.entity.Comment;
import com.example.board.entity.Post;
import com.example.board.exception.BusinessException;
import com.example.board.exception.ErrorCode;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.support.PostFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Nested
    @DisplayName("댓글 조회 테스트")
    class GetComments {

        @Test
        @DisplayName("성공: 게시글의 댓글을 계층형으로 조회한다")
        void getComments_Success() {
            // given
            Long postId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Post post = PostFixture.createPost(1L, postId, "제목", "내용");
            
            // 루트 댓글 2개 생성
            Comment root1 = Comment.builder().post(post).userId(1L).content("루트1").build();
            Comment root2 = Comment.builder().post(post).userId(2L).content("루트2").build();
            
            Page<Comment> rootPage = new PageImpl<>(List.of(root1, root2), pageable, 2);
            given(commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId, pageable)).willReturn(rootPage);
            
            // root1에 대한 대댓글 1개 생성
            Comment reply1 = Comment.builder().post(post).userId(3L).parent(root1).content("대댓글1").build();
            given(commentRepository.findByParentIdInOrderByCreatedAtAsc(any())).willReturn(List.of(reply1));

            // when
            Page<CommentResponse> responses = commentService.getCommentsByPost(postId, pageable);

            // then
            assertThat(responses.getContent()).hasSize(2);
            assertThat(responses.getContent().getFirst().content()).isEqualTo("루트1");
            assertThat(responses.getContent().getFirst().replies()).hasSize(1);
            assertThat(responses.getContent().getFirst().replies().getFirst().content()).isEqualTo("대댓글1");
        }

        @Test
        @DisplayName("성공: 삭제된 댓글은 '삭제된 댓글입니다.'로 마스킹되어 조회된다")
        void getComments_MaskingDeleted() {
            // given
            Long postId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Post post = PostFixture.createPost(1L, postId, "제목", "내용");
            
            Comment deletedComment = Comment.builder().post(post).userId(1L).content("비밀 내용").build();
            deletedComment.delete(); // status = DELETED
            
            Page<Comment> rootPage = new PageImpl<>(List.of(deletedComment), pageable, 1);
            given(commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId, pageable)).willReturn(rootPage);
            given(commentRepository.findByParentIdInOrderByCreatedAtAsc(any())).willReturn(List.of());

            // when
            Page<CommentResponse> responses = commentService.getCommentsByPost(postId, pageable);

            // then
            assertThat(responses.getContent().getFirst().content()).isEqualTo("삭제된 댓글입니다.");
            assertThat(responses.getContent().getFirst().status()).isEqualTo("DELETED");
        }
    }

    @Nested
    @DisplayName("댓글 작성 테스트")
    class CreateComment {

        @Test
        @DisplayName("성공: 부모 댓글이 없는 일반 댓글 작성")
        void createComment_Success() {
            // given
            JwtUserInfo userInfo = new JwtUserInfo(1L, "nickname", "profileUrl");
            Long postId = 10L;
            Post post = PostFixture.createPost(1L, postId, "제목", "내용");
            CommentCreateRequest request = new CommentCreateRequest("댓글 내용", null);

            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            CommentResponse response = commentService.createComment(userInfo, postId, request);

            // then
            assertThat(response.content()).isEqualTo("댓글 내용");
            assertThat(response.nickname()).isEqualTo("nickname");
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("성공: 대댓글 작성 (Depth 2)")
        void createReply_Success() {
            // given
            JwtUserInfo userInfo = new JwtUserInfo(1L, "nickname", "profileUrl");
            Long postId = 10L;
            Long parentId = 100L;
            Post post = PostFixture.createPost(1L, postId, "제목", "내용");
            Comment parentComment = Comment.builder().post(post).userId(1L).content("부모 댓글").build();
            CommentCreateRequest request = new CommentCreateRequest("대댓글 내용", parentId);

            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(commentRepository.findById(parentId)).willReturn(Optional.of(parentComment));
            given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            CommentResponse response = commentService.createComment(userInfo, postId, request);

            // then
            assertThat(response.content()).isEqualTo("대댓글 내용");
            assertThat(response.nickname()).isEqualTo("nickname");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글에 댓글 작성")
        void createComment_PostNotFound() {
            // given
            JwtUserInfo userInfo = new JwtUserInfo(1L, "nickname", "profileUrl");
            given(postRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(userInfo, 1L, new CommentCreateRequest("내용", null)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 대댓글에 대댓글 작성 시도 (Depth 3 초과)")
        void createReply_DepthExceeded() {
            // given
            JwtUserInfo userInfo = new JwtUserInfo(1L, "nickname", "profileUrl");
            Long parentId = 100L;
            Post post = PostFixture.createPost(1L, 1L, "제목", "내용");
            Comment root = Comment.builder().post(post).userId(1L).content("루트 댓글").build();
            Comment parentIsReply = Comment.builder().post(post).userId(1L).parent(root).content("대댓글").build();

            given(postRepository.findById(any())).willReturn(Optional.of(post));
            given(commentRepository.findById(parentId)).willReturn(Optional.of(parentIsReply));

            // when & then
            assertThatThrownBy(() -> commentService.createComment(userInfo, 1L, new CommentCreateRequest("내용", parentId)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PARENT);
        }
    }

    @Nested
    @DisplayName("댓글 삭제 테스트")
    class DeleteComment {

        @Test
        @DisplayName("성공: 작성자가 본인의 댓글을 삭제하면 상태가 DELETED로 변경된다")
        void deleteComment_Success() {
            // given
            Long userId = 1L;
            Long commentId = 100L;
            JwtUserInfo userInfo = new JwtUserInfo(userId, "nickname", "profileUrl");
            Post post = PostFixture.createPost(userId, 1L, "제목", "내용");
            Comment comment = Comment.builder()
                    .post(post)
                    .userId(userId)
                    .content("삭제될 댓글")
                    .build();

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            // when
            commentService.deleteComment(userInfo, commentId);

            // then
            assertThat(comment.getStatus()).isEqualTo("DELETED");
        }

        @Test
        @DisplayName("실패: 작성자가 아닌 사용자가 삭제를 시도하면 FORBIDDEN 예외가 발생한다")
        void deleteComment_Forbidden() {
            // given
            Long ownerId = 1L;
            Long otherUserId = 2L;
            Long commentId = 100L;
            JwtUserInfo otherUserInfo = new JwtUserInfo(otherUserId, "other", "url");
            Comment comment = Comment.builder()
                    .userId(ownerId)
                    .content("댓글")
                    .build();

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(otherUserInfo, commentId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class UpdateComment {

        @Test
        @DisplayName("성공: 작성자가 본인의 댓글 내용을 수정한다")
        void updateComment_Success() {
            // given
            Long userId = 1L;
            Long commentId = 100L;
            JwtUserInfo userInfo = new JwtUserInfo(userId, "nickname", "profileUrl");
            Comment comment = Comment.builder()
                    .userId(userId)
                    .content("기존 내용")
                    .build();
            CommentUpdateRequest request = new CommentUpdateRequest("수정된 내용");

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            // when
            CommentResponse response = commentService.updateComment(userInfo, commentId, request);

            // then
            assertThat(response.content()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("실패: 작성자가 아닌 사용자가 수정을 시도하면 FORBIDDEN 예외가 발생한다")
        void updateComment_Forbidden() {
            // given
            Long ownerId = 1L;
            Long otherUserId = 2L;
            Long commentId = 100L;
            JwtUserInfo otherUserInfo = new JwtUserInfo(otherUserId, "other", "url");
            Comment comment = Comment.builder()
                    .userId(ownerId)
                    .content("기존 내용")
                    .build();
            CommentUpdateRequest request = new CommentUpdateRequest("수정 시도");

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(otherUserInfo, commentId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("실패: 수정할 내용이 비어있으면 CONTENT_REQUIRED 예외가 발생한다")
        void updateComment_ContentRequired() {
            // given
            JwtUserInfo userInfo = new JwtUserInfo(1L, "nickname", "profileUrl");
            CommentUpdateRequest request = new CommentUpdateRequest(" "); // 공백 문자열

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(userInfo, 100L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENT_REQUIRED);
        }
    }
}
