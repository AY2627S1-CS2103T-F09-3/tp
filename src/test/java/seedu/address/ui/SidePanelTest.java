package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Checks which view the side panel shows.
 * Starting JavaFX needs a display; on Linux CI runners, one is provided by xvfb (see gradle.yml).
 */
public class SidePanelTest {

    private final Region help = new Region();
    private final Region productSummary = new Region();
    private final Region deliverySummary = new Region();

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX was already started by an earlier test class
        }
    }

    @Test
    public void constructor_noProductSearch_showsOnlyDeliverySummary() throws Exception {
        productSummary.setVisible(false);
        SidePanel panel = createPanel();

        assertShownViews(panel, deliverySummary);
    }

    @Test
    public void productSummaryVisibility_changes_switchesBetweenSummaries() throws Exception {
        productSummary.setVisible(false);
        SidePanel panel = createPanel();

        onFxThread(() -> {
            productSummary.setVisible(true);
            return null;
        });
        assertShownViews(panel, productSummary);

        onFxThread(() -> {
            productSummary.setVisible(false);
            return null;
        });
        assertShownViews(panel, deliverySummary);
    }

    @Test
    public void showHelp_thenShowSummary_returnsToCurrentSummary() throws Exception {
        productSummary.setVisible(true);
        SidePanel panel = createPanel();

        onFxThread(() -> {
            panel.showHelp();
            return null;
        });
        assertShownViews(panel, help);

        onFxThread(() -> {
            panel.showSummary();
            return null;
        });
        assertShownViews(panel, productSummary);
    }

    @Test
    public void constructor_nullView_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SidePanel(null, productSummary, deliverySummary));
    }

    private SidePanel createPanel() throws Exception {
        return onFxThread(() -> new SidePanel(help, productSummary, deliverySummary));
    }

    /**
     * Checks that {@code expected} is the only one of the three views whose container is shown and laid out.
     */
    private void assertShownViews(SidePanel panel, Region expected) {
        List<Node> containers = ((StackPane) panel.getRoot()).getChildren();
        assertEquals(3, containers.size());
        for (Region view : List.of(help, productSummary, deliverySummary)) {
            Node container = view.getParent();
            assertTrue(containers.contains(container));
            assertEquals(view == expected, container.isVisible());
            assertEquals(view == expected, container.isManaged());
        }
        assertFalse(containers.stream().filter(Node::isVisible).count() > 1);
    }

    private static <T> T onFxThread(Callable<T> task) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(task.call());
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result.get(10, TimeUnit.SECONDS);
    }
}
