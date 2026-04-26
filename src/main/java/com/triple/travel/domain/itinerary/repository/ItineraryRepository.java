package com.triple.travel.domain.itinerary.repository;

import com.triple.travel.domain.itinerary.entity.Itinerary;
import com.triple.travel.domain.itinerary.entity.Itinerary.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    /**
     * 상세 조회: Itinerary + days를 JOIN FETCH (단일 컬렉션만).
     * slots, place, alternatives는 hibernate.default_batch_fetch_size=100 으로
     * 자동 IN-쿼리 batch fetch — 총 3~4 queries로 N+1 회피.
     *
     * 두 컬렉션(List)을 동시에 JOIN FETCH 하면 MultipleBagFetchException 발생.
     */
    @Query("SELECT i FROM Itinerary i " +
           "JOIN FETCH i.user " +
           "LEFT JOIN FETCH i.days " +
           "WHERE i.id = :id")
    Optional<Itinerary> findWithDaysAndSlotsById(@Param("id") Long id);

    /**
     * 내 여행 목록 - 커서 기반 페이지네이션.
     * status가 null이면 전체 조회.
     */
    @Query("SELECT i FROM Itinerary i " +
           "WHERE i.user.id = :userId " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:cursor IS NULL OR i.id < :cursor) " +
           "ORDER BY i.createdAt DESC")
    List<Itinerary> findByUserWithCursor(@Param("userId") Long userId,
                                          @Param("status") Status status,
                                          @Param("cursor") Long cursor,
                                          Pageable pageable);

    /**
     * 소유권 검증 포함 단건 조회 - 권한 체크와 엔티티 로딩을 한 번에.
     */
    Optional<Itinerary> findByIdAndUserId(Long id, Long userId);

    /**
     * 시스템이 보유한 YouTube 시드 일정 (saveCourse deep-copy 시 원본).
     * youtube_source 1개당 시드 일정 1개를 가정.
     */
    @Query("SELECT i FROM Itinerary i " +
           "LEFT JOIN FETCH i.days " +
           "WHERE i.youtubeSource.id = :youtubeSourceId " +
           "AND i.sourceType = com.triple.travel.domain.itinerary.entity.Itinerary$SourceType.YOUTUBE_SEED")
    Optional<Itinerary> findSeedByYoutubeSourceId(@Param("youtubeSourceId") Long youtubeSourceId);
}
