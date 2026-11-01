package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a customer's special request for an item, such as "Less sugar".
 * Guarantees: immutable; values are valid as declared in {@link #isValidRemark(String)}.
 */
public class Remark {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_CONSTRAINTS = "Invalid remark! Remarks must not be blank, "
            + "must not contain forward slashes, must be on a single line, and must be at most "
            + MAX_LENGTH + " characters long.";

    // Remarks follow the same single-line rule as item names
    public static final String VALIDATION_REGEX = ItemName.VALIDATION_REGEX;

    public final String value;

    /**
     * Constructs a {@code Remark}.
     *
     * @param remark A valid remark.
     */
    public Remark(String remark) {
        requireNonNull(remark);
        checkArgument(isValidRemark(remark), MESSAGE_CONSTRAINTS);
        value = remark;
    }

    /**
     * Returns true if a given string is a valid remark.
     */
    public static boolean isValidRemark(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX) && !test.contains("/");
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
        if (!(other instanceof Remark otherRemark)) {
            return false;
        }

        return value.equals(otherRemark.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
