package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.preorder.ItemStatus.COMPLETED;
import static seedu.address.model.preorder.ItemStatus.PENDING;
import static seedu.address.model.preorder.ItemStatus.PREPARED;
import static seedu.address.model.search.TextContainsCriterion.Field.ADDRESS;
import static seedu.address.model.search.TextContainsCriterion.Field.EMAIL;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.PersonCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks live criteria updates and wrapping on the JavaFX thread. Linux CI supplies a display using xvfb.
 */
public class SearchCriteriaDisplayTest {
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX was already started by an earlier test class.
        }
    }

    @Test
    public void constructor_existingCriteria_showsHeadingAndSeparateReadOnlyBadges() throws Exception {
        runOnFxThread(() -> {
            SearchCriteria criteria = new SearchCriteria(List.of(new TagCriterion(new Tag("regular")),
                    new TextContainsCriterion(NAME, "Alice")));
            SearchCriteriaDisplay display = new SearchCriteriaDisplay(new ReadOnlyObjectWrapper<>(criteria));

            assertDisplay(display.getRoot(), "Filters · Match all", "Name: alice", "Tag: regular");
            assertTrue(display.getRoot().isMouseTransparent());
            for (Label badge : getBadges(display.getRoot())) {
                assertFalse(badge.isFocusTraversable());
                assertNull(badge.getTooltip());
            }
        });
    }

    @Test
    public void criteriaChanged_identicalEmptyResults_updatesBadges() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            SearchCriteriaDisplay display = new SearchCriteriaDisplay(model.activeSearchCriteriaProperty());
            assertDisplay(display.getRoot(), "Showing: All customers");
            assertFalse(getBadgePane(display.getRoot()).isVisible());
            assertFalse(getBadgePane(display.getRoot()).isManaged());

            model.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "alice"))));
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertDisplay(display.getRoot(), "Filters · Match all", "Name: alice");

            model.updateFilteredPersonList(new SearchCriteria(List.of(
                    new TextContainsCriterion(EMAIL, "example.com"))));
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertDisplay(display.getRoot(), "Filters · Match all", "Email: example.com");
            assertTrue(getBadgePane(display.getRoot()).isVisible());
            assertTrue(getBadgePane(display.getRoot()).isManaged());
        });
    }

    @Test
    public void layout_manyCriteria_wrapsEveryBadgeBeyondTwoRowsAndReflowsOnResize() throws Exception {
        runOnFxThread(() -> {
            List<PersonCriterion> criteria = new ArrayList<>();
            List<String> expected = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                String name = String.format(Locale.ROOT, "customer %02d", i);
                criteria.add(new TextContainsCriterion(NAME, name));
                expected.add("Name: " + name);
            }
            SearchCriteriaDisplay display = new SearchCriteriaDisplay(
                    new ReadOnlyObjectWrapper<>(new SearchCriteria(criteria)));
            Region root = display.getRoot();
            attachToScene(root);

            layoutAtWidth(root, 250);
            assertDisplay(root, "Filters · Match all", expected.toArray(String[]::new));
            long narrowRows = countRows(root);
            assertTrue(narrowRows > 2);
            assertBadgesFit(root);

            layoutAtWidth(root, 720);
            assertTrue(countRows(root) < narrowRows);
            assertDisplay(root, "Filters · Match all", expected.toArray(String[]::new));
            assertBadgesFit(root);
        });
    }

    @Test
    public void layout_longSpacedAndUnbrokenValues_wrapsFullTextInsideBadges() throws Exception {
        runOnFxThread(() -> {
            String address = "block 123 hougang avenue #04-08 ".repeat(6) + "singapore";
            String email = "longemailfragment".repeat(12) + "@example.com";
            SearchCriteriaDisplay display = new SearchCriteriaDisplay(new ReadOnlyObjectWrapper<>(
                    new SearchCriteria(List.of(new TextContainsCriterion(ADDRESS, address),
                            new TextContainsCriterion(EMAIL, email)))));
            Region root = display.getRoot();
            attachToScene(root);

            layoutAtWidth(root, 250);
            assertDisplay(root, "Filters · Match all", "Email: " + email, "Address: " + address);
            assertBadgesFit(root);
            for (Label badge : getBadges(root)) {
                Text renderedText = (Text) badge.lookup(".text");
                assertEquals(badge.getText(), renderedText.getText());
                assertTrue(badge.getHeight() > 40, "Long text should occupy several lines.");
                assertTrue(badge.getHeight() + 1 >= badge.prefHeight(badge.getWidth()));
            }

            double narrowHeight = root.getHeight();
            layoutAtWidth(root, 720);
            assertTrue(root.getHeight() < narrowHeight);
            assertBadgesFit(root);
            for (Label badge : getBadges(root)) {
                assertEquals(badge.getText(), ((Text) badge.lookup(".text")).getText());
            }
        });
    }

    @Test
    public void mainWindow_commandsAndUpdates_refreshDisplayAndKeepCommandBoxFocus() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder(ALICE).withPreorder(new ItemBuilder().build()).build());
            model.addPerson(BENSON);
            Path dataPath = temporaryFolder.resolve("addressbook.json");
            Logic logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, dataPath);
            window.fillInnerParts();
            Parent root = stage.getScene().getRoot();
            TextField commandBox = (TextField) root.lookup("#commandTextField");
            commandBox.requestFocus();

            assertDisplay(root, "Showing: All customers");
            enterCommand(commandBox, "find n/Alice");
            assertDisplay(root, "Filters · Match all", "Name: alice");
            enterCommand(commandBox, "edit 1 n/Other Customer");
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertDisplay(root, "Filters · Match all", "Name: alice");

            enterCommand(commandBox, "find n/");
            assertTrue(commandBox.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertDisplay(root, "Filters · Match all", "Name: alice");

            enterCommand(commandBox, "track");
            assertEquals(1, model.getFilteredPersonList().size());
            assertDisplay(root, "Filters · Match all", "Item status: Pending or Prepared");
            enterCommand(commandBox, "track s/PENDING");
            assertEquals(1, model.getFilteredPersonList().size());
            assertDisplay(root, "Filters · Match all", "Item status: Pending");
            enterCommand(commandBox, "deleteorder 1");
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertDisplay(root, "Filters · Match all", "Item status: Pending");

            enterCommand(commandBox, "list");
            assertEquals(2, model.getFilteredPersonList().size());
            assertDisplay(root, "Showing: All customers");
            assertFalse(getBadgePane(root).isManaged());
        });
    }

    @Test
    public void mainWindow_postalSearches_updateFullBadgeAndKeepCommandBoxFocus() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person alice = new PersonBuilder(ALICE).withPostalCode("012345").build();
            Person benson = new PersonBuilder(BENSON).withPostalCode("530123").build();
            model.addPerson(alice);
            model.addPerson(benson);
            Path dataPath = temporaryFolder.resolve("postal-addressbook.json");
            Logic logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("postal-preferences.json"))));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, dataPath);
                window.fillInnerParts();
                Parent root = stage.getScene().getRoot();
                TextField commandBox = (TextField) root.lookup("#commandTextField");
                commandBox.requestFocus();

                enterCommand(commandBox, "find c/01");
                assertEquals(List.of(alice), model.getFilteredPersonList());
                assertDisplay(root, "Filters · Match all", "Postal: 01");

                enterCommand(commandBox, "find c/012345");
                assertEquals(List.of(alice), model.getFilteredPersonList());
                assertDisplay(root, "Filters · Match all", "Postal: 012345");

                enterCommand(commandBox, "find c/99");
                assertTrue(model.getFilteredPersonList().isEmpty());
                assertDisplay(root, "Filters · Match all", "Postal: 99");
                enterCommand(commandBox, "find c/98");
                assertTrue(model.getFilteredPersonList().isEmpty());
                assertDisplay(root, "Filters · Match all", "Postal: 98");

                enterCommand(commandBox, "find c/1234567");
                assertTrue(commandBox.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertDisplay(root, "Filters · Match all", "Postal: 98");

                enterCommand(commandBox, "find c/53 n/Benson");
                assertEquals(List.of(benson), model.getFilteredPersonList());
                assertDisplay(root, "Filters · Match all", "Name: benson", "Postal: 53");
                enterCommand(commandBox, "edit 1 c/012346");
                assertTrue(model.getFilteredPersonList().isEmpty());
                assertDisplay(root, "Filters · Match all", "Name: benson", "Postal: 53");

                enterCommand(commandBox, "list");
                assertEquals(2, model.getFilteredPersonList().size());
                assertDisplay(root, "Showing: All customers");
                assertFalse(getBadgePane(root).isManaged());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void mainWindow_productAndStatusSearches_showSameItemConditionsAndKeepCommandBoxFocus() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Item pendingTarts = new ItemBuilder().withName("Pineapple Tarts").withStatus(PENDING).build();
            Item preparedTarts = new ItemBuilder(pendingTarts).withStatus(PREPARED).build();
            Item completedTarts = new ItemBuilder(pendingTarts).withStatus(COMPLETED).build();
            Item preparedCookies = new ItemBuilder().withName("Almond Cookies").withStatus(PREPARED).build();
            Person alice = new PersonBuilder(ALICE).withPostalCode("012345").withTags("regular")
                    .withPreorder(pendingTarts, preparedCookies).build();
            Person benson = new PersonBuilder(BENSON).withPreorder(preparedTarts).build();
            Person carl = new PersonBuilder().withName("Carl").withPhone("83456789")
                    .withPreorder(completedTarts).build();
            model.addPerson(alice);
            model.addPerson(benson);
            model.addPerson(carl);
            Path dataPath = temporaryFolder.resolve("item-addressbook.json");
            Logic logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("item-preferences.json"))));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, dataPath);
                window.fillInnerParts();
                Parent root = stage.getScene().getRoot();
                TextField commandBox = (TextField) root.lookup("#commandTextField");
                commandBox.requestFocus();

                enterCommand(commandBox, "find d/PINEAPPLE TARTS");
                assertEquals(List.of(alice, benson, carl), model.getFilteredPersonList());
                assertDisplay(root, "Filters · Match all", "Item: pineapple tarts · Any status");
                enterCommand(commandBox, "find d/Pineapple Tarts s/pending");
                assertEquals(List.of(alice), model.getFilteredPersonList());
                assertDisplay(root, "Filters · Match all", "Item: pineapple tarts · Pending");

                enterCommand(commandBox, "find n/Alice d/Pineapple Tarts s/prepared");
                assertTrue(model.getFilteredPersonList().isEmpty());
                assertDisplay(root, "Filters · Match all", "Name: alice", "Item: pineapple tarts · Prepared");
                enterCommand(commandBox, "find d/Pineapple Tarts s/prepared");
                assertEquals(List.of(benson), model.getFilteredPersonList());
                assertDisplay(root, "Filters · Match all", "Item: pineapple tarts · Prepared");

                enterCommand(commandBox, "edititem 1 i/1 s/completed");
                assertTrue(model.getFilteredPersonList().isEmpty());
                assertDisplay(root, "Filters · Match all", "Item: pineapple tarts · Prepared");

                enterCommand(commandBox, "find s/completed");
                assertEquals(2, model.getFilteredPersonList().size());
                assertDisplay(root, "Filters · Match all", "Item status: Completed");
                enterCommand(commandBox, "track s/COMPLETED");
                assertEquals(2, model.getFilteredPersonList().size());
                assertDisplay(root, "Filters · Match all", "Item status: Completed");
                enterCommand(commandBox, "track");
                assertEquals(List.of(alice), model.getFilteredPersonList());
                assertDisplay(root, "Filters · Match all", "Item status: Pending or Prepared");

                enterCommand(commandBox, "find c/01 t/regular d/Pineapple Tarts s/pending n/Alice");
                assertEquals(List.of(alice), model.getFilteredPersonList());
                assertDisplay(root, "Filters · Match all", "Name: alice", "Postal: 01", "Tag: regular",
                        "Item: pineapple tarts · Pending");
                enterCommand(commandBox, "find s/unknown");
                assertTrue(commandBox.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertDisplay(root, "Filters · Match all", "Name: alice", "Postal: 01", "Tag: regular",
                        "Item: pineapple tarts · Pending");

                enterCommand(commandBox, "list");
                assertEquals(3, model.getFilteredPersonList().size());
                assertDisplay(root, "Showing: All customers");
                assertFalse(getBadgePane(root).isManaged());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void layout_longProductAndStatus_wrapsFullValueInOneBadge() throws Exception {
        runOnFxThread(() -> {
            String product = "pineapple".repeat(24);
            SearchCriteriaDisplay display = new SearchCriteriaDisplay(new ReadOnlyObjectWrapper<>(
                    new SearchCriteria(List.of(new ItemCriterion(Optional.of(product), Set.of(PENDING))))));
            Region root = display.getRoot();
            attachToScene(root);

            layoutAtWidth(root, 250);
            assertDisplay(root, "Filters · Match all", "Item: " + product + " · Pending");
            assertBadgesFit(root);
            Label badge = getBadges(root).getFirst();
            assertEquals(badge.getText(), ((Text) badge.lookup(".text")).getText());
            assertTrue(badge.getHeight() > 40);

            double narrowHeight = root.getHeight();
            layoutAtWidth(root, 720);
            assertTrue(root.getHeight() < narrowHeight);
            assertBadgesFit(root);
            assertEquals(badge.getText(), ((Text) badge.lookup(".text")).getText());
        });
    }

    private static void enterCommand(TextField commandBox, String text) {
        commandBox.setText(text);
        commandBox.fireEvent(new ActionEvent());
        assertSame(commandBox, commandBox.getScene().getFocusOwner());
    }

    private static void attachToScene(Region root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(SearchCriteriaDisplayTest.class.getResource("/view/DarkTheme.css").toExternalForm());
        root.applyCss();
    }

    private static void layoutAtWidth(Region root, double width) {
        root.resize(width, 1000);
        root.layout();
        root.resize(width, root.prefHeight(width));
        root.layout();
    }

    private static void assertBadgesFit(Parent root) {
        FlowPane pane = getBadgePane(root);
        for (Label badge : getBadges(root)) {
            assertTrue(badge.isVisible());
            assertTrue(badge.isManaged());
            assertTrue(badge.getBoundsInParent().getMaxX() <= pane.getWidth() + 1);
            assertTrue(badge.getBoundsInParent().getMaxY() <= pane.getHeight() + 1,
                    "Badge bounds " + badge.getBoundsInParent() + " exceed pane height " + pane.getHeight()
                            + "; preferred badge height " + badge.prefHeight(badge.getWidth()));
        }
    }

    private static long countRows(Parent root) {
        return getBadges(root).stream().map(Label::getLayoutY).distinct().count();
    }

    private static void assertDisplay(Parent root, String heading, String... segments) {
        assertEquals(heading, ((Label) root.lookup("#criteriaHeading")).getText());
        assertEquals(List.of(segments), getBadges(root).stream().map(Label::getText).toList());
    }

    private static FlowPane getBadgePane(Parent root) {
        return (FlowPane) root.lookup("#criteriaBadges");
    }

    private static List<Label> getBadges(Parent root) {
        return getBadgePane(root).getChildren().stream().map(node -> (Label) node).toList();
    }

    private static void runOnFxThread(Executable test) throws Exception {
        CompletableFuture<Void> execution = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                test.execute();
                execution.complete(null);
            } catch (Throwable throwable) {
                execution.completeExceptionally(throwable);
            }
        });
        execution.get(10, TimeUnit.SECONDS);
    }
}
