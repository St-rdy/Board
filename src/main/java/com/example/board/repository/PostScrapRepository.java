package com.example.board.repository;

import com.example.board.entity.PostScrap;
import com.example.board.entity.PostScrapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostScrapRepository extends JpaRepository<PostScrap, PostScrapId> {
}
