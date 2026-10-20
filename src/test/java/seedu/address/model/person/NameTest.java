package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        for (String invalidName : new String[]{"", " ", "John2", "John@Tan", "John\tTan", "-", "'", " - ' ",
            "José Tan"}) {
            assertThrows(IllegalArgumentException.class, Name.MESSAGE_CONSTRAINTS, () -> new Name(invalidName));
        }
    }

    @Test
    public void constructor_extraSpaces_normalizesName() {
        Name name = new Name("  Mary-Jane   O'Brien  ");
        assertEquals("Mary-Jane O'Brien", name.fullName);
        assertEquals("Mary-Jane O'Brien", name.toString());
    }

    @Test
    public void constructor_mixedCase_preservesCapitalization() {
        assertEquals("mArY-JaNe O'BRIEN", new Name(" mArY-JaNe  O'BRIEN ").fullName);
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("\t\r\n")); // whitespace only
        assertFalse(Name.isValidName("^")); // unsupported symbol
        assertFalse(Name.isValidName("peter*")); // contains an unsupported symbol
        assertFalse(Name.isValidName("12345")); // numbers only
        assertFalse(Name.isValidName("peter the 2nd")); // contains numbers
        assertFalse(Name.isValidName("John\tTan")); // internal tab
        assertFalse(Name.isValidName("John\nTan")); // internal newline

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Junior")); // long names
        assertTrue(Name.isValidName("O'Brien")); // apostrophe
        assertTrue(Name.isValidName("Mary-Jane")); // hyphen
        assertTrue(Name.isValidName("  Mary-Jane   O'Brien  ")); // extra spaces
        assertTrue(Name.isValidName("A")); // one letter
        assertTrue(Name.isValidName("Al")); // short name
        assertTrue(Name.isValidName("Jo")); // short name
        assertTrue(Name.isValidName("'A-")); // no additional punctuation-placement restriction
    }

    @Test
    public void isValidName_punctuationOnly_returnsFalse() {
        for (String name : new String[]{"-", "'", "---", "'''", " - ' "}) {
            assertFalse(Name.isValidName(name));
        }
    }

    @Test
    public void isValidName_nonEnglishLetters_returnsFalse() {
        for (String name : new String[]{"José Tan", "Аlex", "Ｊohn"}) {
            assertFalse(Name.isValidName(name));
        }
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same normalized values -> returns true
        assertTrue(name.equals(new Name("  Valid   Name  ")));

        // different capitalization -> returns false
        assertFalse(name.equals(new Name("valid name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }

    @Test
    public void hashCode_sameNormalizedName_returnsSameHashCode() {
        assertEquals(new Name("Valid Name").hashCode(), new Name("  Valid   Name  ").hashCode());
    }
}
