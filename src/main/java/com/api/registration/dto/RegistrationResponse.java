package com.api.registration.dto;

public record RegistrationResponse(String id, String created, String modified, String token, boolean isActive) {
}
