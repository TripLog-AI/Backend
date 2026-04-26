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
     * 여행기 상세: days → blocks → place 를 JOIN FETCH로 한 번에 로딩.
     * Travelogue는 불변에 가까운 공개 데이터이므로 전체 그래프 로딩이 합리적.
     */
    @Query("SELECT DISTINCT t FROM Travelogue t " +
           "JOIN FETCH t.user " +
           "JOIN FETCH t.days d " +
           "JOIN FETCH d.blocks b " +
           "LEFT JOIN FETCH b.place " +
           "WHERE t.id = :id")
    Optional<Travelogue> findWithDaysAndBlocksById(@Param("id") Long id);

    /**
     * 스크랩용: PLACE 블록만 로딩 (place 포함).
     * 동선 복사 시 TEXT/IMAGE 블록 불필요 → WHERE 조건으로 필터.
     */
    @Query("SELECT DISTINCT t FROM Travelogue t " +
           "JOIN FETCH t.days d " +
           "JOIN FETCH d.blocks b " +
           "JOIN FETCH b.place " +
           "WHERE t.id = :id AND b.blockType = 'PLACE'")
    Optional<Travelogue> findWithPlaceBlocksById(@Param("id") Long id);

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
