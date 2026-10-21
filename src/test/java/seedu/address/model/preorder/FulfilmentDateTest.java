package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class FulfilmentDateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FulfilmentDate(null));
    }

    @Test
    public void constructor_invalidDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FulfilmentDate("2026-02-30"));
    }

    @Test
    public void constructor_validDate_storesDate() {
        assertEquals(LocalDate.of(2026, 12, 20), new FulfilmentDate("2026-12-20").value);
    }

    @Test
    public void isValidDate() {
        // null date
        assertThrows(NullPointerException.class, () -> FulfilmentDate.isValidDate(null));

        // blank date
        assertFalse(FulfilmentDate.isValidDate("")); // empty string
        assertFalse(FulfilmentDate.isValidDate("   ")); // spaces only

        // wrong format
        assertFalse(FulfilmentDate.isValidDate(" 2026-12-20")); // leading space
        assertFalse(FulfilmentDate.isValidDate("+12026-12-20")); // year with sign and five digits
        assertFalse(FulfilmentDate.isValidDate("2026-1-5")); // month and day without leading zeros
        assertFalse(FulfilmentDate.isValidDate("20-12-2026")); // day first

        // dates that do not exist
        assertFalse(FulfilmentDate.isValidDate("2026-02-29")); // 2026 is not a leap year
        assertFalse(FulfilmentDate.isValidDate("2026-02-30"));
        assertFalse(FulfilmentDate.isValidDate("2026-13-01"));

        // valid dates
        assertTrue(FulfilmentDate.isValidDate("2026-12-20"));
        assertTrue(FulfilmentDate.isValidDate("2028-02-29")); // leap day
    }

    @Test
    public void toString_returnsIsoDate() {
        assertEquals("2026-01-05", new FulfilmentDate("2026-01-05").toString());
    }

    @Test
    public void equals() {
        FulfilmentDate date = new FulfilmentDate("2026-12-20");

        // same values -> returns true
        assertTrue(date.equals(new FulfilmentDate("2026-12-20")));

        // same object -> returns true
        assertTrue(date.equals(date));

        // null -> returns false
        assertFalse(date.equals(null));

        // different types -> returns false
        assertFalse(date.equals(5.0f));

        // different values -> returns false
        assertFalse(date.equals(new FulfilmentDate("2026-12-21")));
    }

    @Test
    public void hashCode_sameValue_sameHashCode() {
        assertEquals(new FulfilmentDate("2026-12-20").hashCode(), new FulfilmentDate("2026-12-20").hashCode());
    }
}
