package com.triple.travel.domain.travelogue.service;

import com.triple.travel.common.exception.DuplicateResourceException;
import com.triple.travel.common.exception.EntityNotFoundException;
import com.triple.travel.common.exception.ForbiddenException;
import com.triple.travel.domain.itinerary.dto.ItineraryResponse;
import com.triple.travel.domain.itinerary.entity.*;
import com.triple.travel.domain.itinerary.repository.ItineraryRepository;
import com.triple.travel.domain.itinerary.repository.ItineraryDayRepository;
import com.triple.travel.domain.itinerary.repository.ItinerarySlotRepository;
import com.triple.travel.domain.itinerary.service.ItineraryService;
import com.triple.travel.domain.place.entity.Place;
import com.triple.travel.domain.place.repository.PlaceRepository;
import com.triple.travel.domain.travelogue.dto.TravelogueRequest;
import com.triple.travel.domain.travelogue.dto.TravelogueResponse;
import com.triple.travel.domain.travelogue.entity.*;
import com.triple.travel.domain.travelogue.repository.*;
import com.triple.travel.domain.user.entity.User;
import com.triple.travel.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelogueService {

    private final TravelogueRepository travelogueRepository;
    private final TravelogueDayRepository travelogueDayRepository;
    private final TravelogueBlockRepository travelogueBlockRepository;
    private final TravelogueLikeRepository travelogueLikeRepository;
    private final TravelogueScrapRepository travelogueScrapRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryDayRepository itineraryDayRepository;
    private final ItinerarySlotRepository itinerarySlotRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    // ───────────────────────────────────────────────────────────────
    // 조회
    // ───────────────────────────────────────────────────────────────

    public TravelogueResponse.Detail getTravelogue(Long travelogueId, Long userId) {
        var travelogue = travelogueRepository.findWithDaysAndBlocksById(travelogueId)
            .orElseThrow(() -> new EntityNotFoundException("TRAVELOGUE_NOT_FOUND",
                "여행기를 찾을 수 없습니다."));

        // DRAFT는 본인만 조회 가능
        if (travelogue.getStatus() == Travelogue.Status.DRAFT &&
            !travelogue.getUser().getId().equals(userId)) {
            throw new ForbiddenException("FORBIDDEN");
        }

        travelogue.incrementViewCount(); // 조회수 증가 (dirty checking)

        boolean likedByMe = userId != null &&
            travelogueLikeRepository.existsByTravelogueIdAndUserId(travelogueId, userId);
        boolean scrappedByMe = userId != null &&
            travelogueScrapRepository.existsByUserIdAndTravelogueId(userId, travelogueId);

        return toDetail(travelogue, likedByMe, scrappedByMe);
    }

    public List<TravelogueResponse.Summary> getFeed(String city, Long cursor, int size) {
        return travelogueRepository
            .findPublishedWithCursor(city, cursor, PageRequest.of(0, size))
            .stream()
            .map(this::toSummary)
            .toList();
    }

    public List<TravelogueResponse.Summary> getMyTravelogues(Long userId) {
        return travelogueRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toSummary)
            .toList();
    }

    // ───────────────────────────────────────────────────────────────
    // ★ 핵심 비즈니스 로직 #2: Itinerary → Travelogue 스냅샷 발행
    // ───────────────────────────────────────────────────────────────

    /**
     * 발행(Publish) 흐름:
     * <pre>
     * 1. Itinerary + days + slots + place 로딩 (JOIN FETCH, 단일 쿼리)
     * 2. Travelogue 생성 (메타 스냅샷)
     * 3. ItineraryDay → TravelogueDay 복사
     * 4. ItinerarySlot → TravelogueBlock(PLACE) 복사
     *    - Place 참조 공유: Place는 Google Maps 캐시로 독립 관리되므로 참조 공유 안전
     *    - Itinerary 이후 수정·삭제 → Travelogue 영향 없음 (연결 고리가 sourceItineraryId만)
     * 5. travelogueRepository.save() 단 한 번 → Cascade로 Day → Block 자동 영속화
     * </pre>
     */
    @Transactional
    public TravelogueResponse.Created publish(Long userId, TravelogueRequest.Publish req) {
        var user = findUser(userId);

        // 1. Itinerary + days + slots + place 한 번에 로딩
        var itinerary = itineraryRepository.findWithDaysAndSlotsById(req.itineraryId())
            .orElseThrow(() -> new EntityNotFoundException("ITINERARY_NOT_FOUND",
                "일정을 찾을 수 없습니다."));

        if (!itinerary.getUser().getId().equals(userId)) {
            throw new ForbiddenException("FORBIDDEN");
        }

        // 2. Travelogue 생성 (메타 스냅샷)
        var travelogue = Travelogue.builder()
            .user(user)
            .sourceItineraryId(itinerary.getId())   // 느슨한 참조만 보관
            .title(req.title() != null ? req.title() : itinerary.getTitle())
            .coverImageUrl(req.coverImageUrl())
            .summary(req.summary())
            .city(itinerary.getCity())
            .country(itinerary.getCountry())
            .travelStartDate(itinerary.getStartDate())
            .travelEndDate(itinerary.getEndDate())
            .build();

        // 3 & 4. Day → Slot → Block Deep Copy (Snapshot)
        for (ItineraryDay itDay : itinerary.getDays()) {
            var tDay = TravelogueDay.builder()
                .travelogue(travelogue)
                .dayNumber(itDay.getDayNumber())
                .date(itDay.getDate())
                .build();

            int blockOrder = 1;
            for (ItinerarySlot slot : itDay.getSlots()) {
                // 발행 시점의 선택된 장소(slot.place)를 PLACE 블록으로 스냅샷
                var block = TravelogueBlock.builder()
                    .travelogueDay(tDay)
                    .orderIndex(blockOrder++)
                    .blockType(TravelogueBlock.BlockType.PLACE)
                    .place(slot.getPlace())             // 발행 시점 장소 참조
                    .placeVisitTime(slot.getSlotTime())
                    .build();

                tDay.addBlock(block);
            }

            travelogue.addDay(tDay);    // Travelogue.days 리스트에 추가
        }

        // 5. Cascade 저장: Travelogue → TravelogueDays → TravelogueBlocks
        var saved = travelogueRepository.save(travelogue);

        // 발행 상태로 전환
        saved.publish();

        return new TravelogueResponse.Created(saved.getId());
    }

    // ───────────────────────────────────────────────────────────────
    // 여행기 관리
    // ───────────────────────────────────────────────────────────────

    @Transactional
    public void updateMeta(Long travelogueId, Long userId, TravelogueRequest.UpdateMeta req) {
        var travelogue = findOwnedTravelogue(travelogueId, userId);
        travelogue.updateMeta(req.title(), req.coverImageUrl(), req.summary());
    }

    @Transactional
    public void unpublish(Long travelogueId, Long userId) {
        var travelogue = findOwnedTravelogue(travelogueId, userId);
        travelogue.unpublish();
    }

    @Transactional
    public void deleteTravelogue(Long travelogueId, Long userId) {
        var travelogue = findOwnedTravelogue(travelogueId, userId);
        travelogueRepository.delete(travelogue);
    }

    // ───────────────────────────────────────────────────────────────
    // 콘텐츠 블록 편집
    // ───────────────────────────────────────────────────────────────

    @Transactional
    public void addBlock(Long travelogueId, Long dayId, Long userId,
                         TravelogueRequest.AddBlock req) {
        findOwnedTravelogue(travelogueId, userId);

        var tDay = travelogueDayRepository.findByIdAndTravelogueId(dayId, travelogueId)
            .orElseThrow(() -> new EntityNotFoundException("TRAVELOGUE_DAY_NOT_FOUND",
                "여행기 Day를 찾을 수 없습니다."));

        var blockType = TravelogueBlock.BlockType.valueOf(req.blockType());
        var placeVisitTime = req.placeVisitTime() != null
            ? LocalTime.parse(req.placeVisitTime()) : null;
        Place place = null;
        if (req.placeId() != null) {
            place = placeRepository.findById(req.placeId())
                .orElseThrow(() -> new EntityNotFoundException("PLACE_NOT_FOUND", "장소를 찾을 수 없습니다."));
        }

        var block = TravelogueBlock.builder()
            .travelogueDay(tDay)
            .orderIndex(req.orderIndex())
            .blockType(blockType)
            .content(req.content())
            .imageUrl(req.imageUrl())
            .place(place)
            .placeVisitTime(placeVisitTime)
            .placeMemo(req.placeMemo())
            .build();

        tDay.addBlock(block);
    }

    @Transactional
    public void deleteBlock(Long travelogueId, Long dayId, Long blockId, Long userId) {
        findOwnedTravelogue(travelogueId, userId);
        var block = travelogueBlockRepository.findByIdAndDayId(blockId, dayId)
            .orElseThrow(() -> new EntityNotFoundException("BLOCK_NOT_FOUND",
                "블록을 찾을 수 없습니다."));
        travelogueBlockRepository.delete(block);
    }

    @Transactional
    public void reorderBlocks(Long travelogueId, Long dayId, Long userId,
                               TravelogueRequest.ReorderBlocks req) {
        findOwnedTravelogue(travelogueId, userId);
        var orderMap = req.orders().stream()
            .collect(java.util.stream.Collectors.toMap(
                TravelogueRequest.ReorderBlocks.BlockOrder::blockId,
                TravelogueRequest.ReorderBlocks.BlockOrder::orderIndex
            ));

        travelogueDayRepository.findByIdAndTravelogueId(dayId, travelogueId)
            .orElseThrow(() -> new EntityNotFoundException("TRAVELOGUE_DAY_NOT_FOUND",
                "여행기 Day를 찾을 수 없습니다."))
            .getBlocks()
            .forEach(block -> {
                var newOrder = orderMap.get(block.getId());
                if (newOrder != null) block.updateOrder(newOrder);
            });
    }

    // ───────────────────────────────────────────────────────────────
    // 좋아요 / 스크랩
    // ───────────────────────────────────────────────────────────────

    @Transactional
    public void likeTravelogue(Long travelogueId, Long userId) {
        if (travelogueLikeRepository.existsByTravelogueIdAndUserId(travelogueId, userId)) {
            throw new DuplicateResourceException("ALREADY_LIKED");
        }
        var travelogue = findPublishedTravelogue(travelogueId);
        var user = findUser(userId);

        travelogueLikeRepository.save(TravelogueLike.builder()
            .travelogue(travelogue).user(user).build());
        travelogue.incrementLikeCount();
    }

    @Transactional
    public void unlikeTravelogue(Long travelogueId, Long userId) {
        var like = travelogueLikeRepository.findByTravelogueIdAndUserId(travelogueId, userId)
            .orElseThrow(() -> new EntityNotFoundException("LIKE_NOT_FOUND", "좋아요 이력이 없습니다."));
        travelogueLikeRepository.delete(like);

        var travelogue = findPublishedTravelogue(travelogueId);
        travelogue.decrementLikeCount();
    }

    /**
     * 스크랩: Travelogue의 PLACE 블록 동선을 내 Itinerary로 역방향 Deep Copy.
     *
     * <pre>
     * Travelogue.days → ItineraryDay 복사
     * TravelogueBlock(PLACE) → ItinerarySlot 복사
     * TravelogueScrap 이력 저장 + scrapCount 증가
     * </pre>
     */
    @Transactional
    public ItineraryResponse.Created scrapTravelogue(Long travelogueId, Long userId) {
        if (travelogueScrapRepository.existsByUserIdAndTravelogueId(userId, travelogueId)) {
            throw new DuplicateResourceException("ALREADY_SCRAPED");
        }

        // PLACE 블록만 JOIN FETCH (동선 복사에 필요한 데이터만 로딩)
        var travelogue = travelogueRepository.findWithPlaceBlocksById(travelogueId)
            .orElseThrow(() -> new EntityNotFoundException("TRAVELOGUE_NOT_FOUND",
                "여행기를 찾을 수 없습니다."));

        var user = findUser(userId);

        // 1. 새 Itinerary 생성 (SCRAPED)
        var itinerary = Itinerary.builder()
            .user(user)
            .title(travelogue.getTitle() + " (스크랩)")
            .city(travelogue.getCity())
            .country(travelogue.getCountry())
            .startDate(travelogue.getTravelStartDate())
            .endDate(travelogue.getTravelEndDate())
            .originTravelogueId(travelogue.getId())
            .sourceType(Itinerary.SourceType.SCRAPED)
            .build();

        var savedItinerary = itineraryRepository.save(itinerary);

        // 2. Day + Slot Deep Copy
        for (TravelogueDay tDay : travelogue.getDays()) {
            var itDay = ItineraryDay.builder()
                .itinerary(savedItinerary)
                .dayNumber(tDay.getDayNumber())
                .date(tDay.getDate())
                .build();

            var savedDay = itineraryDayRepository.save(itDay);

            // PLACE 블록만 슬롯으로 변환 (blockType 필터는 Repository 쿼리에서 처리됨)
            int slotOrder = 1;
            for (TravelogueBlock block : tDay.getBlocks()) {
                if (block.getBlockType() != TravelogueBlock.BlockType.PLACE) continue;

                var slot = ItinerarySlot.builder()
                    .itineraryDay(savedDay)
                    .orderIndex(slotOrder++)
                    .slotTime(block.getPlaceVisitTime())
                    .place(block.getPlace())
                    .build();

                itinerarySlotRepository.save(slot);
            }
        }

        // 3. 스크랩 이력 저장 + scrapCount 증가
        travelogueScrapRepository.save(TravelogueScrap.builder()
            .user(user)
            .travelogue(travelogue)
            .createdItinerary(savedItinerary)
            .build());

        travelogue.incrementScrapCount();

        return new ItineraryResponse.Created(savedItinerary.getId());
    }

    // ───────────────────────────────────────────────────────────────
    // 내부 헬퍼: 조회 & 검증
    // ───────────────────────────────────────────────────────────────

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND",
                "사용자를 찾을 수 없습니다."));
    }

    private Travelogue findOwnedTravelogue(Long travelogueId, Long userId) {
        return travelogueRepository.findByIdAndUserId(travelogueId, userId)
            .orElseThrow(() -> new EntityNotFoundException("TRAVELOGUE_NOT_FOUND",
                "여행기를 찾을 수 없거나 접근 권한이 없습니다."));
    }

    private Travelogue findPublishedTravelogue(Long travelogueId) {
        var t = travelogueRepository.findById(travelogueId)
            .orElseThrow(() -> new EntityNotFoundException("TRAVELOGUE_NOT_FOUND",
                "여행기를 찾을 수 없습니다."));
        if (t.getStatus() != Travelogue.Status.PUBLISHED) {
            throw new ForbiddenException("TRAVELOGUE_NOT_PUBLISHED");
        }
        return t;
    }

    // ───────────────────────────────────────────────────────────────
    // 내부 헬퍼: 엔티티 → DTO 변환
    // ───────────────────────────────────────────────────────────────

    private TravelogueResponse.Summary toSummary(Travelogue t) {
        var author = new TravelogueResponse.AuthorInfo(
            t.getUser().getId(), t.getUser().getNickname(), t.getUser().getProfileImageUrl()
        );
        return new TravelogueResponse.Summary(
            t.getId(), t.getTitle(), t.getCoverImageUrl(), t.getSummary(),
            t.getCity(), t.getCountry(), t.getTravelStartDate(), t.getTravelEndDate(),
            author, t.getLikeCount(), t.getScrapCount(), t.getViewCount(),
            t.getStatus().name(), t.getPublishedAt()
        );
    }

    private TravelogueResponse.Detail toDetail(Travelogue t, boolean likedByMe,
                                                boolean scrappedByMe) {
        var author = new TravelogueResponse.AuthorInfo(
            t.getUser().getId(), t.getUser().getNickname(), t.getUser().getProfileImageUrl()
        );
        var dayDetails = t.getDays().stream().map(this::toDayDetail).toList();

        return new TravelogueResponse.Detail(
            t.getId(), t.getTitle(), t.getCoverImageUrl(), t.getSummary(),
            t.getCity(), t.getCountry(), t.getTravelStartDate(), t.getTravelEndDate(),
            author, t.getLikeCount(), t.getScrapCount(), t.getViewCount(),
            likedByMe, scrappedByMe,
            t.getStatus().name(), t.getPublishedAt(), dayDetails
        );
    }

    private TravelogueResponse.DayDetail toDayDetail(TravelogueDay day) {
        var blocks = day.getBlocks().stream().map(this::toBlockDetail).toList();
        return new TravelogueResponse.DayDetail(
            day.getId(), day.getDayNumber(), day.getDate(), blocks
        );
    }

    private TravelogueResponse.BlockDetail toBlockDetail(TravelogueBlock block) {
        var placeSummary = block.getPlace() != null
            ? ItineraryService.toPlaceSummary(block.getPlace()) : null;
        return new TravelogueResponse.BlockDetail(
            block.getId(), block.getOrderIndex(), block.getBlockType().name(),
            block.getContent(), block.getImageUrl(),
            placeSummary, block.getPlaceVisitTime(), block.getPlaceMemo()
        );
    }
}
