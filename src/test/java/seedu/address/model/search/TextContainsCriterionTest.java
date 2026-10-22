package seedu.address.model.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.search.TextContainsCriterion.Field.ADDRESS;
import static seedu.address.model.search.TextContainsCriterion.Field.EMAIL;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

public class TextContainsCriterionTest {
    private static final Person CUSTOMER = new PersonBuilder().withName("Alice Tanaka").withPhone("81234567")
            .withEmail("alice.tan@example.com").withAddress("123 Hougang Avenue, #04-08").build();

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TextContainsCriterion(null, "Alice"));
        assertThrows(NullPointerException.class, () -> new TextContainsCriterion(NAME, null));
    }

    @Test
    public void constructor_blankQuery_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new TextContainsCriterion(NAME, ""));
        assertThrows(IllegalArgumentException.class, () -> new TextContainsCriterion(NAME, " \t\n "));
    }

    @Test
    public void getters_exposeFieldAndNormalizedQuery() {
        TextContainsCriterion criterion = new TextContainsCriterion(ADDRESS, "  HOUGANG  Avenue, #04-08 \t");

        assertEquals(ADDRESS, criterion.getField());
        assertEquals("hougang  avenue, #04-08", criterion.getQuery());
    }

    @Test
    public void test_nameSubstring_ignoresCaseAndSurroundingWhitespace() {
        assertTrue(new TextContainsCriterion(NAME, " ALICE tan ").test(CUSTOMER));
        assertTrue(new TextContainsCriterion(NAME, "lic").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(NAME, "Bob").test(CUSTOMER));
    }

    @Test
    public void test_multiwordQuery_matchesOneContiguousSubstring() {
        assertTrue(new TextContainsCriterion(NAME, "Alice Tan").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(NAME, "Tan Alice").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(NAME, "Alice Bob").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(NAME, "Alice  Tan").test(CUSTOMER));
    }

    @Test
    public void test_phoneFragment_matchesSubstring() {
        assertTrue(new TextContainsCriterion(PHONE, "8123").test(CUSTOMER));
        assertTrue(new TextContainsCriterion(PHONE, "456").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(PHONE, "8999").test(CUSTOMER));
    }

    @Test
    public void test_emailFragment_ignoresCase() {
        assertTrue(new TextContainsCriterion(EMAIL, "EXAMPLE.COM").test(CUSTOMER));
        assertTrue(new TextContainsCriterion(EMAIL, "tan@").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(EMAIL, "other.com").test(CUSTOMER));
    }

    @Test
    public void test_addressFragment_preservesSpacingAndPunctuation() {
        assertTrue(new TextContainsCriterion(ADDRESS, " HOUGANG Avenue ").test(CUSTOMER));
        assertTrue(new TextContainsCriterion(ADDRESS, "#04-08").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(ADDRESS, "Hougang  Avenue").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(ADDRESS, "#0408").test(CUSTOMER));
    }

    @Test
    public void test_textInDifferentField_returnsFalse() {
        assertFalse(new TextContainsCriterion(NAME, "8123").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(PHONE, "Alice").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(EMAIL, "Hougang").test(CUSTOMER));
        assertFalse(new TextContainsCriterion(ADDRESS, "example.com").test(CUSTOMER));
    }

    @Test
    public void test_handoverNotes_matchesSharedNotesIncludingEmptyPreorders() {
        TextContainsCriterion criterion = new TextContainsCriterion(TextContainsCriterion.Field.HANDOVER_NOTES,
                " CALL on ");
        Person customer = new PersonBuilder().withPreorder(new PreorderBuilder()
                .withHandoverNotes("Please call on arrival!").build()).build();
        assertTrue(criterion.test(customer));
        assertFalse(criterion.test(CUSTOMER));
        assertFalse(criterion.test(new PersonBuilder().withPreorder().build()));
        assertFalse(criterion.test(new PersonBuilder().withPreorder(new ItemBuilder()
                .withRemark("Call on arrival").build()).build()));
        assertFalse(new TextContainsCriterion(TextContainsCriterion.Field.HANDOVER_NOTES, "call  on").test(customer));
    }

    @Test
    public void test_turkishDefaultLocale_matchesUsingRootLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(new TextContainsCriterion(NAME, "ALICE").test(CUSTOMER));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        TextContainsCriterion criterion = new TextContainsCriterion(NAME, "Alice");
        assertThrows(NullPointerException.class, () -> criterion.test(null));
    }

    @Test
    public void equals() {
        TextContainsCriterion criterion = new TextContainsCriterion(NAME, "Alice Tan");

        assertTrue(criterion.equals(criterion));
        assertTrue(criterion.equals(new TextContainsCriterion(NAME, " ALICE TAN ")));
        assertFalse(criterion.equals(new TextContainsCriterion(EMAIL, "Alice Tan")));
        assertFalse(criterion.equals(new TextContainsCriterion(NAME, "Alice")));
        assertFalse(criterion.equals(new TextContainsCriterion(NAME, "Alice  Tan")));
        assertFalse(criterion.equals(null));
        assertFalse(criterion.equals(1));
    }

    @Test
    public void hashCode_equivalentQueries_sameHashCode() {
        TextContainsCriterion criterion = new TextContainsCriterion(NAME, "Alice");
        assertEquals(criterion.hashCode(), new TextContainsCriterion(NAME, " ALICE ").hashCode());
    }
}
