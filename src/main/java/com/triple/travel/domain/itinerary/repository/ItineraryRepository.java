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
     * 상세 조회: Itinerary → days → slots → place 를 JOIN FETCH로 한 쿼리에 로딩.
     * 세 단계 중첩 컬렉션이지만 선형(parent→child→grandchild) 구조라
     * MultipleBagFetchException 발생 없음.
     * alternatives는 @BatchSize(100)으로 별도 IN 쿼리 처리.
     */
    @Query("SELECT DISTINCT i FROM Itinerary i " +
           "JOIN FETCH i.user " +
           "JOIN FETCH i.days d " +
           "JOIN FETCH d.slots s " +
           "JOIN FETCH s.place " +
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
}
