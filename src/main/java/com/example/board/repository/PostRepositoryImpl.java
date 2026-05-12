package com.example.board.repository;

import com.example.board.dto.post.request.PostSearchRequest;
import com.example.board.entity.Post;
import com.example.board.entity.QPost;
import com.example.board.exception.BusinessException;
import com.example.board.exception.ErrorCode;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QPost post = QPost.post;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "likeCount", "viewCount", "commentCount"
    );

    @Override
    public Page<Post> findAllByFilters(PostSearchRequest request, Pageable pageable) {
        List<Post> content = queryFactory
                .selectFrom(post)
                .where(
                        regionEq(request.getRegion()),
                        subjectEq(request.getSubject()),
                        keywordContains(request.getKeyword())
                )
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        regionEq(request.getRegion()),
                        subjectEq(request.getSubject()),
                        keywordContains(request.getKeyword())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression regionEq(String region) {
        if (region == null || region.isBlank()) return null;
        StringTemplate regionValue = Expressions.stringTemplate(
                "cast(function('jsonb_extract_path_text', {0}, 'regions') as string)", post.categoryJson);
        return regionValue.eq(region);
    }

    private BooleanExpression subjectEq(String subject) {
        if (subject == null || subject.isBlank()) return null;
        StringTemplate subjectValue = Expressions.stringTemplate(
                "cast(function('jsonb_extract_path_text', {0}, 'subjects') as string)", post.categoryJson);
        return subjectValue.eq(subject);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return post.title.containsIgnoreCase(keyword)
                .or(post.content.containsIgnoreCase(keyword));
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        if (sort.isUnsorted()) {
            return new OrderSpecifier[]{post.createdAt.desc()};
        }
        PathBuilder<Post> postPath = new PathBuilder<>(Post.class, "post");
        return sort.stream()
                .map(order -> {
                    if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                        throw new BusinessException(ErrorCode.INVALID_FIELD);
                    }
                    var path = postPath.getComparable(order.getProperty(), Comparable.class);
                    return order.isAscending() ? path.asc() : path.desc();
                })
                .toArray(OrderSpecifier[]::new);
    }
}
