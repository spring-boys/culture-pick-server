package com.ssafy.culturepick.auth.oauth2;

import com.ssafy.culturepick.auth.security.CustomMemberDetails;
import com.ssafy.culturepick.member.domain.Member;
import com.ssafy.culturepick.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOauth2MemberService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String nickname = (String) attributes.get("name");

        Member member = memberRepository.findByProviderId(providerId)
                .orElseGet(() -> {
                    memberRepository.findByEmail(email)
                            .ifPresent(m -> {
                                throw new OAuth2AuthenticationException(new OAuth2Error("social_email_conflict"));
                            });

                    return memberRepository.save(Member.createGoogleMember(email, nickname, providerId));
                });

        return new CustomMemberDetails(member, attributes);
    }
}
