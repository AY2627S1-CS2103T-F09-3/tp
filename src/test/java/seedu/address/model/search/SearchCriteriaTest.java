package seedu.address.model.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class SearchCriteriaTest {
    private static final PersonCriterion NAME_CRITERION = new TextContainsCriterion(NAME, "Alice");
    private static final PersonCriterion TAG_CRITERION = new TagCriterion(new Tag("friends"));

    @Test
    public void constructor_nullCriteria_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchCriteria(null));
    }

    @Test
    public void constructor_nullCriterion_throwsNullPointerException() {
        List<PersonCriterion> criteria = new ArrayList<>();
        criteria.add(null);
        assertThrows(NullPointerException.class, () -> new SearchCriteria(criteria));
    }

    @Test
    public void constructor_sourceCollectionModified_searchUnchanged() {
        List<PersonCriterion> sourceCriteria = new ArrayList<>(List.of(NAME_CRITERION, TAG_CRITERION));
        SearchCriteria search = new SearchCriteria(sourceCriteria);

        sourceCriteria.clear();
        sourceCriteria.add(new TextContainsCriterion(NAME, "Benson"));

        assertTrue(search.test(ALICE));
        assertFalse(search.test(BENSON));
        assertFalse(search.test(new PersonBuilder(ALICE).withTags().build()));
    }

    @Test
    public void test_emptyCriteria_matchesEveryPerson() {
        SearchCriteria search = new SearchCriteria(List.of());
        assertTrue(search.test(ALICE));
        assertTrue(search.test(BENSON));
    }

    @Test
    public void test_allCriteriaMatch_returnsTrue() {
        SearchCriteria search = new SearchCriteria(List.of(NAME_CRITERION, TAG_CRITERION,
                new TextContainsCriterion(PHONE, "9435")));
        assertTrue(search.test(ALICE));
    }

    @Test
    public void test_onlySomeCriteriaMatch_returnsFalse() {
        SearchCriteria search = new SearchCriteria(List.of(NAME_CRITERION, TAG_CRITERION));
        assertFalse(search.test(BENSON));
        assertFalse(search.test(new PersonBuilder(ALICE).withTags().build()));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchCriteria(List.of()).test(null));
        assertThrows(NullPointerException.class, () -> new SearchCriteria(List.of(NAME_CRITERION)).test(null));
    }

    @Test
    public void getLogSummary_emptyCriteria_reportsZeroCountAndNoTypes() {
        assertEquals("count=0, types=[]", new SearchCriteria(List.of()).getLogSummary());
    }

    @Test
    public void getLogSummary_mixedCriteria_reportsDeduplicatedCountAndSortedTypes() {
        PersonCriterion phoneCriterion = new TextContainsCriterion(PHONE, "9435");
        SearchCriteria search = new SearchCriteria(List.of(NAME_CRITERION, phoneCriterion, TAG_CRITERION,
                new TextContainsCriterion(NAME, " ALICE ")));
        SearchCriteria reorderedSearch = new SearchCriteria(List.of(TAG_CRITERION, phoneCriterion, NAME_CRITERION));

        assertEquals("count=3, types=[TagCriterion, TextContainsCriterion]", search.getLogSummary());
        assertEquals(search.getLogSummary(), reorderedSearch.getLogSummary());
    }

    @Test
    public void getLogSummary_statusAlternatives_reportsOneCriterion() {
        SearchCriteria search = new SearchCriteria(List.of(
                new ItemCriterion(Optional.empty(), Set.of(ItemStatus.PENDING, ItemStatus.PREPARED))));

        assertEquals("count=1, types=[ItemCriterion]", search.getLogSummary());
    }

    @Test
    public void equals() {
        SearchCriteria search = new SearchCriteria(List.of(NAME_CRITERION, TAG_CRITERION));

        assertTrue(search.equals(search));
        assertTrue(search.equals(new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, " ALICE "), new TagCriterion(new Tag("FRIENDS"))))));
        assertTrue(search.equals(new SearchCriteria(List.of(TAG_CRITERION, NAME_CRITERION))));
        assertTrue(search.equals(new SearchCriteria(List.of(NAME_CRITERION, TAG_CRITERION, NAME_CRITERION))));
        assertFalse(search.equals(new SearchCriteria(List.of(NAME_CRITERION))));
        assertFalse(search.equals(new SearchCriteria(List.of())));
        assertFalse(search.equals(null));
        assertFalse(search.equals(1));
    }

    @Test
    public void hashCode_equivalentCriteria_sameHashCode() {
        SearchCriteria search = new SearchCriteria(List.of(NAME_CRITERION, TAG_CRITERION));
        SearchCriteria reorderedSearch = new SearchCriteria(List.of(TAG_CRITERION, NAME_CRITERION));
        assertEquals(search.hashCode(), reorderedSearch.hashCode());
    }

    @Test
    public void updateFilteredPersonList_searchCriteria_filtersFullAddressBook() {
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        SearchCriteria search = new SearchCriteria(List.of(NAME_CRITERION, TAG_CRITERION));

        model.updateFilteredPersonList(search);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        model.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Benson"))));
        assertEquals(List.of(BENSON), model.getFilteredPersonList());

        model.updateFilteredPersonList(new SearchCriteria(List.of()));
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonList_personReplaced_reevaluatesCriteria() {
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredPersonList(new SearchCriteria(List.of(TAG_CRITERION)));
        Person aliceWithoutTags = new PersonBuilder(ALICE).withTags().build();

        model.setPerson(ALICE, aliceWithoutTags);
        assertFalse(model.getFilteredPersonList().contains(aliceWithoutTags));

        model.setPerson(aliceWithoutTags, ALICE);
        assertTrue(model.getFilteredPersonList().contains(ALICE));
    }
}
