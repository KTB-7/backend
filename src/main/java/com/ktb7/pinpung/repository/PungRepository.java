package com.ktb7.pinpung.repository;

import com.ktb7.pinpung.entity.Pung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PungRepository extends JpaRepository<Pung, Long> {

    @Query(value = "SELECT p FROM Pung p WHERE p.placeId = :placeId AND p.createdAt >= :yesterday ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Pung> findLatestByPlaceIdWithin24Hours(Long placeId, LocalDateTime yesterday);
    Page<Pung> findByPlaceIdAndCreatedAtAfter(Long placeId, LocalDateTime yesterday, Pageable pageable);
}


