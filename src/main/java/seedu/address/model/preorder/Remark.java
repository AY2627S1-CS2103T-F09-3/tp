package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a customer's special request for an item, such as "Less sugar".
 * Guarantees: immutable; new values are valid as declared in {@link #isValidRemark(String)}; legacy values loaded
 * from storage may contain forward slashes.
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

    private Remark(String remark, boolean isLegacyData) {
        checkArgument(isLegacyData, "Legacy remarks must be created through the legacy-data factory.");
        value = remark;
    }

    /**
     * Creates a remark loaded from legacy data, where forward slashes were allowed.
     * New user input must use {@link #Remark(String)} so the current validation rule is enforced.
     *
     * @param remark A legacy remark that satisfies all constraints except the slash restriction.
     */
    public static Remark fromLegacyData(String remark) {
        requireNonNull(remark);
        checkArgument(isValidRemarkExceptSlash(remark), MESSAGE_CONSTRAINTS);
        checkArgument(remark.contains("/"), "Legacy remarks must contain a forward slash.");
        return new Remark(remark, true);
    }

    /**
     * Returns true if a given string is a valid remark.
     */
    public static boolean isValidRemark(String test) {
        return isValidRemarkExceptSlash(test) && !test.contains("/");
    }

    private static boolean isValidRemarkExceptSlash(String test) {
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
