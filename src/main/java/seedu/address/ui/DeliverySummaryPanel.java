package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.GroupPostalCommand;
import seedu.address.model.DeliveryPreparationSummary;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.Preorder;

/** Shows outstanding deliveries from the current results and refreshes when those results change. */
public class DeliverySummaryPanel extends UiPart<Region> {
    private static final String FXML = "DeliverySummaryPanel.fxml";

    private final ObservableList<Person> persons;

    @FXML
    private Label totals;
    @FXML
    private VBox stops;

    /** Creates a summary that follows the current observable customer results. */
    public DeliverySummaryPanel(ObservableList<Person> persons) {
        super(FXML);
        this.persons = requireNonNull(persons);
        persons.addListener((ListChangeListener<Person>) change -> refresh());
        refresh();
    }

    private void refresh() {
        DeliveryPreparationSummary summary = new DeliveryPreparationSummary(persons);
        totals.setText(summary.getStopCount() + " outstanding customer stop(s) | "
                + summary.getPostalGroupCount() + " postal group(s)");
        stops.getChildren().clear();
        if (summary.getStops().isEmpty()) {
            stops.getChildren().add(label("No outstanding deliveries in the current results.", "delivery-stop"));
            return;
        }

        String previousGroup = null;
        for (Person person : summary.getStops()) {
            String group = GroupPostalCommand.getPostalGroupKey(person);
            if (!group.equals(previousGroup)) {
                stops.getChildren().add(label(person.getPostalCode() == null
                        ? "MISSING POSTAL CODE" : "POSTAL PREFIX " + group, "delivery-group"));
                previousGroup = group;
            }
            Preorder preorder = person.getPreorder().orElseThrow();
            String date = preorder.getDate().map(FulfilmentDate::toDisplayString).orElse("Not scheduled");
            String instructions = preorder.getHandoverNotes().map(notes -> notes.value).orElse("No instructions");
            stops.getChildren().add(label(person.getName().fullName + " | " + person.getPhone().value + "\n"
                    + person.getAddress().value + " | "
                    + (person.getPostalCode() == null ? "Postal code needed" : person.getPostalCode().value) + "\n"
                    + "Delivery | " + date + "\nInstructions: " + instructions, "delivery-stop"));
        }
    }

    private static Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
