package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.GroupPostalCommand;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.search.SearchCriteria;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private static final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    private final ReadOnlyObjectProperty<SearchCriteria> activeCriteria;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        this(personList, new ReadOnlyObjectWrapper<>(Model.SEARCH_CRITERIA_SHOW_ALL_PERSONS).getReadOnlyProperty());
    }

    /**
     * Creates a list that refreshes item highlighting when the active search changes.
     */
    public PersonListPanel(ObservableList<Person> personList,
            ReadOnlyObjectProperty<SearchCriteria> activeCriteria) {
        super(FXML);
        this.activeCriteria = requireNonNull(activeCriteria);
        requireNonNull(activeCriteria.get());
        activeCriteria.addListener((observable, previous, current) -> {
            logger.fine("Refreshing customer cards after search criteria changed.");
            personListView.refresh();
        });
        requireNonNull(personList);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personList.addListener((ListChangeListener<Person>) change -> {
            logger.fine("Refreshing customer cards after the displayed customer list changed.");
            personListView.refresh();
        });
        if (personList instanceof SortedList<?> sortedPersons) {
            sortedPersons.comparatorProperty().addListener((observable, oldValue, newValue) ->
                    personListView.refresh());
        }
        personListView.focusedProperty().addListener((observable, previous, focused) -> {
            if (focused && personListView.getSelectionModel().isEmpty() && !personListView.getItems().isEmpty()) {
                personListView.getSelectionModel().selectFirst();
            }
        });
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            int index = getIndex();
            if (empty || person == null || index < 0 || index >= personListView.getItems().size()) {
                setGraphic(null);
                setText(null);
            } else {
                VBox cellContent = new VBox();
                String currentGroup = GroupPostalCommand.getPostalGroupKey(person);
                boolean isGrouped = personListView.getItems() instanceof SortedList<?> sortedPersons
                        && sortedPersons.getComparator() == GroupPostalCommand.POSTAL_COMPARATOR;
                boolean isFirstInGroup = index == 0
                        || !currentGroup.equals(GroupPostalCommand.getPostalGroupKey(personListView
                        .getItems().get(index - 1)));
                if (isGrouped && isFirstInGroup) {
                    Label groupHeading = new Label("POSTAL PREFIX " + currentGroup);
                    groupHeading.getStyleClass().add("postal-group-heading");
                    cellContent.getChildren().add(groupHeading);
                }
                cellContent.getChildren().add(new PersonCard(person, getIndex() + 1, activeCriteria.get()).getRoot());
                setGraphic(cellContent);
            }
        }
    }

}
