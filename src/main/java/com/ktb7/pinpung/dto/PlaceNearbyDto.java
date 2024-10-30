package com.ktb7.pinpung.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlaceNearbyDto {
    private Long placeId;
    private Long imageId;

    // for test
    public Long getPlaceId() {
        return placeId;
    }
    public Long getImageId() {
        return imageId;
    }
}