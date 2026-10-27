package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.Remark;

/**
 * Formats a customer's preorder for display on their card.
 * Kept free of JavaFX so that the wording and styling rules can be unit tested.
 */
public class PreorderDisplay {

    public static final String NO_DATE_TEXT = "Not scheduled";
    public static final String NO_METHOD_TEXT = "Method not set";
    public static final String HANDOVER_NOTES_PREFIX = "Handover notes: ";
    public static final String NO_ITEMS_TEXT = "No items yet";
    // Short, like "Qty 2", so that the item's name keeps enough width in a narrow window
    public static final String MISSING_QUANTITY_TEXT = "Qty needed";
    public static final String REMARK_PREFIX = "Remark: ";

    /** A 16 px calendar outline, shown beside a preorder that has no method yet. */
    public static final String CALENDAR_ICON =
            "M3 3.5h10a1 1 0 0 1 1 1v8a1 1 0 0 1 -1 1h-10a1 1 0 0 1 -1 -1v-8a1 1 0 0 1 1 -1z"
            + "M2 6.5h12M5.5 2v3M10.5 2v3";
    /** A 16 px delivery truck outline. */
    public static final String DELIVERY_ICON = "M1.5 4h8v6.5h-8zM9.5 6.5h3l2 2v2h-5"
            + "M3.25 12.25a1.25 1.25 0 1 0 2.5 0a1.25 1.25 0 1 0 -2.5 0z"
            + "M10.25 12.25a1.25 1.25 0 1 0 2.5 0a1.25 1.25 0 1 0 -2.5 0z";
    /** A 16 px shopping bag outline, for a preorder the customer collects. */
    public static final String COLLECTION_ICON = "M3.5 5.5h9l-0.75 8h-7.5zM6 5.5v-1.5a2 2 0 0 1 4 0v1.5";
    /** A 14 px circled exclamation mark, shown beside a detail that still needs to be filled in. */
    public static final String WARNING_ICON = "M1.75 7a5.25 5.25 0 1 0 10.5 0a5.25 5.25 0 1 0 -10.5 0z"
            + "M7 4.4v3.2M7 9.6h0.01";
    /** A 12 px tick, shown on the chip of a completed item. */
    public static final String COMPLETED_ICON = "M2.5 6.25l2.25 2.25L9.5 3.75";

    private PreorderDisplay() {} // prevents instantiation

    /**
     * Returns the first line of the preorder panel, which says when and how the preorder will be handed over,
     * e.g. {@code 20 Dec 2026 | Delivery}. A part that is not set yet is shown as not scheduled or not set.
     */
    public static String formatHeader(Preorder preorder) {
        requireNonNull(preorder);
        String date = preorder.getDate().map(FulfilmentDate::toDisplayString).orElse(NO_DATE_TEXT);
        String method = preorder.getMethod().map(PreorderDisplay::formatMethod).orElse(NO_METHOD_TEXT);
        return date + " | " + method;
    }

    /**
     * Returns the SVG path of the icon shown before {@link #formatHeader(Preorder)}, which matches the method.
     */
    public static String getHeaderIcon(Preorder preorder) {
        requireNonNull(preorder);
        return preorder.getMethod()
                .map(method -> switch (method) {
                    case DELIVERY -> DELIVERY_ICON;
                    case COLLECTION -> COLLECTION_ICON;
                })
                .orElse(CALENDAR_ICON);
    }

    /**
     * Returns the line shown under the header of a preorder with handover notes,
     * e.g. {@code Handover notes: Call on arrival}.
     */
    public static String formatHandoverNotes(HandoverNotes handoverNotes) {
        requireNonNull(handoverNotes);
        return HANDOVER_NOTES_PREFIX + handoverNotes;
    }

    /**
     * Returns the number shown in the first column of an item's row, e.g. {@code 1}.
     *
     * @param itemNumber The item's position in the preorder, starting from 1.
     * @throws IllegalArgumentException if {@code itemNumber} is less than 1.
     */
    public static String formatItemNumber(int itemNumber) {
        checkArgument(itemNumber > 0);
        return String.valueOf(itemNumber);
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

    private static String formatMethod(FulfilmentMethod method) {
        return switch (method) {
            case DELIVERY -> "Delivery";
            case COLLECTION -> "Collection";
        };
    }
}
