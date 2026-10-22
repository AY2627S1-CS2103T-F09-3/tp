package seedu.address.model.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.preorder.ItemStatus.COMPLETED;
import static seedu.address.model.preorder.ItemStatus.PENDING;
import static seedu.address.model.preorder.ItemStatus.PREPARED;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

public class ItemCriterionStatusTest {

    private static final Person PERSON_WITHOUT_PREORDER = new PersonBuilder().build();
    private static final Person PENDING_PERSON =
            new PersonBuilder().withPreorder("2 chocolate cakes", PENDING).build();
    private static final Person PREPARED_PERSON =
            new PersonBuilder().withPreorder("2 chocolate cakes", PREPARED).build();
    private static final Person COMPLETED_PERSON =
            new PersonBuilder().withPreorder("2 chocolate cakes", COMPLETED).build();

    @Test
    public void constructor_nullStatuses_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ItemCriterion(Optional.empty(), null));
    }

    @Test
    public void constructor_nullStatusElement_throwsNullPointerException() {
        Set<ItemStatus> statuses = new HashSet<>();
        statuses.add(null);
        assertThrows(NullPointerException.class, () -> new ItemCriterion(Optional.empty(), statuses));
    }

    @Test
    public void constructor_sourceSetModified_predicateUnchanged() {
        Set<ItemStatus> statuses = new HashSet<>(Set.of(PENDING, PREPARED));
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), statuses);

        statuses.remove(PENDING);
        statuses.add(COMPLETED);

        assertTrue(predicate.test(PENDING_PERSON));
        assertTrue(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(PENDING));
        assertThrows(NullPointerException.class, () -> predicate.test(null));
    }

    @Test
    public void test_personWithoutPreorder_returnsFalse() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(PENDING, PREPARED, COMPLETED));
        assertFalse(predicate.test(PERSON_WITHOUT_PREORDER));
    }

    @Test
    public void test_preorderWithoutItems_returnsFalse() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(PENDING, PREPARED, COMPLETED));
        assertFalse(predicate.test(new PersonBuilder().withPreorder().build()));
    }

    @Test
    public void test_itemsWithDifferentStatuses_matchesIfAnyItemMatches() {
        Person person = new PersonBuilder().withPreorder(
                new ItemBuilder().withName("Pineapple Tarts").withStatus(COMPLETED).build(),
                new ItemBuilder().withName("Almond Cookies").withStatus(PREPARED).build()).build();

        assertTrue(new ItemCriterion(Optional.empty(), Set.of(PREPARED)).test(person));
        assertTrue(new ItemCriterion(Optional.empty(), Set.of(COMPLETED)).test(person));
        assertFalse(new ItemCriterion(Optional.empty(), Set.of(PENDING)).test(person));
    }

    @Test
    public void test_pendingStatus_matchesPendingOnly() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(PENDING));
        assertTrue(predicate.test(PENDING_PERSON));
        assertFalse(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
    }

    @Test
    public void test_preparedStatus_matchesPreparedOnly() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(PREPARED));
        assertFalse(predicate.test(PENDING_PERSON));
        assertTrue(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
    }

    @Test
    public void test_completedStatus_matchesCompletedOnly() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(COMPLETED));
        assertFalse(predicate.test(PENDING_PERSON));
        assertFalse(predicate.test(PREPARED_PERSON));
        assertTrue(predicate.test(COMPLETED_PERSON));
    }

    @Test
    public void test_activeStatuses_matchesPendingAndPreparedOnly() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(PENDING, PREPARED));
        assertTrue(predicate.test(PENDING_PERSON));
        assertTrue(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
        assertFalse(predicate.test(PERSON_WITHOUT_PREORDER));
    }

    @Test
    public void test_emptyStatuses_returnsFalse() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of());
        assertFalse(predicate.test(PENDING_PERSON));
        assertFalse(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
        assertFalse(predicate.test(PERSON_WITHOUT_PREORDER));
    }

    @Test
    public void equals() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(PENDING, PREPARED));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new ItemCriterion(Optional.empty(), Set.of(PREPARED, PENDING))));
        assertFalse(predicate.equals(new ItemCriterion(Optional.empty(), Set.of(PENDING))));
        assertFalse(predicate.equals(new ItemCriterion(Optional.empty(), Set.of(COMPLETED))));
        assertFalse(predicate.equals(new ItemCriterion(Optional.empty(), Set.of())));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(1));
    }

    @Test
    public void hashCode_sameStatuses_sameHashCode() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(PENDING, PREPARED));
        ItemCriterion sameStatusesPredicate = new ItemCriterion(Optional.empty(), Set.of(PREPARED, PENDING));
        assertEquals(predicate.hashCode(), sameStatusesPredicate.hashCode());
    }

    @Test
    public void toStringMethod() {
        ItemCriterion predicate = new ItemCriterion(Optional.empty(), Set.of(PENDING));
        String expected = ItemCriterion.class.getCanonicalName() + "{productName=Optional.empty, statuses=[PENDING]}";
        assertEquals(expected, predicate.toString());
    }
}
