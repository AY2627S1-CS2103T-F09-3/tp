package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Represents the date a customer's preorder is to be delivered or collected, such as 2026-12-20.
 * Guarantees: immutable; is valid as declared in {@link #isValidDate(String)}
 */
public class FulfilmentDate {

    public static final String MESSAGE_CONSTRAINTS = "Invalid date! Dates must be real calendar dates "
            + "in YYYY-MM-DD format, e.g. 2026-12-20.";

    // Exactly four digits for the year and two each for the month and day, so "2026-1-5" is rejected
    public static final String VALIDATION_REGEX = "\\d{4}-\\d{2}-\\d{2}";

    public final LocalDate value;

    /**
     * Constructs a {@code FulfilmentDate}.
     *
     * @param date A valid date in YYYY-MM-DD format.
     */
    public FulfilmentDate(String date) {
        requireNonNull(date);
        checkArgument(isValidDate(date), MESSAGE_CONSTRAINTS);
        value = LocalDate.parse(date);
    }

    /**
     * Returns true if a given string is a real calendar date in YYYY-MM-DD format.
     */
    public static boolean isValidDate(String test) {
        if (!test.matches(VALIDATION_REGEX)) {
            return false;
        }
        try {
            LocalDate.parse(test); // rejects dates that do not exist, such as 2026-02-30
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FulfilmentDate otherDate)) {
            return false;
        }

        return value.equals(otherDate.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
