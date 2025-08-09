package io.github.drag0n1zed.api.ai.validation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of a validation check for AI goal compatibility.
 * <p>
 * This is an immutable value object.
 */
public final class ValidationResult {

    private static final ValidationResult SUCCESS = new ValidationResult(true, Collections.emptyList());

    private final boolean isSuccess;
    private final List<String> reasons;

    private ValidationResult(boolean isSuccess, List<String> reasons) {
        this.isSuccess = isSuccess;
        this.reasons = Collections.unmodifiableList(Objects.requireNonNull(reasons));
    }

    /**
     * @return {@code true} if the validation was successful, {@code false} otherwise.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * @return An unmodifiable list of reasons for the validation outcome.
     *         For a successful result, this list is empty.
     */
    public List<String> getReasons() {
        return reasons;
    }

    /**
     * @return A singleton instance representing a successful validation.
     */
    public static ValidationResult success() {
        return SUCCESS;
    }

    /**
     * Creates a failure result with a single reason.
     *
     * @param reason The reason for the failure.
     * @return A new {@code ValidationResult} instance representing failure.
     */
    public static ValidationResult failure(String reason) {
        return new ValidationResult(false, List.of(reason));
    }

    /**
     * Creates a failure result with multiple reasons.
     *
     * @param reasons The list of reasons for the failure.
     * @return A new {@code ValidationResult} instance representing failure.
     */
    public static ValidationResult failure(List<String> reasons) {
        return new ValidationResult(false, reasons);
    }
}