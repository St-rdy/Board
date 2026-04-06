package com.example.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "post")
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_json", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> categoryJson;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    @ColumnDefault("0")
    private int viewCount = 0;

    @Column(name = "like_count", nullable = false)
    @ColumnDefault("0")
    private int likeCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "comment_count", nullable = false)
    @ColumnDefault("0")
    private int commentCount;

    @Builder
    public Post(Long userId, Map<String, Object> categoryJson, String title, String content) {
        this.userId = userId;
        this.categoryJson = categoryJson;
        this.title = title;
        this.content = content;
    }

    // 게시글 수정
    public void update(Map<String, Object> categoryJson, String title, String content, String thumbnail) {
        this.categoryJson = categoryJson;
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }
}
/*
Table posts {
  id integer [primary key, increment]
  category_json jsonb [not null]
  user_id integer [not null, note: 'External ID (No FK)']
  title varchar [not null]
  content text [not null]
  view_count integer [default: 0]
  like_count integer [default: 0]
  created_at timestamptz [default: `now()`]
  updated_at timestamptz

  indexes { user_id }
}
 */