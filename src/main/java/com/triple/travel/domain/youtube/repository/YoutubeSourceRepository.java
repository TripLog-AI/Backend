package com.triple.travel.domain.youtube.repository;

import com.triple.travel.domain.youtube.entity.YoutubeSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface YoutubeSourceRepository extends JpaRepository<YoutubeSource, Long> {

    Optional<YoutubeSource> findByVideoId(String videoId);

    boolean existsByVideoId(String videoId);

    // 홈 화면 featured 코스 - 커서 페이지네이션
    @Query("SELECT y FROM YoutubeSource y " +
           "WHERE y.featured = true " +
           "AND y.parseStatus = 'COMPLETED' " +
           "AND (:city IS NULL OR LOWER(y.title) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:cursor IS NULL OR y.id < :cursor) " +
           "ORDER BY y.id DESC")
    List<YoutubeSource> findFeaturedWithCursor(@Param("city") String city,
                                                @Param("cursor") Long cursor,
                                                Pageable pageable);
}
