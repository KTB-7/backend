package com.ktb7.pinpung.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeleteReviewRequestDto {
    private Long userId;
    private Long reviewId;
    private Long placeId;
}