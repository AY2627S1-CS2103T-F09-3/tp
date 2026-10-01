package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_unconstrainedText_preservesValue() {
        for (String value : new String[]{"", " ", "Likes baseball! 中文"}) {
            assertEquals(value, new Remark(value).toString());
        }
    }

    @Test
    public void equalsAndHashCode() {
        Remark remark = new Remark("note");
        assertEquals(remark, remark);
        assertEquals(remark, new Remark("note"));
        assertEquals(remark.hashCode(), new Remark("note").hashCode());
        assertNotEquals(remark, new Remark("other"));
        assertNotEquals(remark, null);
        assertNotEquals(remark, "note");
    }
}
