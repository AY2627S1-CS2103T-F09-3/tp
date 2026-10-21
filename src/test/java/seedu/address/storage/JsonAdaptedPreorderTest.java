package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.Preorder;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PreorderBuilder;

public class JsonAdaptedPreorderTest {

    private static final String VALID_DATE = "2026-12-20";
    private static final String VALID_METHOD = "DELIVERY";
    private static final String VALID_HANDOVER_NOTES = "Call on arrival";

    @Test
    public void toModelType_allFulfilmentDetails_returnsPreorder() throws Exception {
        Preorder preorder = new PreorderBuilder().withItems(new ItemBuilder().build())
                .withDate(VALID_DATE).withMethod(FulfilmentMethod.DELIVERY)
                .withHandoverNotes(VALID_HANDOVER_NOTES).build();
        assertEquals(preorder, new JsonAdaptedPreorder(preorder).toModelType());
    }

    @Test
    public void toModelType_nullFulfilmentDetails_returnsPreorderWithoutDetails() throws Exception {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(List.of(), null, null, null);
        assertEquals(new Preorder(List.of()), preorder.toModelType());
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(List.of(), "2026-02-30", VALID_METHOD,
                VALID_HANDOVER_NOTES);
        assertThrows(IllegalValueException.class, FulfilmentDate.MESSAGE_CONSTRAINTS, preorder::toModelType);
    }

    @Test
    public void toModelType_invalidMethod_throwsIllegalValueException() {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(List.of(), VALID_DATE, "PICKUP",
                VALID_HANDOVER_NOTES);
        assertThrows(IllegalValueException.class, FulfilmentMethod.MESSAGE_CONSTRAINTS, preorder::toModelType);
    }

    @Test
    public void toModelType_invalidHandoverNotes_throwsIllegalValueException() {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(List.of(), VALID_DATE, VALID_METHOD,
                "Call on arrival\nLeave at door");
        assertThrows(IllegalValueException.class, HandoverNotes.MESSAGE_CONSTRAINTS, preorder::toModelType);
    }
}
