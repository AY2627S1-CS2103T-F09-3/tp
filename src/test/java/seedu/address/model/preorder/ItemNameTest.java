package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ItemNameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ItemName(null));
    }

    @Test
    public void constructor_invalidItemName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ItemName(""));
    }

    @Test
    public void isValidItemName() {
        // null name
        assertThrows(NullPointerException.class, () -> ItemName.isValidItemName(null));

        // blank name
        assertFalse(ItemName.isValidItemName("")); // empty string
        assertFalse(ItemName.isValidItemName("   ")); // spaces only
        assertFalse(ItemName.isValidItemName("\u2003")); // em space only
        assertFalse(ItemName.isValidItemName("\u00A0\u3000")); // no-break and ideographic spaces only
        assertFalse(ItemName.isValidItemName("\u200B")); // zero-width space only, which shows nothing

        // more than one line
        assertFalse(ItemName.isValidItemName("Pineapple Tarts\nAlmond Cookies"));
        assertFalse(ItemName.isValidItemName("Pineapple Tarts\r\nAlmond Cookies"));
        assertFalse(ItemName.isValidItemName("Pineapple Tarts\u2028Almond Cookies")); // Unicode line separator

        // length boundary, counted after repeated spaces become one
        assertTrue(ItemName.isValidItemName("a".repeat(ItemName.MAX_LENGTH)));
        assertFalse(ItemName.isValidItemName("a".repeat(ItemName.MAX_LENGTH + 1)));
        assertTrue(ItemName.isValidItemName("a".repeat(ItemName.MAX_LENGTH - 2) + "   b"));

        // surrounding spaces, which are removed
        assertTrue(ItemName.isValidItemName(" Pineapple Tarts")); // leading space
        assertTrue(ItemName.isValidItemName("Pineapple Tarts ")); // trailing space
        assertTrue(ItemName.isValidItemName("Pineapple Tarts\u2003")); // trailing em space

        // valid name
        assertTrue(ItemName.isValidItemName("x")); // one character
        assertTrue(ItemName.isValidItemName("2 chocolate cakes, no nuts")); // punctuation
        assertTrue(ItemName.isValidItemName("12 cupcakes (6 red velvet & 6 vanilla) by 3pm!")); // symbols
        assertTrue(ItemName.isValidItemName("Pineapple  Tarts")); // repeated inner spaces
    }

    @Test
    public void constructor_repeatedOrSurroundingSpaces_normalized() {
        assertEquals("Butter Cake", new ItemName("Butter   Cake").value);
        assertEquals("Butter Cake", new ItemName(" Butter\t\u2003Cake\u00A0").value); // tab and Unicode spaces
        assertEquals(new ItemName("Butter Cake"), new ItemName("Butter   Cake"));
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("Pineapple Tarts", new ItemName("Pineapple Tarts").toString());
    }

    @Test
    public void equals() {
        ItemName name = new ItemName("Pineapple Tarts");

        // same values -> returns true
        assertTrue(name.equals(new ItemName("Pineapple Tarts")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new ItemName("Almond Cookies")));

        // different capitalisation -> returns false
        assertFalse(name.equals(new ItemName("pineapple tarts")));
    }

    @Test
    public void hashCode_sameValue_sameHashCode() {
        assertEquals(new ItemName("Pineapple Tarts").hashCode(), new ItemName("Pineapple Tarts").hashCode());
    }
}
