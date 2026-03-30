package com.authServer.shared.tools;

import com.authServer.constant.ApplicationConstants;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TextFieldValidator {

    /**
     * Validates a text field with configurable validation rules.
     * This method performs whitespace trimming and validates the field according to specified rules.
     *
     * @param string The input string to validate
     * @param checkAlphanumeric If true, validates that the string contains only alphanumeric characters
     * @param checkSqlInjection If true, validates against potential SQL injection patterns
     * @return ValidationResult containing validation status, error message (if any), and trimmed value
     */
    public ValidationResult validateTextField(String string, boolean checkAlphanumeric, boolean checkSqlInjection) {
        
        // Validate and trim whitespace
        ValidationResult whitespaceResult = validateAndTrimWhitespace(string);
        if (!whitespaceResult.isValid()) {
            return whitespaceResult;
        }
        
        String trimmedString = whitespaceResult.getTrimmedValue();
        
        // Validate minimum length on trimmed string
        Optional<String> error = validateMinimumLength(trimmedString);
        if (error.isPresent()) {
            return ValidationResult.error(error.get());
        }

        // Validate maximum length on trimmed string
        error = validateMaximumLength(trimmedString);
        if (error.isPresent()) {
            return ValidationResult.error(error.get());
        }

        // Conditionally validate alphanumeric
        if (checkAlphanumeric) {
            error = validateAlphanumericOnly(trimmedString);
            if (error.isPresent()) {
                return ValidationResult.error(error.get());
            }
        }

        // Conditionally validate SQL injection
        if (checkSqlInjection) {
            error = validateNoSqlInjection(trimmedString);
            if (error.isPresent()) {
                return ValidationResult.error(error.get());
            }
        }

        return ValidationResult.success(trimmedString);
    }

    /**
     * Validates and trims whitespace from input string.
     * - Trims leading and trailing whitespace
     * - Preserves internal whitespace
     * - Rejects null, empty, or whitespace-only strings
     * 
     * Examples:
     * - "Joe Doe" → valid, returns "Joe Doe"
     * - "Joel    " → valid, returns "Joel"
     * - "     " → invalid
     * - "   jose   " → valid, returns "jose"
     * - "   maria" → valid, returns "maria"
     *
     * @param string The input string to validate and trim
     * @return ValidationResult with trimmed string if valid, or error message if invalid
     */
    public ValidationResult validateAndTrimWhitespace(String string) {
        // Check for null or empty
        if (string == null || string.isEmpty()) {
            return ValidationResult.error("Field cannot be null or empty");
        }

        // Trim leading and trailing whitespace
        String trimmed = string.trim();

        // Check if trimmed string is empty (was only whitespace)
        if (trimmed.isEmpty()) {
            return ValidationResult.error("Field cannot contain only whitespace");
        }

        return ValidationResult.success(trimmed);
    }

    public Optional<String> validateMaximumLength(String string) {
        if (string.length() > ApplicationConstants.MAX_STR_FIELD) {
            return Optional.of("Field exceeds maximum length of " + ApplicationConstants.MAX_STR_FIELD + " characters");
        }
        return Optional.empty();
    }

    public Optional<String> validateMinimumLength(String string) {
        if (string.length() < ApplicationConstants.MIN_STR_FIELD) {
            return Optional.of("Field must be at least " + ApplicationConstants.MIN_STR_FIELD + " characters long");
        }
        return Optional.empty();
    }

    public Optional<String> validateAlphanumericOnly(String string) {
        if (!string.matches("^[a-zA-Z0-9 ]*$")) {
            return Optional.of("Field can only contain alphanumeric characters and spaces");
        }
        return Optional.empty();
    }

    public Optional<String> validateNoSqlInjection(String string) {
        // Check for common SQL injection characters
        if (string.contains("'") || string.contains("\"") || string.contains("`") || string.contains(";") || string.contains("--") || string.contains("/*") || string.contains("*/")) {
            return Optional.of("Field contains potentially dangerous characters");
        }

        // Check for SQL keywords that might indicate injection attempts
        String upperCaseString = string.toUpperCase();
        if (upperCaseString.contains("UNION") || upperCaseString.contains("SELECT") || upperCaseString.contains("INSERT") || upperCaseString.contains("UPDATE") || upperCaseString.contains("DELETE") || upperCaseString.contains("DROP") || upperCaseString.contains("EXEC") || upperCaseString.contains("EXECUTE") || upperCaseString.contains("ALTER") || upperCaseString.contains("CREATE")) {
            return Optional.of("Field contains potentially dangerous SQL keywords");
        }

        return Optional.empty();
    }
}
