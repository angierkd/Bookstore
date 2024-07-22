package com.shopping.book.user.service;

import com.shopping.book.user.entity.User;
import com.shopping.book.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2MemberInfo memberInfo = getOAuth2MemberInfo(userRequest, oAuth2User.getAttributes());;

        User user = processUserRegistration(memberInfo);

        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }

    private OAuth2MemberInfo getOAuth2MemberInfo(OAuth2UserRequest userRequest, Map<String, Object> attributes) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if ("kakao".equals(registrationId)) {
            return new KaKaoMemberInfo(attributes);
        }
        // 다른 제공자에 대한 로직 추가 가능
        throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
    }

    private User processUserRegistration(OAuth2MemberInfo memberInfo) {
        String username = memberInfo.getProvider() + "_" + memberInfo.getProviderId();
        Optional<User> findMember = userRepository.findByUid(username);
        if (findMember.isEmpty()) {
            User newUser = User.builder()
                    .uid(username)
                    .password(passwordEncoder.encode("password"))
                    .role("ROLE_USER")
                    .socialProvider(memberInfo.getProvider())
                    .build();
            return userRepository.save(newUser);
        }
        return findMember.get();
    }
}
