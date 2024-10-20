package com.ktb7.pinpung.service;

import com.ktb7.pinpung.dto.PlaceInfoResponseDto;
import com.ktb7.pinpung.dto.PlaceNearbyResponseDto;
import com.ktb7.pinpung.dto.SearchResponseDto;
import com.ktb7.pinpung.entity.Place;
import com.ktb7.pinpung.entity.Pung;
import com.ktb7.pinpung.entity.Review;
import com.ktb7.pinpung.repository.PlaceRepository;
import com.ktb7.pinpung.repository.PungRepository;
import com.ktb7.pinpung.repository.ReviewRepository;
import com.ktb7.pinpung.repository.TagRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.Clock;
import java.util.Collections;

@Service
@Slf4j
@AllArgsConstructor
public class PlaceService {

    private final PungRepository pungRepository;
    private final Clock clock;
    private final PlaceRepository placeRepository;
    private final TagRepository tagRepository;
    private final ReviewRepository reviewRepository;

    /*
    GET places/nearby
    place id 리스트를 받아 24시간 내 업로드된 펑이 있는 장소의 id와 대표 펑 이미지 반환
    */
    public List<PlaceNearbyResponseDto> getPlacesWithRepresentativeImage(List<Long> placeIds) {
        LocalDateTime yesterday = LocalDateTime.now(clock).minusDays(1);

        return placeIds.stream().map(placeId -> {
            // PungRepository에서 24시간 내의 이미지 URL을 가져옴. 없으면 null 반환
            Long imageId = pungRepository.findFirstByPlaceIdAndCreatedAtAfterOrderByCreatedAtDesc(placeId, yesterday)
                    .map(Pung::getImageId)
                    .orElse(null);

            log.info("places/nearby placeId imageUrl: {} {}", placeId, imageId);
            return new PlaceNearbyResponseDto(placeId, imageId);
        }).collect(Collectors.toList());
    }

    /*
    GET places/{placeId}
    place id를 받아 해당 장소의 정보, 리뷰, 대표 펑 반환
    */
    public PlaceInfoResponseDto getPlaceInfo(Long placeId) {
        LocalDateTime yesterday = LocalDateTime.now(clock).minusDays(1);

        // place info 조회
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid placeId: " + placeId));
        log.info("places/{placeId} placeId placeInfo: {} {}", placeId, place);

        // tags 조회
        List<Object[]> tagObjects = tagRepository.findTagsByPlaceIds(Collections.singletonList(placeId));

        // Object[]에서 tagName만 추출
        List<String> tags = tagObjects.stream()
                .map(tagObj -> (String) tagObj[1]) // 두 번째 요소(tagName)를 가져옴
                .collect(Collectors.toList());
        log.info("places/{placeId} tags {}", tags);

        // representative pung 조회
        Optional<Pung> representativePung = pungRepository.findFirstByPlaceIdAndCreatedAtAfterOrderByCreatedAtDesc(placeId, yesterday);
        log.info("places/{placeId} representativePung: {}", representativePung);

        // reviews 조회
        List<Review> reviews = reviewRepository.findByPlaceId(placeId);
        log.info("places/{placeId} reviews {}", reviews);

        // PlaceInfoResponseDto로 반환
        return new PlaceInfoResponseDto(
                place.getPlaceId(),
                place.getPlaceName(),
                place.getAddress(),
                tags,
                reviews,
                representativePung.orElse(null)  // 대표 펑이 없는 경우 null
        );
    }

    /*
   GET search/places
   place id 리스트를 받아 장소별 리뷰 개수, 태그 조회
   */
    public List<SearchResponseDto> getPlacesWithReviewCountsAndTags(List<Long> placeIds) {
        // 리뷰 개수 조회
//        List<Object[]> reviewCounts = searchRepository.findReviewCountsByPlaceIds(placeIds);
        List<Object[]> reviewCounts = reviewRepository.findReviewCountsByPlaceIds(placeIds);

        // 태그 조회
        List<Object[]> tags = tagRepository.findTagsByPlaceIds(placeIds);

        // 리뷰 개수 맵으로 변환
        Map<Long, Long> reviewCountMap = reviewCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],  // placeId
                        row -> (Long) row[1]     // reviewCount
                ));
        log.info("/search/places review counts: {}", reviewCountMap);

        // 태그 맵으로 변환
        Map<Long, List<String>> tagMap = tags.stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[0],              // placeId
                        Collectors.mapping(row -> (String) row[1], Collectors.toList()) // tagName
                ));
        log.info("/search/tags tags: {}", tagMap);

        // 결과 생성
        return placeIds.stream().map(placeId -> {
            Long reviewCount = reviewCountMap.getOrDefault(placeId, 0L);
            List<String> tagList = tagMap.getOrDefault(placeId, Collections.emptyList());

            return new SearchResponseDto(placeId, tagList, reviewCount);
        }).collect(Collectors.toList());
    }
}
