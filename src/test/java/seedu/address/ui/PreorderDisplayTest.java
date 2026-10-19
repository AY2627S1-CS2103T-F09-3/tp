package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.PreorderStatus;

public class PreorderDisplayTest {

    private static final Preorder CAKES = new Preorder(new ItemName("2 chocolate cakes"),
            PreorderStatus.PENDING);

    @Test
    public void formatDetails_validPreorder_prefixesDetails() {
        assertEquals("Order: 2 chocolate cakes", PreorderDisplay.formatDetails(CAKES));
    }

    @Test
    public void formatDetails_longestDetails_keepsFullText() {
        String longest = "a".repeat(ItemName.MAX_LENGTH);
        Preorder preorder = new Preorder(new ItemName(longest), PreorderStatus.PENDING);

        assertEquals(PreorderDisplay.DETAILS_PREFIX + longest, PreorderDisplay.formatDetails(preorder));
    }

    @Test
    public void formatStatus_eachStatus_showsStatusName() {
        for (PreorderStatus status : PreorderStatus.values()) {
            Preorder preorder = new Preorder(new ItemName("2 chocolate cakes"), status);
            assertEquals("Status: " + status.name(), PreorderDisplay.formatStatus(preorder));
        }
    }

    @Test
    public void getStatusStyleClass_eachStatus_returnsMatchingClass() {
        assertEquals("status-pending", PreorderDisplay.getStatusStyleClass(PreorderStatus.PENDING));
        assertEquals("status-prepared", PreorderDisplay.getStatusStyleClass(PreorderStatus.PREPARED));
        assertEquals("status-completed", PreorderDisplay.getStatusStyleClass(PreorderStatus.COMPLETED));
    }

    @Test
    public void getStatusStyleClass_differentStatuses_differentClasses() {
        assertNotEquals(PreorderDisplay.getStatusStyleClass(PreorderStatus.PENDING),
                PreorderDisplay.getStatusStyleClass(PreorderStatus.PREPARED));
        assertNotEquals(PreorderDisplay.getStatusStyleClass(PreorderStatus.PREPARED),
                PreorderDisplay.getStatusStyleClass(PreorderStatus.COMPLETED));
    }

    @Test
    public void nullInputs_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatDetails(null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.formatStatus(null));
        assertThrows(NullPointerException.class, () -> PreorderDisplay.getStatusStyleClass(null));
    }
}
