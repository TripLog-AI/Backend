package com.triple.travel.domain.place.service;

import com.triple.travel.common.client.google.GoogleMapsClient;
import com.triple.travel.common.client.google.GoogleMapsDto;
import com.triple.travel.common.exception.EntityNotFoundException;
import com.triple.travel.domain.place.dto.PlaceDto;
import com.triple.travel.domain.place.entity.Place;
import com.triple.travel.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final GoogleMapsClient googleMapsClient;

    @Value("${app.google.maps.cache-ttl-days:7}")
    private int cacheTtlDays;

    public List<PlaceDto.Detail> search(String keyword, String city, String category) {
        Place.Category cat = parseCategory(category);
        return placeRepository.searchPlaces(keyword, city, cat).stream()
            .map(this::toDetail)
            .toList();
    }

    public PlaceDto.Detail getById(Long placeId) {
        return toDetail(findPlace(placeId));
    }

    /**
     * 외부 검색(Google Places) 결과를 DB에 캐싱하고 결과 목록을 반환.
     * Google Maps API 키 미설정 시 → DB 검색으로 폴백.
     */
    @Transactional
    public List<PlaceDto.Detail> importByQuery(String query) {
        if (!googleMapsClient.isEnabled()) {
            return search(query, null, null);
        }
        var results = googleMapsClient.searchText(query);
        return results.stream()
            .map(this::upsertCache)
            .map(this::toDetail)
            .toList();
    }

    private Place upsertCache(GoogleMapsDto.PlaceSearchResult r) {
        return placeRepository.findByGooglePlaceId(r.googlePlaceId())
            .map(existing -> {
                if (existing.isCacheExpired(cacheTtlDays)) {
                    existing.refreshCache(r.name(), r.address(), r.rating(), r.thumbnailUrl());
                }
                return existing;
            })
            .orElseGet(() -> placeRepository.save(Place.builder()
                .googlePlaceId(r.googlePlaceId())
                .name(r.name())
                .address(r.address())
                .category(Place.Category.OTHER)
                .latitude(r.latitude())
                .longitude(r.longitude())
                .googleRating(r.rating())
                .thumbnailUrl(r.thumbnailUrl())
                .build()));
    }

    private Place findPlace(Long id) {
        return placeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("PLACE_NOT_FOUND",
                "장소를 찾을 수 없습니다. id=" + id));
    }

    private Place.Category parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Place.Category.valueOf(value); } catch (IllegalArgumentException e) { return null; }
    }

    private PlaceDto.Detail toDetail(Place p) {
        return new PlaceDto.Detail(
            p.getId(), p.getGooglePlaceId(), p.getName(), p.getNameLocal(),
            p.getAddress(), p.getCategory().name(),
            p.getLatitude(), p.getLongitude(), p.getGoogleRating(), p.getThumbnailUrl(),
            p.getCachedAt()
        );
    }
}
