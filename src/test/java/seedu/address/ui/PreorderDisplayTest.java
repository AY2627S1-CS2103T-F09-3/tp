package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.testutil.ItemBuilder;

public class PreorderDisplayTest {

    @Test
    public void formatItem_itemWithRemark_showsRemarkLast() {
        Item tarts = new ItemBuilder().withName("Pineapple Tarts").withQuantity("2")
                .withStatus(ItemStatus.PENDING).withRemark("Less sugar").build();
        assertEquals("1. Pineapple Tarts - Quantity 2 - PENDING - \"Less sugar\"",
                PreorderDisplay.formatItem(1, tarts));
    }

    @Test
    public void formatItem_itemWithoutRemark_endsWithStatus() {
        Item cookies = new ItemBuilder().withName("Almond Cookies").withQuantity("1")
                .withStatus(ItemStatus.PREPARED).build();
        assertEquals("2. Almond Cookies - Quantity 1 - PREPARED", PreorderDisplay.formatItem(2, cookies));
    }

    @Test
    public void formatItem_itemWithoutQuantity_saysQuantityNeeded() {
        Item cakes = new ItemBuilder().withName("2 chocolate cakes").withoutQuantity()
                .withStatus(ItemStatus.PENDING).build();
        assertEquals("1. 2 chocolate cakes - " + PreorderDisplay.MISSING_QUANTITY_TEXT + " - PENDING",
                PreorderDisplay.formatItem(1, cakes));
    }

    @Test
    public void formatItem_longestName_keepsFullText() {
        String longest = "a".repeat(ItemName.MAX_LENGTH);
        Item item = new ItemBuilder().withName(longest).withQuantity("3").build();
        assertEquals("1. " + longest + " - Quantity 3 - PENDING", PreorderDisplay.formatItem(1, item));
    }

    @Test
    public void formatItem_itemNumberBelowOne_throwsIllegalArgumentException() {
        Item item = new ItemBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> PreorderDisplay.formatItem(0, item));
    }

    @Test
    public void getStatusStyleClass_eachStatus_returnsMatchingClass() {
        assertEquals("status-pending", PreorderDisplay.getStatusStyleClass(ItemStatus.PENDING));
        assertEquals("status-prepared", PreorderDisplay.getStatusStyleClass(ItemStatus.PREPARED));
        assertEquals("status-completed", PreorderDisplay.getStatusStyleClass(ItemStatus.COMPLETED));
    }

    @Test
    public void getStatusStyleClass_differentStatuses_differentClasses() {
        assertNotEquals(PreorderDisplay.getStatusStyleClass(ItemStatus.PENDING),
                PreorderDisplay.getStatusStyleClass(ItemStatus.PREPARED));
        assertNotEquals(PreorderDisplay.getStatusStyleClass(ItemStatus.PREPARED),
                PreorderDisplay.getStatusStyleClass(ItemStatus.COMPLETED));
    }

    @Test
    public void nullInputs_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatItem(1, null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.getStatusStyleClass(null));
    }
}
