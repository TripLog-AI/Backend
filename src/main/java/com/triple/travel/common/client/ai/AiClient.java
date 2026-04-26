package com.triple.travel.common.client.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Python FastAPI AI 서비스(/ai/generate-itinerary, /ai/parse-youtube)에 대한 HTTP 클라이언트.
 * 호출 실패 시 AiServiceException을 던진다.
 */
@Slf4j
@Component
public class AiClient {

    private final RestClient restClient;

    public AiClient(@Value("${app.ai.base-url}") String baseUrl,
                    @Value("${app.ai.timeout-seconds}") long timeoutSeconds) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .requestFactory(buildRequestFactory(timeoutSeconds))
            .build();
        log.info("AiClient initialized: baseUrl={}, timeout={}s", baseUrl, timeoutSeconds);
    }

    public AiServiceDto.ItineraryPayload generateItinerary(AiServiceDto.GenerateRequest req) {
        return post("/ai/generate-itinerary", req);
    }

    public AiServiceDto.ItineraryPayload parseYoutube(AiServiceDto.YoutubeRequest req) {
        return post("/ai/parse-youtube", req);
    }

    private AiServiceDto.ItineraryPayload post(String path, Object body) {
        try {
            return restClient.post()
                .uri(path)
                .body(body)
                .retrieve()
                .body(AiServiceDto.ItineraryPayload.class);
        } catch (RestClientException ex) {
            log.warn("AI service call failed [{}]: {}", path, ex.getMessage());
            throw new AiServiceException("AI 서비스 호출 실패: " + ex.getMessage(), ex);
        }
    }

    private static ClientHttpRequestFactory buildRequestFactory(long timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return factory;
    }
}
