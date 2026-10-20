package seedu.address.model.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.person.PostalCode;
import seedu.address.testutil.PersonBuilder;

public class PostalCodeCriterionTest {
    @Test
    public void constructor_nullPostalCode_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PostalCodeCriterion(null));
    }

    @Test
    public void test_matchingPostalCode_returnsTrue() {
        Person customer = new PersonBuilder().withPostalCode("123456").build();
        assertTrue(new PostalCodeCriterion(new PostalCode("123456")).test(customer));
    }

    @Test
    public void test_leadingZeroPostalCode_matchesExactly() {
        Person customer = new PersonBuilder().withPostalCode("012345").build();
        assertTrue(new PostalCodeCriterion(new PostalCode("012345")).test(customer));
        assertFalse(new PostalCodeCriterion(new PostalCode("123450")).test(customer));
    }

    @Test
    public void test_missingPostalCode_returnsFalse() {
        Person customer = new PersonBuilder().withPostalCode(null).build();
        assertFalse(new PostalCodeCriterion(new PostalCode("012345")).test(customer));
    }

    @Test
    public void test_differentPostalCodeWithSamePrefix_returnsFalse() {
        Person customer = new PersonBuilder().withPostalCode("012346").build();
        assertFalse(new PostalCodeCriterion(new PostalCode("012345")).test(customer));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        PostalCodeCriterion criterion = new PostalCodeCriterion(new PostalCode("012345"));
        assertThrows(NullPointerException.class, () -> criterion.test(null));
    }

    @Test
    public void equals() {
        PostalCodeCriterion criterion = new PostalCodeCriterion(new PostalCode("012345"));

        assertTrue(criterion.equals(criterion));
        assertTrue(criterion.equals(new PostalCodeCriterion(new PostalCode("012345"))));
        assertFalse(criterion.equals(new PostalCodeCriterion(new PostalCode("012346"))));
        assertFalse(criterion.equals(null));
        assertFalse(criterion.equals(1));
    }

    @Test
    public void hashCode_equivalentPostalCodes_sameHashCode() {
        PostalCodeCriterion criterion = new PostalCodeCriterion(new PostalCode("012345"));
        assertEquals(criterion.hashCode(), new PostalCodeCriterion(new PostalCode("012345")).hashCode());
    }

    @Test
    public void toStringMethod() {
        PostalCodeCriterion criterion = new PostalCodeCriterion(new PostalCode("012345"));
        String expected = PostalCodeCriterion.class.getCanonicalName() + "{postalCode=012345}";
        assertEquals(expected, criterion.toString());
    }
}
