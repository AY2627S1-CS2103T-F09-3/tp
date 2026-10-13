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
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

    @Test
    public void equals() {
        Tag tag = new Tag("vip");

        // same object -> returns true
        assertTrue(tag.equals(tag));

        // same values -> returns true
        assertTrue(tag.equals(new Tag("vip")));

        // difference case -> returns true
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
