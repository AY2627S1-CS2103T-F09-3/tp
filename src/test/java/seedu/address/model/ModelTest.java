package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ModelTest {

    @Test
    public void updatePersonListComparator_modelWithoutOrdering_preservesDataAndFilter() {
        Model model = new ModelWithoutOrdering();
        Person later = new PersonBuilder().withName("Later").withPostalCode("654321").build();
        Person earlier = new PersonBuilder().withName("Earlier").withPostalCode("012345").build();
        Person hidden = new PersonBuilder().withName("Hidden").build();
        List.of(later, earlier, hidden).forEach(model::addPerson);
        model.updateFilteredPersonList(person -> !person.equals(hidden));
        AddressBook original = new AddressBook(model.getAddressBook());

        // Older Model implementations may ignore the optional display-ordering operation.
        model.updatePersonListComparator(Comparator.comparing(person -> person.getPostalCode().value));
        assertEquals(List.of(later, earlier), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());

        model.updatePersonListComparator(null);
        assertEquals(List.of(later, earlier), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
    }

    /** Delegates the original Model API while inheriting its default ordering behaviour. */
    private static class ModelWithoutOrdering implements Model {
        private final ModelManager delegate = new ModelManager();

        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            return delegate.getUserPrefs();
        }

        @Override
        public GuiSettings getGuiSettings() {
            return delegate.getGuiSettings();
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            delegate.setGuiSettings(guiSettings);
        }

        @Override
        public void setAddressBook(ReadOnlyAddressBook addressBook) {
            delegate.setAddressBook(addressBook);
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return delegate.getAddressBook();
        }

        @Override
        public boolean hasPerson(Person person) {
            return delegate.hasPerson(person);
        }

        @Override
        public void deletePerson(Person target) {
            delegate.deletePerson(target);
        }

        @Override
        public void addPerson(Person person) {
            delegate.addPerson(person);
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            delegate.setPerson(target, editedPerson);
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return delegate.getFilteredPersonList();
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            delegate.updateFilteredPersonList(predicate);
        }
    }
}
