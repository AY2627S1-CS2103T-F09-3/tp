package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class QuantityTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Quantity(null));
    }

    @Test
    public void constructor_invalidQuantity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity("0"));
    }

    @Test
    public void constructor_validQuantity_storesValue() {
        assertEquals(12, new Quantity("12").value);
    }

    @Test
    public void isValidQuantity() {
        // null quantity
        assertThrows(NullPointerException.class, () -> Quantity.isValidQuantity(null));

        // invalid quantities
        assertFalse(Quantity.isValidQuantity("")); // empty string
        assertFalse(Quantity.isValidQuantity(" ")); // spaces only
        assertFalse(Quantity.isValidQuantity("0")); // zero
        assertFalse(Quantity.isValidQuantity("-1")); // negative
        assertFalse(Quantity.isValidQuantity("1.5")); // not a whole number
        assertFalse(Quantity.isValidQuantity("01")); // leading zero
        assertFalse(Quantity.isValidQuantity("1000")); // above the limit
        assertFalse(Quantity.isValidQuantity("99999999999")); // too big for an int
        assertFalse(Quantity.isValidQuantity(" 2")); // leading space
        assertFalse(Quantity.isValidQuantity("two")); // not digits

        // valid quantities
        assertTrue(Quantity.isValidQuantity("1")); // smallest
        assertTrue(Quantity.isValidQuantity("50"));
        assertTrue(Quantity.isValidQuantity("999")); // largest
    }

    @Test
    public void toString_returnsNumber() {
        assertEquals("12", new Quantity("12").toString());
    }

    @Test
    public void equals() {
        Quantity quantity = new Quantity("2");

        // same values -> returns true
        assertTrue(quantity.equals(new Quantity("2")));

        // same object -> returns true
        assertTrue(quantity.equals(quantity));

        // null -> returns false
        assertFalse(quantity.equals(null));

        // different types -> returns false
        assertFalse(quantity.equals(2));

        // different values -> returns false
        assertFalse(quantity.equals(new Quantity("3")));
    }

    @Test
    public void hashCode_sameValue_sameHashCode() {
        assertEquals(new Quantity("2").hashCode(), new Quantity("2").hashCode());
    }
}
