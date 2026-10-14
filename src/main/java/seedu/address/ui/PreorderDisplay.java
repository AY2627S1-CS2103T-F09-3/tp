package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.PreorderStatus;

/**
 * Formats a {@code Preorder} for display on a customer card.
 * Kept free of JavaFX so that the wording and styling rules can be unit tested.
 */
public class PreorderDisplay {

    public static final String DETAILS_PREFIX = "Order: ";
    public static final String STATUS_PREFIX = "Status: ";

    private PreorderDisplay() {} // prevents instantiation

    /**
     * Returns the text shown on the order chip, e.g. {@code "Order: 2 chocolate cakes"}.
     */
    public static String formatDetails(Preorder preorder) {
        requireNonNull(preorder);
        return DETAILS_PREFIX + preorder.getDetails().value;
    }

    /**
     * Returns the text shown on the status chip, e.g. {@code "Status: PENDING"}.
     */
    public static String formatStatus(Preorder preorder) {
        requireNonNull(preorder);
        return STATUS_PREFIX + preorder.getStatus().name();
    }

    /**
     * Returns the CSS style class that gives each status its own colour.
     */
    public static String getStatusStyleClass(PreorderStatus status) {
        requireNonNull(status);
        return switch (status) {
            case PENDING -> "status-pending";
            case PREPARED -> "status-prepared";
            case COMPLETED -> "status-completed";
        };
    }
}
