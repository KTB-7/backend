package com.ktb7.pinpung.dto.AI;

import com.ktb7.pinpung.dto.Place.PlaceNearbyDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PlacesPerTagDto {
    private String tagName;
    private Integer count;
    private List<PlaceNearbyDto> places;
}