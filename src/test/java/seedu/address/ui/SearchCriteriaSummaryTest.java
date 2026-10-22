package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SearchCriteriaSummaryTest {
    @Test
    public void constructor_sourceListModified_summaryUnchanged() {
        List<String> segments = new ArrayList<>(List.of("Name: alice", "Tag: regular"));
        SearchCriteriaSummary summary = new SearchCriteriaSummary("Filters · Match all", segments);

        segments.clear();
        segments.add("Name: bob");

        assertEquals("Filters · Match all", summary.heading());
        assertEquals(List.of("Name: alice", "Tag: regular"), summary.segments());
    }

    @Test
    public void segments_returnsImmutableList() {
        SearchCriteriaSummary summary = new SearchCriteriaSummary("Filters · Match all", List.of("Name: alice"));

        assertThrows(UnsupportedOperationException.class, () -> summary.segments().clear());
        assertThrows(UnsupportedOperationException.class, () -> summary.segments().add("Tag: regular"));
        assertEquals(List.of("Name: alice"), summary.segments());
    }

    @Test
    public void constructor_nullArgumentOrSegment_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchCriteriaSummary(null, List.of()));
        assertThrows(NullPointerException.class, () -> new SearchCriteriaSummary("Filters · Match all", null));
        List<String> segments = new ArrayList<>();
        segments.add(null);
        assertThrows(NullPointerException.class, () -> new SearchCriteriaSummary("Filters · Match all", segments));
    }
}
