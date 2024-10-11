package com.ktb7.pinpung.service;

import com.ktb7.pinpung.dto.SearchResponseDto;
import com.ktb7.pinpung.repository.SearchRepository;
import com.ktb7.pinpung.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class SearchServiceTest {

    @Mock
    private SearchRepository searchRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        // Mockito 어노테이션을 초기화
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPlacesWithReviewCountsAndTags() {
        // given
        List<String> placeIds = Arrays.asList("1abc", "2def", "3ghi");

        // Mock reviewCounts 결과
        List<Object[]> mockReviewCounts = Arrays.asList(
                new Object[]{"1abc", 3L},
                new Object[]{"2def", 5L},
                new Object[]{"3ghi", 0L}
        );
        when(searchRepository.findReviewCountsByPlaceIds(placeIds)).thenReturn(mockReviewCounts);

        // Mock tags 결과
        List<Object[]> mockTags = Arrays.asList(
                new Object[]{"1abc", "태그1"},
                new Object[]{"1abc", "태그2"},
                new Object[]{"2def", "태그2"},
                new Object[]{"3ghi", "태그3"}
        );
        when(tagRepository.findTagsByPlaceIds(placeIds)).thenReturn(mockTags);

        // when
        List<SearchResponseDto> result = searchService.getPlacesWithReviewCountsAndTags(placeIds);

        // then
        assertEquals(3, result.size(), "입력된 placeIds 개수와 반환된 개수는 동일해야 합니다");

        // 전달된 placeIds가 모두 존재하는지 확인
        placeIds.forEach(placeId -> {
            SearchResponseDto foundPlace = result.stream()
                    .filter(place -> place.getPlaceId().equals(placeId))
                    .findFirst()
                    .orElse(null);

            assertNotNull(foundPlace, "각 placeId는 반드시 반환 결과에 존재해야 합니다");
            assertEquals(placeId, foundPlace.getPlaceId(), "반환된 placeId는 입력된 placeId와 일치해야 합니다");
        });

        // 첫 번째 장소 검증
        SearchResponseDto place1 = result.get(0);
        assertEquals("1abc", place1.getPlaceId());
        assertEquals(Arrays.asList("태그1", "태그2"), place1.getTags());
        assertEquals(3L, place1.getReviewCount());

        // 두 번째 장소 검증
        SearchResponseDto place2 = result.get(1);
        assertEquals("2def", place2.getPlaceId());
        assertEquals(Collections.singletonList("태그2"), place2.getTags());
        assertEquals(5L, place2.getReviewCount());

        // 세 번째 장소 검증
        SearchResponseDto place3 = result.get(2);
        assertEquals("3ghi", place3.getPlaceId());
        assertEquals(Collections.singletonList("태그3"), place3.getTags());
        assertEquals(0L, place3.getReviewCount());
    }
}
