package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.SearchCriteria;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    public static final String ITEM_ROW_STYLE_CLASS = "preorder-item-row";
    public static final String ITEM_NAME_STYLE_CLASS = "preorder-item-name";
    public static final String QUANTITY_STYLE_CLASS = "preorder-quantity";
    public static final String MISSING_QUANTITY_STYLE_CLASS = "preorder-quantity-missing";
    public static final String STATUS_CHIP_STYLE_CLASS = "status-chip";
    public static final String REMARK_STYLE_CLASS = "preorder-remark";
    public static final String NO_ITEMS_STYLE_CLASS = "preorder-empty";

    public static final String MATCH_STYLE_CLASS = "matching-item";
    private static final String FXML = "PersonListCard.fxml";

    // Wide enough for "COMPLETED", so the chips line up from row to row
    private static final double STATUS_CHIP_WIDTH = 80;
    private static final double ITEM_ROW_GAP = 12;

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;
    private final List<ItemCriterion> itemCriteria;

    @FXML
    private GridPane cardPane;
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
    @FXML
    private SVGPath preorderIcon;
    @FXML
    private Label preorderHeader;
    @FXML
    private Label handoverNotes;
    @FXML
    private VBox preorderItems;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        this(person, displayedIndex, new SearchCriteria(List.of()));
    }

    /**
     * Creates a card that marks matching items while retaining all original item numbers.
     */
    public PersonCard(Person person, int displayedIndex, SearchCriteria criteria) {
        super(FXML);
        requireNonNull(person);
        requireNonNull(criteria);
        checkArgument(displayedIndex > 0, "Displayed index must be positive.");
        this.person = person;
        itemCriteria = criteria.getCriteria().stream().filter(ItemCriterion.class::isInstance)
                .map(ItemCriterion.class::cast).toList();
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        postalCode.setText(person.getPostalCode() == null ? "Postal code needed" : person.getPostalCode().value);
        email.setText(person.getEmail().value);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        person.getPreorder().ifPresentOrElse(this::showPreorder, () -> removeFromLayout(preorder));
    }

    /**
     * Fills the preorder panel: how the preorder will be handed over and any handover notes,
     * then one row per item, numbered in the order the items were added.
     */
    private void showPreorder(Preorder currentPreorder) {
        preorderIcon.setContent(PreorderDisplay.getHeaderIcon(currentPreorder));
        preorderHeader.setText(PreorderDisplay.formatHeader(currentPreorder));
        currentPreorder.getHandoverNotes().map(PreorderDisplay::formatHandoverNotes)
                .ifPresentOrElse(handoverNotes::setText, () -> removeFromLayout(handoverNotes));

        List<Item> items = currentPreorder.getItems();
        if (items.isEmpty()) {
            preorderItems.getChildren().add(createWrappingLabel(PreorderDisplay.NO_ITEMS_TEXT, NO_ITEMS_STYLE_CLASS));
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            preorderItems.getChildren().add(createItemRow(i + 1, items.get(i)));
        }
    }

    /**
     * Returns an item's row: its number and name, its quantity and a status chip, with any remark underneath.
     */
    private GridPane createItemRow(int itemNumber, Item item) {
        GridPane row = new GridPane();
        row.getStyleClass().add(ITEM_ROW_STYLE_CLASS);
        row.setHgap(ITEM_ROW_GAP);

        // The name takes the spare width and wraps, while the quantity and status keep their own width
        ColumnConstraints nameColumn = new ColumnConstraints();
        nameColumn.setHgrow(Priority.ALWAYS);
        row.getColumnConstraints().addAll(nameColumn, new ColumnConstraints(), new ColumnConstraints());

        Label name = createWrappingLabel(PreorderDisplay.formatItemName(itemNumber, item), ITEM_NAME_STYLE_CLASS);
        Label quantity = createLabel(PreorderDisplay.formatQuantity(item),
                item.getQuantity().isPresent() ? QUANTITY_STYLE_CLASS : MISSING_QUANTITY_STYLE_CLASS);
        Label status = createLabel(item.getStatus().name(), STATUS_CHIP_STYLE_CLASS);
        status.getStyleClass().add(PreorderDisplay.getStatusStyleClass(item.getStatus()));
        status.setMinWidth(STATUS_CHIP_WIDTH);
        status.setAlignment(Pos.CENTER);
        if (!itemCriteria.isEmpty() && itemCriteria.stream().allMatch(criterion -> criterion.matches(item))) {
            row.getStyleClass().add(MATCH_STYLE_CLASS);
        }
        row.addRow(0, name, quantity, status);

        item.getRemark().ifPresent(remark -> {
            Label remarkLabel = createWrappingLabel(PreorderDisplay.formatRemark(remark), REMARK_STYLE_CLASS);
            // On the line below, across all the columns
            row.add(remarkLabel, 0, 1, row.getColumnCount(), 1);
        });
        return row;
    }

    private static Label createLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        // Keeps its full text, so a long item name wraps instead of squeezing the quantity or status
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    private static Label createWrappingLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        return label;
    }

    /**
     * Removes {@code node} from the layout, such as the preorder panel of a customer without a preorder,
     * so that it leaves no empty panel or blank gap.
     */
    private static void removeFromLayout(Node node) {
        node.setVisible(false);
        node.setManaged(false);
    }
}
