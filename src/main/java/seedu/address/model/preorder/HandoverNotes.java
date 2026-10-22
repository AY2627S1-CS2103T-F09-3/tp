package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a note on how to hand over a customer's preorder, such as "Call on arrival".
 * Guarantees: immutable; is valid as declared in {@link #isValidHandoverNotes(String)}
 */
public class HandoverNotes {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_CONSTRAINTS = "Invalid handover notes! Handover notes must not be blank, "
            + "must be on a single line, and must be at most " + MAX_LENGTH + " characters long.";

    // Handover notes follow the same single-line rule as item names
    public static final String VALIDATION_REGEX = ItemName.VALIDATION_REGEX;

    public final String value;

    /**
     * Constructs a {@code HandoverNotes}.
     *
     * @param handoverNotes Valid handover notes.
     */
    public HandoverNotes(String handoverNotes) {
        requireNonNull(handoverNotes);
        checkArgument(isValidHandoverNotes(handoverNotes), MESSAGE_CONSTRAINTS);
        value = handoverNotes;
    }

    /**
     * Returns true if a given string is valid handover notes.
     */
    public static boolean isValidHandoverNotes(String test) {
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
        if (!(other instanceof HandoverNotes otherHandoverNotes)) {
            return false;
        }

        return value.equals(otherHandoverNotes.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
