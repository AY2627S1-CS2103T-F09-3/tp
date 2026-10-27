package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.Remark;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PreorderBuilder;

public class PreorderDisplayTest {

    private static final Preorder TARTS_PREORDER = new Preorder(List.of(new ItemBuilder().build()));

    @Test
    public void formatHeader_dateAndMethodSet_showsBoth() {
        Preorder delivery = new PreorderBuilder().withDate("2026-12-20").withMethod(FulfilmentMethod.DELIVERY).build();
        Preorder collection = new PreorderBuilder().withDate("2026-01-05").withMethod(FulfilmentMethod.COLLECTION)
                .build();

        assertEquals("20 Dec 2026 | Delivery", PreorderDisplay.formatHeader(delivery));
        assertEquals("5 Jan 2026 | Collection", PreorderDisplay.formatHeader(collection));
    }

    @Test
    public void formatHeader_onlyDateOrMethodSet_showsPlaceholderForOther() {
        Preorder onlyDate = new PreorderBuilder().withDate("2026-12-20").build();
        Preorder onlyMethod = new PreorderBuilder().withMethod(FulfilmentMethod.COLLECTION).build();

        assertEquals("20 Dec 2026 | Method not set", PreorderDisplay.formatHeader(onlyDate));
        assertEquals("Not scheduled | Collection", PreorderDisplay.formatHeader(onlyMethod));
    }

    @Test
    public void formatHeader_neitherSet_notScheduled() {
        assertEquals("Not scheduled | Method not set", PreorderDisplay.formatHeader(TARTS_PREORDER));
        assertEquals("Not scheduled | Method not set", PreorderDisplay.formatHeader(new Preorder(List.of())));
    }

    @Test
    public void getHeaderIcon_eachMethod_returnsMatchingIcon() {
        Preorder delivery = new PreorderBuilder().withMethod(FulfilmentMethod.DELIVERY).build();
        Preorder collection = new PreorderBuilder().withMethod(FulfilmentMethod.COLLECTION).build();

        assertEquals(PreorderDisplay.DELIVERY_ICON, PreorderDisplay.getHeaderIcon(delivery));
        assertEquals(PreorderDisplay.COLLECTION_ICON, PreorderDisplay.getHeaderIcon(collection));
    }

    @Test
    public void getHeaderIcon_noMethod_returnsCalendar() {
        Preorder onlyDate = new PreorderBuilder().withDate("2026-12-20").build();

        assertEquals(PreorderDisplay.CALENDAR_ICON, PreorderDisplay.getHeaderIcon(onlyDate));
        assertEquals(PreorderDisplay.CALENDAR_ICON, PreorderDisplay.getHeaderIcon(TARTS_PREORDER));
    }

    @Test
    public void formatHandoverNotes_validNotes_addsPrefix() {
        assertEquals("Handover notes: Call on arrival",
                PreorderDisplay.formatHandoverNotes(new HandoverNotes("Call on arrival")));
    }

    @Test
    public void formatItemNumber_positiveNumber_showsNumberOnly() {
        assertEquals("1", PreorderDisplay.formatItemNumber(1));
        assertEquals("12", PreorderDisplay.formatItemNumber(12));
    }

    @Test
    public void formatItemNumber_numberBelowOne_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PreorderDisplay.formatItemNumber(0));
    }

    @Test
    public void formatQuantity_itemWithQuantity_showsQuantity() {
        assertEquals("Qty 12", PreorderDisplay.formatQuantity(new ItemBuilder().withQuantity("12").build()));
    }

    @Test
    public void formatQuantity_itemWithoutQuantity_saysQuantityNeeded() {
        Item cakes = new ItemBuilder().withName("2 chocolate cakes").withoutQuantity().build();
        assertEquals("Qty needed", PreorderDisplay.formatQuantity(cakes));
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
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatHandoverNotes(null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatQuantity(null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatRemark(null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.getStatusStyleClass(null));
    }
}
