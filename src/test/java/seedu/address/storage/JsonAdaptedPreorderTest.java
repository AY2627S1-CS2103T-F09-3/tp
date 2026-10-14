package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.PreorderDetails;
import seedu.address.model.preorder.PreorderStatus;

public class JsonAdaptedPreorderTest {
    @Test
    public void jsonRoundTrip_allStatuses_preservesPreorder() throws Exception {
        for (PreorderStatus status : PreorderStatus.values()) {
            Preorder preorder = new Preorder(new PreorderDetails("Cake \"special\" \\ no nuts"), status);
            String json = JsonUtil.toJsonString(new JsonAdaptedPreorder(preorder));
            assertEquals(preorder, JsonUtil.fromJsonString(json, JsonAdaptedPreorder.class).toModelType());
        }
    }

    @Test
    public void toModelType_missingFields_reportsField() {
        JsonAdaptedPreorder missingDetails = new JsonAdaptedPreorder(null, "PENDING");
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedPreorder.MISSING_FIELD_MESSAGE_FORMAT, "details"),
                missingDetails::toModelType);
        JsonAdaptedPreorder missingStatus = new JsonAdaptedPreorder("Cake", null);
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedPreorder.MISSING_FIELD_MESSAGE_FORMAT, "status"),
                missingStatus::toModelType);
    }

    @Test
    public void toModelType_invalidDetails_failure() {
        for (String details : new String[]{"", " ", "Cake\nTarts", " Cake", "Cake ",
            "a".repeat(PreorderDetails.MAX_LENGTH + 1)}) {
            JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(details, "PENDING");
            assertThrows(IllegalValueException.class, PreorderDetails.MESSAGE_CONSTRAINTS, preorder::toModelType);
        }
    }

    @Test
    public void toModelType_invalidStatus_failure() {
        for (String status : new String[]{"", " ", "DONE", "pending", "PENDING "}) {
            JsonAdaptedPreorder preorder = new JsonAdaptedPreorder("Cake", status);
            assertThrows(IllegalValueException.class, PreorderStatus.MESSAGE_CONSTRAINTS, preorder::toModelType);
        }
    }
}
