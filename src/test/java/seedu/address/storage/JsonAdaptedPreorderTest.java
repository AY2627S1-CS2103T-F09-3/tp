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
    public void toModelType_legacySlashHandoverNotes_preservesValue() throws Exception {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(List.of(), VALID_DATE, VALID_METHOD,
                "Leave w/o ringing");

        assertEquals("Leave w/o ringing", preorder.toModelType().getHandoverNotes().orElseThrow().value);
    }

    @Test
    public void toModelType_invalidLegacySlashHandoverNotes_throwsIllegalValueException() {
        JsonAdaptedPreorder oversizedNotes = new JsonAdaptedPreorder(List.of(), null, null,
                "N".repeat(HandoverNotes.MAX_LENGTH) + "/");
        JsonAdaptedPreorder multilineNotes = new JsonAdaptedPreorder(List.of(), null, null,
                "Leave/\nat door");

        assertThrows(IllegalValueException.class, HandoverNotes.MESSAGE_CONSTRAINTS, oversizedNotes::toModelType);
        assertThrows(IllegalValueException.class, HandoverNotes.MESSAGE_CONSTRAINTS, multilineNotes::toModelType);
    }

    @Test
    public void toModelType_lowerCaseMethod_returnsPreorderWithMethod() throws Exception {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(List.of(), VALID_DATE, "delivery",
                VALID_HANDOVER_NOTES);
        assertEquals(buildPreorderWithAllDetails(), preorder.toModelType());
    }

    @Test
    public void toModelType_paddedFulfilmentDetails_returnsStrippedDetails() throws Exception {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(List.of(), "\u00A0" + VALID_DATE + "\u00A0",
                "\u00A0" + VALID_METHOD + "\u00A0", "\u00A0" + VALID_HANDOVER_NOTES + "\u00A0");
        assertEquals(buildPreorderWithAllDetails(), preorder.toModelType());
    }

    @Test
    public void toModelType_blankFulfilmentDetails_returnsPreorderWithoutDetails() throws Exception {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(List.of(), "", "\u00A0", "\u2003");
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

    /**
     * Returns an empty preorder with the valid date, method and handover notes.
     */
    private Preorder buildPreorderWithAllDetails() {
        return new PreorderBuilder().withDate(VALID_DATE).withMethod(FulfilmentMethod.DELIVERY)
                .withHandoverNotes(VALID_HANDOVER_NOTES).build();
    }
}
