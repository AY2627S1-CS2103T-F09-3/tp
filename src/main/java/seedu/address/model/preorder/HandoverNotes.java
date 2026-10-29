package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a note on how to hand over a customer's preorder, such as "Call on arrival".
 * Guarantees: immutable; new values are valid as declared in {@link #isValidHandoverNotes(String)}; legacy values
 * loaded from storage may contain forward slashes.
 */
public class HandoverNotes {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_CONSTRAINTS = "Invalid handover notes! Handover notes must not be blank, "
            + "must not contain forward slashes, must be on a single line, and must be at most "
            + MAX_LENGTH + " characters long.";

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

    private HandoverNotes(String handoverNotes, boolean isLegacyData) {
        checkArgument(isLegacyData, "Legacy handover notes must be created through the legacy-data factory.");
        value = handoverNotes;
    }

    /**
     * Creates handover notes loaded from legacy data, where forward slashes were allowed.
     * New user input must use {@link #HandoverNotes(String)} so the current validation rule is enforced.
     *
     * @param handoverNotes Legacy notes that satisfy all constraints except the slash restriction.
     */
    public static HandoverNotes fromLegacyData(String handoverNotes) {
        requireNonNull(handoverNotes);
        checkArgument(isValidHandoverNotesExceptSlash(handoverNotes), MESSAGE_CONSTRAINTS);
        checkArgument(handoverNotes.contains("/"), "Legacy handover notes must contain a forward slash.");
        return new HandoverNotes(handoverNotes, true);
    }

    /**
     * Returns true if a given string is valid handover notes.
     */
    public static boolean isValidHandoverNotes(String test) {
        return isValidHandoverNotesExceptSlash(test) && !test.contains("/");
    }

    private static boolean isValidHandoverNotesExceptSlash(String test) {
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
