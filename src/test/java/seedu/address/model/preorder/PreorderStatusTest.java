package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PreorderStatusTest {

    @Test
    public void isValidStatus() {
        // null status
        assertThrows(NullPointerException.class, () -> PreorderStatus.isValidStatus(null));

        // invalid statuses
        assertFalse(PreorderStatus.isValidStatus("")); // empty string
        assertFalse(PreorderStatus.isValidStatus("DONE")); // not a status
        assertFalse(PreorderStatus.isValidStatus("COLLECTED")); // covered by COMPLETED instead
        assertFalse(PreorderStatus.isValidStatus("pending")); // case must already be normalized
        assertFalse(PreorderStatus.isValidStatus(" PENDING")); // spaces must already be trimmed

        // valid statuses
        assertTrue(PreorderStatus.isValidStatus("PENDING"));
        assertTrue(PreorderStatus.isValidStatus("PREPARED"));
        assertTrue(PreorderStatus.isValidStatus("COMPLETED"));
    }
}
