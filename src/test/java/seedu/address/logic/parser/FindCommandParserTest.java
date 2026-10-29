package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_METHOD;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HANDOVER_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_POSTAL_CODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.model.search.TextContainsCriterion.Field.ADDRESS;
import static seedu.address.model.search.TextContainsCriterion.Field.EMAIL;
import static seedu.address.model.search.TextContainsCriterion.Field.HANDOVER_NOTES;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.FulfilmentDateCriterion;
import seedu.address.model.search.FulfilmentMethodCriterion;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.PostalCodeCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.search.TextContainsCriterion.Field;
import seedu.address.model.tag.Tag;

public class FindCommandParserTest {

    private static final String FORMAT_ERROR =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
    private static final Map<String, String> CONSTRAINED_QUERIES = Map.of(
            "c/123", PostalCodeCriterion.MESSAGE_CONSTRAINTS,
            "t/VIP", Tag.MESSAGE_CONSTRAINTS,
            "f/2026-12-20", FulfilmentDate.MESSAGE_CONSTRAINTS,
            "m/delivery", FulfilmentMethod.MESSAGE_CONSTRAINTS,
            "s/pending", ItemStatus.MESSAGE_CONSTRAINTS);

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
    public void parse_unprefixedName_throwsParseException() {
        for (String input : List.of("Alice", "Alice Tan", "nAlice", "/Alice", " \t Alice  Bob \n")) {
            assertParseFailure(parser, input, FORMAT_ERROR);
        }
    }

    @Test
    public void parse_unsupportedPrefixBeforeFirstCriterion_throwsParseException() {
        for (String input : List.of("x/Alice", "x/Alice n/Bob", "N/Alice", "N/Alice n/Bob", "S/pending",
                "o/Pineapple Tarts", "z/2026-10-22", "M/delivery", "C/012345", "H/arrival",
                "i/1", "x/ n/Alice", "x/foo h/Leave w/o ringing")) {
            assertParseFailure(parser, input, FORMAT_ERROR);
        }
    }

    @Test
    public void parse_validContactFragments_returnsFindCommand() {
        for (String name : List.of("Ali", "O'", "-Jane", "'", "-", "Alice  Tan")) {
            assertParseSuccess(parser, "n/" + name, textCommand(NAME, name));
        }
        for (String phone : List.of("0", "1", "456", "9123", "01234567", "91234567")) {
            assertParseSuccess(parser, "p/" + phone, textCommand(PHONE, phone));
        }
        for (String email : List.of("example.com", "alice+", "_tan", "tan@", "@example", "@", ".com",
                "alice.tan+cake@example.com")) {
            assertParseSuccess(parser, "e/" + email, textCommand(EMAIL, email));
        }
    }

    @Test
    public void parse_noBreakSpacesAroundContactFragments_stripped() {
        for (String whitespace : List.of("\u00A0", "\u202F", "\u00A0\u2003\u202F")) {
            assertParseSuccess(parser, "n/" + whitespace + "Ali" + whitespace, textCommand(NAME, "Ali"));
            assertParseSuccess(parser, "p/" + whitespace + "9123" + whitespace, textCommand(PHONE, "9123"));
            assertParseSuccess(parser, "e/" + whitespace + "example.com" + whitespace,
                    textCommand(EMAIL, "example.com"));
        }
    }

    @Test
    public void parse_invalidContactFragments_throwsSearchConstraintMessage() {
        for (String name : List.of("Alice2", "Alice@Tan", "Alice/Tan", "Alice\tTan", "José")) {
            assertParseFailure(parser, "n/" + name, FindCommandParser.MESSAGE_NAME_QUERY_CONSTRAINTS);
        }
        for (String phone : List.of("912345678", "+6591234567", "9123 4567", "9123-4567", "Ali", "１２３")) {
            assertParseFailure(parser, "p/" + phone, FindCommandParser.MESSAGE_PHONE_QUERY_CONSTRAINTS);
        }
        for (String email : List.of("alice example.com", "alice/p@example.com", "alice!", "alice:com", "é.com")) {
            assertParseFailure(parser, "e/" + email, FindCommandParser.MESSAGE_EMAIL_QUERY_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unsupportedPrefixesInContactFragments_namesPrefix() {
        for (String query : List.of("n/Ali", "p/9123", "e/example.com")) {
            for (String prefix : List.of("x/", "P/", "i/", "unknown/", "w/")) {
                for (String value : List.of("foo", "2", "")) {
                    assertParseFailure(parser, query + " " + prefix + value,
                            "Unsupported prefix '" + prefix + "' for find.");
                }
            }
            assertParseFailure(parser, query + " x/foo q/2", "Unsupported prefix 'x/' for find.");
            for (String separator : List.of("\t", "\n", "\r\n", " \t ")) {
                assertParseFailure(parser, query + separator + "x/foo", "Unsupported prefix 'x/' for find.");
            }
        }
    }

    @Test
    public void parse_unsupportedPrefixesInUnicodePaddedContactFragments_namesPrefix() {
        for (String query : List.of("n/\u00A0Ali\u00A0 x/foo\u00A0", "p/\u00A09123\u00A0 x/foo\u00A0",
                "e/\u00A0example.com\u00A0 x/foo\u00A0")) {
            assertParseFailure(parser, query, "Unsupported prefix 'x/' for find.");
        }
    }

    @Test
    public void parse_invalidContactFragmentsWithoutPrefixBoundary_keepsSearchConstraintMessages() {
        assertParseFailure(parser, "n/Alix/foo", FindCommandParser.MESSAGE_NAME_QUERY_CONSTRAINTS);
        assertParseFailure(parser, "p/9123x/foo", FindCommandParser.MESSAGE_PHONE_QUERY_CONSTRAINTS);
        assertParseFailure(parser, "e/example.comx/foo", FindCommandParser.MESSAGE_EMAIL_QUERY_CONSTRAINTS);
        assertParseFailure(parser, "n/x/foo", FindCommandParser.MESSAGE_NAME_QUERY_CONSTRAINTS);
        assertParseFailure(parser, "p/x/foo", FindCommandParser.MESSAGE_PHONE_QUERY_CONSTRAINTS);
        assertParseFailure(parser, "e/x/foo", FindCommandParser.MESSAGE_EMAIL_QUERY_CONSTRAINTS);
    }

    @Test
    public void parse_invalidContactFragmentsWithEarlierErrors_preservesValidationOrder() {
        assertParseFailure(parser, "x/foo n/Ali", FORMAT_ERROR);
        assertParseFailure(parser, "n/Ali x/foo n/Bob", Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, "n/Ali x/foo p/", FORMAT_ERROR);
        assertParseFailure(parser, "n/Ali2 p/9123 x/foo", FindCommandParser.MESSAGE_NAME_QUERY_CONSTRAINTS);
        assertParseFailure(parser, "p/invalid e/example.com x/foo", FindCommandParser.MESSAGE_PHONE_QUERY_CONSTRAINTS);
        assertParseFailure(parser, "e/invalid! c/01 x/foo", FindCommandParser.MESSAGE_EMAIL_QUERY_CONSTRAINTS);
    }

    @Test
    public void parse_unsupportedPrefixesInConstrainedCriteria_namesPrefix() {
        for (String query : CONSTRAINED_QUERIES.keySet()) {
            for (String prefix : List.of("x/", "i/", "q/", "P/", "unknown/", "w/")) {
                for (String value : List.of("foo", "2", "")) {
                    assertParseFailure(parser, query + " " + prefix + value,
                            "Unsupported prefix '" + prefix + "' for find.");
                }
            }
            assertParseFailure(parser, query + " x/foo q/2", "Unsupported prefix 'x/' for find.");
        }
    }

    @Test
    public void parse_unsupportedPrefixAfterWhitespaceInConstrainedCriteria_namesPrefix() {
        for (String query : CONSTRAINED_QUERIES.keySet()) {
            for (String separator : List.of("\t", "\n", "\r\n", " \t ")) {
                assertParseFailure(parser, query + separator + "x/foo", "Unsupported prefix 'x/' for find.");
            }
        }
    }

    @Test
    public void parse_invalidConstrainedValuesWithoutPrefixBoundary_keepsConstraintMessages() {
        for (Map.Entry<String, String> query : CONSTRAINED_QUERIES.entrySet()) {
            for (String suffix : List.of("x/foo", " x /foo", " #04/08", " https://example.com/path", " 2x/foo")) {
                assertParseFailure(parser, query.getKey() + suffix, query.getValue());
            }
            String prefix = query.getKey().substring(0, query.getKey().indexOf('/') + 1);
            assertParseFailure(parser, prefix + "x/foo", query.getValue());
        }
    }

    @Test
    public void parse_freeTextPrefixesBeforeInvalidConstrainedValue_keepsConstraintMessage() {
        for (Map.Entry<String, String> query : CONSTRAINED_QUERIES.entrySet()) {
            for (String text : List.of("a/Block 123 #04/08")) {
                assertParseFailure(parser, text + " " + query.getKey() + " invalid", query.getValue());
            }
        }
    }

    @Test
    public void parse_freeTextPrefixesBeforeUnsupportedConstrainedArgument_namesOnlyArgument() {
        for (String query : CONSTRAINED_QUERIES.keySet()) {
            assertParseFailure(parser, "h/Leave ringing " + query + " y/later",
                    "Unsupported prefix 'y/' for find.");
        }
    }

    @Test
    public void parse_unsupportedConstrainedArgumentWithEarlierErrors_preservesValidationOrder() {
        assertParseFailure(parser, "x/foo c/123", FORMAT_ERROR);
        assertParseFailure(parser, "c/123 x/foo c/456",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_POSTAL_CODE));
        assertParseFailure(parser, "c/123 x/foo t/", FORMAT_ERROR);
        assertParseFailure(parser, "n/Ali2 c/123 x/foo", FindCommandParser.MESSAGE_NAME_QUERY_CONSTRAINTS);
        assertParseFailure(parser, "f/2026-02-30 m/delivery x/foo", FulfilmentDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "m/pickup c/123 x/foo", FulfilmentMethod.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "c/invalid t/VIP x/foo", PostalCodeCriterion.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/VIP! s/pending x/foo", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_slashesInProductAndHandoverQueries_rejected() {
        assertParseFailure(parser, "d/Cake w/ candles", ItemName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "h/Leave w/o ringing x/foo", HandoverNotes.MESSAGE_CONSTRAINTS);
        assertParseSuccess(parser, "a/Block 123 c/o Alice #04/08",
                textCommand(ADDRESS, "Block 123 c/o Alice #04/08"));
    }

    @Test
    public void parse_slashTextAfterWhitespace_rejectedOutsideAddress() {
        for (String separator : List.of(" ", "\t", "\n", "\r\n", " \t ")) {
            String text = "Cake" + separator + "x/foo";
            assertParseFailure(parser, PREFIX_DETAILS + text, ItemName.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, PREFIX_HANDOVER_NOTES + text, HandoverNotes.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_prefixLikeTextWithSupportedPrefixes_delimitsCriteria() {
        FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Ali"),
                new TextContainsCriterion(PHONE, "9123"),
                new TextContainsCriterion(EMAIL, "example.com"),
                new TextContainsCriterion(ADDRESS, "Block 123 c/o Alice #04/08"),
                new TextContainsCriterion(HANDOVER_NOTES, "Leave ringing"),
                new ItemCriterion(Optional.of("Cake candles"), Set.of(ItemStatus.PENDING)),
                new PostalCodeCriterion("01"), new TagCriterion(new Tag("VIP")),
                new FulfilmentDateCriterion(new FulfilmentDate("2026-12-20")),
                new FulfilmentMethodCriterion(FulfilmentMethod.DELIVERY))));
        assertParseSuccess(parser, "n/Ali p/9123 e/example.com "
                + "a/Block 123 c/o Alice #04/08 c/01 t/VIP d/Cake candles s/pending "
                + "h/Leave ringing f/2026-12-20 m/delivery", expected);
        assertParseSuccess(parser, "m/delivery f/2026-12-20 h/Leave ringing "
                + "s/pending d/Cake candles t/VIP c/01 a/Block 123 c/o Alice #04/08 "
                + "e/example.com p/9123 n/Ali", expected);
    }

    @Test
    public void parse_prefixLikeTextWithDuplicateOrBlankCriterion_preservesValidationOrder() {
        for (Prefix prefix : List.of(PREFIX_ADDRESS, PREFIX_DETAILS, PREFIX_HANDOVER_NOTES)) {
            String value = prefix.equals(PREFIX_ADDRESS) ? "Block 123 x/foo" : "Cake";
            assertParseFailure(parser, prefix + value + " " + prefix + "Other",
                    Messages.getErrorMessageForDuplicatePrefixes(prefix));
            assertParseFailure(parser, prefix + value + " s/", FORMAT_ERROR);
            assertParseFailure(parser, prefix + value + " c/invalid", PostalCodeCriterion.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_careOfOutsideAddress_delimitsPostalCriterion() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_DETAILS, PREFIX_HANDOVER_NOTES)) {
            assertParseFailure(parser, prefix + "Text c/o Alice", PostalCodeCriterion.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "p/9123 c/o Alice", PostalCodeCriterion.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "e/example.com c/o Alice", PostalCodeCriterion.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_blankValue_throwsParseException() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_POSTAL_CODE,
                PREFIX_TAG, PREFIX_DETAILS, PREFIX_STATUS,
                PREFIX_FULFILMENT_DATE, PREFIX_FULFILMENT_METHOD, PREFIX_HANDOVER_NOTES)) {
            assertParseFailure(parser, prefix + " \t\n ", FORMAT_ERROR);
            assertParseFailure(parser, prefix + " \t\n n/Alice", prefix.equals(PREFIX_NAME)
                    ? Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME) : FORMAT_ERROR);
        }
    }

    @Test
    public void parse_unicodeSpacesOnly_throwsParseException() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_DETAILS)) {
            for (String whitespace : List.of("\u00A0", "\u2003", "\u202F", "\u00A0\u2003\u202F")) {
                assertParseFailure(parser, prefix + whitespace, FORMAT_ERROR);
            }
        }
    }

    @Test
    public void parse_duplicatePrefix_throwsParseException() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_POSTAL_CODE,
                PREFIX_TAG, PREFIX_DETAILS, PREFIX_STATUS,
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
    public void parse_tagLengthBoundaries_checksTrimmedLength() {
        for (int length : new int[]{29, 30}) {
            String tag = "A".repeat(length);
            FindCommand expected = new FindCommand(new SearchCriteria(List.of(new TagCriterion(new Tag(tag)))));
            assertParseSuccess(parser, "t/  " + tag + "  ", expected);
        }
        assertParseFailure(parser, "t/  " + "A".repeat(31) + "  ", Tag.MESSAGE_LENGTH_CONSTRAINTS);
    }

    @Test
    public void parse_textBeyondStoredFieldLimits_returnsFindCommand() {
        String name = "A".repeat(101);
        String email = "a".repeat(255);
        String address = "A".repeat(301);
        String handoverNotes = "A".repeat(101);
        String product = "A".repeat(201);

        assertParseSuccess(parser, "n/" + name, textCommand(NAME, name));
        assertParseSuccess(parser, "e/" + email, textCommand(EMAIL, email));
        assertParseSuccess(parser, "a/" + address, textCommand(ADDRESS, address));
        assertParseSuccess(parser, "h/" + handoverNotes, textCommand(HANDOVER_NOTES, handoverNotes));
        assertParseSuccess(parser, "d/" + product,
                itemCommand(Optional.of(product), EnumSet.allOf(ItemStatus.class)));
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
    public void parse_noBreakSpacesAroundText_stripped() {
        assertParseSuccess(parser, "h/\u00A0side gate\u00A0", textCommand(HANDOVER_NOTES, "side gate"));
    }

    @Test
    public void parse_validPostalCodePrefix_returnsFindCommand() {
        for (String prefix : List.of("0", "53", "530", "5301", "53012", "123456", "012345", "000000", "999999")) {
            FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                    new PostalCodeCriterion(prefix))));
            assertParseSuccess(parser, "c/" + prefix, expected);
            assertParseSuccess(parser, " \t c/ " + prefix + " \n", expected);
        }
    }

    @Test
    public void parse_invalidPostalCodePrefix_throwsParseException() {
        for (String prefix : List.of("1234567", "abcdef", "01234a", "123 456", "o", "o12345",
                "+12345", "-12345", "123.45", "123/456", "123456 extra", "\u0665\u0663")) {
            assertParseFailure(parser, "c/" + prefix, PostalCodeCriterion.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "n/Alice c/" + prefix, PostalCodeCriterion.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_malformedPostalCodeSyntax_throwsParseException() {
        for (String input : List.of("c012345", "c /012345", "/012345", "012345 c/123456",
                "n/Alice c/")) {
            assertParseFailure(parser, input, FORMAT_ERROR);
        }
        assertParseFailure(parser, "c/012345 x/value", "Unsupported prefix 'x/' for find.");
        assertParseFailure(parser, "c/012345 c/123456",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_POSTAL_CODE));
    }

    @Test
    public void parse_postalCodePrefixWithName_returnsFindCommand() {
        FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Alice"),
                new PostalCodeCriterion("01"))));
        assertParseSuccess(parser, "n/Alice c/01", expected);
        assertParseSuccess(parser, "c/01 n/Alice", expected);
        assertParseSuccess(parser, "n/Alice\tc/01", expected);
    }

    @Test
    public void parse_addressCareOfWithPostalCode_preservesAddressText() {
        String address = "Block 123 c/o Alice";
        FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                new TextContainsCriterion(ADDRESS, address),
                new PostalCodeCriterion("012345"))));
        assertParseSuccess(parser, "a/" + address + " c/012345", expected);
        assertParseSuccess(parser, "c/012345 a/" + address, expected);
        assertParseSuccess(parser, "a/" + address, textCommand(ADDRESS, address));
    }

    @Test
    public void parse_addressCareOfAfterWhitespace_preservesAddressText() {
        for (String separator : List.of("\t", "\n", "\r\n", " \t ")) {
            String address = "Block 123" + separator + "c/o Alice";
            FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                    new TextContainsCriterion(ADDRESS, address), new PostalCodeCriterion("53"))));
            assertParseSuccess(parser, "a/" + address, textCommand(ADDRESS, address));
            assertParseSuccess(parser, "a/" + address + separator + "c/53", expected);
            assertParseSuccess(parser, "c/53" + separator + "a/" + address, expected);
        }
    }

    @Test
    public void parse_postalProductAndStatus_returnsCombinedCriteria() {
        FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                new PostalCodeCriterion("01"),
                new ItemCriterion(Optional.of("Tarts"), Set.of(ItemStatus.PENDING)))));
        assertParseSuccess(parser, "c/01 d/Tarts s/pending", expected);
        assertParseSuccess(parser, "s/pending\td/Tarts\nc/01", expected);
    }

    @Test
    public void parse_postalFulfilmentAndItemCriteria_returnsCombinedFindCommand() {
        FindCommand expected = new FindCommand(new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Alice"), new TextContainsCriterion(PHONE, "8123"),
                new TextContainsCriterion(EMAIL, "example.com"), new TextContainsCriterion(ADDRESS, "Hougang"),
                new PostalCodeCriterion("01"), new TagCriterion(new Tag("regular")),
                new ItemCriterion(Optional.of("Tarts"), Set.of(ItemStatus.PENDING)),
                new FulfilmentDateCriterion(new FulfilmentDate("2026-12-20")),
                new FulfilmentMethodCriterion(FulfilmentMethod.DELIVERY),
                new TextContainsCriterion(HANDOVER_NOTES, "arrival"))));
        assertParseSuccess(parser, "n/Alice p/8123 e/example.com a/Hougang c/01 t/regular d/Tarts s/pending "
                + "f/2026-12-20 m/delivery h/arrival", expected);
        assertParseSuccess(parser, "h/ARRIVAL\tm/DELIVERY\nf/2026-12-20 s/PENDING d/TARTS t/REGULAR "
                + "c/01 a/HOUGANG e/EXAMPLE.COM p/8123 n/ALICE", expected);
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
        assertParseSuccess(parser, "e/alice+cake@example.com", textCommand(EMAIL, "alice+cake@example.com"));
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
    public void parse_productNameWithSlash_failure() {
        assertParseFailure(parser, "d/Tarts  (Large)/Box", ItemName.MESSAGE_CONSTRAINTS);
    }

    private FindCommand itemCommand(Optional<String> product, Set<ItemStatus> statuses) {
        return new FindCommand(new SearchCriteria(List.of(new ItemCriterion(product, statuses))));
    }

    private FindCommand textCommand(Field field, String query) {
        return new FindCommand(new SearchCriteria(List.of(new TextContainsCriterion(field, query))));
    }
}
