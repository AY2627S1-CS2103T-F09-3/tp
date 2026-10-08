package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents what a customer has pre-ordered, as free text such as "2 chocolate cakes, no nuts".
 * Guarantees: immutable; is valid as declared in {@link #isValidDetails(String)}
 */
public class PreorderDetails {

    public static final int MAX_LENGTH = 200;

    public static final String MESSAGE_CONSTRAINTS = "Invalid details! Order details must not be blank, "
            + "must be on a single line, and must be at most " + MAX_LENGTH + " characters long.";

    /*
     * The first and last characters must not be whitespace, so blank details and details with
     * untrimmed spaces are rejected. Line breaks are not allowed anywhere.
     */
    public static final String VALIDATION_REGEX = "[^\\s]([^\\r\\n]*[^\\s])?";

    public final String value;

    /**
     * Constructs a {@code PreorderDetails}.
     *
     * @param details Valid order details.
     */
    public PreorderDetails(String details) {
        requireNonNull(details);
        checkArgument(isValidDetails(details), MESSAGE_CONSTRAINTS);
        value = details;
    }

    /**
     * Returns true if a given string is valid order details.
     */
    public static boolean isValidDetails(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
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

        // instanceof handles nulls
        if (!(other instanceof PreorderDetails otherDetails)) {
            return false;
        }

        return value.equals(otherDetails.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
