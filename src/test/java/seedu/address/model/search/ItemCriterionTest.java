package seedu.address.model.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.preorder.ItemStatus.COMPLETED;
import static seedu.address.model.preorder.ItemStatus.PENDING;
import static seedu.address.model.preorder.ItemStatus.PREPARED;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

public class ItemCriterionTest {
    private static final Item PENDING_TARTS = new ItemBuilder().withName("Pineapple Tarts").build();
    private static final Item PREPARED_COOKIES = new ItemBuilder().withName("Almond Cookies")
            .withStatus(PREPARED).build();
    private static final Person MIXED_CUSTOMER = new PersonBuilder()
            .withPreorder(PENDING_TARTS, PREPARED_COOKIES).build();

    @Test
    public void getProductName_preservesCapitalizationAndStripsSurroundingWhitespace() {
        ItemCriterion criterion = productCriterion("  Pineapple TARTS  ", PENDING);
        assertEquals(Optional.of("Pineapple TARTS"), criterion.getProductName());
        assertTrue(criterion.matches(PENDING_TARTS));
        assertEquals(Optional.empty(), new ItemCriterion(Optional.empty(), Set.of(PENDING)).getProductName());
    }

    @Test
    public void constructor_invalidArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new ItemCriterion(null, Set.of(PENDING)));
        assertThrows(NullPointerException.class, () -> new ItemCriterion(Optional.empty(), null));
        Set<ItemStatus> statuses = new HashSet<>();
        statuses.add(null);
        assertThrows(NullPointerException.class, () -> new ItemCriterion(Optional.empty(), statuses));
        assertThrows(IllegalArgumentException.class, () -> productCriterion(" \t\n ", PENDING));
    }

    @Test
    public void constructor_sourceSetChanged_criteriaUnchanged() {
        Set<ItemStatus> statuses = new HashSet<>(Set.of(PENDING));
        ItemCriterion criterion = new ItemCriterion(Optional.empty(), statuses);
        statuses.clear();
        statuses.add(PREPARED);
        assertTrue(criterion.matches(PENDING_TARTS));
        assertFalse(criterion.matches(PREPARED_COOKIES));
    }

    @Test
    public void test_productAndStatusOnDifferentItems_returnsFalse() {
        assertFalse(productCriterion("Pineapple Tarts", PREPARED).test(MIXED_CUSTOMER));
        assertTrue(productCriterion("Pineapple Tarts", PENDING).test(MIXED_CUSTOMER));
    }

    @Test
    public void matches_productName_requiresCompleteName() {
        assertTrue(productCriterion("  PINEAPPLE TARTS  ", PENDING).matches(PENDING_TARTS));
        for (String product : List.of("Pineapple", "Tarts Pineapple", "PineappleTarts", "Pineapple-Tarts")) {
            assertFalse(productCriterion(product, PENDING).matches(PENDING_TARTS));
        }
    }

    @Test
    public void matches_repeatedSpacesInProductName_treatedAsOneSpace() {
        // Item names are saved with one space between words, so a search must collapse spaces too
        ItemCriterion criterion = productCriterion("Pineapple   Tarts", PENDING);
        assertEquals(Optional.of("Pineapple Tarts"), criterion.getProductName());
        assertTrue(criterion.matches(PENDING_TARTS));
    }

    @Test
    public void matches_productOnly_acceptsEveryStatus() {
        ItemCriterion criterion = productCriterion("Pineapple Tarts", ItemStatus.values());
        for (ItemStatus status : ItemStatus.values()) {
            assertTrue(criterion.matches(new ItemBuilder(PENDING_TARTS).withStatus(status).build()));
        }
    }

    @Test
    public void matches_statusOnly_acceptsAnyProductWithRequestedStatus() {
        ItemCriterion criterion = new ItemCriterion(Optional.empty(), Set.of(PENDING, PREPARED));
        assertTrue(criterion.matches(PENDING_TARTS));
        assertTrue(criterion.matches(PREPARED_COOKIES));
        assertFalse(criterion.matches(new ItemBuilder(PENDING_TARTS).withStatus(COMPLETED).build()));
        assertFalse(new ItemCriterion(Optional.empty(), Set.of()).test(MIXED_CUSTOMER));
    }

    @Test
    public void test_missingOrEmptyPreorder_returnsFalse() {
        ItemCriterion criterion = productCriterion("Pineapple Tarts", PENDING);
        assertFalse(criterion.test(new PersonBuilder().build()));
        assertFalse(criterion.test(new PersonBuilder().withPreorder().build()));
        assertEquals(List.of(), criterion.getMatchingItems(new PersonBuilder().build()));
    }

    @Test
    public void getMatchingItems_repeatedItems_preservesOrderAndDuplicates() {
        Person person = new PersonBuilder().withPreorder(PENDING_TARTS, PREPARED_COOKIES, PENDING_TARTS).build();
        List<Item> matches = productCriterion("Pineapple Tarts", PENDING).getMatchingItems(person);
        assertEquals(List.of(PENDING_TARTS, PENDING_TARTS), matches);
        assertThrows(UnsupportedOperationException.class, () -> matches.add(PREPARED_COOKIES));
    }

    @Test
    public void matches_turkishDefaultLocale_usesRootLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(productCriterion("PINEAPPLE TARTS", PENDING).matches(PENDING_TARTS));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void matching_nullInput_rejected() {
        ItemCriterion criterion = productCriterion("Pineapple Tarts", PENDING);
        assertThrows(NullPointerException.class, () -> criterion.matches(null));
        assertThrows(NullPointerException.class, () -> criterion.test(null));
        assertThrows(NullPointerException.class, () -> criterion.getMatchingItems(null));
    }

    @Test
    public void equals_normalizedConditions_haveValueSemantics() {
        ItemCriterion criterion = productCriterion("Pineapple Tarts", PENDING, PREPARED);
        ItemCriterion equivalent = productCriterion(" PINEAPPLE TARTS ", PREPARED, PENDING);
        assertEquals(criterion, criterion);
        assertEquals(criterion, equivalent);
        assertEquals(criterion.hashCode(), equivalent.hashCode());
        assertNotEquals(criterion, productCriterion("Pineapple Tarts", PENDING));
        assertNotEquals(criterion, productCriterion("Almond Cookies", PENDING, PREPARED));
        assertNotEquals(criterion, new ItemCriterion(Optional.empty(), Set.of(PENDING, PREPARED)));
        assertNotEquals(criterion, null);
        assertNotEquals(criterion, 1);
    }

    private ItemCriterion productCriterion(String product, ItemStatus... statuses) {
        return new ItemCriterion(Optional.of(product), Set.of(statuses));
    }
}
