package com.triple.travel.domain.itinerary.service;

import com.triple.travel.common.exception.EntityNotFoundException;
import com.triple.travel.common.exception.ForbiddenException;
import com.triple.travel.domain.itinerary.dto.ItineraryRequest;
import com.triple.travel.domain.itinerary.dto.ItineraryResponse;
import com.triple.travel.domain.itinerary.dto.PlaceSummary;
import com.triple.travel.domain.itinerary.entity.*;
import com.triple.travel.domain.itinerary.entity.Itinerary.Status;
import com.triple.travel.domain.itinerary.repository.*;
import com.triple.travel.domain.place.entity.Place;
import com.triple.travel.domain.place.repository.PlaceRepository;
import com.triple.travel.domain.user.entity.User;
import com.triple.travel.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final ItineraryDayRepository dayRepository;
    private final ItinerarySlotRepository slotRepository;
    private final ItinerarySlotAlternativeRepository alternativeRepository;
    private final AiGenerationRequestRepository aiRequestRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    // ───────────────────────────────────────────────────────────────
    // 조회
    // ───────────────────────────────────────────────────────────────

    public List<ItineraryResponse.Summary> getMyItineraries(Long userId, String status,
                                                             Long cursor, int size) {
        var statusEnum = status != null ? Status.valueOf(status) : null;
        return itineraryRepository
            .findByUserWithCursor(userId, statusEnum, cursor, PageRequest.of(0, size))
            .stream()
            .map(this::toSummary)
            .toList();
    }

    /**
     * N+1 방지 전략:
     *  Query #1 — findWithDaysAndSlotsById: Itinerary + days + slots + place (JOIN FETCH)
     *  Query #2 — findWithPlaceBySlotIdIn: 모든 슬롯의 alternatives를 IN 쿼리 단일 실행
     *  총 2 queries로 전체 그래프 로딩 완료.
     */
    public ItineraryResponse.Detail getItinerary(Long itineraryId, Long userId) {
        var itinerary = itineraryRepository.findWithDaysAndSlotsById(itineraryId)
            .orElseThrow(() -> new EntityNotFoundException("ITINERARY_NOT_FOUND",
                "일정을 찾을 수 없습니다. id=" + itineraryId));

        validateOwner(itinerary, userId);

        // 전체 슬롯 ID 수집
        var slotIds = itinerary.getDays().stream()
            .flatMap(d -> d.getSlots().stream())
            .map(ItinerarySlot::getId)
            .toList();

        // Query #2: 모든 대안 장소를 한 번에 로딩 → slotId 기준으로 그루핑
        Map<Long, List<ItinerarySlotAlternative>> altsBySlotId = slotIds.isEmpty()
            ? Map.of()
            : alternativeRepository.findWithPlaceBySlotIdIn(slotIds).stream()
                .collect(groupingBy(a -> a.getSlot().getId()));

        var dayDetails = itinerary.getDays().stream()
            .map(day -> toDayDetail(day, altsBySlotId))
            .toList();

        return new ItineraryResponse.Detail(
            itinerary.getId(),
            itinerary.getTitle(),
            itinerary.getCity(),
            itinerary.getCountry(),
            itinerary.getStartDate(),
            itinerary.getEndDate(),
            itinerary.getCompanionType() != null ? itinerary.getCompanionType().name() : null,
            deserializeThemes(itinerary.getThemes()),
            itinerary.getPace() != null ? itinerary.getPace().name() : null,
            itinerary.getSourceType().name(),
            itinerary.getStatus().name(),
            dayDetails,
            itinerary.getCreatedAt(),
            itinerary.getUpdatedAt()
        );
    }

    public ItineraryResponse.JobStatus pollAiRequest(Long requestId, Long userId) {
        var req = aiRequestRepository.findByIdAndUserId(requestId, userId)
            .orElseThrow(() -> new EntityNotFoundException("AI_REQUEST_NOT_FOUND",
                "AI 요청을 찾을 수 없습니다."));
        return toJobStatus(req);
    }

    public List<ItineraryResponse.AlternativeDetail> getAlternatives(Long itineraryId,
                                                                       Long dayId, Long slotId,
                                                                       Long userId) {
        // 소유권 검증
        itineraryRepository.findByIdAndUserId(itineraryId, userId)
            .orElseThrow(() -> new EntityNotFoundException("ITINERARY_NOT_FOUND", "일정을 찾을 수 없습니다."));

        return alternativeRepository.findWithPlaceBySlotId(slotId).stream()
            .map(this::toAlternativeDetail)
            .toList();
    }

    // ───────────────────────────────────────────────────────────────
    // 일정 생성 / 수정 / 삭제
    // ───────────────────────────────────────────────────────────────

    @Transactional
    public ItineraryResponse.Created createManual(Long userId, ItineraryRequest.CreateManual req) {
        var user = findUser(userId);
        var itinerary = Itinerary.builder()
            .user(user)
            .title(req.title())
            .city(req.city())
            .country(req.country())
            .startDate(req.startDate())
            .endDate(req.endDate())
            .sourceType(Itinerary.SourceType.MANUAL)
            .build();

        return new ItineraryResponse.Created(itineraryRepository.save(itinerary).getId());
    }

    @Transactional
    public ItineraryResponse.Created updateMeta(Long itineraryId, Long userId,
                                                 ItineraryRequest.UpdateMeta req) {
        var itinerary = findOwnedItinerary(itineraryId, userId);
        itinerary.updateMeta(req.title(), req.city(), req.country(), req.startDate(), req.endDate());
        // dirty checking → UPDATE 자동 처리
        return new ItineraryResponse.Created(itinerary.getId());
    }

    @Transactional
    public void deleteItinerary(Long itineraryId, Long userId) {
        var itinerary = findOwnedItinerary(itineraryId, userId);
        itineraryRepository.delete(itinerary);
    }

    // ───────────────────────────────────────────────────────────────
    // AI / YouTube 비동기 요청
    // ───────────────────────────────────────────────────────────────

    @Transactional
    public ItineraryResponse.JobStatus requestAiGenerate(Long userId, ItineraryRequest.GenerateByAi req) {
        var user = findUser(userId);
        var inputJson = buildAiFormJson(req);

        var aiRequest = AiGenerationRequest.builder()
            .user(user)
            .requestType(AiGenerationRequest.RequestType.AI_FORM)
            .inputData(inputJson)
            .build();

        return toJobStatus(aiRequestRepository.save(aiRequest));
    }

    @Transactional
    public ItineraryResponse.JobStatus requestYoutubeParse(Long userId,
                                                            ItineraryRequest.ParseYoutube req) {
        var user = findUser(userId);
        var inputJson = "{\"youtubeUrl\":\"" + req.youtubeUrl() + "\"}";

        var aiRequest = AiGenerationRequest.builder()
            .user(user)
            .requestType(AiGenerationRequest.RequestType.YOUTUBE_PARSE)
            .inputData(inputJson)
            .build();

        return toJobStatus(aiRequestRepository.save(aiRequest));
    }

    // ───────────────────────────────────────────────────────────────
    // Day CRUD
    // ───────────────────────────────────────────────────────────────

    @Transactional
    public void addDay(Long itineraryId, Long userId, ItineraryRequest.AddDay req) {
        var itinerary = findOwnedItinerary(itineraryId, userId);

        if (dayRepository.existsByItineraryIdAndDayNumber(itineraryId, req.dayNumber())) {
            throw new IllegalArgumentException("이미 존재하는 day_number입니다: " + req.dayNumber());
        }

        var day = ItineraryDay.builder()
            .itinerary(itinerary)
            .dayNumber(req.dayNumber())
            .date(req.date())
            .build();

        dayRepository.save(day);
    }

    @Transactional
    public void updateDay(Long itineraryId, Long dayId, Long userId,
                          ItineraryRequest.UpdateDay req) {
        findOwnedItinerary(itineraryId, userId);
        var day = findDay(dayId, itineraryId);
        day.update(req.date(), req.memo());
    }

    @Transactional
    public void deleteDay(Long itineraryId, Long dayId, Long userId) {
        findOwnedItinerary(itineraryId, userId);
        var day = findDay(dayId, itineraryId);
        dayRepository.delete(day); // orphanRemoval cascade → slots 자동 삭제
    }

    // ───────────────────────────────────────────────────────────────
    // Slot CRUD
    // ───────────────────────────────────────────────────────────────

    @Transactional
    public void addSlot(Long itineraryId, Long dayId, Long userId,
                        ItineraryRequest.AddSlot req) {
        findOwnedItinerary(itineraryId, userId);
        var day = findDay(dayId, itineraryId);
        var place = findPlace(req.placeId());

        var slotTime = req.slotTime() != null ? LocalTime.parse(req.slotTime()) : null;
        var category = req.timeCategory() != null
            ? ItinerarySlot.TimeCategory.valueOf(req.timeCategory()) : null;

        var slot = ItinerarySlot.builder()
            .itineraryDay(day)
            .orderIndex(req.orderIndex())
            .slotTime(slotTime)
            .place(place)
            .timeCategory(category)
            .stayDurationMinutes(req.stayDurationMinutes())
            .build();

        slotRepository.save(slot);
    }

    @Transactional
    public void updateSlot(Long itineraryId, Long dayId, Long slotId, Long userId,
                           ItineraryRequest.UpdateSlot req) {
        findOwnedItinerary(itineraryId, userId);
        var slot = findSlotWithPlace(slotId, dayId);

        var slotTime = req.slotTime() != null ? LocalTime.parse(req.slotTime()) : null;
        slot.update(slotTime, req.orderIndex(), req.memo(), req.stayDurationMinutes());
    }

    @Transactional
    public void deleteSlot(Long itineraryId, Long dayId, Long slotId, Long userId) {
        findOwnedItinerary(itineraryId, userId);
        var slot = findSlotWithPlace(slotId, dayId);
        slotRepository.delete(slot);
    }

    // ───────────────────────────────────────────────────────────────
    // ★ 핵심 비즈니스 로직 #1: Place Swap (원자적 교환)
    // ───────────────────────────────────────────────────────────────

    /**
     * 현재 슬롯의 장소와 대안 장소를 하나의 트랜잭션 내에서 교환한다.
     *
     * <pre>
     * 교환 전: slot.place = A,  alternative.place = B
     * 교환 후: slot.place = B,  alternative.place = A
     * </pre>
     *
     * JPA Dirty Checking이 트랜잭션 커밋 시점에 두 UPDATE를 모두 실행하므로
     * 별도의 save() 호출 없이 원자적으로 처리된다.
     */
    @Transactional
    public ItineraryResponse.SlotDetail swapPlace(Long itineraryId, Long dayId, Long slotId,
                                                   ItineraryRequest.SwapPlace req, Long userId) {
        // 1. 소유권 검증 (Itinerary가 이 유저 소유인지)
        findOwnedItinerary(itineraryId, userId);

        // 2. Slot 조회: place JOIN FETCH 포함, day 소속 검증
        var slot = slotRepository.findWithPlaceByIdAndDayId(slotId, dayId)
            .orElseThrow(() -> new EntityNotFoundException("SLOT_NOT_FOUND",
                "슬롯을 찾을 수 없습니다. slotId=" + slotId));

        // 3. Alternative 조회: place JOIN FETCH 포함, slot 소속 검증
        var alternative = alternativeRepository
            .findWithPlaceByIdAndSlotId(req.alternativeId(), slotId)
            .orElseThrow(() -> new EntityNotFoundException("ALTERNATIVE_NOT_FOUND",
                "대안 장소를 찾을 수 없습니다. alternativeId=" + req.alternativeId()));

        // 4. 현재 장소 참조 보관
        Place currentPlace = slot.getPlace();
        Place newPlace = alternative.getPlace();

        // 5. 도메인 메서드를 통한 상태 변경 (캡슐화)
        //    slot.place    ← newPlace  (대안으로 교체)
        //    alt.place     ← currentPlace (구 장소를 대안으로 보관)
        slot.swapPlace(newPlace);
        alternative.updatePlace(currentPlace);

        // 6. Dirty Checking → 트랜잭션 종료 시 UPDATE 2건 자동 실행

        // 7. 업데이트된 슬롯 응답 반환
        var updatedAlternatives = alternativeRepository.findWithPlaceBySlotId(slotId).stream()
            .map(this::toAlternativeDetail)
            .toList();

        return toSlotDetail(slot, updatedAlternatives);
    }

    // ───────────────────────────────────────────────────────────────
    // Slot 순서 일괄 변경
    // ───────────────────────────────────────────────────────────────

    @Transactional
    public void reorderSlots(Long itineraryId, Long dayId, Long userId,
                              ItineraryRequest.ReorderSlots req) {
        findOwnedItinerary(itineraryId, userId);

        // orderIndex 맵 구성 (slotId → newOrderIndex)
        var orderMap = req.orders().stream()
            .collect(Collectors.toMap(
                ItineraryRequest.ReorderSlots.SlotOrder::slotId,
                ItineraryRequest.ReorderSlots.SlotOrder::orderIndex
            ));

        slotRepository.findByDayIdOrderByOrderIndex(dayId)
            .forEach(slot -> {
                var newOrder = orderMap.get(slot.getId());
                if (newOrder != null) {
                    slot.update(slot.getSlotTime(), newOrder, slot.getMemo(),
                        slot.getStayDurationMinutes());
                }
            });
        // Dirty Checking → 변경된 slot들 UPDATE
    }

    // ───────────────────────────────────────────────────────────────
    // 내부 헬퍼: 조회 & 검증
    // ───────────────────────────────────────────────────────────────

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND",
                "사용자를 찾을 수 없습니다."));
    }

    private Place findPlace(Long placeId) {
        return placeRepository.findById(placeId)
            .orElseThrow(() -> new EntityNotFoundException("PLACE_NOT_FOUND",
                "장소를 찾을 수 없습니다. placeId=" + placeId));
    }

    private Itinerary findOwnedItinerary(Long itineraryId, Long userId) {
        return itineraryRepository.findByIdAndUserId(itineraryId, userId)
            .orElseThrow(() -> new EntityNotFoundException("ITINERARY_NOT_FOUND",
                "일정을 찾을 수 없거나 접근 권한이 없습니다."));
    }

    private ItineraryDay findDay(Long dayId, Long itineraryId) {
        return dayRepository.findByIdAndItineraryId(dayId, itineraryId)
            .orElseThrow(() -> new EntityNotFoundException("DAY_NOT_FOUND",
                "해당 일정에 속한 Day를 찾을 수 없습니다."));
    }

    private ItinerarySlot findSlotWithPlace(Long slotId, Long dayId) {
        return slotRepository.findWithPlaceByIdAndDayId(slotId, dayId)
            .orElseThrow(() -> new EntityNotFoundException("SLOT_NOT_FOUND",
                "슬롯을 찾을 수 없습니다."));
    }

    private void validateOwner(Itinerary itinerary, Long userId) {
        if (!itinerary.getUser().getId().equals(userId)) {
            throw new ForbiddenException("FORBIDDEN");
        }
    }

    // ───────────────────────────────────────────────────────────────
    // 내부 헬퍼: 엔티티 → DTO 변환
    // ───────────────────────────────────────────────────────────────

    private ItineraryResponse.Summary toSummary(Itinerary it) {
        return new ItineraryResponse.Summary(
            it.getId(), it.getTitle(), it.getCity(), it.getCountry(),
            it.getStartDate(), it.getEndDate(),
            it.getSourceType().name(), it.getStatus().name(),
            it.getCreatedAt()
        );
    }

    private ItineraryResponse.DayDetail toDayDetail(ItineraryDay day,
                                                     Map<Long, List<ItinerarySlotAlternative>> altsBySlotId) {
        var slotDetails = day.getSlots().stream()
            .map(slot -> {
                var alts = altsBySlotId.getOrDefault(slot.getId(), List.of()).stream()
                    .map(this::toAlternativeDetail)
                    .toList();
                return toSlotDetail(slot, alts);
            })
            .toList();

        return new ItineraryResponse.DayDetail(
            day.getId(), day.getDayNumber(), day.getDate(), day.getMemo(), slotDetails
        );
    }

    private ItineraryResponse.SlotDetail toSlotDetail(ItinerarySlot slot,
                                                       List<ItineraryResponse.AlternativeDetail> alts) {
        return new ItineraryResponse.SlotDetail(
            slot.getId(),
            slot.getOrderIndex(),
            slot.getSlotTime(),
            slot.getTimeCategory() != null ? slot.getTimeCategory().name() : null,
            slot.getStayDurationMinutes(),
            slot.getMemo(),
            toPlaceSummary(slot.getPlace()),
            alts
        );
    }

    private ItineraryResponse.AlternativeDetail toAlternativeDetail(ItinerarySlotAlternative alt) {
        return new ItineraryResponse.AlternativeDetail(
            alt.getId(), alt.getOrderIndex(), toPlaceSummary(alt.getPlace())
        );
    }

    public static PlaceSummary toPlaceSummary(Place place) {
        return new PlaceSummary(
            place.getId(), place.getName(), place.getAddress(),
            place.getCategory().name(),
            place.getLatitude(), place.getLongitude(),
            place.getGoogleRating(), place.getThumbnailUrl()
        );
    }

    private ItineraryResponse.JobStatus toJobStatus(AiGenerationRequest req) {
        return new ItineraryResponse.JobStatus(
            req.getId(),
            req.getStatus().name(),
            req.getResultItineraryId(),
            req.getErrorMessage()
        );
    }

    // themes: "FOOD,CULTURE" ↔ List<String>
    private List<String> deserializeThemes(String themes) {
        if (themes == null || themes.isBlank()) return List.of();
        return Arrays.asList(themes.split(","));
    }

    private String buildAiFormJson(ItineraryRequest.GenerateByAi req) {
        // 실제 구현에서는 ObjectMapper 사용 권장
        return String.format(
            "{\"city\":\"%s\",\"startDate\":\"%s\",\"endDate\":\"%s\"," +
            "\"companionType\":\"%s\",\"pace\":\"%s\"}",
            req.city(), req.startDate(), req.endDate(),
            req.companionType(), req.pace()
        );
    }
}
