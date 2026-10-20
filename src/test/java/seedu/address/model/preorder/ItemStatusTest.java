package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ItemStatusTest {

    @Test
    public void isValidStatus() {
        // null status
        assertThrows(NullPointerException.class, () -> ItemStatus.isValidStatus(null));

        // invalid statuses
        assertFalse(ItemStatus.isValidStatus("")); // empty string
        assertFalse(ItemStatus.isValidStatus("DONE")); // not a status
        assertFalse(ItemStatus.isValidStatus("COLLECTED")); // covered by COMPLETED instead
        assertFalse(ItemStatus.isValidStatus("pending")); // case must already be normalized
        assertFalse(ItemStatus.isValidStatus(" PENDING")); // spaces must already be trimmed

        // valid statuses
        assertTrue(ItemStatus.isValidStatus("PENDING"));
        assertTrue(ItemStatus.isValidStatus("PREPARED"));
        assertTrue(ItemStatus.isValidStatus("COMPLETED"));
    }
}
