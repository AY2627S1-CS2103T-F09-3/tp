package seedu.address.commons.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class DataValidationExceptionTest {

    @Test
    public void constructor_userMessageAndDetails_preservesBothMessages() {
        DataValidationException exception = new DataValidationException("invalid email.", "Detailed email rules");

        assertEquals("invalid email.", exception.getUserMessage());
        assertEquals("Detailed email rules", exception.getMessage());
    }

    @Test
    public void constructor_nestedContexts_preservesMessagesAndCause() {
        DataValidationException field = new DataValidationException("invalid quantity.", "Detailed quantity rules");
        DataValidationException item = new DataValidationException("item 3", field);

        DataValidationException person = new DataValidationException("Person 2", item);

        assertEquals("Person 2: item 3: invalid quantity.", person.getUserMessage());
        assertEquals("Detailed quantity rules", person.getMessage());
        assertSame(item, person.getCause());
        assertSame(field, item.getCause());
    }

    @Test
    public void constructor_existingValidationError_preservesOriginalReason() {
        IllegalValueException cause = new IllegalValueException("Invalid record");

        DataValidationException exception = new DataValidationException("Person 2", cause);

        assertEquals("Person 2: Invalid record", exception.getUserMessage());
        assertEquals("Invalid record", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
