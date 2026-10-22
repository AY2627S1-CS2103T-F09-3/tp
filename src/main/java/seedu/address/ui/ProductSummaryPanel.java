package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.summary.ProductPreparationSummary;
import seedu.address.model.summary.ProductPreparationSummary.MatchingItem;

/**
 * A persistent, scrollable preparation summary for the current product search.
 * Both inputs are observed for the lifetime of the main window.
 */
public class ProductSummaryPanel extends UiPart<Region> {
    private static final double KEY_SCROLL_PIXELS = 24;
    private static final String FXML = "ProductSummaryPanel.fxml";
    private static final Logger logger = LogsCenter.getLogger(ProductSummaryPanel.class);

    private final ObservableList<Person> customers;
    private final ReadOnlyObjectProperty<SearchCriteria> activeCriteria;

    @FXML
    private ScrollPane productSummary;
    @FXML
    private Label productHeading;
    @FXML
    private Label counts;
    @FXML
    private Label quantity;
    @FXML
    private Label remarks;

    /**
     * Creates a panel backed by the displayed customers and the active search criteria.
     * Must be constructed and updated on the JavaFX application thread.
     */
    public ProductSummaryPanel(ObservableList<Person> customers,
            ReadOnlyObjectProperty<SearchCriteria> activeCriteria) {
        super(FXML);
        this.customers = requireNonNull(customers);
        this.activeCriteria = requireNonNull(activeCriteria);
        requireNonNull(activeCriteria.get());
        customers.addListener((ListChangeListener<Person>) change -> refresh());
        // Equivalent searches can still use different capitalization in the heading.
        activeCriteria.addListener((InvalidationListener) observable -> refresh());
        productSummary.addEventFilter(KeyEvent.KEY_PRESSED, this::handleScrollKey);
        refresh();
    }

    private void handleScrollKey(KeyEvent event) {
        double viewportHeight = productSummary.getViewportBounds().getHeight();
        double scrollableHeight = productSummary.getContent().getBoundsInLocal().getHeight() - viewportHeight;
        if (scrollableHeight <= 0) {
            return;
        }
        double step = KEY_SCROLL_PIXELS / scrollableHeight;
        double page = viewportHeight / scrollableHeight;
        double current = productSummary.getVvalue();
        Double next = switch (event.getCode()) {
            case UP -> current - step;
            case DOWN -> current + step;
            case PAGE_UP -> current - page;
            case PAGE_DOWN -> current + page;
            case HOME -> 0.0;
            case END -> 1.0;
            default -> null;
        };
        if (next != null) {
            productSummary.setVvalue(Math.max(0, Math.min(1, next)));
            event.consume();
        }
    }

    private void refresh() {
        Optional<ItemCriterion> product = activeCriteria.get().getCriteria().stream()
                .filter(ItemCriterion.class::isInstance)
                .map(ItemCriterion.class::cast)
                .filter(criterion -> criterion.getProductName().isPresent())
                .findFirst();
        getRoot().setVisible(product.isPresent());
        getRoot().setManaged(product.isPresent());
        if (product.isEmpty()) {
            logger.fine("Hiding product preparation summary because no product search is active.");
        }
        product.ifPresent(this::showSummary);
    }

    private void showSummary(ItemCriterion criterion) {
        ProductPreparationSummary summary = ProductPreparationSummary.from(customers, criterion);
        logger.fine("Refreshing product preparation summary for "
                + criterion.getProductName().orElseThrow() + ": " + summary.getCustomerCount()
                + " customer(s), " + summary.getMatchingItemCount() + " matching item(s).");
        productHeading.setText("Preparation: " + criterion.getProductName().orElseThrow()
                + " | " + formatStatuses(criterion));
        counts.setText(summary.getCustomerCount() + " customer(s) | "
                + summary.getMatchingItemCount() + " matching item(s)");
        quantity.setText(formatQuantity(summary));
        String requests = summary.getMatchingItems().stream()
                .filter(match -> match.getItem().getRemark().isPresent())
                .map(ProductSummaryPanel::formatRemark)
                .collect(Collectors.joining("\n"));
        remarks.setText(requests.isEmpty() ? "No special requests for matching items." : requests);
    }

    private static String formatStatuses(ItemCriterion criterion) {
        if (criterion.getStatuses().isEmpty()) {
            return "No statuses";
        }
        if (criterion.getStatuses().equals(EnumSet.allOf(ItemStatus.class))) {
            return "All statuses";
        }
        return criterion.getStatuses().stream().sorted().map(ItemStatus::name).collect(Collectors.joining(", "));
    }

    private static String formatQuantity(ProductPreparationSummary summary) {
        if (summary.getMissingQuantityCount() > 0) {
            return "Known quantity: " + summary.getKnownQuantity() + " | "
                    + summary.getMissingQuantityCount() + " item(s) need a quantity";
        }
        return "Total quantity: " + summary.getKnownQuantity();
    }

    private static String formatRemark(MatchingItem match) {
        return match.getCustomer().getName().fullName + ", item " + match.getItemNumber() + " - "
                + match.getItem().getRemark().orElseThrow().value;
    }
}
