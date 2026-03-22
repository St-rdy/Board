package com.example.board.support;

import com.example.board.dto.post.request.PostCreateRequest;

import java.util.List;
import java.util.Map;

public class PostFixture {

    final static List<Long> imageIds = List.of(1L, 2L, 3L);

    //게시글 성공 데이터 타입
    public static PostCreateRequest createPostRequest(){
        return new PostCreateRequest(
                Map.of("regions", "Seoul", "subject", "Korean"),
                "게시글 제목",
                "게시글 내용",
                imageIds
        );
    }
    
    //게시글 성공 데이터 타입 - imageIds == null
    public static PostCreateRequest createPostRequestWithNullImageIds(){
        return new PostCreateRequest(
                Map.of("regions", "Seoul", "subject", "Korean"),
                "게시글 제목",
                "게시글 내용",
                null
        );
    }

    // 게시글 성공 데이터 타입 - imageIds = []
    public static  PostCreateRequest createPostRequestWithEmptyImageIds(){
        return new PostCreateRequest(
                Map.of("regions", "Seoul", "subject", "Korean"),
                "게시글 제목",
                "게시글 내용",
                List.of()
        );
    }

    //게시글 제목 없는 데이터
    public static PostCreateRequest createPostRequestWithoutTitle(){
        return new PostCreateRequest(
                Map.of("regions","Seoul", "subject","Korean"),
                null,
                "게시글 내용",
                imageIds
        );
    }

    //게시글 내용 없는 데이터
    public static PostCreateRequest createPostRequestWithoutContent(){
        return new PostCreateRequest(
                Map.of("regions","Seoul", "subject","Korean"),
                "게시글 제목",
                null,
                imageIds
        );
    }



}
