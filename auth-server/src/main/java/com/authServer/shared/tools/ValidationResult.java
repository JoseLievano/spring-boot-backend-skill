package com.authServer.shared.tools;

import lombok.Getter;
import lombok.Setter;

/**
 * Result of text field validation containing validation status and trimmed value
 */
@Getter
@Setter
public class ValidationResult {
    private final boolean valid;
    private final String errorMessage;
    private final String trimmedValue;

    private ValidationResult(boolean valid, String errorMessage, String trimmedValue) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.trimmedValue = trimmedValue;
    }

    public static ValidationResult success(String trimmedValue) {
        return new ValidationResult(true, null, trimmedValue);
    }

    public static ValidationResult error(String errorMessage) {
        return new ValidationResult(false, errorMessage, null);
    }

    public boolean isValid() {
        return valid;
    }

}