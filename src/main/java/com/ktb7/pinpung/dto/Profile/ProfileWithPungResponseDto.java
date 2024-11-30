package com.ktb7.pinpung.dto.Profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ProfileWithPungResponseDto {

    private DefaultProfileDto defaultProfile;
    private Long pungCount;
    private Long reviewCount;
    private List<SimplePung> pungs;

}