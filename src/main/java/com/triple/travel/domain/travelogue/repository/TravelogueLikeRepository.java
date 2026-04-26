package com.triple.travel.domain.travelogue.repository;

import com.triple.travel.domain.travelogue.entity.TravelogueLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelogueLikeRepository extends JpaRepository<TravelogueLike, Long> {

    Optional<TravelogueLike> findByTravelogueIdAndUserId(Long travelogueId, Long userId);

    boolean existsByTravelogueIdAndUserId(Long travelogueId, Long userId);
}
