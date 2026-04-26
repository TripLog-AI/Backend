package com.triple.travel.domain.itinerary.repository;

import com.triple.travel.domain.itinerary.entity.ItinerarySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItinerarySlotRepository extends JpaRepository<ItinerarySlot, Long> {

    /**
     * Swap 전용 쿼리: slot + place를 JOIN FETCH해서 지연 로딩 없이 조회.
     * Day 소속 검증도 함께 처리.
     */
    @Query("SELECT s FROM ItinerarySlot s " +
           "JOIN FETCH s.place " +
           "WHERE s.id = :slotId AND s.itineraryDay.id = :dayId")
    Optional<ItinerarySlot> findWithPlaceByIdAndDayId(@Param("slotId") Long slotId,
                                                       @Param("dayId") Long dayId);

    /**
     * 순서 일괄 변경용: dayId에 속한 슬롯 목록 조회.
     */
    @Query("SELECT s FROM ItinerarySlot s WHERE s.itineraryDay.id = :dayId ORDER BY s.orderIndex ASC")
    List<ItinerarySlot> findByDayIdOrderByOrderIndex(@Param("dayId") Long dayId);
}
