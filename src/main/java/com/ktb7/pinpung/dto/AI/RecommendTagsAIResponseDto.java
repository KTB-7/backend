package com.ktb7.pinpung.dto.AI;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class RecommendTagsAIResponseDto {
    private List<Long> cafe_list;
}
