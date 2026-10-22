package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class FulfilmentMethodTest {

    @Test
    public void isValidMethod() {
        // null method
        assertThrows(NullPointerException.class, () -> FulfilmentMethod.isValidMethod(null));

        // invalid methods
        assertFalse(FulfilmentMethod.isValidMethod("")); // empty string
        assertFalse(FulfilmentMethod.isValidMethod("PICKUP")); // covered by COLLECTION instead
        assertFalse(FulfilmentMethod.isValidMethod("delivery")); // case must already be normalized
        assertFalse(FulfilmentMethod.isValidMethod(" DELIVERY")); // spaces must already be trimmed

        // valid methods
        assertTrue(FulfilmentMethod.isValidMethod("DELIVERY"));
        assertTrue(FulfilmentMethod.isValidMethod("COLLECTION"));
    }
}
