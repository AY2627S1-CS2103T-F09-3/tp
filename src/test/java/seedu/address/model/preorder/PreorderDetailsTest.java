package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PreorderDetailsTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PreorderDetails(null));
    }

    @Test
    public void constructor_invalidDetails_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PreorderDetails(""));
    }

    @Test
    public void isValidDetails() {
        // null details
        assertThrows(NullPointerException.class, () -> PreorderDetails.isValidDetails(null));

        // blank details
        assertFalse(PreorderDetails.isValidDetails("")); // empty string
        assertFalse(PreorderDetails.isValidDetails("   ")); // spaces only

        // untrimmed details
        assertFalse(PreorderDetails.isValidDetails(" 2 chocolate cakes")); // leading space
        assertFalse(PreorderDetails.isValidDetails("2 chocolate cakes ")); // trailing space

        // more than one line
        assertFalse(PreorderDetails.isValidDetails("2 chocolate cakes\nno nuts"));
        assertFalse(PreorderDetails.isValidDetails("2 chocolate cakes\r\nno nuts"));

        // length boundary
        assertTrue(PreorderDetails.isValidDetails("a".repeat(PreorderDetails.MAX_LENGTH)));
        assertFalse(PreorderDetails.isValidDetails("a".repeat(PreorderDetails.MAX_LENGTH + 1)));

        // valid details
        assertTrue(PreorderDetails.isValidDetails("x")); // one character
        assertTrue(PreorderDetails.isValidDetails("2 chocolate cakes, no nuts")); // punctuation
        assertTrue(PreorderDetails.isValidDetails("12 cupcakes (6 red velvet & 6 vanilla) by 3pm!")); // symbols
        assertTrue(PreorderDetails.isValidDetails("1 cake  2 tarts")); // repeated inner spaces
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("2 chocolate cakes", new PreorderDetails("2 chocolate cakes").toString());
    }

    @Test
    public void equals() {
        PreorderDetails details = new PreorderDetails("2 chocolate cakes");

        // same values -> returns true
        assertTrue(details.equals(new PreorderDetails("2 chocolate cakes")));

        // same object -> returns true
        assertTrue(details.equals(details));

        // null -> returns false
        assertFalse(details.equals(null));

        // different types -> returns false
        assertFalse(details.equals(5.0f));

        // different values -> returns false
        assertFalse(details.equals(new PreorderDetails("1 strawberry shortcake")));
    }

    @Test
    public void hashCode_sameValue_sameHashCode() {
        assertEquals(new PreorderDetails("2 chocolate cakes").hashCode(),
                new PreorderDetails("2 chocolate cakes").hashCode());
    }
}
