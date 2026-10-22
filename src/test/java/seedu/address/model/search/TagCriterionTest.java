package seedu.address.model.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class TagCriterionTest {
    private static final Person CUSTOMER = new PersonBuilder().withTags("regular", "VIP").build();

    @Test
    public void constructor_nullTag_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TagCriterion(null));
    }

    @Test
    public void getTag_preservesTagCase() {
        TagCriterion criterion = new TagCriterion(new Tag("VIP"));
        assertEquals("VIP", criterion.getTag().tagName);
    }

    @Test
    public void test_completeTag_ignoresCase() {
        assertTrue(new TagCriterion(new Tag("REGULAR")).test(CUSTOMER));
        assertTrue(new TagCriterion(new Tag("vip")).test(CUSTOMER));
    }

    @Test
    public void test_partialOrMissingTag_returnsFalse() {
        assertFalse(new TagCriterion(new Tag("reg")).test(CUSTOMER));
        assertFalse(new TagCriterion(new Tag("regulars")).test(CUSTOMER));
        assertFalse(new TagCriterion(new Tag("wholesale")).test(CUSTOMER));
        assertFalse(new TagCriterion(new Tag("regular")).test(new PersonBuilder().withTags().build()));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        TagCriterion criterion = new TagCriterion(new Tag("regular"));
        assertThrows(NullPointerException.class, () -> criterion.test(null));
    }

    @Test
    public void equals() {
        TagCriterion criterion = new TagCriterion(new Tag("regular"));

        assertTrue(criterion.equals(criterion));
        assertTrue(criterion.equals(new TagCriterion(new Tag("REGULAR"))));
        assertFalse(criterion.equals(new TagCriterion(new Tag("vip"))));
        assertFalse(criterion.equals(null));
        assertFalse(criterion.equals(1));
    }

    @Test
    public void hashCode_equivalentTags_sameHashCode() {
        TagCriterion criterion = new TagCriterion(new Tag("regular"));
        assertEquals(criterion.hashCode(), new TagCriterion(new Tag("REGULAR")).hashCode());
    }

    @Test
    public void toStringMethod() {
        TagCriterion criterion = new TagCriterion(new Tag("regular"));
        String expected = TagCriterion.class.getCanonicalName() + "{tag=[regular]}";

        assertEquals(expected, criterion.toString());
    }
}
