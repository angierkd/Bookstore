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

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2MemberInfo memberInfo = null;

        System.out.println(oAuth2User.getAttributes());
        System.out.println(userRequest.getClientRegistration().getRegistrationId());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        System.out.println("registrationId = " + registrationId);

        if (registrationId.equals("kakao")) {
            memberInfo = new KaKaoMemberInfo(oAuth2User.getAttributes());
        }

        String provider = memberInfo.getProvider();
        String providerId = memberInfo.getProviderId();
        String username = provider + "_" + providerId; //중복이 발생하지 않도록 provider와 providerId를 조합
        String email = memberInfo.getEmail();
        String nickname = ((KaKaoMemberInfo) memberInfo).getNickname();
        String profileImage = ((KaKaoMemberInfo) memberInfo).getProfileImage();
        String role = "ROLE_USER"; // 일반 유저
        System.out.println(oAuth2User.getAttributes());

        Optional<User> findMember = userRepository.findByUid(username);
        User user=null;
        if (findMember.isEmpty()) { //찾지 못했다면
            user = User.builder()
                    .uid(username)
                    .password(passwordEncoder.encode("password"))
                    .role(role)
                    .socialProvider(provider)
                    .build();
            userRepository.save(user);
        }
        else{
            user=findMember.get();
        }

        return new PrincipalDetails(user, oAuth2User.getAttributes());

    }
}
