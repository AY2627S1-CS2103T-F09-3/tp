package seedu.address.model.summary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.preorder.ItemStatus.PENDING;
import static seedu.address.model.preorder.ItemStatus.PREPARED;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.summary.ProductPreparationSummary.MatchingItem;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

public class ProductPreparationSummaryTest {
    private static final Item TARTS = new ItemBuilder().withName("Pineapple Tarts").withQuantity("2")
            .withRemark("Less sugar").build();
    private static final Item COOKIES = new ItemBuilder().withName("Almond Cookies").withQuantity("99").build();
    private static final Item PREPARED_TARTS = new ItemBuilder(TARTS).withStatus(PREPARED).withQuantity("3").build();
    private static final Item BEN_TARTS = new ItemBuilder(TARTS).withQuantity("5").build();
    private static final Person ALICE = new PersonBuilder().withName("Alice")
            .withPreorder(COOKIES, TARTS, TARTS, PREPARED_TARTS).build();
    private static final Person BEN = new PersonBuilder().withName("Ben").withPreorder(BEN_TARTS).build();
    private static final ItemCriterion ALL_TARTS =
            new ItemCriterion(Optional.of("PINEAPPLE TARTS"), EnumSet.allOf(ItemStatus.class));

    @Test
    public void from_mixedProducts_countsOnlySelectedProduct() {
        ProductPreparationSummary summary = ProductPreparationSummary.from(List.of(ALICE, BEN), ALL_TARTS);
        assertCounts(summary, 2, 4, 12);
    }

    @Test
    public void from_statusRestricted_countsOnlyMatchingItems() {
        ItemCriterion pending = new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of(PENDING));
        assertCounts(ProductPreparationSummary.from(List.of(ALICE, BEN), pending), 2, 3, 9);
        ItemCriterion prepared = new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of(PREPARED));
        assertCounts(ProductPreparationSummary.from(List.of(ALICE, BEN), prepared), 1, 1, 3);
    }

    @Test
    public void getMatchingItems_retainsCustomerItemNumbersAndRemarks() {
        ProductPreparationSummary summary = ProductPreparationSummary.from(List.of(ALICE, BEN), ALL_TARTS);
        List<MatchingItem> matches = summary.getMatchingItems();
        assertEquals(List.of(2, 3, 4, 1), matches.stream().map(MatchingItem::getItemNumber).toList());
        assertEquals(List.of(ALICE, ALICE, ALICE, BEN), matches.stream().map(MatchingItem::getCustomer).toList());
        assertEquals("Less sugar", matches.getFirst().getItem().getRemark().orElseThrow().value);
        assertEquals(List.of(TARTS, TARTS, PREPARED_TARTS, BEN_TARTS),
                matches.stream().map(MatchingItem::getItem).toList());
    }

    @Test
    public void from_displayedSubset_excludesHiddenCustomers() {
        assertCounts(ProductPreparationSummary.from(List.of(BEN), ALL_TARTS), 1, 1, 5);
        assertCounts(ProductPreparationSummary.from(List.of(ALICE, ALICE), ALL_TARTS), 1, 3, 7);
    }

    @Test
    public void from_noMatchingItems_returnsZeroCounts() {
        Person noPreorder = new PersonBuilder().withName("No preorder").build();
        Person emptyPreorder = new PersonBuilder().withName("Empty preorder").withPreorder().build();
        Person otherProduct = new PersonBuilder().withName("Other product").withPreorder(COOKIES).build();
        assertCounts(ProductPreparationSummary.from(List.of(noPreorder, emptyPreorder, otherProduct), ALL_TARTS),
                0, 0, 0);
        assertCounts(ProductPreparationSummary.from(List.of(), ALL_TARTS), 0, 0, 0);
        ItemCriterion noStatuses = new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of());
        assertCounts(ProductPreparationSummary.from(List.of(ALICE), noStatuses), 0, 0, 0);
    }

    @Test
    public void from_sourceChanged_snapshotUnchangedAndResultsUnmodifiable() {
        List<Person> source = new ArrayList<>(List.of(ALICE));
        ProductPreparationSummary summary = ProductPreparationSummary.from(source, ALL_TARTS);
        source.clear();
        source.add(BEN);
        assertCounts(summary, 1, 3, 7);
        assertThrows(UnsupportedOperationException.class, () -> summary.getMatchingItems().clear());
    }

    @Test
    public void from_noRemarks_stillCountsItem() {
        Item item = new ItemBuilder().withName("Pineapple Tarts").build();
        Person person = new PersonBuilder().withPreorder(item).build();
        ProductPreparationSummary summary = ProductPreparationSummary.from(List.of(person), ALL_TARTS);
        assertCounts(summary, 1, 1, 2);
        assertEquals(Optional.empty(), summary.getMatchingItems().getFirst().getItem().getRemark());
    }

    @Test
    public void from_invalidArguments_rejected() {
        assertThrows(NullPointerException.class, () -> ProductPreparationSummary.from(null, ALL_TARTS));
        assertThrows(NullPointerException.class, () -> ProductPreparationSummary.from(List.of(ALICE), null));
        assertThrows(NullPointerException.class, () ->
                ProductPreparationSummary.from(Arrays.asList(ALICE, null), ALL_TARTS));
        ItemCriterion statusOnly = new ItemCriterion(Optional.empty(), Set.of(PENDING));
        assertThrows(IllegalArgumentException.class, () ->
                ProductPreparationSummary.from(List.of(ALICE), statusOnly));
    }

    @Test
    public void matchingItem_invalidReference_rejected() {
        assertThrows(NullPointerException.class, () -> new MatchingItem(null, 1, TARTS));
        assertThrows(NullPointerException.class, () -> new MatchingItem(ALICE, 1, null));
        assertThrows(IllegalArgumentException.class, () -> new MatchingItem(ALICE, 0, TARTS));
        assertThrows(IllegalArgumentException.class, () -> new MatchingItem(ALICE, 5, TARTS));
        assertThrows(IllegalArgumentException.class, () -> new MatchingItem(ALICE, 1, TARTS));
        assertThrows(IllegalArgumentException.class, () -> new MatchingItem(new PersonBuilder().build(), 1, TARTS));
    }

    @Test
    public void matchingItem_equals_comparesCustomerItemAndNumber() {
        MatchingItem match = new MatchingItem(ALICE, 2, TARTS);
        MatchingItem equivalent = new MatchingItem(ALICE, 2, new ItemBuilder(TARTS).build());
        assertEquals(match, match);
        assertEquals(match, equivalent);
        assertEquals(match.hashCode(), equivalent.hashCode());
        assertEquals(match.toString(), equivalent.toString());
        assertTrue(match.toString().contains("itemNumber=2"));
        assertNotEquals(match, new MatchingItem(ALICE, 3, TARTS));
        Person otherCustomer = new PersonBuilder(ALICE).withName("Other customer").build();
        assertNotEquals(match, new MatchingItem(otherCustomer, 2, TARTS));
        assertNotEquals(match, null);
        assertNotEquals(match, 1);
    }

    private void assertCounts(ProductPreparationSummary summary, long customers, int items,
            long totalQuantity) {
        assertEquals(customers, summary.getCustomerCount());
        assertEquals(items, summary.getMatchingItemCount());
        assertEquals(totalQuantity, summary.getTotalQuantity());
    }
}
