package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/** Represents a customer's six-digit postal code. */
public class PostalCode {
    public static final String MESSAGE_CONSTRAINTS = "Postal codes must contain exactly six digits.";
    public static final String VALIDATION_REGEX = "\\d{6}";

    public final String value;

    /**
     * Constructs a {@code PostalCode}.
     *
     * @param postalCode a six-digit postal code
     */
    public PostalCode(String postalCode) {
        requireNonNull(postalCode);
        checkArgument(isValidPostalCode(postalCode), MESSAGE_CONSTRAINTS);
        value = postalCode;
    }

    /**
     * Returns true if the given string is a valid six-digit postal code.
     *
     * @param test The non-null string to validate.
     * @return {@code true} if {@code test} contains exactly six digits; {@code false} otherwise.
     */
    public static boolean isValidPostalCode(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PostalCode otherPostalCode)) {
            return false;
        }
        return value.equals(otherPostalCode.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
