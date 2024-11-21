package com.api.registration.dto;

import java.util.List;

public record RegistrationRequest(
        String name,
        String email,
        String password,
        List<PhoneRequest> phones) {

}
