package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.search.SearchCriteria;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private ClearCommand pendingClearCommand;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        requireNonNull(commandText);
        if (commandText.isEmpty() && !isClearConfirmationPending()) {
            return new CommandResult("");
        }
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        // A pending clear consumes the next input so commands typed during confirmation are not run accidentally.
        if (isClearConfirmationPending()) {
            ClearCommand clearCommand = pendingClearCommand;
            pendingClearCommand = null;
            clearCommand.cancelConfirmation();
            if (commandText.strip().equalsIgnoreCase(ClearCommand.CONFIRMATION_INPUT)) {
                return executeAndSave(clearCommand);
            }
            return new CommandResult(ClearCommand.MESSAGE_CONFIRMATION_CANCELLED);
        }

        Command command = addressBookParser.parseCommand(commandText);
        if (command instanceof ClearCommand) {
            // Defer the destructive command until the user explicitly confirms it.
            pendingClearCommand = (ClearCommand) command;
            pendingClearCommand.requestConfirmation();
            return new CommandResult(ClearCommand.MESSAGE_CONFIRMATION_REQUIRED);
        }

        return executeAndSave(command);
    }

    private boolean isClearConfirmationPending() {
        return pendingClearCommand != null && pendingClearCommand.isConfirmationPending();
    }

    private CommandResult executeAndSave(Command command) throws CommandException {
        // Only executed commands reach persistence; prompts and cancellations return before this method.
        CommandResult commandResult = command.execute(model);
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public SearchCriteria getActiveSearchCriteria() {
        return model.getActiveSearchCriteria();
    }

    @Override
    public ReadOnlyObjectProperty<SearchCriteria> activeSearchCriteriaProperty() {
        return model.activeSearchCriteriaProperty();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
