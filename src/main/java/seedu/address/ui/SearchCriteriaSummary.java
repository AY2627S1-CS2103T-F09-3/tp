package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;

/**
 * Holds an immutable heading and the complete text of each read-only filter badge.
 * Layout, wrapping and keyboard focus are managed by the UI that renders this summary.
 *
 * @param heading The text above the badges.
 * @param segments One complete badge label per criterion, in display order.
 */
public record SearchCriteriaSummary(String heading, List<String> segments) {
    /**
     * Creates a summary using an immutable copy of {@code segments}.
     *
     * @throws NullPointerException If either argument or any segment is null.
     */
    public SearchCriteriaSummary {
        requireNonNull(heading);
        segments = List.copyOf(segments);
    }
}
