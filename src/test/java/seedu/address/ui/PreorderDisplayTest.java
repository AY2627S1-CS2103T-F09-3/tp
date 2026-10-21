package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.Remark;
import seedu.address.testutil.ItemBuilder;

public class PreorderDisplayTest {

    private static final Preorder TARTS_PREORDER = new Preorder(List.of(new ItemBuilder().build()));

    @Test
    public void formatHeader_anyPreorder_notScheduled() {
        assertEquals(PreorderDisplay.NOT_SCHEDULED_TEXT, PreorderDisplay.formatHeader(TARTS_PREORDER));
        assertEquals(PreorderDisplay.NOT_SCHEDULED_TEXT, PreorderDisplay.formatHeader(new Preorder(List.of())));
    }

    @Test
    public void getHeaderIcon_anyPreorder_returnsCalendar() {
        assertEquals(PreorderDisplay.CALENDAR_ICON, PreorderDisplay.getHeaderIcon(TARTS_PREORDER));
    }

    @Test
    public void formatItemName_validItem_numbersName() {
        Item cookies = new ItemBuilder().withName("Almond Cookies").build();
        assertEquals("2. Almond Cookies", PreorderDisplay.formatItemName(2, cookies));
    }

    @Test
    public void formatItemName_longestName_keepsFullText() {
        String longest = "a".repeat(ItemName.MAX_LENGTH);
        Item item = new ItemBuilder().withName(longest).build();
        assertEquals("1. " + longest, PreorderDisplay.formatItemName(1, item));
    }

    @Test
    public void formatItemName_itemNumberBelowOne_throwsIllegalArgumentException() {
        Item item = new ItemBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> PreorderDisplay.formatItemName(0, item));
    }

    @Test
    public void formatQuantity_itemWithQuantity_showsQuantity() {
        assertEquals("Qty 12", PreorderDisplay.formatQuantity(new ItemBuilder().withQuantity("12").build()));
    }

    @Test
    public void formatQuantity_itemWithoutQuantity_saysQuantityNeeded() {
        Item cakes = new ItemBuilder().withName("2 chocolate cakes").withoutQuantity().build();
        assertEquals(PreorderDisplay.MISSING_QUANTITY_TEXT, PreorderDisplay.formatQuantity(cakes));
    }

    @Test
    public void formatRemark_validRemark_addsPrefix() {
        assertEquals("Remark: Less sugar", PreorderDisplay.formatRemark(new Remark("Less sugar")));
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
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatHeader(null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.getHeaderIcon(null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatItemName(1, null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatQuantity(null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatRemark(null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.getStatusStyleClass(null));
    }
}
