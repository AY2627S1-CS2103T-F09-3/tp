package seedu.address.commons.exceptions;

/**
 * Represents invalid saved data with a concise user message and detailed validation information.
 */
public class DataValidationException extends IllegalValueException {

    private final String userMessage;

    /**
     * Creates a validation error with a concise user message and a detailed message for diagnostics.
     */
    public DataValidationException(String userMessage, String detailedMessage) {
        super(detailedMessage);
        this.userMessage = userMessage;
    }

    /**
     * Adds record context to a validation error while preserving its detailed message and cause.
     */
    public DataValidationException(String context, IllegalValueException cause) {
        super(cause.getMessage(), cause);
        String reason = cause instanceof DataValidationException validationException
                ? validationException.getUserMessage() : cause.getMessage();
        userMessage = context + ": " + reason;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
