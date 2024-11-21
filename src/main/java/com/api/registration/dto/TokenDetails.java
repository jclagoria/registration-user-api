package com.api.registration.dto;

import java.time.Instant;

public record TokenDetails(String token, Instant issuedAt, Instant expiration) {
}
