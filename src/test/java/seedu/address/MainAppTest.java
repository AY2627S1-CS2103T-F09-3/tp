package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class MainAppTest {

    @Test
    public void getAddressBookFilePath_resolvesRelativeToCodeSource() {
        Path path = MainApp.getAddressBookFilePath().normalize();

        assertEquals("addressbook.json", path.getFileName().toString());
        assertEquals("data", path.getParent().getFileName().toString());
    }
}
