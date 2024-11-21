package com.api.registration.registration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.regex.Pattern;

@Service
public class PasswordValidator implements Predicate<String> {

    private final Pattern passwordPattern;

    public PasswordValidator(@Value("${validation.password-regex}") String regexValidationPassword) {
        this.passwordPattern = Pattern.compile(regexValidationPassword);
    }

    @Override
    public boolean test(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        return passwordPattern.matcher(password).matches();
    }
}
