package seedu.address.model;

import java.util.List;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;
import seedu.address.model.search.SearchCriteria;

/**
 * The API of the Model component.
 */
public interface Model {
    /** Empty search criteria that match all customers. */
    SearchCriteria SEARCH_CRITERIA_SHOW_ALL_PERSONS = new SearchCriteria(List.of());

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     * Preserves the active criteria; the person is displayed only if they match.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     * Preserves the active criteria and re-evaluates whether the edited person matches.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Returns the immutable criteria for the active customer search.
     */
    SearchCriteria getActiveSearchCriteria();

    /**
     * Returns a read-only property for observing changes to the active search criteria.
     */
    ReadOnlyObjectProperty<SearchCriteria> activeSearchCriteriaProperty();

    /**
     * Replaces the active search with {@code criteria}, filtering the full customer list.
     * Empty criteria restore all customers.
     *
     * @throws NullPointerException If {@code criteria} is null.
     */
    void updateFilteredPersonList(SearchCriteria criteria);
}
