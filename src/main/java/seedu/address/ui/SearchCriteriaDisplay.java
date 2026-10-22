package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.address.model.search.SearchCriteria;

/**
 * Displays the active search as a heading and read-only badges above the customer list.
 * Badges wrap onto as many rows as needed, and long values wrap within a badge.
 */
public class SearchCriteriaDisplay extends UiPart<Region> {
    private static final String FXML = "SearchCriteriaDisplay.fxml";

    @FXML
    private Label criteriaHeading;

    @FXML
    private FlowPane criteriaBadges;

    /**
     * Creates a display of the current criteria and observes subsequent changes.
     * Updating the display does not request keyboard focus.
     *
     * @param criteria The current criteria and subsequent search updates.
     */
    public SearchCriteriaDisplay(ObservableValue<SearchCriteria> criteria) {
        super(FXML);
        requireNonNull(criteria);
        updateDisplay(criteria.getValue());
        criteria.addListener((observable, oldValue, newValue) -> updateDisplay(newValue));
    }

    private void updateDisplay(SearchCriteria criteria) {
        SearchCriteriaSummary summary = SearchCriteriaFormatter.format(criteria);
        criteriaHeading.setText(summary.heading());
        criteriaBadges.getChildren().setAll(summary.segments().stream().map(this::createBadge).toList());
        boolean hasBadges = !summary.segments().isEmpty();
        criteriaBadges.setVisible(hasBadges);
        criteriaBadges.setManaged(hasBadges);
    }

    private Label createBadge(String text) {
        Label badge = new Label(text) {
            @Override
            protected double computePrefHeight(double width) {
                // FlowPane measures row heights without a width; include wrapping in that measurement too.
                return super.computePrefHeight(width < 0 ? criteriaBadges.getWidth() : width);
            }
        };
        badge.getStyleClass().add("search-criterion-badge");
        badge.setWrapText(true);
        badge.setTextOverrun(OverrunStyle.CLIP);
        badge.setMinWidth(0);
        badge.setMinHeight(Region.USE_PREF_SIZE);
        // FlowPane wraps badges; this width limit also lets a single long badge wrap its own text.
        badge.maxWidthProperty().bind(criteriaBadges.widthProperty());
        badge.setFocusTraversable(false);
        return badge;
    }
}
