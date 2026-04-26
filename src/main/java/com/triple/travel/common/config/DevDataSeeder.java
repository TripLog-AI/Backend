package com.triple.travel.common.config;

import com.triple.travel.domain.itinerary.entity.Itinerary;
import com.triple.travel.domain.itinerary.entity.ItineraryDay;
import com.triple.travel.domain.itinerary.entity.ItinerarySlot;
import com.triple.travel.domain.itinerary.repository.ItineraryDayRepository;
import com.triple.travel.domain.itinerary.repository.ItineraryRepository;
import com.triple.travel.domain.itinerary.repository.ItinerarySlotRepository;
import com.triple.travel.domain.place.entity.Place;
import com.triple.travel.domain.place.repository.PlaceRepository;
import com.triple.travel.domain.user.entity.User;
import com.triple.travel.domain.user.repository.UserRepository;
import com.triple.travel.domain.youtube.entity.YoutubeSource;
import com.triple.travel.domain.youtube.repository.YoutubeSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * dev 프로필 부팅 시 데모용 시드 데이터를 채워 넣는다.
 * H2 in-memory + ddl-auto=create-drop 이라 매 부팅마다 새로 생성됨.
 *
 * - admin 시스템 유저 1명
 * - YouTube featured 코스 3개 (Tokyo / Paris / Barcelona)
 * - Tokyo 코스에는 시드 일정(YOUTUBE_SEED) + Place 4개를 연결
 *   → /youtube-courses/{id}/save 시 deep copy 동작 시연 가능
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final YoutubeSourceRepository youtubeSourceRepository;
    private final PlaceRepository placeRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryDayRepository itineraryDayRepository;
    private final ItinerarySlotRepository itinerarySlotRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (youtubeSourceRepository.count() > 0) return;

        User admin = ensureAdmin();

        var tokyo = saveYoutubeSource("dQw4w9WgXcQ",
            "도쿄 2박 3일 완벽 가이드 | 현지인이 추천하는 숨은 맛집", "여행하는 삶");
        saveYoutubeSource("xvFZjo5PgG0",
            "파리 혼자 여행 4박 5일 Vlog | 에펠탑, 루브르, 몽마르트", "솔로트래블러");
        saveYoutubeSource("C0DPdy98e4c",
            "바르셀로나 3일 루트 | 가우디 건축 투어 완전정복", "건축여행자");

        seedTokyoItinerary(admin, tokyo);

        log.info("DevDataSeeder: seeded {} courses, {} places, {} seed itineraries",
            youtubeSourceRepository.count(),
            placeRepository.count(),
            itineraryRepository.count());
    }

    private User ensureAdmin() {
        return userRepository.findByEmail("admin@triplog.local").orElseGet(() ->
            userRepository.save(User.builder()
                .email("admin@triplog.local")
                .nickname("triplog-admin")
                .password(passwordEncoder.encode("admin-password-not-for-login"))
                .provider(User.Provider.LOCAL)
                .build()));
    }

    private YoutubeSource saveYoutubeSource(String videoId, String title, String channel) {
        var source = YoutubeSource.builder()
            .videoId(videoId)
            .url("https://www.youtube.com/watch?v=" + videoId)
            .title(title)
            .channelName(channel)
            .thumbnailUrl("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg")
            .featured(true)
            .build();
        source.markCompleted();
        return youtubeSourceRepository.save(source);
    }

    private void seedTokyoItinerary(User admin, YoutubeSource tokyo) {
        Place shinjuku = savePlace("SEED_TKY_001", "신주쿠 교엔",
            "11 Naitomachi, Shinjuku City, Tokyo", Place.Category.ATTRACTION,
            "35.6852", "139.7100");
        Place ichiran = savePlace("SEED_TKY_002", "이치란 라멘 신주쿠",
            "3-34-11 Shinjuku, Shinjuku City, Tokyo", Place.Category.RESTAURANT,
            "35.6897", "139.7006");
        Place shibuya = savePlace("SEED_TKY_003", "시부야 스크램블",
            "2-1 Dogenzaka, Shibuya City, Tokyo", Place.Category.ATTRACTION,
            "35.6595", "139.7004");
        Place tsukiji = savePlace("SEED_TKY_004", "츠키지 시장",
            "5-2-1 Tsukiji, Chuo City, Tokyo", Place.Category.ATTRACTION,
            "35.6654", "139.7707");

        var itinerary = itineraryRepository.save(Itinerary.builder()
            .user(admin)
            .title("도쿄 2박 3일 완벽 코스")
            .city("Tokyo")
            .country("Japan")
            .youtubeSource(tokyo)
            .sourceType(Itinerary.SourceType.YOUTUBE_SEED)
            .build());

        var day1 = itineraryDayRepository.save(ItineraryDay.builder()
            .itinerary(itinerary).dayNumber(1).build());
        saveSlot(day1, 1, "10:00", ItinerarySlot.TimeCategory.MORNING, 120, shinjuku);
        saveSlot(day1, 2, "12:30", ItinerarySlot.TimeCategory.LUNCH, 60, ichiran);
        saveSlot(day1, 3, "18:00", ItinerarySlot.TimeCategory.NIGHT, 90, shibuya);

        var day2 = itineraryDayRepository.save(ItineraryDay.builder()
            .itinerary(itinerary).dayNumber(2).build());
        saveSlot(day2, 1, "07:00", ItinerarySlot.TimeCategory.BREAKFAST, 90, tsukiji);
    }

    private Place savePlace(String googleId, String name, String address,
                             Place.Category category, String lat, String lng) {
        return placeRepository.save(Place.builder()
            .googlePlaceId(googleId)
            .name(name)
            .address(address)
            .category(category)
            .latitude(new BigDecimal(lat))
            .longitude(new BigDecimal(lng))
            .build());
    }

    private void saveSlot(ItineraryDay day, int orderIndex, String time,
                          ItinerarySlot.TimeCategory category, int stayMinutes, Place place) {
        itinerarySlotRepository.save(ItinerarySlot.builder()
            .itineraryDay(day)
            .orderIndex(orderIndex)
            .slotTime(LocalTime.parse(time))
            .timeCategory(category)
            .stayDurationMinutes(stayMinutes)
            .place(place)
            .build());
    }
}
