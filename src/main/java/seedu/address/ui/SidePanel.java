package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * The panel beside the customer list, which shows one long output at a time: help after the {@code help} command,
 * the product preparation summary during a product search, and the delivery preparation summary otherwise.
 */
public class SidePanel extends UiPart<Region> {

    private static final String FXML = "SidePanel.fxml";

    private final BooleanProperty showsHelp = new SimpleBooleanProperty(false);

    @FXML
    private StackPane helpView;
    @FXML
    private StackPane productView;
    @FXML
    private StackPane deliveryView;

    /**
     * Creates a side panel for the given views. The product summary shows itself during a product search,
     * so the panel shows it whenever it is visible and help is not shown.
     */
    public SidePanel(Region help, Region productSummary, Region deliverySummary) {
        super(FXML);
        requireAllNonNull(help, productSummary, deliverySummary);
        helpView.getChildren().add(help);
        productView.getChildren().add(productSummary);
        deliveryView.getChildren().add(deliverySummary);

        BooleanBinding showsProduct = showsHelp.not().and(productSummary.visibleProperty());
        BooleanBinding showsDelivery = showsHelp.not().and(productSummary.visibleProperty().not());
        show(helpView, showsHelp);
        show(productView, showsProduct);
        show(deliveryView, showsDelivery);
    }

    /**
     * Shows help in the panel until {@link #showSummary()} is called.
     */
    public void showHelp() {
        showsHelp.set(true);
    }

    /**
     * Shows the preparation summary that suits the current search.
     */
    public void showSummary() {
        showsHelp.set(false);
    }

    /**
     * Shows {@code view} only while {@code condition} holds. A hidden view is left out of the layout,
     * so its size does not affect the panel.
     */
    private static void show(StackPane view, BooleanExpression condition) {
        view.visibleProperty().bind(condition);
        view.managedProperty().bind(condition);
    }
}
