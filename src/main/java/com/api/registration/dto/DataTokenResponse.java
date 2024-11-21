package com.api.registration.dto;

public record DataTokenResponse(String token, String nombreUsuario, String createAt ,String expirationDate) {
}

