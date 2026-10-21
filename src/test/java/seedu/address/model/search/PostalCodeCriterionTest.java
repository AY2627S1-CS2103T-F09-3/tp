package seedu.address.model.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PostalCodeCriterionTest {
    @Test
    public void constructor_nullPrefix_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PostalCodeCriterion(null));
    }

    @Test
    public void constructor_invalidPrefix_throwsIllegalArgumentException() {
        for (String prefix : new String[]{"", " ", "1234567", "abc", "53a", "5 3", "+53", "53.0",
            " 53", "53 ", "\u0665\u0663"}) {
            assertThrows(IllegalArgumentException.class, PostalCodeCriterion.MESSAGE_CONSTRAINTS, ()
                -> new PostalCodeCriterion(prefix));
        }
    }

    @Test
    public void test_matchingPostalCode_returnsTrue() {
        Person customer = new PersonBuilder().withPostalCode("123456").build();
        assertTrue(new PostalCodeCriterion("123456").test(customer));
    }

    @Test
    public void test_prefix_matchesOnlyAtStart() {
        Person customer = new PersonBuilder().withPostalCode("530123").build();
        for (String prefix : new String[]{"5", "53", "530", "5301", "53012", "530123"}) {
            assertTrue(new PostalCodeCriterion(prefix).test(customer));
        }
        assertFalse(new PostalCodeCriterion("531").test(customer));
        assertFalse(new PostalCodeCriterion("301").test(customer));
    }

    @Test
    public void test_leadingZeroPostalCode_preservesPrefixAndExactMatches() {
        Person customer = new PersonBuilder().withPostalCode("012345").build();
        assertTrue(new PostalCodeCriterion("01").test(customer));
        assertTrue(new PostalCodeCriterion("012345").test(customer));
        assertFalse(new PostalCodeCriterion("1").test(customer));
        assertFalse(new PostalCodeCriterion("123450").test(customer));
    }

    @Test
    public void test_missingPostalCode_returnsFalse() {
        Person customer = new PersonBuilder().withPostalCode(null).build();
        assertFalse(new PostalCodeCriterion("01").test(customer));
        assertFalse(new PostalCodeCriterion("012345").test(customer));
    }

    @Test
    public void test_differentPostalCodeWithSamePrefix_returnsFalse() {
        Person customer = new PersonBuilder().withPostalCode("012346").build();
        assertFalse(new PostalCodeCriterion("012345").test(customer));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        PostalCodeCriterion criterion = new PostalCodeCriterion("012345");
        assertThrows(NullPointerException.class, () -> criterion.test(null));
    }

    @Test
    public void equals() {
        PostalCodeCriterion criterion = new PostalCodeCriterion("53");

        assertTrue(criterion.equals(criterion));
        assertTrue(criterion.equals(new PostalCodeCriterion("53")));
        assertFalse(criterion.equals(new PostalCodeCriterion("530")));
        assertFalse(criterion.equals(null));
        assertFalse(criterion.equals(1));
    }

    @Test
    public void hashCode_equivalentPrefixes_sameHashCode() {
        PostalCodeCriterion criterion = new PostalCodeCriterion("53");
        assertEquals(criterion.hashCode(), new PostalCodeCriterion("53").hashCode());
    }

    @Test
    public void toStringMethod() {
        PostalCodeCriterion criterion = new PostalCodeCriterion("01");
        String expected = PostalCodeCriterion.class.getCanonicalName() + "{postalCodePrefix=01}";
        assertEquals(expected, criterion.toString());
    }
}
