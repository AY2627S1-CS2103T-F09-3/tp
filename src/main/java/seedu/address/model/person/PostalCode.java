package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/** Represents a customer's six-digit postal code. */
public class PostalCode {
    public static final String MESSAGE_CONSTRAINTS = "Postal codes must contain exactly six digits.";
    public static final String VALIDATION_REGEX = "\\d{6}";

    public final String value;

    public PostalCode(String postalCode) {
        requireNonNull(postalCode);
        checkArgument(isValidPostalCode(postalCode), MESSAGE_CONSTRAINTS);
        value = postalCode;
    }

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
