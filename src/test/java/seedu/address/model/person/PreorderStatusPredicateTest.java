package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.preorder.PreorderStatus.COMPLETED;
import static seedu.address.model.preorder.PreorderStatus.PENDING;
import static seedu.address.model.preorder.PreorderStatus.PREPARED;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.preorder.PreorderStatus;
import seedu.address.testutil.PersonBuilder;

public class PreorderStatusPredicateTest {

    private static final Person PERSON_WITHOUT_PREORDER = new PersonBuilder().build();
    private static final Person PENDING_PERSON =
            new PersonBuilder().withPreorder("2 chocolate cakes", PENDING).build();
    private static final Person PREPARED_PERSON =
            new PersonBuilder().withPreorder("2 chocolate cakes", PREPARED).build();
    private static final Person COMPLETED_PERSON =
            new PersonBuilder().withPreorder("2 chocolate cakes", COMPLETED).build();

    @Test
    public void constructor_nullStatuses_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PreorderStatusPredicate(null));
    }

    @Test
    public void constructor_nullStatusElement_throwsNullPointerException() {
        Set<PreorderStatus> statuses = new HashSet<>();
        statuses.add(null);
        assertThrows(NullPointerException.class, () -> new PreorderStatusPredicate(statuses));
    }

    @Test
    public void constructor_sourceSetModified_predicateUnchanged() {
        Set<PreorderStatus> statuses = new HashSet<>(Set.of(PENDING, PREPARED));
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(statuses);

        statuses.remove(PENDING);
        statuses.add(COMPLETED);

        assertTrue(predicate.test(PENDING_PERSON));
        assertTrue(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of(PENDING));
        assertThrows(NullPointerException.class, () -> predicate.test(null));
    }

    @Test
    public void test_personWithoutPreorder_returnsFalse() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of(PENDING, PREPARED, COMPLETED));
        assertFalse(predicate.test(PERSON_WITHOUT_PREORDER));
    }

    @Test
    public void test_pendingStatus_matchesPendingOnly() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of(PENDING));
        assertTrue(predicate.test(PENDING_PERSON));
        assertFalse(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
    }

    @Test
    public void test_preparedStatus_matchesPreparedOnly() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of(PREPARED));
        assertFalse(predicate.test(PENDING_PERSON));
        assertTrue(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
    }

    @Test
    public void test_completedStatus_matchesCompletedOnly() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of(COMPLETED));
        assertFalse(predicate.test(PENDING_PERSON));
        assertFalse(predicate.test(PREPARED_PERSON));
        assertTrue(predicate.test(COMPLETED_PERSON));
    }

    @Test
    public void test_activeStatuses_matchesPendingAndPreparedOnly() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of(PENDING, PREPARED));
        assertTrue(predicate.test(PENDING_PERSON));
        assertTrue(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
        assertFalse(predicate.test(PERSON_WITHOUT_PREORDER));
    }

    @Test
    public void test_emptyStatuses_returnsFalse() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of());
        assertFalse(predicate.test(PENDING_PERSON));
        assertFalse(predicate.test(PREPARED_PERSON));
        assertFalse(predicate.test(COMPLETED_PERSON));
        assertFalse(predicate.test(PERSON_WITHOUT_PREORDER));
    }

    @Test
    public void equals() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of(PENDING, PREPARED));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new PreorderStatusPredicate(Set.of(PREPARED, PENDING))));
        assertFalse(predicate.equals(new PreorderStatusPredicate(Set.of(PENDING))));
        assertFalse(predicate.equals(new PreorderStatusPredicate(Set.of(COMPLETED))));
        assertFalse(predicate.equals(new PreorderStatusPredicate(Set.of())));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(1));
    }

    @Test
    public void hashCode_sameStatuses_sameHashCode() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of(PENDING, PREPARED));
        PreorderStatusPredicate sameStatusesPredicate = new PreorderStatusPredicate(Set.of(PREPARED, PENDING));
        assertEquals(predicate.hashCode(), sameStatusesPredicate.hashCode());
    }

    @Test
    public void toStringMethod() {
        PreorderStatusPredicate predicate = new PreorderStatusPredicate(Set.of(PENDING));
        String expected = PreorderStatusPredicate.class.getCanonicalName() + "{statuses=[PENDING]}";
        assertEquals(expected, predicate.toString());
    }
}
