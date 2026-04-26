package com.triple.travel.domain.itinerary.repository;

import com.triple.travel.domain.itinerary.entity.ItinerarySlotAlternative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItinerarySlotAlternativeRepository extends JpaRepository<ItinerarySlotAlternative, Long> {

    /**
     * N+1 방지 핵심 쿼리 #2.
     * 여러 슬롯의 대안 장소를 IN 쿼리 한 번으로 일괄 로딩.
     * Service에서 slotIds 수집 → 단일 쿼리 실행 → Map<slotId, List<Alt>>로 그루핑.
     */
    @Query("SELECT a FROM ItinerarySlotAlternative a " +
           "JOIN FETCH a.place " +
           "WHERE a.slot.id IN :slotIds " +
           "ORDER BY a.orderIndex ASC")
    List<ItinerarySlotAlternative> findWithPlaceBySlotIdIn(@Param("slotIds") List<Long> slotIds);

    /**
     * Swap 전용: alternative 단건 조회 (place fetch 포함, slot 소속 검증).
     */
    @Query("SELECT a FROM ItinerarySlotAlternative a " +
           "JOIN FETCH a.place " +
           "WHERE a.id = :altId AND a.slot.id = :slotId")
    Optional<ItinerarySlotAlternative> findWithPlaceByIdAndSlotId(@Param("altId") Long altId,
                                                                    @Param("slotId") Long slotId);

    /**
     * 특정 슬롯의 대안 목록 단건 조회 (place fetch 포함).
     */
    @Query("SELECT a FROM ItinerarySlotAlternative a " +
           "JOIN FETCH a.place " +
           "WHERE a.slot.id = :slotId " +
           "ORDER BY a.orderIndex ASC")
    List<ItinerarySlotAlternative> findWithPlaceBySlotId(@Param("slotId") Long slotId);
}
