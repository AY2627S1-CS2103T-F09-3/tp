package seedu.address.ui;

import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.Preorder;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label postalCode;
    @FXML
    private Label email;
    @FXML
    private FlowPane tags;
    @FXML
    private VBox preorder;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        postalCode.setText(person.getPostalCode() == null ? "Postal code needed" : person.getPostalCode().value);
        email.setText(person.getEmail().value);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        person.getPreorder().ifPresentOrElse(this::showPreorder, this::hidePreorder);
    }

    /**
     * Shows one chip per item, numbered in the order the items were added and coloured by status.
     */
    private void showPreorder(Preorder currentPreorder) {
        List<Item> items = currentPreorder.getItems();
        if (items.isEmpty()) {
            preorder.getChildren().add(createChip(PreorderDisplay.NO_ITEMS_TEXT, PreorderDisplay.NO_ITEMS_STYLE_CLASS));
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            preorder.getChildren().add(createChip(PreorderDisplay.formatItem(i + 1, item),
                    PreorderDisplay.getStatusStyleClass(item.getStatus())));
        }
    }

    private static Label createChip(String text, String styleClass) {
        Label chip = new Label(text);
        chip.getStyleClass().add(styleClass);
        // As wide as its text, like the tags, unless the text is too long and has to wrap
        chip.setWrapText(true);
        chip.setMaxWidth(Region.USE_PREF_SIZE);
        return chip;
    }

    /**
     * Removes the preorder section from the layout, so a customer without a preorder
     * shows no empty chips or blank gap.
     */
    private void hidePreorder() {
        preorder.setVisible(false);
        preorder.setManaged(false);
    }
}
