package seedu.address.ui;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.GroupPostalCommand;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                VBox cellContent = new VBox();
                String currentGroup = GroupPostalCommand.getPostalGroupKey(person);
                boolean isFirstInGroup = getIndex() == 0
                        || !currentGroup.equals(GroupPostalCommand.getPostalGroupKey(personListView
                        .getItems().get(getIndex() - 1)));
                if (isFirstInGroup) {
                    Label groupHeading = new Label(currentGroup.equals("\uffff")
                            ? "MISSING POSTAL CODE" : "POSTAL PREFIX " + currentGroup);
                    groupHeading.getStyleClass().add("cell_big_label");
                    cellContent.getChildren().add(groupHeading);
                }
                cellContent.getChildren().add(new PersonCard(person, getIndex() + 1).getRoot());
                setGraphic(cellContent);
            }
        }
    }

}
