package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.model.preorder.PreorderStatus.COMPLETED;
import static seedu.address.model.preorder.PreorderStatus.PENDING;
import static seedu.address.model.preorder.PreorderStatus.PREPARED;
import static seedu.address.model.search.TextContainsCriterion.Field.ADDRESS;
import static seedu.address.model.search.TextContainsCriterion.Field.EMAIL;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.person.PreorderStatusPredicate;
import seedu.address.model.preorder.PreorderStatus;
import seedu.address.model.search.PersonCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.tag.Tag;

public class SearchCriteriaFormatterTest {
    @Test
    public void format_nullCriteria_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> SearchCriteriaFormatter.format(null));
    }

    @Test
    public void format_noCriteria_showsAllCustomersWithoutBadges() {
        SearchCriteriaSummary summary = SearchCriteriaFormatter.format(new SearchCriteria(List.of()));

        assertEquals("Showing: All customers", summary.heading());
        assertEquals(List.of(), summary.segments());
    }

    @Test
    public void format_textCriterion_usesFieldLabelAndNormalizedValue() {
        assertSummary(List.of(new TextContainsCriterion(NAME, " ALICE ")), "Name: alice");
        assertSummary(List.of(new TextContainsCriterion(PHONE, " 8123 ")), "Phone: 8123");
        assertSummary(List.of(new TextContainsCriterion(EMAIL, " EXAMPLE.COM ")), "Email: example.com");
        assertSummary(List.of(new TextContainsCriterion(ADDRESS, " HOUGANG  Avenue, #04-08 ")),
                "Address: hougang  avenue, #04-08");
    }

    @Test
    public void format_tagCriterion_preservesCaseWithoutDiagnosticBrackets() {
        assertSummary(List.of(new TagCriterion(new Tag("VIP"))), "Tag: VIP");
    }

    @Test
    public void format_eachStatus_usesReadableStatusLabel() {
        assertSummary(List.of(new PreorderStatusPredicate(Set.of(PENDING))), "Item status: Pending");
        assertSummary(List.of(new PreorderStatusPredicate(Set.of(PREPARED))), "Item status: Prepared");
        assertSummary(List.of(new PreorderStatusPredicate(Set.of(COMPLETED))), "Item status: Completed");
    }

    @Test
    public void format_defaultTracking_keepsAlternativesInOneSegment() {
        Set<PreorderStatus> statuses = new LinkedHashSet<>(List.of(PREPARED, PENDING));
        assertSummary(List.of(new PreorderStatusPredicate(statuses)), "Item status: Pending or Prepared");
    }

    @Test
    public void format_allStatuses_ordersByProgress() {
        assertSummary(List.of(new PreorderStatusPredicate(Set.of(COMPLETED, PREPARED, PENDING))),
                "Item status: Pending or Prepared or Completed");
    }

    @Test
    public void format_emptyStatusSet_retainsActiveFilter() {
        assertSummary(List.of(new PreorderStatusPredicate(Set.of())), "Item status: None");
    }

    @Test
    public void format_combinedCriteria_usesStableFieldOrder() {
        List<PersonCriterion> criteria = new ArrayList<>(List.of(
                new PreorderStatusPredicate(Set.of(PREPARED, PENDING)),
                new TagCriterion(new Tag("regular")),
                new TextContainsCriterion(ADDRESS, "hougang"),
                new TextContainsCriterion(EMAIL, "example.com"),
                new TextContainsCriterion(PHONE, "8123"),
                new TextContainsCriterion(NAME, "alice")));
        String[] expected = {"Name: alice", "Phone: 8123", "Email: example.com", "Address: hougang", "Tag: regular",
            "Item status: Pending or Prepared"};

        assertSummary(criteria, expected);
        Collections.reverse(criteria);
        assertSummary(criteria, expected);
        Collections.rotate(criteria, 2);
        assertSummary(criteria, expected);
    }

    @Test
    public void format_sameFieldCriteria_ordersByFullLabel() {
        assertSummary(List.of(new TextContainsCriterion(NAME, "bob"), new TextContainsCriterion(NAME, "alice"),
                new TagCriterion(new Tag("vip")), new TagCriterion(new Tag("regular"))),
                "Name: alice", "Name: bob", "Tag: regular", "Tag: vip");
    }

    @Test
    public void format_duplicateCriteria_showsOneBadgePerDistinctCriterion() {
        assertSummary(List.of(new TextContainsCriterion(NAME, "alice"), new TextContainsCriterion(NAME, " ALICE ")),
                "Name: alice");
    }

    @Test
    public void format_longValuesAndManyCriteria_keepsEverySegmentAndFullValue() {
        String longAddress = "block 123, hougang avenue, #04-08, ".repeat(12) + "singapore";
        List<PersonCriterion> criteria = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            String name = String.format(Locale.ROOT, "customer %02d", i);
            criteria.add(new TextContainsCriterion(NAME, name));
            expected.add("Name: " + name);
        }
        criteria.add(new TextContainsCriterion(ADDRESS, longAddress));
        expected.add("Address: " + longAddress);

        assertSummary(criteria, expected.toArray(String[]::new));
    }

    @Test
    public void format_turkishDefaultLocale_keepsLocaleIndependentLabelsAndValues() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertSummary(List.of(new TextContainsCriterion(NAME, "ALICE"),
                    new PreorderStatusPredicate(Set.of(PENDING))), "Name: alice", "Item status: Pending");
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void format_statusCriterion_readsStructuredDataWithoutMatchingOrDiagnosticText() {
        PreorderStatusPredicate criterion = new PreorderStatusPredicate(Set.of(PENDING)) {
            @Override
            public boolean test(Person person) {
                throw new AssertionError("Formatting must not evaluate a search.");
            }

            @Override
            public String toString() {
                throw new AssertionError("Formatting must not parse diagnostic text.");
            }
        };

        assertSummary(List.of(criterion), "Item status: Pending");
    }

    @Test
    public void format_unsupportedCriterion_throwsInsteadOfHidingFilter() {
        PersonCriterion criterion = person -> true;
        assertThrows(IllegalArgumentException.class,
                "Unsupported search criterion: " + criterion.getClass().getName(), () ->
                        SearchCriteriaFormatter.format(new SearchCriteria(List.of(criterion))));
    }

    private void assertSummary(List<? extends PersonCriterion> criteria, String... segments) {
        SearchCriteriaSummary summary = SearchCriteriaFormatter.format(new SearchCriteria(criteria));

        assertEquals("Filters · Match all", summary.heading());
        assertEquals(List.of(segments), summary.segments());
    }
}
