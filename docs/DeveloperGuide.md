---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# LeBake Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-F09-3/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-F09-3/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F09-3/tp/blob/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F09-3/tp/blob/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F09-3/tp/blob/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F09-3/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F09-3/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores each customer's preorder, if any, as a `Preorder` holding the `Item`s they ordered (see [Preorders and items](#preorders-and-items)).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


#### Customer search criteria

The `seedu.address.model.search` package provides reusable search predicates. `FindCommandParser` constructs a
`SearchCriteria` from prefixed customer fields, and `FindCommand` applies it through the existing model filtering API.

`ModelManager` keeps the active `SearchCriteria` in a `ReadOnlyObjectWrapper`. The filtered list's predicate is bound
to this value, so replacing the criteria updates both the search state and the results. All filtering calls supply
structured `SearchCriteria`, rather than an arbitrary predicate. `getActiveSearchCriteria()` returns the current
immutable value, while `activeSearchCriteriaProperty()` exposes read-only observation through both `Model` and
`Logic`. Observers can detect a different search even when its results are identical. This state is kept in memory
and is not saved with customer data.

Each successful `find` or `track` replaces the active criteria. `list` applies `SEARCH_CRITERIA_SHOW_ALL_PERSONS`,
an empty search, to reset the state and show every customer. Rejected commands leave the active criteria unchanged.
`track` wraps the immutable `PreorderStatusPredicate` as a `PersonCriterion`. It accepts a customer when any item
in their preorder has an accepted `ItemStatus`; customers without a preorder or with an empty preorder never match.

Customer changes preserve the active criteria. `addPerson` inserts into the observable backing list, while
`setPerson` replaces an immutable customer; `FilteredList` re-evaluates the affected entry automatically. A new
customer is displayed only if they match, and an edited customer disappears when they stop matching. Tag and
preorder updates follow the same replacement path. Commands resolve their target from the displayed list before
making changes, and later commands use the resulting displayed indexes. Rejected updates preserve both criteria
and results.

`SearchCriteria` implements `Predicate<Person>` and combines `PersonCriterion` objects using **AND**. A customer must
match every criterion. An empty search matches every customer. The constructor defensively copies its criteria into an
immutable set, so changing the source collection cannot change an existing search. Criterion order and duplicate
conditions do not affect matching or equality; rejecting repeated CLI prefixes is the parser's responsibility.

For example, the following predicate matches customers whose name contains `Alice Tan` and who have the tag `regular`:

```java
SearchCriteria criteria = new SearchCriteria(List.of(
        new TextContainsCriterion(TextContainsCriterion.Field.NAME, "Alice Tan"),
        new TagCriterion(new Tag("regular"))));
```

`TextContainsCriterion` supports name, phone, email and address fields. It strips surrounding query whitespace and
matches the entire query as a case-insensitive substring using locale-independent case normalization. Internal spacing
and punctuation remain significant. Queries must be nonblank, but may be fragments such as `8123` or `example.com`;
they do not use the validation rules for creating complete contact fields. `TagCriterion` accepts a validated `Tag` and
uses its complete, case-insensitive equality rule.

To add a search condition with different matching rules, implement `PersonCriterion` and include it in a
`SearchCriteria` collection. The implementation must be immutable and provide value-based `equals` and `hashCode`.
CLI prefixes belong in the parser, not in the criteria. Add the prefix to `FindCommandParser`'s supported-prefix list
and construct its criterion there. The parser accepts each supported prefix once, requires a prefix before the first
value, rejects blank or unsupported parameters, and validates tags using `ParserUtil.parseTag`. It recognizes prefixes
at whitespace boundaries without collapsing internal query spacing. `FindCommand` passes `SearchCriteria` directly to
`Model.updateFilteredPersonList`, which replaces the active criteria on the list backed by the full address book.
There is no need to filter the previous search's results or execute another search command as an intermediate step.

For future multi-item searches, product and status conditions must be grouped inside **one** `PersonCriterion` that
checks whether any single item satisfies both conditions. Separate customer predicates for "has this product" and
"has this status" could incorrectly match different items. Date/method and postal conditions can be separate criteria,
because they describe the preorder or customer rather than an individual item. These extensions, `find s/` support,
final multi-item tracking rules and the active-criteria display remain deferred.

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F09-3/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)
* saves preorders through `JsonAdaptedPreorder` and `JsonAdaptedItem`, and reads preorders saved by v1.3 through `JsonAdaptedLegacyPreorder` (see [Saving preorders](#saving-preorders)).

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Preorders and items

A customer's preorder records the products they ordered. Each customer has at most one `Preorder`, and each
`Preorder` holds the customer's `Item`s in the order they were added.

<puml src="diagrams/PreorderClassDiagram.puml" width="380" />

* `Person` stores its preorder as an `Optional<Preorder>`, which is empty for a customer without one.
* `Preorder` keeps an unmodifiable copy of its items and may have none yet. The number shown before an item on
  the customer's card is its position in this list, starting from 1.
* `Item` has an `ItemName` of up to 200 characters, an `ItemStatus`, and an optional `Quantity` and `Remark`.
  A `Quantity` is a whole number from 1 to 999, and a `Remark` is a special request of up to 100 characters.
* New items always have a quantity. `Item#getQuantity()` is empty only for items converted from v1.3 data, as
  described under "Loading v1.3 data" below.
* `ItemStatus` belongs to each item, so a customer can have one item `PREPARED` while another is still
  `PENDING`. There is no overall status for a customer or a preorder. `track` matches a customer when any item
  has one of the requested statuses.

`Preorder` and `Item` are immutable. A command that changes a customer creates a new `Person` and replaces the old
one through `Model#setPerson`. Commands that do this for other reasons, such as `edit`, `tag` and `untag`, pass the
existing preorder to the new `Person`, so it is not lost.

#### Saving preorders

`JsonAdaptedPerson` saves a customer's preorder in a `"preorders"` list, which holds one `JsonAdaptedPreorder`, or
none for a customer without a preorder. Each `JsonAdaptedPreorder` holds its `JsonAdaptedItem`s, which save each
field as a string. A missing remark, or the missing quantity of a converted item, is saved as `null`.

```json
"preorders" : [ {
  "items" : [ {
    "name" : "Pineapple Tarts",
    "quantity" : "2",
    "status" : "PENDING",
    "remark" : "Less sugar"
  } ]
} ]
```

When the file is loaded, a customer with more than one preorder, a `null` preorder or item, or an item field that
breaks its rules is reported as invalid data, in the same way as an invalid name or phone number. A preorder without
an `"items"` field loads as a preorder with no items.

#### Loading v1.3 data

LeBake v1.3 saved a single `"preorder"` object with free-text `"details"` and one `"status"`.
`JsonAdaptedPerson` still reads that field through `JsonAdaptedLegacyPreorder`, which turns it into a preorder with
one item named after the old details, with the old status. v1.3 did not record a quantity, and guessing one from the
details could be wrong, so the item has no quantity and its card shows `Quantity needed`. The old field is never
written back, so the next save stores the customer in the current format.

#### Design considerations

**Aspect: where the status is kept**

* **Alternative 1 (current choice):** each `Item` has its own `ItemStatus`.
  * Pros: Items that are prepared at different times can be tracked separately.
  * Cons: There is no single status for a customer, so a summary has to look at every item.
* **Alternative 2:** one status per `Preorder`.
  * Pros: Simpler to display and to filter by.
  * Cons: Cannot show that one product is ready while another is not, which is common when a customer orders
    several products.

**Aspect: how a quantity is stored**

* **Alternative 1 (current choice):** a `Quantity` class.
  * Pros: The rule and its error message are in one place, shared by `ParserUtil#parseQuantity` and the JSON
    loader. An item without a quantity is an empty `Optional<Quantity>`, the same way an item without a remark is
    an empty `Optional<Remark>`.
  * Cons: One more small class.
* **Alternative 2:** a plain `int`.
  * Pros: Less code.
  * Cons: Each place that creates an item has to repeat the range check, and a missing quantity needs a special
    value such as 0.

**Aspect: how preorders are saved**

* **Alternative 1 (current choice):** a list of preorders in the data file, while the model allows only one.
  * Pros: Allowing several preorders per customer later will not change the file format again.
  * Cons: The loader has to reject a list with more than one preorder.
* **Alternative 2:** a single `"preorder"` object, as in v1.3.
  * Pros: Matches the model exactly.
  * Cons: Supporting several preorders later would need another conversion of saved data.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a tech-savvy independent home baker
* manages a high volume of seasonal pre-orders
* handles customer and order management independently
* wants to spend less time on administrative tasks and more time baking
* is reasonably comfortable using CLI apps

**Value proposition**: LeBake helps solo home bakers manage seasonal pre-orders efficiently, so they can spend less time juggling customers and more time baking.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                               | I want to …                                                                       | So that I can …                                                     |
|----------|------------------------------------------------------|-----------------------------------------------------------------------------------|---------------------------------------------------------------------|
| `* * *`  | first-time home baker                                | view usage instructions                                                           | learn how to manage my customer contacts                            |
| `* * *`  | home baker                                           | add a customer with their name, phone number, email address, and delivery address | keep their essential contact information in one place               |
| `* * *`  | home baker                                           | list all saved customers                                                          | see everyone in my customer address book                            |
| `* * *`  | home baker                                           | view a customer’s complete profile                                                | retrieve their contact details when needed                          |
| `* * *`  | home baker                                           | edit a customer’s details                                                         | keep their contact information accurate                             |
| `* * *`  | home baker                                           | find customers by name                                                            | retrieve a customer without scanning the entire address book        |
| `* * *`  | home baker                                           | delete a customer profile                                                         | remove records I no longer need                                     |
| `* * *`  | home baker                                           | clear all customer records                                                        | start again with an empty address book when necessary               |
| `* * *`  | home baker                                           | assign tags to customers                                                          | organise contacts into meaningful groups                            |
| `* * *`  | busy home baker                                      | have my changes saved automatically                                               | keep my customer information after closing the application         |
| `* * *`  | returning home baker                                 | retrieve my previously saved contacts after reopening LeBake                      | continue where I left off                                           |
| `* * *`  | keyboard-oriented home baker                         | exit LeBake using a command                                                       | complete my workflow without using a mouse                          |
| `* * *`  | home baker                                           | add or remove tags from an existing customer                                      | keep the customer’s classifications current                         |
| `* * *`  | home baker entering contacts quickly                 | be warned about possible duplicate profiles                                       | avoid recording the same customer twice                             |
| `* * *`  | home baker                                           | record a concise seasonal pre-order summary in a customer’s profile               | keep the order connected to the correct customer                    |
| `* * *`  | home baker                                           | update a customer’s current pre-order requirements                                | record later changes accurately                                     |
| `* * *`  | home baker preparing an order                        | view its requirements together with the customer’s contact and address details    | avoid searching in separate places                                  |
| `* * *`  | home baker                                           | record a customer’s fulfilment status                                             | know whether their order is pending, prepared, or completed         |
| `* * *`  | home baker during a busy season                      | filter customer profiles by fulfilment status                                     | focus on customers requiring the same next action                   |
| `* * *`  | home baker                                           | view only customers with active pre-orders                                        | focus on active orders without distraction from inactive contacts   |
| `* * *`  | keyboard-oriented home baker                         | perform common customer-management tasks without using a mouse                    | work efficiently through the CLI                                    |
| `* * *`  | home baker with a large customer base                | receive search and update results promptly                                        | keep using LeBake efficiently during peak periods                   |
| `* *`    | home baker                                           | find a customer by phone number                                                   | identify someone who contacts me by phone                           |
| `* *`    | home baker                                           | find a customer by email address                                                  | identify someone from an email enquiry                              |
| `* *`    | home baker who remembers only part of a name         | search using partial and case-insensitive text                                    | still locate the correct customer                                   |
| `* *`    | home baker with many customers                       | sort customer profiles alphabetically                                             | browse them predictably                                             |
| `* *`    | home baker                                           | filter customers by tag                                                           | focus on a relevant group of contacts                               |
| `* *`    | home baker                                           | record notes about a customer                                                     | remember information relevant to serving them                       |
| `* *`    | home baker serving repeat customers                  | record their contact and delivery preferences                                     | provide consistent service                                          |
| `* *`    | home baker                                           | archive an inactive customer without deleting them                                | keep my active address book free of old contacts                    |
| `* *`    | home baker                                           | restore an archived customer                                                      | serve a returning customer without re-entering their details        |
| `* *`    | home baker                                           | identify customer profiles with missing essential information                     | complete them before fulfilment begins                              |
| `* *`    | home baker migrating from another system             | import existing customer contacts                                                 | avoid re-entering every profile manually                            |
| `* *`    | home baker                                           | export my customer contacts                                                       | keep a backup or use them outside LeBake                            |
| `* *`    | home baker                                           | record delivery instructions with an address                                      | remember access details such as gate codes or drop-off directions   |
| `* *`    | home baker preparing deliveries                      | mark an address as verified or unverified                                         | identify addresses that still require confirmation                  |
| `* *`    | home baker                                           | find customers using an address or postal-code keyword                            | retrieve contacts based on delivery location                        |
| `* *`    | home baker planning deliveries                       | filter customers by neighbourhood or postal area                                  | focus on customers in the same vicinity                             |
| `* *`    | home baker preparing a seasonal delivery run         | view customers who do not have a delivery address                                 | obtain the missing information in advance                           |
| `* *`    | home baker                                           | group customer contacts by delivery area                                          | organise delivery batches efficiently                               |
| `* *`    | home baker making deliveries                         | produce a list containing selected customers’ names, phone numbers, and addresses | have the necessary contact information on hand during delivery      |
| `* *`    | home baker                                           | record customer-provided special requirements with their pre-order                | refer to them while fulfilling it                                   |
| `* *`    | home baker                                           | associate customers with a seasonal campaign                                      | distinguish orders from different occasions                         |
| `* *`    | home baker serving a repeat customer                 | view their previous seasonal order summaries                                      | understand their past preferences                                   |
| `*`      | home baker                                           | store an alternative contact number for a customer                                | reach them another way if necessary                                 |
| `*`      | home baker who makes occasional input mistakes       | undo my most recent change                                                        | recover quickly without reconstructing the original profile         |
| `*`      | home baker serving customers at different locations  | store multiple addresses for a customer                                           | retain their commonly used delivery destinations                    |
| `*`      | home baker                                           | mark one address as a customer’s preferred delivery address                       | know which address to use by default                                |
| `*`      | home baker planning deliveries                       | sort customers by postal area                                                     | see nearby addresses together                                       |
| `*`      | home baker processing many contacts                  | add multiple customer profiles in one batch                                       | spend less time on initial data entry                               |
| `*`      | home baker managing a seasonal campaign              | apply or remove a tag from multiple customers at once                             | organise large groups efficiently                                   |
| `*`      | experienced LeBake user                              | reuse previously entered commands                                                 | repeat common operations with fewer keystrokes                      |
| `*`      | experienced LeBake user                              | use short forms for frequently used operations                                    | do repetitive customer-management work faster                       |

### Use cases

**Use case: U1. Add a customer**\
**System: LeBake**\
**Actor: User**\
**MSS**

1. User requests to add a customer.
2. LeBake adds the customer.

    Use case ends.

**Extensions**

* 1a. The given input is invalid.

    * 1a1. LeBake shows an error message.

      Use case resumes at step 1.

* 1b. The customer already exists in LeBake.

    * 1b1. LeBake shows an error message.

      Use case resumes at step 1.

**Use case: U2. Find a customer**\
**System: LeBake**\
**Actor: User**\
**MSS**

1. User searches for a customer.
2. LeBake shows a list of matching customers.

    Use case ends.

**Extensions**

* 1a. No customers match the search.

    * 1a1. LeBake shows an empty list.

      Use case ends.

* 2a. The search returns other customers, but not the one the user wants.

  Use case ends.

**Use case: U3. Delete a customer**\
**System: LeBake**\
**Actor: User**\
**MSS**

1. User finds a customer (<u>U2. Find a customer</u>).
2. User requests to delete that customer from the displayed list.
3. LeBake deletes the customer.

   Use case ends.

**Extensions**

* 1a. The intended customer is not found.

  Use case ends.

* 2a. The customer selection is invalid.

    * 2a1. LeBake shows an error message.

      Use case resumes at step 2.

**Use case: U4. Record a customer's pre-order**\
**System: LeBake**\
**Actor: User**\
**MSS**

1. User finds a customer (<u>U2. Find a customer</u>).
2. User selects a customer and requests to record a pre-order with order details and a fulfilment status.
3. LeBake records and displays the pre-order.

    Use case ends.

**Extensions**

* 1a. The intended customer is not found.

  Use case ends.

* 2a. The customer selection or order information is invalid.

    * 2a1. LeBake shows an error message.

      Use case resumes at step 2.

* 2b. The customer already has a pre-order.

    * 2b1. LeBake updates and displays the existing pre-order.

      Use case ends.

**Use case: U5. Update a pre-order's fulfilment status**\
**System: LeBake**\
**Actor: User**\
**MSS**

1. User requests to view pre-orders with a chosen fulfilment status.
2. LeBake shows customers with pre-orders in that status.
3. User selects a customer and requests to change the pre-order to a valid fulfilment status.
4. LeBake updates and displays the pre-order's fulfilment status.

    Use case ends.

**Extensions**

* 1a. No pre-orders match the chosen status.

    * 1a1. LeBake shows an empty list.

      Use case ends.

* 3a. The customer selection is invalid.

    * 3a1. LeBake shows an error message.

      Use case resumes at step 3.

*{More to be added}*

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should support storing up to 1,000 customer details.
3.  Should display the result or an error message within 3 seconds of submitting a command.
4.  User with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
5.  All core customer and pre-order management functionalities should remain available without an internet connection.
6.  Invalid commands should produce a message identifying the error and explaining the correct syntax or accepted values. 
7.  If changes cannot be saved, LeBake should clearly inform the user that those changes have not been saved. 
8.  Commands operating on the same customer fields should use consistent parameter names and field formats, unless a difference is explicitly documented.

*{More to be added}*

### Glossary

* **Active order**: A pre-order with at least one item whose fulfilment status is `PENDING` or `PREPARED`
* **Archived customer**: A customer hidden from the active customer list but kept, so that it can be restored for a future seasonal campaign
* **Customer**: A person whose contact and delivery details are stored in LeBake, represented by the `Person` class in the code
* **Duplicate customer**: A customer with the same name as an existing customer, ignoring case and extra spaces (phone number, email and address are not compared)
* **Fulfilment status**: The stage an item in a pre-order has reached, which is `PENDING` (recorded but not ready), `PREPARED` (ready for collection or delivery), or `COMPLETED` (collected or delivered). Each item has its own status, represented by `ItemStatus` in the code; a pre-order has no overall status
* **Item**: One product in a pre-order, with its name, quantity, fulfilment status and an optional remark
* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Postal area**: A group of nearby addresses that share the same leading digits of their postal code, used to group deliveries
* **Pre-order**: An order placed in advance for a later collection or delivery, made up of the items the customer ordered, with each customer having at most one current pre-order
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Seasonal campaign**: A period of high order demand tied to an occasion, such as Chinese New Year or Christmas

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Preserving searches after customer changes

Use a test address book without customers whose names contain `SearchTest` or `OtherTest`.

1. Add two customers:
   * `add n/SearchTest Alice p/81111111 e/searchalice@example.com a/Test Street`
   * `add n/SearchTest Ben p/82222222 e/searchben@example.com a/Test Street`
1. Run `find n/SearchTest`.<br>
   Expected: Only those two customers are shown.
1. Run `add n/SearchTest Cara p/83333333 e/searchcara@example.com a/Test Street`.<br>
   Expected: The new customer joins the results; only the three matching customers are shown.
1. Run `add n/OtherTest Dana p/84444444 e/otherdana@example.com a/Other Street`.<br>
   Expected: The customer is saved, and the three search results remain displayed.
1. Run `edit 1 n/OtherTest Alice`.<br>
   Expected: The edited customer disappears. `SearchTest Ben` and `SearchTest Cara` remain at indexes 1 and 2.
1. Run `edit 1 p/89999999`.<br>
   Expected: Ben's phone number changes; only Ben and Cara remain displayed.
1. Run `list`.<br>
   Expected: All customers are shown, including `OtherTest Alice` and `OtherTest Dana`.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Tagging a person

1. Tagging a person while all persons are being shown

   1. Prerequisites: Start with the sample data. List all persons using the `list` command. The first person is `Alex Yeoh`, who has the tag `friends`.

   1. Test case: `tag 1 t/regular`<br>
      Expected: `regular` is added to the first person's tags. The status message shows `Added tag(s) [regular] to Alex Yeoh`.

   1. Test case: `tag 1 t/FRIENDS`<br>
      Expected: The person's tags are unchanged. The status message shows `Alex Yeoh already has tag [FRIENDS]. No changes made.`

   1. Test case: `tag 1 t/gluten-free`<br>
      Expected: No tag is added. The status message shows `Tag names must not be blank and must contain only letters (A-Z, a-z) and digits (0-9), with no spaces or special characters. Examples: VIP, friends, regular123.`

   1. Test case: `tag 1 t/vip t/regular`<br>
      Expected: No tag is added. The status message shows `Multiple values specified for the following single-valued field(s): t/`.

   1. Test case: `tag 0 t/vip`<br>
      Expected: No tag is added. The status message shows `Invalid command format!`, followed by the usage instructions for `tag`.

   1. Other incorrect tag commands to try: `tag`, `tag 1`, `tag t/vip`, `tag x t/vip`<br>
      Expected: Similar to previous.

   1. Test case: `tag 0 t/!`<br>
      Expected: No tag is added. The index format error and usage instructions are shown, rather than the tag constraint message, because the index format is checked first.

   1. Test case: `tag 100 t/vip` (where 100 is larger than the list size)<br>
      Expected: No tag is added. The status message shows `The person index provided is invalid.`

1. Tagging a person in a filtered list

   1. Prerequisites: Find a person using `find n/Bernice`. Only `Bernice Yu` is shown.

   1. Test case: `tag 1 t/vip`<br>
      Expected: `vip` is added to `Bernice Yu`'s tags. The status message shows `Added tag(s) [vip] to Bernice Yu`. Only `Bernice Yu` remains displayed; the filter is preserved.

### Untagging a person

Run each group below independently, starting with the sample data unless stated otherwise. Within a group, run the test cases in order. For every rejected command, verify that all persons' tags, contact details, preorders, and the displayed list remain unchanged.

1. Removing tags while all persons are being shown

   1. Prerequisites: Run `list`. The first person is `Alex Yeoh`, who has the tag `friends`. Run `tag 1 t/regular` to give Alex a second tag.

   1. Test case: `untag 1 t/FRIENDS`<br>
      Expected: The `friends` tag is removed despite the different capitalization. The `regular` tag and Alex's contact details remain unchanged. The status message shows `Removed tag(s) [FRIENDS] from Alex Yeoh`.

   1. Test case: `untag 1 t/regular`<br>
      Expected: Alex has no tags left, but remains in the list with the same contact details. The status message shows `Removed tag(s) [regular] from Alex Yeoh`.

   1. Test case: Repeat `untag 1 t/regular`.<br>
      Expected: No data is changed. The status message shows `Alex Yeoh does not have tag [regular]`.

1. Rejecting invalid input

   1. Prerequisites: Run `list` with the sample persons displayed.

   1. Test cases: `untag 0 t/friends`, `untag -1 t/friends`, `untag abc t/friends`<br>
      Expected: Each command shows `Invalid command format!`, followed by the usage instructions for `untag`.

   1. Test cases: `untag`, `untag t/friends`, `untag 1`<br>
      Expected: Each command shows the same format error and usage instructions.

   1. Test cases: `untag 1 t/`, `untag 1 t/close friends`, `untag 1 t/gluten-free`<br>
      Expected: Each command shows `Tag names must not be blank and must contain only letters (A-Z, a-z) and digits (0-9), with no spaces or special characters. Examples: VIP, friends, regular123.`

   1. Test case: `untag 1 t/friends t/regular`<br>
      Expected: The status message shows `Multiple values specified for the following single-valued field(s): t/`.

   1. Test case: `untag 0 t/!`<br>
      Expected: The index format error and usage instructions are shown, rather than the tag constraint message, because the index format is checked first.

   1. Test case: `untag 100 t/friends` (where 100 is larger than the displayed list size)<br>
      Expected: The status message shows `The person index provided is invalid.`

1. Removing a tag in a filtered list

   1. Prerequisites: Run `find n/Bernice`. Only `Bernice Yu` is shown, at index 1, with the tags `colleagues` and `friends`.

   1. Test case: `untag 2 t/friends`<br>
      Expected: The status message shows `The person index provided is invalid.`, even though the full address book contains more than one person.

   1. Test case: `untag 1 t/FRIENDS`<br>
      Expected: Bernice's `friends` tag is removed and `colleagues` remains. The status message shows `Removed tag(s) [FRIENDS] from Bernice Yu`. Only Bernice remains displayed; the filter is preserved. Other persons are unchanged.

   1. Test case: `untag 1 t/FRIENDS` again.<br>
      Expected: The status message shows `Bernice Yu does not have tag [FRIENDS]`. Only Bernice remains displayed.

   1. Test case: Run `find n/NoMatchingCustomer`, confirm that the displayed list is empty, then run `untag 1 t/friends`.<br>
      Expected: The status message shows `The person index provided is invalid.` The list remains empty and no data is changed.

1. Preserving a preorder and saving tag removal

   1. Prerequisites: Use a saved customer with a preorder. Run `list`, note their displayed index as `INDEX`, and record their contact details, preorder details, and fulfilment status. Run `tag INDEX t/untagtest`, replacing `INDEX` with that number.

   1. Test case: `untag INDEX t/untagtest`<br>
      Expected: Only `untagtest` is removed. Other tags, contact details, preorder details, and fulfilment status are unchanged.

   1. Test case: Close and relaunch the application, then locate the same customer.<br>
      Expected: The removed tag remains absent. The customer's other tags, contact details, and preorder are preserved.

### Deleting a preorder

Start with the sample data. List all customers using the `list` command. The first customer is `Alex Yeoh`, whose preorder has two items.

1. Run `deleteorder 1`.<br>
   Expected: The status message shows `Deleted preorder for Alex Yeoh`. Both item chips disappear from the card. The name, phone number, email, address and tags are unchanged.
1. Run `deleteorder 1` again.<br>
   Expected: The status message shows `Alex Yeoh has no preorder to delete.` Nothing changes.
1. Try `deleteorder 0`, `deleteorder` and `deleteorder 1 2`.<br>
   Expected: Nothing changes. The status message shows an invalid command format error, followed by the correct format of the `deleteorder` command.
1. Run `deleteorder 100` (where 100 is larger than the list size).<br>
   Expected: Nothing changes. The status message shows `The person index provided is invalid.`
1. Run `track`, then `deleteorder` with the displayed index of `Charlotte Oliveiro`.<br>
   Expected: The status message shows `Deleted preorder for Charlotte Oliveiro`. `Charlotte Oliveiro` disappears from the tracked view. Run `list` to confirm that `Charlotte Oliveiro` is still a customer, with no preorder chips.

### Loading preorder data

These steps edit the data file, so use a copy of LeBake in an empty folder. Launch it once and run `list` to create
`data/addressbook.json`, then close LeBake before each edit.

1. Loading a preorder saved by v1.3

   1. Prerequisites: In `data/addressbook.json`, replace the first customer's `"preorders"` list with
      `"preorder" : { "details" : "2 chocolate cakes", "status" : "PENDING" }`.

   1. Test case: Launch LeBake.<br>
      Expected: The first customer's card shows one item, `2 chocolate cakes`, with `Quantity needed` and the
      status `PENDING`. Their other details are unchanged.

   1. Test case: Run `list`, close LeBake, then open `data/addressbook.json` again.<br>
      Expected: The customer has a `"preorders"` list holding one item whose `"quantity"` is `null`, and no
      `"preorder"` field.

1. Loading a data file with an invalid item

   1. Prerequisites: In `data/addressbook.json`, change one item's `"quantity"` to `"0"`. Keep a copy of the file.

   1. Test case: Launch LeBake.<br>
      Expected: The data file cannot be loaded, so LeBake starts with an empty customer list, and the log warns
      that the data file could not be loaded. Restore the file from your copy before running a command, as the
      next command saves the empty list over it.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
