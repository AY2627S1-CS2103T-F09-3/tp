package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.SEARCH_CRITERIA_SHOW_ALL_PERSONS;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.testutil.AddressBookBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
        assertEquals(SEARCH_CRITERIA_SHOW_ALL_PERSONS, modelManager.getActiveSearchCriteria());
        assertEquals(SEARCH_CRITERIA_SHOW_ALL_PERSONS, modelManager.activeSearchCriteriaProperty().get());
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void updateFilteredPersonList_searchReplaced_updatesCriteriaAndResults() {
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(),
                new UserPrefs());
        SearchCriteria aliceCriteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alice")));
        SearchCriteria bensonCriteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Benson")));

        modelManager.updateFilteredPersonList(aliceCriteria);
        assertEquals(aliceCriteria, modelManager.getActiveSearchCriteria());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());

        modelManager.updateFilteredPersonList(bensonCriteria);
        assertEquals(bensonCriteria, modelManager.getActiveSearchCriteria());
        assertEquals(List.of(BENSON), modelManager.getFilteredPersonList());

        modelManager.updateFilteredPersonList(SEARCH_CRITERIA_SHOW_ALL_PERSONS);
        assertEquals(SEARCH_CRITERIA_SHOW_ALL_PERSONS, modelManager.getActiveSearchCriteria());
        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonList_nullCriteria_preservesCriteriaAndResults() {
        modelManager.addPerson(ALICE);
        SearchCriteria criteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alice")));
        modelManager.updateFilteredPersonList(criteria);

        assertThrows(NullPointerException.class, () -> modelManager.updateFilteredPersonList(null));

        assertEquals(criteria, modelManager.getActiveSearchCriteria());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonList_noMatches_keepsActiveCriteria() {
        modelManager.addPerson(ALICE);
        SearchCriteria criteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Nobody")));

        modelManager.updateFilteredPersonList(criteria);

        assertEquals(criteria, modelManager.getActiveSearchCriteria());
        assertTrue(modelManager.getFilteredPersonList().isEmpty());
    }

    @Test
    public void activeSearchCriteriaProperty_sameResultsDifferentCriteria_notifiesObservers() {
        modelManager.addPerson(ALICE);
        ReadOnlyObjectProperty<SearchCriteria> property = modelManager.activeSearchCriteriaProperty();
        assertFalse(property instanceof Property<?>);
        List<SearchCriteria> observedCriteria = new ArrayList<>();
        property.addListener((observable, oldCriteria, newCriteria) -> observedCriteria.add(newCriteria));
        SearchCriteria firstCriteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alice")));
        SearchCriteria secondCriteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Pauline")));

        modelManager.updateFilteredPersonList(firstCriteria);
        modelManager.updateFilteredPersonList(secondCriteria);

        assertEquals(List.of(firstCriteria, secondCriteria), observedCriteria);
        assertEquals(secondCriteria, property.get());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
    }

    @Test
    public void equals_sameResultsDifferentCriteria_returnsFalse() {
        modelManager.addPerson(ALICE);
        ModelManager otherModel = new ModelManager(modelManager.getAddressBook(), new UserPrefs());
        modelManager.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alice"))));
        otherModel.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Pauline"))));

        assertEquals(modelManager.getFilteredPersonList(), otherModel.getFilteredPersonList());
        assertFalse(modelManager.equals(otherModel));

        otherModel.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "ALICE"))));
        assertTrue(modelManager.equals(otherModel));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        modelManager.updateFilteredPersonList(new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, ALICE.getName().fullName))));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(SEARCH_CRITERIA_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
