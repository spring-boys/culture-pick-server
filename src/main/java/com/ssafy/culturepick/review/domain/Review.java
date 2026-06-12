package com.ssafy.culturepick.review.domain;

import com.ssafy.culturepick.culture.domain.Culture;
import com.ssafy.culturepick.global.domain.BaseEntity;
import com.ssafy.culturepick.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "culture_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_culture")
    )
    private Culture culture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_member")
    )
    private Member member;

    @Column(nullable = false, length = 500)
    private String content;

    private Review(Culture culture, Member member, String content) {
        this.culture = culture;
        this.member = member;
        this.content = content;
    }

    public static Review create(Culture culture, Member member, String content) {
        return new Review(culture, member, content);
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
