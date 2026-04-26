package com.triple.travel.domain.travelogue.repository;

import com.triple.travel.domain.travelogue.entity.TravelogueDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TravelogueDayRepository extends JpaRepository<TravelogueDay, Long> {

    @Query("SELECT d FROM TravelogueDay d " +
           "WHERE d.id = :dayId AND d.travelogue.id = :travelogueId")
    Optional<TravelogueDay> findByIdAndTravelogueId(@Param("dayId") Long dayId,
                                                     @Param("travelogueId") Long travelogueId);
}
