package com.ssafy.culturepick.bookmark.domain;

import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.global.domain.BaseEntity;
import com.ssafy.culturepick.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "culture_id"}))
public class Bookmark extends BaseEntity {

    @Id
    @Column(name = "bookmark_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "culture_id", nullable = false)
    private Culture culture;

    private Bookmark(Member member, Culture culture) {
        this.member = member;
        this.culture = culture;
    }

    public static Bookmark createBookmark(Member member, Culture culture) {
        return new Bookmark(member, culture);
    }
}
