package com.triple.travel.domain.place.entity;

import com.triple.travel.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "places",
    indexes = {
        @Index(name = "idx_places_google_place_id", columnList = "google_place_id"),
        @Index(name = "idx_places_category_cached", columnList = "category, cached_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_place_id", nullable = false, unique = true, length = 255)
    private String googlePlaceId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "name_local", length = 255)
    private String nameLocal;

    @Column(length = 512)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 50)
    private String phone;

    @Column(name = "website_url", length = 512)
    private String websiteUrl;

    @Column(name = "google_rating", precision = 3, scale = 2)
    private BigDecimal googleRating;

    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;

    // Google Maps API 캐시 TTL 판단 기준
    @Column(name = "cached_at", nullable = false)
    private LocalDateTime cachedAt;

    @Builder
    private Place(String googlePlaceId, String name, String nameLocal, String address,
                  Category category, BigDecimal latitude, BigDecimal longitude,
                  String phone, String websiteUrl, BigDecimal googleRating, String thumbnailUrl) {
        this.googlePlaceId = googlePlaceId;
        this.name = name;
        this.nameLocal = nameLocal;
        this.address = address;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.websiteUrl = websiteUrl;
        this.googleRating = googleRating;
        this.thumbnailUrl = thumbnailUrl;
        this.cachedAt = LocalDateTime.now();
    }

    public void refreshCache(String name, String address, BigDecimal googleRating, String thumbnailUrl) {
        this.name = name;
        this.address = address;
        this.googleRating = googleRating;
        this.thumbnailUrl = thumbnailUrl;
        this.cachedAt = LocalDateTime.now();
    }

    public boolean isCacheExpired(int ttlDays) {
        return cachedAt.isBefore(LocalDateTime.now().minusDays(ttlDays));
    }

    public enum Category {
        RESTAURANT, CAFE, ATTRACTION, ACCOMMODATION, SHOPPING, OTHER
    }
}
