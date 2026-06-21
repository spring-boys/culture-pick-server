package com.ssafy.culturepick.review.repository;

import com.ssafy.culturepick.review.domain.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"member"})// 리뷰 정보 조회 시 작성자 회원 정보 함께 조회
    List<Review> findByCulture_IdOrderByCreatedAtDesc(Long cultureId);
}
