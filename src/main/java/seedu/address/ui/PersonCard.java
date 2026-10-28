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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.SearchCriteria;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    public static final String TAG_STYLE_CLASS = "card-tag";
    public static final String POSTAL_CODE_PREFIX = "Postal ";
    public static final String ITEM_ROW_STYLE_CLASS = "preorder-item-row";
    public static final String ITEM_NUMBER_STYLE_CLASS = "preorder-item-number";
    public static final String ITEM_NAME_STYLE_CLASS = "preorder-item-name";
    public static final String QUANTITY_STYLE_CLASS = "preorder-quantity";
    public static final String MISSING_QUANTITY_STYLE_CLASS = "preorder-quantity-missing";
    public static final String STATUS_CHIP_STYLE_CLASS = "status-chip";
    public static final String REMARK_STYLE_CLASS = "preorder-remark";
    public static final String NO_ITEMS_STYLE_CLASS = "preorder-empty";

    public static final String MATCH_STYLE_CLASS = "matching-item";
    private static final String FXML = "PersonListCard.fxml";
    private static final String WARNING_ICON_STYLE_CLASS = "warning-icon";
    private static final String COMPLETED_ICON_STYLE_CLASS = "completed-icon";

    // Wide enough for "COMPLETED" and its tick, so the chips line up from row to row
    private static final double STATUS_CHIP_WIDTH = 104;
    private static final double ITEM_NUMBER_WIDTH = 20;
    private static final double ITEM_ROW_GAP = 10;
    // About 10 px in the name's font
    private static final String TAG_GAP_TEXT = "  ";
    /*
     * The name line is 401 px wide on the narrowest card, in an 800 px window. A fixed limit is known before the
     * card is laid out, so the card's height is right from the start, and it leaves room for the gap after a
     * chip, so the next tag is not indented.
     */
    private static final double TAG_MAX_WIDTH = 360;
    private static final String NAME_STYLE_CLASS = "card-name";

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
    private Label id;
    @FXML
    private TextFlow nameAndTags;
    @FXML
    private Text name;
    @FXML
    private Text phone;
    @FXML
    private Text email;
    @FXML
    private Text address;
    @FXML
    private Label postalCode;
    @FXML
    private Label noPreorder;
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
        id.setText(String.valueOf(displayedIndex));
        name.setText(person.getName().fullName);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> nameAndTags.getChildren().addAll(createTagGap(), createTag(tag.tagName)));
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        address.setText(person.getAddress().value);
        postalCode.setText(POSTAL_CODE_PREFIX + person.getPostalCode().value);
        person.getPreorder().ifPresentOrElse(currentPreorder -> {
            removeFromLayout(noPreorder);
            showPreorder(currentPreorder);
        }, () -> removeFromLayout(preorder));
    }

    /**
     * Returns the space before a tag. Being plain spaces, it stays at the end of a line when the tag wraps,
     * so a tag that starts a new line is not indented.
     */
    private static Text createTagGap() {
        Text gap = new Text(TAG_GAP_TEXT);
        gap.getStyleClass().add(NAME_STYLE_CLASS);
        return gap;
    }

    /**
     * Returns a tag chip. A label inside a TextFlow is never split, so a long tag moves to the next line whole.
     * A tag wider than {@link #TAG_MAX_WIDTH} wraps inside its chip, so it never runs past the card's edge.
     */
    private static Label createTag(String tagName) {
        Label tag = new Label(tagName) {
            // TextFlow works out a chip's height from its preferred width, so the limit goes on that width
            @Override
            protected double computePrefWidth(double height) {
                return Math.min(super.computePrefWidth(height), TAG_MAX_WIDTH);
            }
        };
        tag.getStyleClass().add(TAG_STYLE_CLASS);
        tag.setWrapText(true);
        // TextFlow lowers a chip to line up with the name's baseline, then gives it only its own height from
        // there, which would cut the second line of a wrapped tag short. The line itself has room for it.
        tag.setMinHeight(Region.USE_PREF_SIZE);
        return tag;
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
     * Returns an item's row: its number, name, quantity and a status chip, with any remark underneath.
     */
    private GridPane createItemRow(int itemNumber, Item item) {
        GridPane row = new GridPane();
        row.getStyleClass().add(ITEM_ROW_STYLE_CLASS);
        row.setHgap(ITEM_ROW_GAP);

        // The name takes the spare width and wraps, while the other columns keep their own width
        ColumnConstraints numberColumn = new ColumnConstraints(ITEM_NUMBER_WIDTH);
        ColumnConstraints nameColumn = new ColumnConstraints();
        nameColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints statusColumn = new ColumnConstraints();
        statusColumn.setMinWidth(STATUS_CHIP_WIDTH);
        row.getColumnConstraints().addAll(numberColumn, nameColumn, new ColumnConstraints(), statusColumn);

        Label number = createLabel(PreorderDisplay.formatItemNumber(itemNumber), ITEM_NUMBER_STYLE_CLASS);
        Label name = createWrappingLabel(item.getName().toString(), ITEM_NAME_STYLE_CLASS);
        row.addRow(0, number, name, createQuantity(item), createStatusChip(item.getStatus()));
        if (!itemCriteria.isEmpty() && itemCriteria.stream().allMatch(criterion -> criterion.matches(item))) {
            row.getStyleClass().add(MATCH_STYLE_CLASS);
        }

        item.getRemark().ifPresent(remark -> {
            Label remarkLabel = createWrappingLabel(PreorderDisplay.formatRemark(remark), REMARK_STYLE_CLASS);
            // On the line below, under the name, quantity and status
            row.add(remarkLabel, 1, 1, row.getColumnCount() - 1, 1);
        });
        return row;
    }

    private static Label createQuantity(Item item) {
        if (item.getQuantity().isPresent()) {
            return createLabel(PreorderDisplay.formatQuantity(item), QUANTITY_STYLE_CLASS);
        }
        Label quantity = createLabel(PreorderDisplay.formatQuantity(item), MISSING_QUANTITY_STYLE_CLASS);
        quantity.setGraphic(createIcon(PreorderDisplay.WARNING_ICON, WARNING_ICON_STYLE_CLASS));
        return quantity;
    }

    private static Label createStatusChip(ItemStatus status) {
        Label chip = createLabel(status.name(), STATUS_CHIP_STYLE_CLASS);
        chip.getStyleClass().add(PreorderDisplay.getStatusStyleClass(status));
        // Fills the status column, so the chips line up from row to row
        chip.setMaxWidth(Double.MAX_VALUE);
        chip.setAlignment(Pos.CENTER);
        if (status == ItemStatus.COMPLETED) {
            chip.setGraphic(createIcon(PreorderDisplay.COMPLETED_ICON, COMPLETED_ICON_STYLE_CLASS));
        }
        return chip;
    }

    private static SVGPath createIcon(String content, String styleClass) {
        SVGPath icon = new SVGPath();
        icon.setContent(content);
        icon.getStyleClass().add(styleClass);
        return icon;
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
