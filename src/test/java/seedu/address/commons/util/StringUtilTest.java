package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    //---------------- Tests for isNonZeroUnsignedInteger --------------------------------------

    @Test
    public void isNonZeroUnsignedInteger() {

        // EP: empty strings
        assertFalse(StringUtil.isNonZeroUnsignedInteger("")); // Boundary value
        assertFalse(StringUtil.isNonZeroUnsignedInteger("  "));

        // EP: not a number
        assertFalse(StringUtil.isNonZeroUnsignedInteger("a"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("aaa"));

        // EP: zero
        assertFalse(StringUtil.isNonZeroUnsignedInteger("0"));

        // EP: zero as prefix
        assertTrue(StringUtil.isNonZeroUnsignedInteger("01"));

        // EP: signed numbers
        assertFalse(StringUtil.isNonZeroUnsignedInteger("-1"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("+1"));

        // EP: numbers with white space
        assertFalse(StringUtil.isNonZeroUnsignedInteger(" 10 ")); // Leading/trailing spaces
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1 0")); // Spaces in the middle

        // EP: number larger than Integer.MAX_VALUE
        assertFalse(StringUtil.isNonZeroUnsignedInteger(Long.toString(Integer.MAX_VALUE + 1)));

        // EP: valid numbers, should return true
        assertTrue(StringUtil.isNonZeroUnsignedInteger("1")); // Boundary value
        assertTrue(StringUtil.isNonZeroUnsignedInteger("10"));
    }


    //---------------- Tests for containsWordIgnoreCase --------------------------------------

    /*
     * Invalid equivalence partitions for word: null, empty, multiple words
     * Invalid equivalence partitions for sentence: null
     * The four test cases below test one invalid input at a time.
     */

    @Test
    public void containsWordIgnoreCase_nullWord_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase("typical sentence", null));
    }

    @Test
    public void containsWordIgnoreCase_emptyWord_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter cannot be empty", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "  "));
    }

    @Test
    public void containsWordIgnoreCase_multipleWords_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter should be a single word", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "aaa BBB"));
    }

    @Test
    public void containsWordIgnoreCase_nullSentence_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase(null, "abc"));
    }

    /*
     * Valid equivalence partitions for word:
     *   - any word
     *   - word containing symbols/numbers
     *   - word with leading/trailing spaces
     *
     * Valid equivalence partitions for sentence:
     *   - empty string
     *   - one word
     *   - multiple words
     *   - sentence with extra spaces
     *
     * Possible scenarios returning true:
     *   - matches first word in sentence
     *   - last word in sentence
     *   - middle word in sentence
     *   - matches multiple words
     *
     * Possible scenarios returning false:
     *   - query word matches part of a sentence word
     *   - sentence word matches part of the query word
     *
     * The test method below tries to verify all above with a reasonably low number of test cases.
     */

    @Test
    public void containsWordIgnoreCase_validInputs_correctResult() {

        // Empty sentence
        assertFalse(StringUtil.containsWordIgnoreCase("", "abc")); // Boundary case
        assertFalse(StringUtil.containsWordIgnoreCase("    ", "123"));

        // Matches a partial word only
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bb")); // Sentence word bigger than query word
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bbbb")); // Query word bigger than sentence word

        // Matches word in the sentence, different upper/lower case letters
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc", "Bbb")); // First word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc@1", "CCc@1")); // Last word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("  AAA   bBb   ccc  ", "aaa")); // Sentence has extra spaces
        assertTrue(StringUtil.containsWordIgnoreCase("Aaa", "aaa")); // Only one word in sentence (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "  ccc  ")); // Leading/trailing spaces

        // Matches multiple words in sentence
        assertTrue(StringUtil.containsWordIgnoreCase("AAA bBb ccc  bbb", "bbB"));
    }

    //---------------- Tests for containsSubstringIgnoreCase -------------------------------

    @Test
    public void containsSubstringIgnoreCase_nullInputs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsSubstringIgnoreCase(null, "abc"));
        assertThrows(NullPointerException.class, () -> StringUtil.containsSubstringIgnoreCase("abc", null));
    }

    @Test
    public void containsSubstringIgnoreCase_emptySubstring_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Substring parameter cannot be empty", ()
            -> StringUtil.containsSubstringIgnoreCase("typical sentence", ""));
    }

    @Test
    public void containsSubstringIgnoreCase_validInputs_correctResult() {
        assertTrue(StringUtil.containsSubstringIgnoreCase("Tan Ah Kow", "an"));
        assertTrue(StringUtil.containsSubstringIgnoreCase("Anita Tan", "AN"));
        assertTrue(StringUtil.containsSubstringIgnoreCase("Alice (friend)", "(FRIEND)"));
        assertFalse(StringUtil.containsSubstringIgnoreCase("Alice Bob", "car"));
    }

    //---------------- Tests for normalizeSpaces --------------------------------------

    @Test
    public void normalizeSpaces_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.normalizeSpaces(null));
    }

    @Test
    public void normalizeSpaces_validInputs_correctResult() {
        assertEquals("Butter Cake", StringUtil.normalizeSpaces("Butter Cake")); // already normalized
        assertEquals("Butter Cake", StringUtil.normalizeSpaces("  Butter   Cake \r\n")); // repeated and surrounding
        assertEquals("Butter Cake", StringUtil.normalizeSpaces("Butter\t\u2003\u00A0Cake")); // tab and Unicode spaces
        assertEquals("", StringUtil.normalizeSpaces("\u2003\u3000")); // Unicode spaces only
        assertEquals("Butter\nCake", StringUtil.normalizeSpaces("Butter\nCake")); // line breaks are kept
    }

    @Test
    public void normalizeSpaces_unicodeLineBreakAtEnd_stripped() {
        assertEquals("Butter Cake", StringUtil.normalizeSpaces("Butter Cake\u0085"));
    }

    //---------------- Tests for stripSpaces --------------------------------------

    @Test
    public void stripSpaces_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.stripSpaces(null));
    }

    @Test
    public void stripSpaces_validInputs_correctResult() {
        assertEquals("Call on arrival", StringUtil.stripSpaces("Call on arrival")); // nothing to strip
        assertEquals("Call on arrival", StringUtil.stripSpaces(" \t Call on arrival \r\n")); // ASCII whitespace
        // No-break spaces, which String.strip() keeps
        assertEquals("Call on arrival", StringUtil.stripSpaces("\u00A0Call on arrival\u202F\u2007"));
        assertEquals("Call on arrival", StringUtil.stripSpaces("\u001FCall on arrival")); // only strip() removes it
        assertEquals("Call on arrival", StringUtil.stripSpaces("Call on arrival\u0085")); // only Unicode counts it
        assertEquals("", StringUtil.stripSpaces("\u00A0\u2003")); // Unicode spaces only
    }

    @Test
    public void stripSpaces_spacesInside_kept() {
        assertEquals("Call  on\u00A0arrival", StringUtil.stripSpaces("\u2003Call  on\u00A0arrival\u2003"));
    }

    //---------------- Tests for getDetails --------------------------------------

    /*
     * Equivalence Partitions: null, valid throwable object
     */

    @Test
    public void getDetails_exceptionGiven() {
        assertTrue(StringUtil.getDetails(new FileNotFoundException("file not found"))
            .contains("java.io.FileNotFoundException: file not found"));
    }

    @Test
    public void getDetails_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.getDetails(null));
    }

}
