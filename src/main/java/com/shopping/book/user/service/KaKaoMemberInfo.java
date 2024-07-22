package com.shopping.book.user.service;

import java.util.Map;

public class KaKaoMemberInfo implements OAuth2MemberInfo  {

    private Map<String, Object> attributes;
    private Map<String, Object> kakaoAccountAttributes;
//    private Map<String, Object> profileAttributes;

    public KaKaoMemberInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
//        this.kakaoAccountAttributes = (Map<String, Object>) attributes.get("kakao_account");
//        this.profileAttributes = (Map<String, Object>) attributes.get("profile");

    }


    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

//    @Override
//    public String getName() {
//        return kakaoAccountAttributes.get("nickname").toString();
//    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return (String) kakaoAccount.get("email");
    }

    public String getNickname() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return (String) properties.get("nickname");
    }

    public String getProfileImage() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return (String) properties.get("profile_image");
    }
}
