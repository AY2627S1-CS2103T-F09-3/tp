package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_invalidRemark_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Remark(" "));
    }

    @Test
    public void isValidRemark() {
        // null remark
        assertThrows(NullPointerException.class, () -> Remark.isValidRemark(null));

        // blank remark
        assertFalse(Remark.isValidRemark("")); // empty string
        assertFalse(Remark.isValidRemark("   ")); // spaces only
        assertFalse(Remark.isValidRemark("\u2003")); // em space only
        assertFalse(Remark.isValidRemark("\u200B")); // zero-width space only, which shows nothing

        // untrimmed remark
        assertFalse(Remark.isValidRemark(" Less sugar")); // leading space
        assertFalse(Remark.isValidRemark("Less sugar ")); // trailing space
        assertFalse(Remark.isValidRemark("Less sugar\u00A0")); // trailing no-break space

        // more than one line
        assertFalse(Remark.isValidRemark("Less sugar\nNo nuts"));

        // length boundary
        assertTrue(Remark.isValidRemark("a".repeat(Remark.MAX_LENGTH)));
        assertFalse(Remark.isValidRemark("a".repeat(Remark.MAX_LENGTH + 1)));

        // valid remarks
        assertTrue(Remark.isValidRemark("x")); // one character
        assertTrue(Remark.isValidRemark("Less sugar, no nuts!")); // punctuation
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("Less sugar", new Remark("Less sugar").toString());
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Less sugar");

        // same values -> returns true
        assertTrue(remark.equals(new Remark("Less sugar")));

        // same object -> returns true
        assertTrue(remark.equals(remark));

        // null -> returns false
        assertFalse(remark.equals(null));

        // different types -> returns false
        assertFalse(remark.equals(5.0f));

        // different values -> returns false
        assertFalse(remark.equals(new Remark("Separate packaging")));
    }

    @Test
    public void hashCode_sameValue_sameHashCode() {
        assertEquals(new Remark("Less sugar").hashCode(), new Remark("Less sugar").hashCode());
    }
}
