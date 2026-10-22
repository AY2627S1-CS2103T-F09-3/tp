package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.logic.Messages;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.Remark;

/**
 * Formats a customer's preorder for display on their card.
 * Kept free of JavaFX so that the wording and styling rules can be unit tested.
 */
public class PreorderDisplay {

    public static final String NOT_SCHEDULED_TEXT = "Not scheduled | Method not set";
    public static final String NO_ITEMS_TEXT = "No items yet";
    public static final String MISSING_QUANTITY_TEXT = Messages.MISSING_QUANTITY_TEXT;
    public static final String REMARK_PREFIX = "Remark: ";

    /** A 16 px calendar outline, shown beside a preorder that has no handover date. */
    public static final String CALENDAR_ICON =
            "M3 3.5h10a1 1 0 0 1 1 1v8a1 1 0 0 1 -1 1h-10a1 1 0 0 1 -1 -1v-8a1 1 0 0 1 1 -1z"
            + "M2 6.5h12M5.5 2v3M10.5 2v3";

    private PreorderDisplay() {} // prevents instantiation

    /**
     * Returns the first line of the preorder panel, which says how the preorder will be handed over.
     * Preorders do not record a handover date or method yet, so every preorder is not scheduled.
     */
    public static String formatHeader(Preorder preorder) {
        requireNonNull(preorder);
        return NOT_SCHEDULED_TEXT;
    }

    /**
     * Returns the SVG path of the icon shown before {@link #formatHeader(Preorder)}.
     */
    public static String getHeaderIcon(Preorder preorder) {
        requireNonNull(preorder);
        return CALENDAR_ICON;
    }

    /**
     * Returns an item's number and name, e.g. {@code 1. Pineapple Tarts}.
     *
     * @param itemNumber The item's position in the preorder, starting from 1.
     * @throws IllegalArgumentException if {@code itemNumber} is less than 1.
     */
    public static String formatItemName(int itemNumber, Item item) {
        requireNonNull(item);
        checkArgument(itemNumber > 0);
        return itemNumber + ". " + item.getName();
    }

    /**
     * Returns an item's quantity, e.g. {@code Qty 2}, or a reminder for items converted from v1.3 without one.
     */
    public static String formatQuantity(Item item) {
        requireNonNull(item);
        return item.getQuantity()
                .map(quantity -> "Qty " + quantity)
                .orElse(MISSING_QUANTITY_TEXT);
    }

    /**
     * Returns the line shown under an item with a special request, e.g. {@code Remark: Less sugar}.
     */
    public static String formatRemark(Remark remark) {
        requireNonNull(remark);
        return REMARK_PREFIX + remark;
    }

    /**
     * Returns the CSS style class that gives each status its own color.
     */
    public static String getStatusStyleClass(ItemStatus status) {
        requireNonNull(status);
        return switch (status) {
            case PENDING -> "status-pending";
            case PREPARED -> "status-prepared";
            case COMPLETED -> "status-completed";
        };
    }
}
