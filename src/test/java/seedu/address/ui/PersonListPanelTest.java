package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
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
        PersonListPanel.PersonListViewCell cell = panel.new PersonListViewCell();
        cell.updateIndex(0);
        cell.updateItem(person, false);
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
