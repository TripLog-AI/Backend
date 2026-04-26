package com.triple.travel.common.security;

/**
 * SecurityContext의 Authentication.principal로 들어가는 값.
 * Controller에서 @AuthenticationPrincipal AuthPrincipal user 로 받아 사용.
 */
public record AuthPrincipal(Long userId, String email) {
}
