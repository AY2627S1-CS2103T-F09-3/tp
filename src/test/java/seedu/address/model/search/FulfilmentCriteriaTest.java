package seedu.address.model.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

public class FulfilmentCriteriaTest {
    private final FulfilmentDateCriterion date = new FulfilmentDateCriterion(new FulfilmentDate("2026-12-20"));
    private final FulfilmentMethodCriterion method = new FulfilmentMethodCriterion(FulfilmentMethod.DELIVERY);

    @Test
    public void test_sharedFields_matchWithOrWithoutItems() {
        PreorderBuilder preorder = new PreorderBuilder().withDate("2026-12-20")
                .withMethod(FulfilmentMethod.DELIVERY);
        for (Person person : List.of(new PersonBuilder().withPreorder(preorder.build()).build(),
                new PersonBuilder().withPreorder(preorder.withItems(new ItemBuilder().build()).build()).build())) {
            assertTrue(date.test(person));
            assertTrue(method.test(person));
        }
    }

    @Test
    public void test_missingOrDifferentFields_doNotMatch() {
        for (Person person : List.of(new PersonBuilder().build(), new PersonBuilder().withPreorder().build(),
                new PersonBuilder().withPreorder(new PreorderBuilder().withDate("2026-12-21")
                        .withMethod(FulfilmentMethod.COLLECTION).build()).build())) {
            assertFalse(date.test(person));
            assertFalse(method.test(person));
        }
    }

    @Test
    public void nullArguments_areRejected() {
        assertThrows(NullPointerException.class, () -> new FulfilmentDateCriterion(null));
        assertThrows(NullPointerException.class, () -> new FulfilmentMethodCriterion(null));
        assertThrows(NullPointerException.class, () -> date.test(null));
        assertThrows(NullPointerException.class, () -> method.test(null));
    }

    @Test
    public void equalityAndHashCode_useTheTypedField() {
        FulfilmentDateCriterion sameDate = new FulfilmentDateCriterion(new FulfilmentDate("2026-12-20"));
        FulfilmentMethodCriterion sameMethod = new FulfilmentMethodCriterion(FulfilmentMethod.DELIVERY);
        assertEquals(date, date);
        assertEquals(date, sameDate);
        assertEquals(date.hashCode(), sameDate.hashCode());
        assertFalse(date.equals(new FulfilmentDateCriterion(new FulfilmentDate("2026-12-21"))));
        assertFalse(date.equals(null));
        assertFalse(date.equals(method));
        assertEquals(method, method);
        assertEquals(method, sameMethod);
        assertEquals(method.hashCode(), sameMethod.hashCode());
        assertFalse(method.equals(new FulfilmentMethodCriterion(FulfilmentMethod.COLLECTION)));
        assertFalse(method.equals(null));
        assertFalse(method.equals(date));
        assertEquals(FulfilmentDateCriterion.class.getCanonicalName() + "{date=2026-12-20}", date.toString());
        assertEquals(FulfilmentMethodCriterion.class.getCanonicalName() + "{method=DELIVERY}", method.toString());
    }
}
