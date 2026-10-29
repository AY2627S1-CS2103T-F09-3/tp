package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // blank names
        assertFalse(Tag.isValidTagName(""));
        assertFalse(Tag.isValidTagName(" "));

        // spaces and special characters
        assertFalse(Tag.isValidTagName("close friends"));
        assertFalse(Tag.isValidTagName(" VIP "));
        assertFalse(Tag.isValidTagName("VIP!"));
        assertFalse(Tag.isValidTagName("regular_customer"));

        // non-ASCII letters
        assertFalse(Tag.isValidTagName("caf\u00e9"));

        // letters, digits, and combinations
        assertTrue(Tag.isValidTagName("A"));
        assertTrue(Tag.isValidTagName("1"));
        assertTrue(Tag.isValidTagName("VIP"));
        assertTrue(Tag.isValidTagName("friends"));
        assertTrue(Tag.isValidTagName("123"));
        assertTrue(Tag.isValidTagName("regular123"));
    }

    @Test
    public void isValidTagName_lengthBoundaries_returnsExpectedResult() {
        assertTrue(Tag.isValidTagName("A".repeat(29)));
        assertTrue(Tag.isValidTagName("A".repeat(30)));
        assertFalse(Tag.isValidTagName("A".repeat(31)));
    }

    @Test
    public void constructor_lengthBoundaries_enforcesLimit() {
        for (int length : new int[]{29, 30}) {
            String tagName = "A".repeat(length);
            assertEquals(tagName, new Tag(tagName).tagName);
        }
        assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () -> new Tag("A".repeat(31)));
    }

    @Test
    public void equals() {
        Tag tag = new Tag("vip");

        // same object -> returns true
        assertTrue(tag.equals(tag));

        // same values -> returns true
        assertTrue(tag.equals(new Tag("vip")));

        // different case -> returns true
        assertTrue(tag.equals(new Tag("VIP")));

        // different types -> returns false
        assertFalse(tag.equals(1));

        // null -> returns false
        assertFalse(tag.equals(null));

        // different value -> returns false
        assertFalse(tag.equals(new Tag("regular")));
    }

    @Test
    public void hashCode_differentCase_sameHashCode() {
        assertEquals(new Tag("vip").hashCode(), new Tag("VIP").hashCode());
    }
}
