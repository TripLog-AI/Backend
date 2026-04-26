package com.triple.travel.domain.place.repository;

import com.triple.travel.domain.place.entity.Place;
import com.triple.travel.domain.place.entity.Place.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByGooglePlaceId(String googlePlaceId);

    // 검색 - 이름 또는 주소 포함 (N+1 없음, 단순 SELECT)
    @Query("SELECT p FROM Place p " +
           "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:city IS NULL OR LOWER(p.address) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "AND (:category IS NULL OR p.category = :category)")
    List<Place> searchPlaces(@Param("keyword") String keyword,
                              @Param("city") String city,
                              @Param("category") Category category);
}
