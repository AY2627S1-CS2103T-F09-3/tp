package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class HandoverNotesTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HandoverNotes(null));
    }

    @Test
    public void constructor_invalidHandoverNotes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HandoverNotes(" "));
    }

    @Test
    public void isValidHandoverNotes() {
        // null handover notes
        assertThrows(NullPointerException.class, () -> HandoverNotes.isValidHandoverNotes(null));

        // blank handover notes
        assertFalse(HandoverNotes.isValidHandoverNotes("")); // empty string
        assertFalse(HandoverNotes.isValidHandoverNotes("   ")); // spaces only
        assertFalse(HandoverNotes.isValidHandoverNotes("\u2003")); // em space only
        assertFalse(HandoverNotes.isValidHandoverNotes("\u200B")); // zero-width space only, which shows nothing

        // untrimmed handover notes
        assertFalse(HandoverNotes.isValidHandoverNotes(" Call on arrival")); // leading space
        assertFalse(HandoverNotes.isValidHandoverNotes("Call on arrival ")); // trailing space
        assertFalse(HandoverNotes.isValidHandoverNotes("Call on arrival\u00A0")); // trailing no-break space

        // more than one line
        assertFalse(HandoverNotes.isValidHandoverNotes("Call on arrival\nLeave at door"));

        // length boundary
        assertTrue(HandoverNotes.isValidHandoverNotes("a".repeat(HandoverNotes.MAX_LENGTH)));
        assertFalse(HandoverNotes.isValidHandoverNotes("a".repeat(HandoverNotes.MAX_LENGTH + 1)));

        // valid handover notes
        assertTrue(HandoverNotes.isValidHandoverNotes("x")); // one character
        assertTrue(HandoverNotes.isValidHandoverNotes("Collect from side gate, ring #2!")); // punctuation
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("Call on arrival", new HandoverNotes("Call on arrival").toString());
    }

    @Test
    public void equals() {
        HandoverNotes handoverNotes = new HandoverNotes("Call on arrival");

        // same values -> returns true
        assertTrue(handoverNotes.equals(new HandoverNotes("Call on arrival")));

        // same object -> returns true
        assertTrue(handoverNotes.equals(handoverNotes));

        // null -> returns false
        assertFalse(handoverNotes.equals(null));

        // different types -> returns false
        assertFalse(handoverNotes.equals(5.0f));

        // different values -> returns false
        assertFalse(handoverNotes.equals(new HandoverNotes("Collect from side gate")));
    }

    @Test
    public void hashCode_sameValue_sameHashCode() {
        assertEquals(new HandoverNotes("Call on arrival").hashCode(), new HandoverNotes("Call on arrival").hashCode());
    }
}
