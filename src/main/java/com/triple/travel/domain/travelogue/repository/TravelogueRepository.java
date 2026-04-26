package com.triple.travel.domain.travelogue.repository;

import com.triple.travel.domain.travelogue.entity.Travelogue;
import com.triple.travel.domain.travelogue.entity.Travelogue.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TravelogueRepository extends JpaRepository<Travelogue, Long> {

    /**
     * 여행기 상세: Travelogue + days를 JOIN FETCH (단일 컬렉션만).
     * blocks와 place는 hibernate.default_batch_fetch_size=100 으로 자동 batch fetch.
     */
    @Query("SELECT t FROM Travelogue t " +
           "JOIN FETCH t.user " +
           "LEFT JOIN FETCH t.days " +
           "WHERE t.id = :id")
    Optional<Travelogue> findWithDaysAndBlocksById(@Param("id") Long id);

    /**
     * 스크랩용: 동일 쿼리. blocks의 PLACE 필터링은 service에서 처리.
     */
    default Optional<Travelogue> findWithPlaceBlocksById(Long id) {
        return findWithDaysAndBlocksById(id);
    }

    /**
     * 공개 피드 - 커서 기반 페이지네이션.
     * published_at 인덱스 활용.
     */
    @Query("SELECT t FROM Travelogue t " +
           "JOIN FETCH t.user " +
           "WHERE t.status = 'PUBLISHED' " +
           "AND (:city IS NULL OR LOWER(t.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:cursor IS NULL OR t.id < :cursor) " +
           "ORDER BY t.publishedAt DESC")
    List<Travelogue> findPublishedWithCursor(@Param("city") String city,
                                              @Param("cursor") Long cursor,
                                              Pageable pageable);

    List<Travelogue> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Travelogue> findByIdAndUserId(Long id, Long userId);
}
