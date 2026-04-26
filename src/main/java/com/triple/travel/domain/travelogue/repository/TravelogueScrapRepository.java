package com.triple.travel.domain.travelogue.repository;

import com.triple.travel.domain.travelogue.entity.TravelogueScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelogueScrapRepository extends JpaRepository<TravelogueScrap, Long> {

    Optional<TravelogueScrap> findByUserIdAndTravelogueId(Long userId, Long travelogueId);

    boolean existsByUserIdAndTravelogueId(Long userId, Long travelogueId);
}
