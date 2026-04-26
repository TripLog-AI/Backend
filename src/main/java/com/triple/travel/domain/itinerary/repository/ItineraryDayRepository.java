package com.triple.travel.domain.itinerary.repository;

import com.triple.travel.domain.itinerary.entity.ItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItineraryDayRepository extends JpaRepository<ItineraryDay, Long> {

    /**
     * Day가 해당 Itinerary 소속인지 검증하며 조회.
     * Service에서 소유권 체인 검증에 활용.
     */
    @Query("SELECT d FROM ItineraryDay d " +
           "WHERE d.id = :dayId AND d.itinerary.id = :itineraryId")
    Optional<ItineraryDay> findByIdAndItineraryId(@Param("dayId") Long dayId,
                                                   @Param("itineraryId") Long itineraryId);

    boolean existsByItineraryIdAndDayNumber(Long itineraryId, Integer dayNumber);
}
