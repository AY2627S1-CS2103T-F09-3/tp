package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.model.search.TextContainsCriterion.Field.ADDRESS;
import static seedu.address.model.search.TextContainsCriterion.Field.EMAIL;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.search.TextContainsCriterion.Field;
import seedu.address.model.tag.Tag;

public class FindCommandParserTest {

    private static final String FORMAT_ERROR =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);

    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_missingPrefixesOrBarePreamble_throwsParseException() {
        for (String input : List.of("", " \t\n ", "Alice", "Alice Tan", "Alice t/regular",
                "Alice n/Tan", "nAlice", "/Alice")) {
            assertParseFailure(parser, input, FORMAT_ERROR);
        }
    }

    @Test
    public void parse_unsupportedPrefix_throwsParseException() {
        for (String input : List.of("x/Alice", "n/Alice x/unknown", "n/Alice\tunknown/value",
                "N/Alice", "n/Alice P/8123", "S/pending", "n/Alice D/Tarts",
                "o/Pineapple Tarts", "f/2026-10-22", "m/delivery", "c/53")) {
            assertParseFailure(parser, input, FORMAT_ERROR);
        }
    }

    @Test
    public void parse_blankValue_throwsParseException() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG,
                PREFIX_DETAILS, PREFIX_STATUS)) {
            assertParseFailure(parser, prefix + " \t\n ", FORMAT_ERROR);
            assertParseFailure(parser, prefix + " \t\n n/Alice", prefix.equals(PREFIX_NAME)
                    ? Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME) : FORMAT_ERROR);
        }
    }

    @Test
    public void parse_duplicatePrefix_throwsParseException() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG,
                PREFIX_DETAILS, PREFIX_STATUS)) {
            String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(prefix);
            assertParseFailure(parser, prefix + "first " + prefix + "second", expectedMessage);
            assertParseFailure(parser, prefix + "first " + prefix + "first", expectedMessage);
        }
    }

    @Test
    public void parse_textFragments_returnsFindCommand() {
        assertParseSuccess(parser, "n/Alice Tan", textCommand(NAME, "Alice Tan"));
        assertParseSuccess(parser, "p/8123", textCommand(PHONE, "8123"));
        assertParseSuccess(parser, "e/EXAMPLE.COM", textCommand(EMAIL, "example.com"));
        assertParseSuccess(parser, "a/Hougang Avenue", textCommand(ADDRESS, "Hougang Avenue"));
    }

    @Test
    public void parse_validTag_returnsFindCommand() {
        FindCommand expected = new FindCommand(new SearchCriteria(List.of(new TagCriterion(new Tag("regular")))));
        assertParseSuccess(parser, "t/REGULAR", expected);
        assertParseSuccess(parser, " \t t/ regular \n", expected);
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        for (String input : List.of("t/VIP!", "t/regular customer", "t/gluten-free")) {
            assertParseFailure(parser, input, Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_combinedCriteria_returnsFindCommand() {
        FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Alice Tan"),
                new TextContainsCriterion(PHONE, "8123"),
                new TextContainsCriterion(EMAIL, "example.com"),
                new TextContainsCriterion(ADDRESS, "Hougang Avenue"),
                new TagCriterion(new Tag("regular")))));

        assertParseSuccess(parser, "n/Alice Tan p/8123 e/example.com a/Hougang Avenue t/regular", expected);
        assertParseSuccess(parser, "t/REGULAR a/Hougang Avenue e/example.com p/8123 n/ALICE TAN", expected);
        assertParseSuccess(parser,
                "\t n/\tAlice Tan \n\tp/8123\te/example.com \n a/Hougang Avenue \t t/regular \n", expected);
    }

    @Test
    public void parse_internalSpacing_preservesPhrase() throws Exception {
        assertParseSuccess(parser, "n/Alice  Tan", textCommand(NAME, "Alice  Tan"));
        assertParseSuccess(parser, "a/Hougang  Avenue", textCommand(ADDRESS, "Hougang  Avenue"));
        assertNotEquals(parser.parse("n/Alice Tan"), parser.parse("n/Alice  Tan"));
    }

    @Test
    public void parse_punctuationAndEmbeddedSlashes_preservesText() {
        assertParseSuccess(parser, "n/O'Brien", textCommand(NAME, "O'Brien"));
        assertParseSuccess(parser, "n/Mary-Jane", textCommand(NAME, "Mary-Jane"));
        assertParseSuccess(parser, "a/123 Sample Lane, #04/08", textCommand(ADDRESS, "123 Sample Lane, #04/08"));
        assertParseSuccess(parser, "e/alice/p@example.com", textCommand(EMAIL, "alice/p@example.com"));
        assertParseSuccess(parser, "a/https://example.com/path", textCommand(ADDRESS, "https://example.com/path"));
    }

    @Test
    public void parse_productWithoutStatus_acceptsAllStatuses() {
        FindCommand expected = itemCommand(Optional.of("Pineapple Tarts"), EnumSet.allOf(ItemStatus.class));
        assertParseSuccess(parser, "d/Pineapple Tarts", expected);
        assertParseSuccess(parser, " d/ PINEAPPLE TARTS ", expected);
    }

    @Test
    public void parse_statusWithoutProduct_acceptsAnyProduct() {
        for (ItemStatus status : ItemStatus.values()) {
            FindCommand expected = itemCommand(Optional.empty(), Set.of(status));
            assertParseSuccess(parser, "s/" + status, expected);
        }
        assertParseSuccess(parser, "s/pEnDiNg", itemCommand(Optional.empty(), Set.of(ItemStatus.PENDING)));
    }

    @Test
    public void parse_productAndStatus_createsSingleItemCriterion() {
        FindCommand expected = itemCommand(Optional.of("Pineapple Tarts"), Set.of(ItemStatus.PREPARED));
        assertParseSuccess(parser, "d/Pineapple Tarts s/prepared", expected);
        assertParseSuccess(parser, "s/PREPARED\td/ Pineapple Tarts", expected);
    }

    @Test
    public void parse_customerAndItemCriteria_combinesConditions() {
        FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Alice"), new TagCriterion(new Tag("regular")),
                new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of(ItemStatus.PENDING)))));
        assertParseSuccess(parser, "n/Alice d/Pineapple Tarts t/regular s/pending", expected);
    }

    @Test
    public void parse_invalidItemStatus_throwsParseException() {
        for (String input : List.of("s/unknown", "s/pending prepared", "d/Tarts s/123")) {
            assertParseFailure(parser, input, ItemStatus.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_productSpacingAndPunctuation_preservesText() {
        assertParseSuccess(parser, "d/Tarts  (Large)/Box",
                itemCommand(Optional.of("Tarts  (Large)/Box"), EnumSet.allOf(ItemStatus.class)));
    }

    private FindCommand itemCommand(Optional<String> product, Set<ItemStatus> statuses) {
        return new FindCommand(new SearchCriteria(List.of(new ItemCriterion(product, statuses))));
    }

    private FindCommand textCommand(Field field, String query) {
        return new FindCommand(new SearchCriteria(List.of(new TextContainsCriterion(field, query))));
    }
}
