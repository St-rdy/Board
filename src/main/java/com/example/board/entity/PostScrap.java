package com.example.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "scrap")
public class PostScrap {
    @EmbeddedId
    private PostScrapId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

}
