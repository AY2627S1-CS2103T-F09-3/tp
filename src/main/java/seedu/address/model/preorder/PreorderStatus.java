package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

/**
 * Represents how far an item in a customer's preorder has progressed.
 * A fixed set of values prevents variations such as "done" and "complete" from becoming different statuses.
 */
public enum PreorderStatus {
    /** The item has been ordered but is not ready yet. */
    PENDING,
    /** The item is ready for collection or delivery. */
    PREPARED,
    /** The item has been collected or delivered. */
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
