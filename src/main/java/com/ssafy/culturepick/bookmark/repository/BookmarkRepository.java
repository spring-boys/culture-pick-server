package com.ssafy.culturepick.bookmark.repository;

import com.ssafy.culturepick.bookmark.domain.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.awt.print.Book;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByMemberIdAndCultureId(Long memberId, Long cultureId);

    Optional<Bookmark> findByMemberIdAndCultureId(Long memberId, Long cultureId);

    @Query("SELECT b.culture.id FROM Bookmark b WHERE b.member.id = :memberId AND b.culture.id IN :cultureIds")
    Set<Long> findBookmarkedCultureIds(@Param("memberId") Long memberId, @Param("cultureIds") Collection<Long> cultureIds);

    @Query(value = "SELECT b FROM Bookmark b JOIN FETCH b.culture WHERE b.member.id = :memberId ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(b) FROM Bookmark b WHERE b.member.id = :memberId")
    Page<Bookmark> findCulturesByMemberId(Long memberId, Pageable pageable);
}
