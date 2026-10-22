package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.PersonCriterion;
import seedu.address.model.search.PostalCodeCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;

/**
 * Formats structured search state into a heading and separate badge labels, independently of JavaFX and matching.
 * Add formatting and a display position here when introducing a new criterion type. Each criterion stays in one
 * segment so that future product and status conditions on the same item can be displayed together.
 */
public final class SearchCriteriaFormatter {
    public static final String FILTERS_HEADING = "Filters · Match all";
    public static final String ALL_CUSTOMERS_HEADING = "Showing: All customers";

    /**
     * Defines display order independently of the model's unordered criterion set.
     */
    private enum Position {
        NAME, PHONE, EMAIL, ADDRESS, POSTAL, TAG, ITEM_STATUS, HANDOVER_NOTES
    }

    /**
     * Associates one complete badge label with its position in the display.
     */
    private record Segment(Position position, String text) {}

    private SearchCriteriaFormatter() {}

    /**
     * Returns all criteria and their full values in a predictable order, without adding layout markup.
     * Criteria of the same kind are ordered by their complete labels. An empty search has no badge labels.
     *
     * @throws NullPointerException If {@code criteria} is null.
     * @throws IllegalArgumentException If a criterion type does not yet have a formatter.
     */
    public static SearchCriteriaSummary format(SearchCriteria criteria) {
        requireNonNull(criteria);
        List<String> segments = criteria.getCriteria().stream()
                .map(SearchCriteriaFormatter::formatCriterion)
                .sorted(Comparator.comparing(Segment::position).thenComparing(Segment::text))
                .map(Segment::text)
                .toList();
        String heading = segments.isEmpty() ? ALL_CUSTOMERS_HEADING : FILTERS_HEADING;
        return new SearchCriteriaSummary(heading, segments);
    }

    private static Segment formatCriterion(PersonCriterion criterion) {
        return switch (criterion) {
            case TextContainsCriterion text -> formatText(text);
            case PostalCodeCriterion postal -> new Segment(Position.POSTAL, "Postal: " + postal.getPostalCodePrefix());
            case TagCriterion tag -> new Segment(Position.TAG, "Tag: " + tag.getTag().tagName);
            case ItemCriterion status when status.getProductName().isEmpty() ->
                new Segment(Position.ITEM_STATUS, "Item status: " + formatStatuses(status.getStatuses()));
            default -> throw new IllegalArgumentException("Unsupported search criterion: "
                    + criterion.getClass().getName());
        };
    }

    private static Segment formatText(TextContainsCriterion criterion) {
        String query = criterion.getQuery();
        return switch (criterion.getField()) {
            case NAME -> new Segment(Position.NAME, "Name: " + query);
            case PHONE -> new Segment(Position.PHONE, "Phone: " + query);
            case EMAIL -> new Segment(Position.EMAIL, "Email: " + query);
            case ADDRESS -> new Segment(Position.ADDRESS, "Address: " + query);
            case HANDOVER_NOTES -> new Segment(Position.HANDOVER_NOTES, "Handover notes: " + query);
        };
    }

    private static String formatStatuses(Set<ItemStatus> statuses) {
        if (statuses.isEmpty()) {
            return "None";
        }
        return statuses.stream()
                .sorted()
                .map(SearchCriteriaFormatter::formatStatus)
                .collect(Collectors.joining(" or "));
    }

    private static String formatStatus(ItemStatus status) {
        return switch (status) {
            case PENDING -> "Pending";
            case PREPARED -> "Prepared";
            case COMPLETED -> "Completed";
        };
    }
}
