package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.Preorder;

/**
 * Container for user visible messages.
 */
public class Messages {
    public static final String MESSAGE_INVALID_ITEM_INDEX = "Item index must be a positive integer.";
    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_UNSUPPORTED_PREFIX = "Unsupported prefix '%1$s' for %2$s.";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String NO_FULFILMENT_DATE_TEXT = "Not scheduled";
    public static final String NO_FULFILMENT_METHOD_TEXT = "Not set";
    public static final String NO_HANDOVER_NOTES_TEXT = "None";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Postal code: ")
                .append(person.getPostalCode())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        return builder.toString();
    }

    /**
     * Formats the fulfilment details shared by all items of {@code preorder} for display to the user,
     * e.g. {@code Date: 20 Dec 2026 | Method: DELIVERY | Handover notes: Call on arrival}.
     */
    public static String formatFulfilmentDetails(Preorder preorder) {
        String date = preorder.getDate().map(FulfilmentDate::toDisplayString).orElse(NO_FULFILMENT_DATE_TEXT);
        String method = preorder.getMethod().map(FulfilmentMethod::name).orElse(NO_FULFILMENT_METHOD_TEXT);
        String handoverNotes = preorder.getHandoverNotes().map(HandoverNotes::toString)
                .orElse(NO_HANDOVER_NOTES_TEXT);
        return "Date: " + date + " | Method: " + method + " | Handover notes: " + handoverNotes;
    }

}
