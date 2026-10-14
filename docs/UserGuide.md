---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# AB-3 User Guide

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `edit INDEX [n/NAME] [p/PHONE_NUMBER]` can be used as `edit 1 n/John Doe` or as `edit 1 p/91234567`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[MORE_KEYWORDS]...` may be omitted, or written as `Mary` or `Mary Jane`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book using their name, phone number, email address, and address.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS`

* All four fields (`n/`, `p/`, `e/`, and `a/`) must be provided.
* Names must contain at least one English letter (`A-Z` or `a-z`) and may contain only English letters, spaces, straight apostrophes (`'`) and hyphens (`-`). Accented letters, non-English characters, digits and other symbols are rejected. For example, `Mary-Jane O'Brien`, `Li` and `A` are valid names; `José Tan` and `---` are invalid.
* Leading and trailing spaces are removed, and consecutive spaces become a single space. Capitalization is preserved. For example, `n/  Mary-Jane   O'Brien  ` is stored as `Mary-Jane O'Brien`.
* A person is a duplicate if their name matches an existing person's name after ignoring capitalization and normalizing spaces. For example, `John Doe`, `john doe` and `  JOHN   DOE  ` refer to the same name. Phone number, email, address and tags do not affect this check.
* Duplicate checks include all persons in the address book, even those hidden by a `find` command.
* New persons are added without tags or a preorder.

Examples:

* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe e/betsycrowe@example.com a/Newgate Prison p/81234567`
* `add n/Mary-Jane O'Brien p/98765432 e/maryjane@example.com a/123 Baker Street`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits the name, phone number, email address, or address of an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS]`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of `n/`, `p/`, `e/`, or `a/` must be provided.
* Fields you provide replace their existing values. Fields you omit remain unchanged.
* The name validation and spacing rules described for `add` also apply to `edit`.
* You can retain a person's own name or change its capitalization or spacing while editing. A name matching another person's name is rejected, including persons hidden by a `find` command. Other contact details do not affect this duplicate check.
* Existing tags and preorders are always preserved.

Examples:

*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower` Edits the name of the 2nd person to be `Betsy Crower`, preserving all other details and tags.
*  `edit 1 n/JOHN DOE` Changes the capitalization of the 1st person's name if they are already named `John Doe`.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Adding or updating a preorder: `order`

Records or updates a customer's current preorder. Each customer can have at most one current preorder.

Format: `order INDEX [d/DETAILS] [s/STATUS]`

* `INDEX` must be a positive integer referring to a customer in the **currently displayed list**, including results of `find` or `track`.
* For a customer **without a preorder**, both `d/DETAILS` and `s/STATUS` are required.
* For a customer **with a preorder**, supply either field or both. Supplied fields replace their existing values; omitted fields remain unchanged. Contact details and tags are preserved.
* `DETAILS` must be non-blank, on a single line, and at most 200 characters long. Leading and trailing whitespace is removed.
* `STATUS` must be `PENDING`, `PREPARED`, or `COMPLETED`. Status input is case-insensitive, so `s/pending`, `s/Prepared`, and `s/COMPLETED` are all accepted.
* A supplied prefix must have a value. For example, `order 1 d/` is invalid.
* Each prefix may appear at most once and may appear in any order
* Put a space before each prefix.
* `order INDEX` without either field leaves an existing preorder unchanged and reports `No preorder changes specified.` For a customer without a preorder, it reports that both fields are required.
* Adding or updating an order preserves the current filter. If a status change means the customer no longer matches a `track` filter, their card disappears from that view. Check the displayed indices again before your next command. Use `list` to show all customers. A command with neither field leaves the current list unchanged.

The customer's card shows an order box with `Order:` and a coloured status badge on the first row, followed by the details as plain text. Customers without preorders have no order box.

![Customer cards showing preorder details and colour-coded status badges](images/uiWithPreorderBox.png)

Status | Meaning | Badge colour
-------|---------|-------------
`PENDING` | Recorded but not ready | Red
`PREPARED` | Ready for collection or delivery | Amber
`COMPLETED` | Collected or delivered | Green

You can change a preorder to any of these statuses. Completing an order keeps its details; updating it later replaces the supplied fields rather than creating an order history.

Examples (assuming customer 1 exists):

* `order 1 d/2 chocolate cakes, no nuts s/PENDING` creates a preorder, or replaces both fields of an existing preorder.
* `order 1 d/3 chocolate cakes, no nuts` changes only the details of an existing preorder.
* `order 1 s/PREPARED` changes only the status of an existing preorder.
* `order 1 s/COMPLETED` marks the existing preorder as completed.

### Viewing preorders: `track`

Format: `track [s/STATUS]`

* `track` shows customers with active preorders (`PENDING` or `PREPARED`).
* `track s/COMPLETED` shows customers with completed preorders. You can also filter by `PENDING` or `PREPARED`; status input is case-insensitive.
* Customers without preorders are excluded. Use `list` to show all customers again.
* To update a result, use its displayed index with `order`. The current filter is preserved; customers whose updated status no longer matches it disappear from the view.

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every successful command. You do not need to save manually.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

Names in the data file follow the same validation and duplicate rules as `add` and `edit`. Extra spaces are normalized when the file is loaded. A file containing names without an English letter, names with unsupported characters (including accented letters and non-English characters), or duplicate names that differ only in capitalization or extra spaces, cannot be loaded. This also applies to data saved by earlier versions.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS` <br> e.g., `add n/James Ho p/92224444 e/jamesho@example.com a/123, Clementi Rd, 1234665`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS]`<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**Order**  | `order INDEX [d/DETAILS] [s/STATUS]` (both fields required for a new preorder)<br> e.g., `order 1 d/2 chocolate cakes s/PENDING`
**Track**  | `track [s/STATUS]`<br> e.g., `track s/PREPARED`
**List**   | `list`
**Help**   | `help`
