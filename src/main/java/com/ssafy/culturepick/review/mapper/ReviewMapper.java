package com.ssafy.culturepick.review.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    void insert(ReviewCreateCommand command);

    List<ReviewMapperResult> findAllByCultureId(@Param("cultureId") Long cultureId);

    List<ReviewMapperResult> findPageByCultureId(@Param("cultureId") Long cultureId,
                                                  @Param("offset") int offset,
                                                  @Param("size") int size);

    ReviewMapperResult findById(@Param("reviewId") Long reviewId);

    int updateContent(@Param("reviewId") Long reviewId, @Param("content") String content);

    int deleteById(@Param("reviewId") Long reviewId);
}
