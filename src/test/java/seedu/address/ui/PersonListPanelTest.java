package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.GroupPostalCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Checks postal headings against the current display ordering. */
public class PersonListPanelTest {

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test has already started JavaFX.
        }
    }

    @Test
    public void ordinaryList_doesNotShowPostalHeadings() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            assertEquals(List.of(), headingsForFirstCell(panel, model));
        });
    }

    @Test
    public void groupedThenList_showsHeadingsOnlyWhileGrouped() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            new GroupPostalCommand().execute(model);
            assertEquals(List.of("POSTAL PREFIX 01"), headingsForFirstCell(panel, model));
            new ListCommand().execute(model);
            assertEquals(List.of(), headingsForFirstCell(panel, model));
        });
    }

    @Test
    public void groupedList_onlyFirstCustomerInEachPrefixHasHeading() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            model.addPerson(new PersonBuilder().withName("Same Prefix").withPostalCode("019999").build());
            model.addPerson(new PersonBuilder().withName("Missing Amy").build());
            model.addPerson(new PersonBuilder().withName("Missing Zoe").build());
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            new GroupPostalCommand().execute(model);

            assertEquals(List.of("POSTAL PREFIX 01"), headingsForCell(panel,
                    model.getFilteredPersonList().get(0), 0));
            assertEquals(List.of(), headingsForCell(panel, model.getFilteredPersonList().get(1), 1));
            assertEquals(List.of("POSTAL PREFIX 65"), headingsForCell(panel,
                    model.getFilteredPersonList().get(2), 2));
            assertEquals(List.of("MISSING POSTAL CODE"), headingsForCell(panel,
                    model.getFilteredPersonList().get(3), 3));
            assertEquals(List.of(), headingsForCell(panel, model.getFilteredPersonList().get(4), 4));
        });
    }

    @Test
    public void plainObservableList_doesNotShowPostalHeadings() throws Exception {
        runOnFxThread(() -> {
            Person first = new PersonBuilder().withName("First").withPostalCode("012345").build();
            Person second = new PersonBuilder().withName("Second").build();
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(first, second));
            assertEquals(List.of(), headingsForCell(panel, first, 0));
            assertEquals(List.of(), headingsForCell(panel, second, 1));
        });
    }

    @Test
    public void renderedList_comparatorAndCustomerChanges_refreshHeadings() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            new Scene(panel.getRoot(), 600, 900);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            assertEquals(List.of(), visibleHeadings(panel));

            // The list is already ordered, so grouping must refresh even without a permutation.
            new GroupPostalCommand().execute(model);
            panel.getRoot().layout();
            assertEquals(List.of("POSTAL PREFIX 01", "POSTAL PREFIX 65"), visibleHeadings(panel));

            Person first = model.getFilteredPersonList().getFirst();
            model.setPerson(first, new PersonBuilder(first).withPostalCode("659999").build());
            panel.getRoot().layout();
            assertEquals(List.of("POSTAL PREFIX 65"), visibleHeadings(panel));

            new ListCommand().execute(model);
            panel.getRoot().layout();
            assertEquals(List.of(), visibleHeadings(panel));
        });
    }

    @Test
    public void recycledCell_nullPersonOrOutOfBoundsIndex_clearsGraphicAndText() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            Person person = model.getFilteredPersonList().getFirst();
            PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();
            cell.updateIndex(0);
            cell.updateItem(person, false);
            cell.setText("Previous customer");
            cell.updateItem(null, false);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());

            cell.updateItem(person, false);
            cell.updateIndex(model.getFilteredPersonList().size());
            cell.setText("Previous customer");
            cell.updateItem(person, false);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }

    @Test
    public void recycledCell_invalidOrEmptyIndex_clearsGraphic() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();
            cell.updateIndex(-1);
            cell.updateItem(model.getFilteredPersonList().getFirst(), false);
            assertNull(cell.getGraphic());
            cell.updateIndex(0);
            cell.updateItem(model.getFilteredPersonList().getFirst(), false);
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
        });
    }

    private static ModelManager createModel() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Earlier").withPostalCode("012345").build());
        model.addPerson(new PersonBuilder().withName("Later").withPostalCode("654321").build());
        return model;
    }

    private static List<String> headingsForFirstCell(PersonListPanel panel, ModelManager model) {
        Person person = model.getFilteredPersonList().getFirst();
        return headingsForCell(panel, person, 0);
    }

    private static List<String> headingsForCell(PersonListPanel panel, Person person, int index) {
        PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();
        cell.updateIndex(index);
        cell.updateItem(person, false);
        return headings(cell);
    }

    private static List<String> visibleHeadings(PersonListPanel panel) {
        return panel.getRoot().lookupAll(".list-cell").stream().map(node -> (ListCell<?>) node)
                .filter(cell -> !cell.isEmpty()).sorted(Comparator.comparingInt(ListCell::getIndex))
                .flatMap(cell -> headings(cell).stream()).toList();
    }

    private static List<String> headings(ListCell<?> cell) {
        return ((VBox) cell.getGraphic()).getChildren().stream()
                .filter(Label.class::isInstance).map(Label.class::cast).map(Label::getText).toList();
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        CompletableFuture<Void> completion = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                completion.complete(null);
            } catch (Throwable e) {
                completion.completeExceptionally(e);
            }
        });
        completion.get(10, TimeUnit.SECONDS);
    }
}
