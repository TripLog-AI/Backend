package com.triple.travel.domain.travelogue.repository;

import com.triple.travel.domain.travelogue.entity.TravelogueBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TravelogueBlockRepository extends JpaRepository<TravelogueBlock, Long> {

    @Query("SELECT b FROM TravelogueBlock b " +
           "WHERE b.id = :blockId AND b.travelogueDay.id = :dayId")
    Optional<TravelogueBlock> findByIdAndDayId(@Param("blockId") Long blockId,
                                                @Param("dayId") Long dayId);
}
