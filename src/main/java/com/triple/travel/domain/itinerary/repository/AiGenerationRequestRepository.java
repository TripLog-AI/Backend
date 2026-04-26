package com.triple.travel.domain.itinerary.repository;

import com.triple.travel.domain.itinerary.entity.AiGenerationRequest;
import com.triple.travel.domain.itinerary.entity.AiGenerationRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiGenerationRequestRepository extends JpaRepository<AiGenerationRequest, Long> {

    Optional<AiGenerationRequest> findByIdAndUserId(Long id, Long userId);

    // 배치 처리 워커가 PENDING 건을 순서대로 가져올 때 사용
    List<AiGenerationRequest> findTop10ByStatusOrderByCreatedAtAsc(Status status);
}
