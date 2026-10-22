package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.model.preorder.ItemStatus.COMPLETED;
import static seedu.address.model.preorder.ItemStatus.PENDING;
import static seedu.address.model.preorder.ItemStatus.PREPARED;
import static seedu.address.model.search.TextContainsCriterion.Field.ADDRESS;
import static seedu.address.model.search.TextContainsCriterion.Field.EMAIL;
import static seedu.address.model.search.TextContainsCriterion.Field.HANDOVER_NOTES;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.FulfilmentDateCriterion;
import seedu.address.model.search.FulfilmentMethodCriterion;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.PersonCriterion;
import seedu.address.model.search.PostalCodeCriterion;
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
        assertSummary(List.of(new TextContainsCriterion(HANDOVER_NOTES,
                " Call on arrival ")), "Handover notes: call on arrival");
    }

    @Test
    public void format_tagCriterion_preservesCaseWithoutDiagnosticBrackets() {
        assertSummary(List.of(new TagCriterion(new Tag("VIP"))), "Tag: VIP");
    }

    @Test
    public void format_postalCriterion_preservesLeadingZerosAndCompleteValue() {
        for (String prefix : List.of("0", "01", "53", "012345")) {
            assertSummary(List.of(new PostalCodeCriterion(prefix)), "Postal: " + prefix);
        }
    }

    @Test
    public void format_dateCriterion_usesReadableDateIncludingLeapDay() {
        assertSummary(List.of(new FulfilmentDateCriterion(new FulfilmentDate("2026-10-22"))), "Date: 22 Oct 2026");
        assertSummary(List.of(new FulfilmentDateCriterion(new FulfilmentDate("2028-02-29"))), "Date: 29 Feb 2028");
    }

    @Test
    public void format_methodCriterion_usesReadableMethodLabel() {
        assertSummary(List.of(new FulfilmentMethodCriterion(FulfilmentMethod.DELIVERY)), "Method: Delivery");
        assertSummary(List.of(new FulfilmentMethodCriterion(FulfilmentMethod.COLLECTION)), "Method: Collection");
    }

    @Test
    public void format_eachStatus_usesReadableStatusLabel() {
        assertSummary(List.of(new ItemCriterion(Optional.empty(), Set.of(PENDING))), "Item status: Pending");
        assertSummary(List.of(new ItemCriterion(Optional.empty(), Set.of(PREPARED))), "Item status: Prepared");
        assertSummary(List.of(new ItemCriterion(Optional.empty(), Set.of(COMPLETED))), "Item status: Completed");
    }

    @Test
    public void format_defaultTracking_keepsAlternativesInOneSegment() {
        Set<ItemStatus> statuses = new LinkedHashSet<>(List.of(PREPARED, PENDING));
        assertSummary(List.of(new ItemCriterion(Optional.empty(), statuses)), "Item status: Pending or Prepared");
    }

    @Test
    public void format_allStatuses_ordersByProgress() {
        assertSummary(List.of(new ItemCriterion(Optional.empty(), Set.of(COMPLETED, PREPARED, PENDING))),
                "Item status: Pending or Prepared or Completed");
    }

    @Test
    public void format_emptyStatusSet_retainsActiveFilter() {
        assertSummary(List.of(new ItemCriterion(Optional.empty(), Set.of())), "Item status: None");
    }

    @Test
    public void format_productAndStatus_keepsSameItemConditionsInOneSegment() {
        assertSummary(List.of(new ItemCriterion(Optional.of(" PINEAPPLE TARTS "), Set.of(PENDING))),
                "Item: pineapple tarts · Pending");
        assertSummary(List.of(new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of(PREPARED))),
                "Item: pineapple tarts · Prepared");
        assertSummary(List.of(new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of(COMPLETED))),
                "Item: pineapple tarts · Completed");
    }

    @Test
    public void format_productWithoutStatus_showsAnyStatusIncludingCompleted() {
        assertSummary(List.of(new ItemCriterion(Optional.of("Pineapple Tarts"), EnumSet.allOf(ItemStatus.class))),
                "Item: pineapple tarts · Any status");
    }

    @Test
    public void format_productWithStatusAlternatives_keepsOrderedAlternativesTogether() {
        assertSummary(List.of(new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of(PREPARED, PENDING))),
                "Item: pineapple tarts · Pending or Prepared");
    }

    @Test
    public void format_productWithEmptyStatusSet_retainsActiveFilter() {
        assertSummary(List.of(new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of())),
                "Item: pineapple tarts · None");
    }

    @Test
    public void format_productName_preservesSignificantSpacingAndPunctuation() {
        assertSummary(List.of(new ItemCriterion(Optional.of(" Tarts  (Large)/Box "), Set.of(PENDING))),
                "Item: tarts  (large)/box · Pending");
    }

    @Test
    public void format_combinedCriteria_usesStableFieldOrder() {
        List<PersonCriterion> criteria = new ArrayList<>(List.of(
                new TextContainsCriterion(HANDOVER_NOTES, "call on arrival"),
                new FulfilmentMethodCriterion(FulfilmentMethod.DELIVERY),
                new FulfilmentDateCriterion(new FulfilmentDate("2026-10-22")),
                new ItemCriterion(Optional.empty(), Set.of(PREPARED, PENDING)),
                new TagCriterion(new Tag("regular")),
                new PostalCodeCriterion("01"),
                new TextContainsCriterion(ADDRESS, "hougang"),
                new TextContainsCriterion(EMAIL, "example.com"),
                new TextContainsCriterion(PHONE, "8123"),
                new TextContainsCriterion(NAME, "alice")));
        String[] expected = {"Name: alice", "Phone: 8123", "Email: example.com", "Address: hougang", "Postal: 01",
            "Tag: regular", "Item status: Pending or Prepared", "Date: 22 Oct 2026", "Method: Delivery",
            "Handover notes: call on arrival"};

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
    public void format_productWithCustomerCriteria_showsOneItemBadgeAfterCustomerBadges() {
        List<PersonCriterion> criteria = new ArrayList<>(List.of(
                new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of(PENDING)),
                new TagCriterion(new Tag("regular")),
                new PostalCodeCriterion("01"),
                new TextContainsCriterion(NAME, "Alice")));
        String[] expected = {"Name: alice", "Postal: 01", "Tag: regular", "Item: pineapple tarts · Pending"};

        assertSummary(criteria, expected);
        Collections.reverse(criteria);
        assertSummary(criteria, expected);
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
                    new ItemCriterion(Optional.empty(), Set.of(PENDING))), "Name: alice", "Item status: Pending");
            assertSummary(List.of(new ItemCriterion(Optional.of("PINEAPPLE TARTS"), Set.of(PENDING))),
                    "Item: pineapple tarts · Pending");
            assertSummary(List.of(new FulfilmentMethodCriterion(FulfilmentMethod.DELIVERY),
                    new FulfilmentDateCriterion(new FulfilmentDate("2026-10-22"))),
                    "Date: 22 Oct 2026", "Method: Delivery");
        } finally {
            Locale.setDefault(originalLocale);
        }
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
