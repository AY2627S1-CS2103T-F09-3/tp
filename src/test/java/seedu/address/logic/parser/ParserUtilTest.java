package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";
    private static final String INVALID_ITEM_NAME = "Pineapple Tarts\nAlmond Cookies"; // more than one line
    private static final String INVALID_QUANTITY = "0";
    private static final String INVALID_REMARK = "Less sugar\nNo nuts"; // more than one line
    private static final String INVALID_ITEM_STATUS = "done";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "91234567";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";
    private static final String VALID_ITEM_NAME = "Pineapple Tarts";
    private static final String VALID_QUANTITY = "12";
    private static final String VALID_REMARK = "Less sugar";
    private static final String VALID_HANDOVER_NOTES = "Call on arrival";
    private static final String INVALID_HANDOVER_NOTES = "Call on arrival\nLeave at door"; // more than one line

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseFulfilmentDate_validAndInvalidCalendarDates() throws Exception {
        assertEquals(new FulfilmentDate("2024-02-29"), ParserUtil.parseFulfilmentDate(" 2024-02-29 "));
        for (String date : List.of("2026-02-29", "2026-02-30", "2026-1-05", "2026/12/20", "")) {
            assertThrows(ParseException.class, FulfilmentDate.MESSAGE_CONSTRAINTS, ()
                -> ParserUtil.parseFulfilmentDate(date));
        }
        assertThrows(NullPointerException.class, () -> ParserUtil.parseFulfilmentDate(null));
    }

    @Test
    public void parseFulfilmentMethod_normalizesCaseAndRejectsUnknownMethods() throws Exception {
        assertEquals(FulfilmentMethod.DELIVERY, ParserUtil.parseFulfilmentMethod(" delivery "));
        assertEquals(FulfilmentMethod.COLLECTION, ParserUtil.parseFulfilmentMethod("CoLlEcTiOn"));
        assertThrows(ParseException.class, FulfilmentMethod.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseFulfilmentMethod("pickup"));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseFulfilmentMethod(null));
    }

    @Test
    public void parsePreambleIndex_invalidIndex_usesSuppliedMessage() {
        ArgumentMultimap args = ArgumentTokenizer.tokenize("0 t/VIP", CliSyntax.PREFIX_TAG);
        assertThrows(ParseException.class, "Expected a positive index", ()
            -> ParserUtil.parsePreambleIndex(args, "Expected a positive index"));
    }

    @Test
    public void parseSingleTag_missingPrefix_usesSuppliedMessage() {
        ArgumentMultimap args = ArgumentTokenizer.tokenize("1", CliSyntax.PREFIX_TAG);
        assertThrows(ParseException.class, "Expected t/TAG", ()
            -> ParserUtil.parseSingleTag(args, "Expected t/TAG"));
    }

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        for (String invalidName : new String[]{INVALID_NAME, "John2", " ", "John\tTan", "-", "'", " - ' ",
            "José Tan"}) {
            assertThrows(ParseException.class, Name.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseName(invalidName));
        }
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parseName_commonNameWithExtraSpaces_returnsNormalizedName() throws Exception {
        Name parsedName = ParserUtil.parseName("  Mary-Jane   O'Brien  ");
        assertEquals("Mary-Jane O'Brien", parsedName.fullName);
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }

    @Test
    public void parseItemName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseItemName(null));
    }

    @Test
    public void parseItemName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, ItemName.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseItemName(INVALID_ITEM_NAME));
        assertThrows(ParseException.class, () -> ParserUtil.parseItemName(WHITESPACE));
        assertThrows(ParseException.class, () -> ParserUtil.parseItemName("a".repeat(ItemName.MAX_LENGTH + 1)));
    }

    @Test
    public void parseItemName_validValueWithWhitespace_returnsTrimmedItemName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_ITEM_NAME + WHITESPACE;
        assertEquals(new ItemName(VALID_ITEM_NAME), ParserUtil.parseItemName(nameWithWhitespace));
    }

    @Test
    public void parseItemName_unicodeSpacesOnly_throwsParseException() {
        assertThrows(ParseException.class, ItemName.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseItemName("\u2003"));
    }

    @Test
    public void parseItemName_maxLengthAfterTrimming_returnsItemName() throws Exception {
        String longestName = "a".repeat(ItemName.MAX_LENGTH);
        assertEquals(new ItemName(longestName), ParserUtil.parseItemName(" " + longestName + " "));
    }

    @Test
    public void parseQuantity_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseQuantity(null));
    }

    @Test
    public void parseQuantity_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Quantity.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseQuantity(INVALID_QUANTITY));
        assertThrows(ParseException.class, () -> ParserUtil.parseQuantity(WHITESPACE));
        assertThrows(ParseException.class, () -> ParserUtil.parseQuantity("1 2"));
    }

    @Test
    public void parseQuantity_validValueWithWhitespace_returnsTrimmedQuantity() throws Exception {
        String quantityWithWhitespace = WHITESPACE + VALID_QUANTITY + WHITESPACE;
        assertEquals(new Quantity(VALID_QUANTITY), ParserUtil.parseQuantity(quantityWithWhitespace));
    }

    @Test
    public void parseRemark_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseRemark(null));
    }

    @Test
    public void parseRemark_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Remark.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseRemark(INVALID_REMARK));
        assertThrows(ParseException.class, () -> ParserUtil.parseRemark(WHITESPACE));
        assertThrows(ParseException.class, () -> ParserUtil.parseRemark("a".repeat(Remark.MAX_LENGTH + 1)));
    }

    @Test
    public void parseRemark_validValueWithWhitespace_returnsTrimmedRemark() throws Exception {
        String remarkWithWhitespace = WHITESPACE + VALID_REMARK + WHITESPACE;
        assertEquals(new Remark(VALID_REMARK), ParserUtil.parseRemark(remarkWithWhitespace));
        assertEquals(new Remark(VALID_REMARK), ParserUtil.parseRemark("\u2003" + VALID_REMARK + "\u2003"));
    }

    @Test
    public void parseRemark_unicodeSpacesOnly_throwsParseException() {
        assertThrows(ParseException.class, Remark.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseRemark("\u2003"));
    }

    @Test
    public void parseItemStatus_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseItemStatus(null));
    }

    @Test
    public void parseItemStatus_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, ItemStatus.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseItemStatus(INVALID_ITEM_STATUS));
        assertThrows(ParseException.class, () -> ParserUtil.parseItemStatus(WHITESPACE));
    }

    @Test
    public void parseItemStatus_anyLetterCase_returnsStatus() throws Exception {
        assertEquals(ItemStatus.PENDING, ParserUtil.parseItemStatus("PENDING"));
        assertEquals(ItemStatus.PENDING, ParserUtil.parseItemStatus("pending"));
        assertEquals(ItemStatus.PREPARED, ParserUtil.parseItemStatus("Prepared"));
        assertEquals(ItemStatus.COMPLETED, ParserUtil.parseItemStatus(WHITESPACE + "completed" + WHITESPACE));
    }

    @Test
    public void parseHandoverNotes_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseHandoverNotes(null));
    }

    @Test
    public void parseHandoverNotes_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, HandoverNotes.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseHandoverNotes(INVALID_HANDOVER_NOTES));
        assertThrows(ParseException.class, () -> ParserUtil.parseHandoverNotes(WHITESPACE));
        assertThrows(ParseException.class, ()
            -> ParserUtil.parseHandoverNotes("a".repeat(HandoverNotes.MAX_LENGTH + 1)));
    }

    @Test
    public void parseHandoverNotes_validValueWithWhitespace_returnsTrimmedHandoverNotes() throws Exception {
        String handoverNotesWithWhitespace = WHITESPACE + VALID_HANDOVER_NOTES + WHITESPACE;
        assertEquals(new HandoverNotes(VALID_HANDOVER_NOTES),
                ParserUtil.parseHandoverNotes(handoverNotesWithWhitespace));
        assertEquals(new HandoverNotes(VALID_HANDOVER_NOTES),
                ParserUtil.parseHandoverNotes("\u2003" + VALID_HANDOVER_NOTES + "\u2003"));
    }

    @Test
    public void parseHandoverNotes_unicodeSpacesOnly_throwsParseException() {
        assertThrows(ParseException.class, HandoverNotes.MESSAGE_CONSTRAINTS, ()
            -> ParserUtil.parseHandoverNotes("\u2003"));
    }

    @Test
    public void parseHandoverNotes_maximumLength_returnsHandoverNotes() throws Exception {
        String longestHandoverNotes = "a".repeat(HandoverNotes.MAX_LENGTH);
        assertEquals(new HandoverNotes(longestHandoverNotes), ParserUtil.parseHandoverNotes(longestHandoverNotes));
    }
}
