package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_METHOD;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HANDOVER_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.model.search.TextContainsCriterion.Field.ADDRESS;
import static seedu.address.model.search.TextContainsCriterion.Field.EMAIL;
import static seedu.address.model.search.TextContainsCriterion.Field.HANDOVER_NOTES;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.search.FulfilmentDateCriterion;
import seedu.address.model.search.FulfilmentMethodCriterion;
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
    public void parse_emptyOrMixedSyntax_throwsParseException() {
        for (String input : List.of("", " \t\n ", "Alice t/regular", "Alice n/Tan")) {
            assertParseFailure(parser, input, FORMAT_ERROR);
        }
    }

    @Test
    public void parse_unprefixedName_reusesLegacyKeywordMatcher() {
        for (String input : List.of("Alice", "Alice Tan", "nAlice", "/Alice", " \t Alice  Bob \n")) {
            FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                    new NameContainsKeywordsPredicate(Arrays.asList(input.strip().split("\\s+"))))));
            assertParseSuccess(parser, input, expected);
        }
    }

    @Test
    public void parse_unsupportedPrefix_throwsParseException() {
        for (String input : List.of("x/Alice", "n/Alice x/unknown", "n/Alice\tunknown/value",
                "N/Alice", "n/Alice P/8123", "s/pending", "n/Alice s/pending",
                "o/Pineapple Tarts", "z/2026-10-22", "M/delivery", "c/53",
                "i/1", "n/Alice i/arrival", "h/arrival i/1")) {
            assertParseFailure(parser, input, FORMAT_ERROR);
        }
    }

    @Test
    public void parse_blankValue_throwsParseException() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG,
                PREFIX_FULFILMENT_DATE, PREFIX_FULFILMENT_METHOD, PREFIX_HANDOVER_NOTES)) {
            assertParseFailure(parser, prefix + " \t\n ", FORMAT_ERROR);
            assertParseFailure(parser, prefix + " \t\n n/Alice", prefix.equals(PREFIX_NAME)
                    ? Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME) : FORMAT_ERROR);
        }
    }

    @Test
    public void parse_duplicatePrefix_throwsParseException() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG,
                PREFIX_FULFILMENT_DATE, PREFIX_FULFILMENT_METHOD, PREFIX_HANDOVER_NOTES)) {
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
    public void parse_fulfilmentCriteria_returnsCombinedFindCommand() {
        FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                new FulfilmentDateCriterion(new FulfilmentDate("2026-12-20")),
                new FulfilmentMethodCriterion(FulfilmentMethod.DELIVERY),
                new TextContainsCriterion(HANDOVER_NOTES, "call on arrival"),
                new TextContainsCriterion(NAME, "Alice"))));
        assertParseSuccess(parser, "f/2026-12-20 m/delivery h/Call on arrival n/Alice", expected);
        assertParseSuccess(parser, " n/Alice\th/ CALL ON ARRIVAL \nm/DeLiVeRy f/2026-12-20 ", expected);
        assertParseSuccess(parser, "m/ collection", new FindCommand(new SearchCriteria(List.of(
                new FulfilmentMethodCriterion(FulfilmentMethod.COLLECTION)))));
    }

    @Test
    public void parse_invalidFulfilmentValues_throwsFieldMessage() {
        for (String date : List.of("2026-02-30", "2026-2-03", "2026/12/20", "2026-12-20 extra")) {
            assertParseFailure(parser, "f/" + date, FulfilmentDate.MESSAGE_CONSTRAINTS);
        }
        for (String method : List.of("pickup", "del", "delivery collection")) {
            assertParseFailure(parser, "m/" + method, FulfilmentMethod.MESSAGE_CONSTRAINTS);
        }
        assertParseSuccess(parser, "h/ Call  on arrival! ", textCommand(HANDOVER_NOTES, "Call  on arrival!"));
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

    private FindCommand textCommand(Field field, String query) {
        return new FindCommand(new SearchCriteria(List.of(new TextContainsCriterion(field, query))));
    }
}
