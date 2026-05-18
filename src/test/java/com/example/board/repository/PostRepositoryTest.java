package com.example.board.repository;

import com.example.board.dto.post.request.PostSearchRequest;
import com.example.board.entity.Post;
import com.example.board.support.PostFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.example.board.config.JpaAuditingConfig;
import com.example.board.config.QueryDslConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeAll
    void registerH2Functions() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE ALIAS IF NOT EXISTS JSONB_EXTRACT_PATH_TEXT " +
                "FOR \"com.example.board.support.H2JsonFunction.jsonbExtractPathText\""
            );
        }
    }

    @Nested
    @DisplayName("게시글 목록 필터링 조회")
    class FindAllByFilters {

        @Test
        @DisplayName("성공 - 필터 없이 전체 게시글 목록 조회")
        void findAllByFilters_noFilter() {
            // given
            postRepository.save(PostFixture.createPost(1L, null, "제목1", "내용1"));
            postRepository.save(PostFixture.createPost(1L, null, "제목2", "내용2"));

            PostSearchRequest request = new PostSearchRequest();
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Post> result = postRepository.findAllByFilters(request, pageable);

            // then
            Assertions.assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 지역 필터로 게시글 목록 조회")
        void findAllByFilters_regionFilter() {
            // given
            postRepository.save(PostFixture.createPostWithRegionSubject(1L, null, "Seoul", "Mathematics", "서울 게시글", "내용"));
            postRepository.save(PostFixture.createPostWithRegionSubject(1L, null, "Busan", "Korean", "부산 게시글", "내용"));

            PostSearchRequest request = new PostSearchRequest();
            request.setRegion("Seoul");
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Post> result = postRepository.findAllByFilters(request, pageable);

            // then
            Assertions.assertThat(result.getContent())
                    .allMatch(post -> "Seoul".equals(post.getCategoryJson().get("regions")));
        }

        @Test
        @DisplayName("성공 - 키워드로 제목 검색")
        void findAllByFilters_keywordInTitle() {
            // given
            postRepository.save(PostFixture.createPost(1L, null, "자바 질문입니다", "내용"));
            postRepository.save(PostFixture.createPost(1L, null, "파이썬 질문입니다", "내용"));

            PostSearchRequest request = new PostSearchRequest();
            request.setKeyword("자바");
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Post> result = postRepository.findAllByFilters(request, pageable);

            // then
            Assertions.assertThat(result.getContent())
                    .allMatch(post -> post.getTitle().contains("자바"));
        }

        @Test
        @DisplayName("성공 - 검색 결과 없으면 빈 페이지 반환")
        void findAllByFilters_emptyResult() {
            // given
            PostSearchRequest request = new PostSearchRequest();
            request.setKeyword("존재하지않는키워드xyz");
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Post> result = postRepository.findAllByFilters(request, pageable);

            // then
            Assertions.assertThat(result.getContent()).isEmpty();
            Assertions.assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }
}
