package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

/**
 * Represents how far a customer's pre-order has progressed.
 * A fixed set of values prevents variations such as "done" and "complete" from becoming different statuses.
 */
public enum PreorderStatus {
    /** The order has been recorded but is not ready yet. */
    PENDING,
    /** The order is ready for collection or delivery. */
    PREPARED,
    /** The order has been collected or delivered. */
    COMPLETED;

    public static final String MESSAGE_CONSTRAINTS =
            "Invalid status! Status must be PENDING, PREPARED, or COMPLETED.";

    /**
     * Returns true if a given string is exactly the name of a status, such as "PENDING".
     */
    public static boolean isValidStatus(String test) {
        requireNonNull(test);
        return Arrays.stream(values()).anyMatch(status -> status.name().equals(test));
    }
}
