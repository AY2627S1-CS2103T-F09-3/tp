package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;

/**
 * Formats a customer's preorder for display on their card.
 * Kept free of JavaFX so that the wording and styling rules can be unit tested.
 */
public class PreorderDisplay {

    public static final String NO_ITEMS_TEXT = "Preorder: no items yet";
    public static final String MISSING_QUANTITY_TEXT = "Quantity needed";
    public static final String NO_ITEMS_STYLE_CLASS = "preorder-empty";

    private PreorderDisplay() {} // prevents instantiation

    /**
     * Returns the text shown on an item's chip,
     * e.g. {@code 1. Pineapple Tarts - Quantity 2 - PENDING - "Less sugar"}.
     *
     * @param itemNumber The item's position in the preorder, starting from 1.
     * @throws IllegalArgumentException if {@code itemNumber} is less than 1.
     */
    public static String formatItem(int itemNumber, Item item) {
        requireNonNull(item);
        checkArgument(itemNumber > 0);

        String quantity = item.getQuantity()
                .map(itemQuantity -> "Quantity " + itemQuantity)
                .orElse(MISSING_QUANTITY_TEXT);
        String text = itemNumber + ". " + item.getName() + " - " + quantity + " - " + item.getStatus().name();
        return item.getRemark()
                .map(remark -> text + " - \"" + remark + "\"")
                .orElse(text);
    }

    /**
     * Returns the CSS style class that gives each status its own colour.
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
