package com.api.registration.registration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.regex.Pattern;

@Service
public class EmailValidator implements Predicate<String> {

    private final Pattern emailPattern;

    public EmailValidator(@Value("${validation.email-regex}") String regexValidationEmail) {
        this.emailPattern = Pattern.compile(regexValidationEmail);
    }

    @Override
    public boolean test(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return emailPattern.matcher(email).matches();
    }
}
