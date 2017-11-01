# eldonng
###### /java/seedu/address/commons/events/ui/GroupPanelSelectionChangedEvent.java
``` java
/**
 * Represents a selection change in the Group List Panel
 */
public class GroupPanelSelectionChangedEvent extends BaseEvent {

    private final GroupCard newSelection;

    public GroupPanelSelectionChangedEvent(GroupCard newSelection) {
        this.newSelection = newSelection;
    }
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public GroupCard getNewSelection() {
        return newSelection;
    }
}
```
###### /java/seedu/address/commons/events/ui/NewGroupListEvent.java
``` java
/**
 * Indicates that the group list has been changed.
 */
public class NewGroupListEvent extends BaseEvent {

    private ObservableList<ReadOnlyGroup> groups;
    private ObservableList<ReadOnlyPerson> persons;

    public NewGroupListEvent(ObservableList<ReadOnlyGroup> groups, ObservableList<ReadOnlyPerson> persons) {
        this.groups = groups;
        this.persons = persons;
    }

    public ObservableList<ReadOnlyPerson> getPersonsList() {
        return persons;
    }

    public ObservableList<ReadOnlyGroup> getGroupsList() {
        return groups;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
```
###### /java/seedu/address/commons/events/ui/NewPersonListEvent.java
``` java
/**
 * Indicates that the person list has been changed.
 */
public class NewPersonListEvent extends BaseEvent {

    private ObservableList<ReadOnlyPerson> persons;

    public NewPersonListEvent(ObservableList<ReadOnlyPerson> persons) {
        this.persons = persons;
    }

    public ObservableList<ReadOnlyPerson> getPersonsList() {
        return persons;
    }
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
```
###### /java/seedu/address/logic/commands/CreateGroupCommand.java
``` java
/**
 * Creates a group in the address book
 */
public class CreateGroupCommand extends UndoableCommand {
    public static final String COMMAND_WORD = "creategroup";
    public static final String MESSAGE_SUCCESS = "New group added: %1$s, with %2$s member(s)";
    public static final String MESSAGE_DUPLICATE_GROUP = "This group already exists in the address book";

    /**
     * Shows message usage for Group Command
     */
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds one or more people into a group\n"
            + "Parameters: "
            + PREFIX_NAME + "GROUP NAME "
            + PREFIX_INDEX + "INDEX...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "CS2103 Project "
            + PREFIX_INDEX + "1 3 4";

    private GroupName groupName;
    private List<Index> indexes;

    public CreateGroupCommand(GroupName name, List<Index> indexList) {
        groupName = name;
        indexes = indexList;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        requireNonNull(model);
        List<ReadOnlyPerson> lastShownList = model.getFilteredPersonList();
        List<ReadOnlyPerson> groupMembers = new ArrayList<>();

        for (Index index: indexes) {
            if (index.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }

            ReadOnlyPerson personToAdd = lastShownList.get(index.getZeroBased());
            if (!groupMembers.contains(personToAdd)) {
                groupMembers.add(personToAdd);
            }
        }
        ReadOnlyGroup newGroup = new Group(groupName, groupMembers);
        try {
            model.addGroup(newGroup);
        } catch (DuplicateGroupException dge) {
            throw new CommandException(MESSAGE_DUPLICATE_GROUP);
        }
        model.updateFilteredGroupList(Model.PREDICATE_SHOW_ALL_GROUPS);

        return new CommandResult(String.format(MESSAGE_SUCCESS, groupName, groupMembers.size()));
    }
}
```
###### /java/seedu/address/logic/commands/DeleteGroupCommand.java
``` java
/**
 * Deletes a group from the address book
 */
public class DeleteGroupCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "deletegroup";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the group identified by the index number used in the group list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_GROUP_SUCCESS = "Deleted Group: %1$s";

    private final Index targetIndex;

    public DeleteGroupCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {

        List<ReadOnlyGroup> lastShownList = model.getGroupList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        ReadOnlyGroup groupToDelete = lastShownList.get(targetIndex.getZeroBased());
        try {
            model.deleteGroup(groupToDelete);
        } catch (GroupNotFoundException gnfe) {
            assert false : "The target group cannot be missing";
        }

        return new CommandResult(String.format(MESSAGE_DELETE_GROUP_SUCCESS, groupToDelete.getGroupName().fullName));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DeleteGroupCommand // instanceof handles nulls
                && this.targetIndex.equals(((DeleteGroupCommand) other).targetIndex)); // state check
    }
}
```
###### /java/seedu/address/logic/commands/PinCommand.java
``` java
/**
 * Pins an existing person on top of the address book
 */
public class PinCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "pin";

    /**
     * Shows message usage for pin command
     */
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Pins the selected person identified "
            + "by the index number used in the last person listing.\n "
            + "Parameters: INDEX (must be a positive integer)\n "
            + "Example: " + COMMAND_WORD + " 1 ";

    public static final String MESSAGE_PIN_PERSON_SUCCESS = "Pinned Person: %1$s";
    public static final String MESSAGE_ALREADY_PINNED = "Person is already pinned!";
    public static final String MESSAGE_PIN_PERSON_FAILED = "Pin was unsuccessful";

    private final Index index;

    public PinCommand(Index index) {
        requireNonNull(index);
        this.index = index;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        List<ReadOnlyPerson> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        ReadOnlyPerson personToPin = lastShownList.get(index.getZeroBased());
        try {
            if (personToPin.isPinned()) {
                return new CommandResult(MESSAGE_ALREADY_PINNED);
            } else {
                model.pinPerson(personToPin);
                model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
                return new CommandResult(String.format(MESSAGE_PIN_PERSON_SUCCESS, personToPin));
            }
        } catch (PersonNotFoundException pnfe) {
            throw new CommandException(MESSAGE_PIN_PERSON_FAILED);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof PinCommand // instanceof handles nulls
                && this.index.equals(((PinCommand) other).index)); // state check
    }
}
```
###### /java/seedu/address/logic/commands/SetColourCommand.java
``` java
/**
 * Sets all iteration of the specified tag to the requested colour
 */
public class SetColourCommand extends Command {

    public static final String COMMAND_WORD = "setcolour";

    /**
     * Shows message usage for SetColour Command
     */
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sets colour to all iterations of the tag identified\n"
            + "Parameters: TAG_NAME (must be a valid tag name) COLOUR\n"
            + "Example: " + COMMAND_WORD + " friends blue";

    public static final String SETCOLOUR_SUCCESS = "All tags [%1s] are now coloured %2s";
    public static final String SETCOLOUR_INVALID_COLOUR = "Unfortunately, %1s is unavailable to be set in addressbook";
    private static final String[] colours = {"blue", " red", "brown", "green", "black", "purple", "indigo", "grey",
        "chocolate", "orange", "aquamarine"};

    private String tag;
    private String newColour;

    public SetColourCommand(String tag, String colour) {
        this.tag = tag;
        newColour = colour;
    }

    @Override
    public CommandResult execute() {
        try {
            if (isColourValid()) {
                model.setTagColour(tag, newColour);
                model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
                return new CommandResult(String.format(SETCOLOUR_SUCCESS, tag, newColour));
            }
            return new CommandResult(String.format(SETCOLOUR_INVALID_COLOUR, newColour));
        } catch (IllegalValueException ive) {
            return new CommandResult(ive.getMessage());
        }
    }

    /**
     * Checks whether the requested colour is within String[]
     * @return true is colour is a colour inside the String[]
     */
    private boolean isColourValid() {
        for (String colour: colours) {
            if (colour.equals(newColour)) {
                return true;
            }
        }
        return false;
    }
}
```
###### /java/seedu/address/logic/commands/UnpinCommand.java
``` java
/**
 * Unpins an existing pinned person from the address book
 */
public class UnpinCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "unpin";

    /**
     * Shows message usage for unpin command
     */
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Unpins the selected person identified "
            + "by the index number used in the last person listing.\n "
            + "Parameters: INDEX (must be a positive integer)\n "
            + "Example: " + COMMAND_WORD + " 1 ";

    public static final String MESSAGE_UNPIN_PERSON_SUCCESS = "Unpinned Person: %1$s";
    public static final String MESSAGE_ALREADY_UNPINNED = "Person is not pinned!";
    public static final String MESSAGE_UNPIN_PERSON_FAILED = "Unpin was unsuccessful";

    private final Index index;

    public UnpinCommand(Index index) {
        requireNonNull(index);
        this.index = index;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        List<ReadOnlyPerson> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        ReadOnlyPerson personToUnpin = lastShownList.get(index.getZeroBased());
        try {
            if (personToUnpin.isPinned()) {
                model.unpinPerson(personToUnpin);
                model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
                return new CommandResult(String.format(MESSAGE_UNPIN_PERSON_SUCCESS, personToUnpin));
            } else {
                return new CommandResult(MESSAGE_ALREADY_UNPINNED);
            }
        } catch (PersonNotFoundException pnfe) {
            throw new CommandException(MESSAGE_UNPIN_PERSON_FAILED);
        }

    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UnpinCommand // instanceof handles nulls
                && this.index.equals(((UnpinCommand) other).index)); // state check
    }
}
```
###### /java/seedu/address/logic/parser/DeleteGroupCommandParser.java
``` java
/**
 * Parses input arguments and creates a new DeleteGroupCommand object
 */
public class DeleteGroupCommandParser implements Parser<DeleteGroupCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteGroupCommand
     * and returns an DeleteGroupCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public DeleteGroupCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new DeleteGroupCommand(index);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteGroupCommand.MESSAGE_USAGE));
        }
    }

}
```
###### /java/seedu/address/logic/parser/GroupCommandParser.java
``` java
/**
 * Parses input arguments and create a CreateGroupCommand object
 */
public class GroupCommandParser implements  Parser<CreateGroupCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the GroupCommand
     * and returns an GroupCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public CreateGroupCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_INDEX);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_INDEX)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, CreateGroupCommand.MESSAGE_USAGE));
        }

        try {
            GroupName name = ParserUtil.parseGroupName(argMultimap.getValue(PREFIX_NAME)).get();
            List<Index> indexList = ParserUtil.parseIndexes(argMultimap.getValue(PREFIX_INDEX).get());
            return new CreateGroupCommand(name, indexList);

        } catch (IllegalValueException ive) {
            throw new ParseException(ive.getMessage(), ive);
        }
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
```
###### /java/seedu/address/logic/parser/ParserUtil.java
``` java
    /**
     * Parses a {@code Optional<String> name} into an {@code Optional<Name>} if {@code name} is present.
     * See header comment of this class regarding the use of {@code Optional} parameters.
     */
    public static Optional<GroupName> parseGroupName(Optional<String> name) throws IllegalValueException {
        requireNonNull(name);
        return name.isPresent() ? Optional.of(new GroupName(name.get())) : Optional.empty();
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws IllegalValueException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(new Tag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses a String and checks for validity. Leading and trailing whitespaces will be removed
     * @throws IllegalValueException if specified string is invalid (not 1 of 3 options)
     */
    public static String parseSortType(String sortType) throws IllegalValueException {
        String trimmedSortType = sortType.trim();
        switch (trimmedSortType) {
        case "name":
        case "phone":
        case "email":
            return trimmedSortType;
        default:
            throw new IllegalValueException(MESSAGE_INVALID_SORT);
        }
    }

```
###### /java/seedu/address/logic/parser/ParserUtil.java
``` java
    /**
     * Parses a String and checks whether it has two different .
     */
    public static String[] parseColourCommand(String args) throws IllegalValueException {
        String[] splitArgs = args.trim().split("\\s+");
        if (splitArgs.length == 2) {
            return splitArgs;
        }
        throw new IllegalValueException(MESSAGE_INVALID_ARGUMENTS);
    }

    /**
     * Parses a String argument for tag. Leading and trailing whitespaces will be removed
     */
    public static String parseTag(String tag) {
        String trimmedTag = tag.trim();
        return trimmedTag;

    }

    /**
     * Parses a String argument for a file path destination for Export.
     * Leading and trailing whitespaces will be removed
     */
    public static String parseFilePath(String path) {
        String trimmedPath = path.trim();
        return trimmedPath;
    }
}
```
###### /java/seedu/address/logic/parser/PinCommandParser.java
``` java
/**
 * Parses input arguments and creates a new PinCommand object
 */
public class PinCommandParser implements Parser<PinCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the PinCommand
     * and returns an PinCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public PinCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new PinCommand(index);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, PinCommand.MESSAGE_USAGE));
        }
    }

}

```
###### /java/seedu/address/logic/parser/SetColourCommandParser.java
``` java
/**
 * Parses input arguments and create a new SetColourCommand object.
 */
public class SetColourCommandParser implements Parser<SetColourCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the SetColourCommand
     * and returns an SetColourCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public SetColourCommand parse(String args) throws ParseException {
        try {
            String[] setColourArgs = ParserUtil.parseColourCommand(args);
            return new SetColourCommand(setColourArgs[0], setColourArgs[1]);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SetColourCommand.MESSAGE_USAGE));
        }
    }
}
```
###### /java/seedu/address/logic/parser/UnpinCommandParser.java
``` java
/**
 * Parses input arguments and creates a new UnpinCommand object
 */
public class UnpinCommandParser implements Parser<UnpinCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns an DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public UnpinCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new UnpinCommand(index);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnpinCommand.MESSAGE_USAGE));
        }
    }

}
```
###### /java/seedu/address/model/AddressBook.java
``` java
    /**
     * Adds a group to the address book.
     * @throws DuplicateGroupException if an equivalent group already exists.
     */
    public void addGroup(ReadOnlyGroup g) throws DuplicateGroupException {
        Group newGroup = new Group(g);
        groups.add(newGroup);
    }

```
###### /java/seedu/address/model/AddressBook.java
``` java
    /**
     * Removes a group from the address book
     * @throws GroupNotFoundException if the group is not found
     */
    public boolean deleteGroup(ReadOnlyGroup g) throws GroupNotFoundException {
        if (groups.remove(g)) {
            return true;
        } else {
            throw new GroupNotFoundException();
        }
    }

    //// tag-level operations

    public void addTag(Tag t) throws UniqueTagList.DuplicateTagException {
        tags.add(t);
    }

    //// util methods

    @Override
    public String toString() {
        return persons.asObservableList().size() + " persons, " + tags.asObservableList().size() +  " tags, "
                + groups.asObservableList().size() + " groups.";
        // TODO: refine later
    }

    @Override
    public ObservableList<ReadOnlyPerson> getPersonList() {
        return persons.asObservableList();
    }

    @Override
    public ObservableList<Tag> getTagList() {
        return tags.asObservableList();
    }

```
###### /java/seedu/address/model/AddressBook.java
``` java
    @Override
    public ObservableList<ReadOnlyGroup> getGroupList() {
        return groups.asObservableList();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddressBook // instanceof handles nulls
                && this.persons.equals(((AddressBook) other).persons)
                && this.tags.equalsOrderInsensitive(((AddressBook) other).tags))
                && this.groups.equals(((AddressBook) other).groups);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(persons, groups, tags);
    }

}
```
###### /java/seedu/address/model/group/exceptions/DuplicateGroupException.java
``` java
/**
 * Signals that the operation will result in duplicate Person objects.
 */
public class DuplicateGroupException extends DuplicateDataException {
    public DuplicateGroupException() {
        super("Operation would result in duplicate groups");
    }
}
```
###### /java/seedu/address/model/group/exceptions/GroupNotFoundException.java
``` java
/**
 * Signals that the operation is unable to find the specified group.
 */
public class GroupNotFoundException extends Exception {
}
```
###### /java/seedu/address/model/group/Group.java
``` java
/**
 * Represents a group in the address book
 * Guarantees: details are present and not null, field values are validated.
 */
public class Group implements ReadOnlyGroup {

    private ObjectProperty<GroupName> groupName;
    private ObjectProperty<List<ReadOnlyPerson>> groupMembers;

    public Group(GroupName name, List<ReadOnlyPerson> members) {
        this.groupName = new SimpleObjectProperty<>(name);
        this.groupMembers = new SimpleObjectProperty<>(members);
    }

    public Group(ReadOnlyGroup group) {
        this(group.getGroupName(), group.getGroupMembers());
    }

    @Override
    public ObjectProperty<GroupName> nameProperty() {
        return groupName;
    }

    @Override
    public GroupName getGroupName() {
        return groupName.get();
    }

    @Override
    public ObjectProperty<List<ReadOnlyPerson>> membersProperty() {
        return groupMembers;
    }

    @Override
    public List<ReadOnlyPerson> getGroupMembers() {
        return groupMembers.get();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ReadOnlyGroup // instanceof handles nulls
                && this.isSameStateAs((ReadOnlyGroup) other));
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(groupName, groupMembers);
    }
}
```
###### /java/seedu/address/model/group/GroupName.java
``` java
/**
 * Represents a group's name in the address book
 */
public class GroupName {
    public static final String MESSAGE_NAME_CONSTRAINTS =
            "Person names should only contain alphanumeric characters and spaces, and it should not be blank";

    /*
     * The first character of the address must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String NAME_VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} ]*";

    public final String fullName;

    /**
     * Validates given name.
     *
     * @throws IllegalValueException if given name string is invalid.
     */
    public GroupName(String name) throws IllegalValueException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!isValidName(trimmedName)) {
            throw new IllegalValueException(MESSAGE_NAME_CONSTRAINTS);
        }
        this.fullName = trimmedName;
    }

    /**
     * Returns true if a given string is a valid person name.
     */
    public static boolean isValidName(String test) {
        return test.matches(NAME_VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof GroupName // instanceof handles nulls
                && this.fullName.equals(((GroupName) other).fullName)); // state check
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

    public int compareTo(GroupName compareTarget) {
        return this.toString().compareToIgnoreCase(compareTarget.toString());
    }
}
```
###### /java/seedu/address/model/group/ReadOnlyGroup.java
``` java
/**
 * A read-only immutable interface for a Group in the addressbook.
 * Implementations should guarantee: details are present and not null, field values are validated.
 */
public interface ReadOnlyGroup {
    ObjectProperty<GroupName> nameProperty();
    GroupName getGroupName();
    ObjectProperty<List<ReadOnlyPerson>> membersProperty();
    List<ReadOnlyPerson> getGroupMembers();

    /**
     * Returns true if both have the same state. (interfaces cannot override .equals)
     */
    default boolean isSameStateAs(ReadOnlyGroup other) {
        return other == this // short circuit if same object
                || (other != null // this is first to avoid NPE below
                && other.getGroupName().equals(this.getGroupName())
                && other.getGroupMembers().equals(this.getGroupMembers())); // state checks here onwards

    }
}
```
###### /java/seedu/address/model/group/UniqueGroupList.java
``` java
/**
 * A list of groups that enforces uniqueness between its elements and does not allow nulls.
 *
 * Supports a minimal set of list operations.
 *
 * @see Group#equals(Object)
 * @see CollectionUtil#elementsAreUnique(Collection)
 */
public class UniqueGroupList implements Iterable<Group> {

    private final ObservableList<Group> internalList = FXCollections.observableArrayList();
    // used by asObservableList()
    private final ObservableList<ReadOnlyGroup> mappedList = EasyBind.map(internalList, (group) -> group);

    /**
     * Returns true if the list contains an equivalent group as the given argument.
     */
    public boolean contains(ReadOnlyGroup toCheck) {
        requireNonNull(toCheck);
        return internalList.contains(toCheck);
    }

    /**
     * Adds a group to the list.
     *
     * @throws DuplicateGroupException if the group to add is a duplicate of an existing group in the list.
     */
    public void add(ReadOnlyGroup toAdd) throws DuplicateGroupException {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateGroupException();
        }
        internalList.add(new Group(toAdd));
    }

    /**
     * Removes the equivalent group from the list.
     *
     * @throws GroupNotFoundException if no such group could be found in the list.
     */
    public boolean remove(ReadOnlyGroup toRemove) throws GroupNotFoundException {
        requireNonNull(toRemove);
        final boolean groupFoundAndDeleted = internalList.remove(toRemove);
        if (!groupFoundAndDeleted) {
            throw new GroupNotFoundException();
        }
        return groupFoundAndDeleted;
    }

    /**
     * Removes the equivalent person from all groups
     * @param toRemove
     */
    public void removePerson(ReadOnlyPerson toRemove) {
        requireNonNull(toRemove);
        internalList.forEach(group -> {
            if (group.getGroupMembers().contains(toRemove)) {
                group.getGroupMembers().remove(toRemove);
            }
        });
    }

    public void setGroups(UniqueGroupList replacement) {
        this.internalList.setAll(replacement.internalList);
    }

    public void setGroups(List<? extends ReadOnlyGroup> groups) throws DuplicateGroupException {
        final UniqueGroupList replacement = new UniqueGroupList();
        for (final ReadOnlyGroup group : groups) {
            replacement.add(new Group(group));
        }
        setGroups(replacement);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<ReadOnlyGroup> asObservableList() {
        return FXCollections.unmodifiableObservableList(mappedList);
    }

    @Override
    public Iterator<Group> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UniqueGroupList // instanceof handles nulls
                && this.internalList.equals(((UniqueGroupList) other).internalList));
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }


}
```
###### /java/seedu/address/model/ModelManager.java
``` java
        raise(new NewGroupListEvent(getGroupList(), addressBook.getPersonList()));

        indicateAddressBookChanged();
    }

    @Override
    public synchronized void addPerson(ReadOnlyPerson person) throws DuplicatePersonException {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        indicateAddressBookChanged();
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    @Override
    public synchronized void addGroup(ReadOnlyGroup group) throws DuplicateGroupException {
        addressBook.addGroup(group);
        indicateAddressBookChanged();
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    @Override
    public synchronized void deleteGroup(ReadOnlyGroup group) throws GroupNotFoundException {
        addressBook.deleteGroup(group);
        indicateAddressBookChanged();
    }

    @Override
    public void updatePerson(ReadOnlyPerson target, ReadOnlyPerson editedPerson)
            throws DuplicatePersonException, PersonNotFoundException {
        requireAllNonNull(target, editedPerson);

        addressBook.updatePerson(target, editedPerson);
        indicateAddressBookChanged();
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    @Override
    public void pinPerson(ReadOnlyPerson person) throws CommandException, PersonNotFoundException {
        try {
            Person addPin = addPinTag(person);
            updatePerson(person, addPin);
            sort(SortCommand.ARGUMENT_NAME);
        } catch (DuplicatePersonException dpe) {
            throw new CommandException(AddCommand.MESSAGE_DUPLICATE_PERSON);
        }
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    @Override
    public void unpinPerson(ReadOnlyPerson person) throws CommandException, PersonNotFoundException {
        try {
            Person removePin = removePinTag(person);
            updatePerson(person, removePin);
            sort(SortCommand.ARGUMENT_NAME);
        } catch (DuplicatePersonException dpe) {
            throw new CommandException(AddCommand.MESSAGE_DUPLICATE_PERSON);
        }
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    @Override
    public void setTagColour(String tagName, String colour) throws IllegalValueException {
        List<Tag> tagList = addressBook.getTagList();
        for (Tag tag: tagList) {
            if (tagName.equals(tag.tagName)) {
                tagColours.put(tag, colour);
                updateAllPersons(tagColours);
                indicateAddressBookChanged();
                raise(new NewPersonListEvent(getFilteredPersonList()));
                colourPrefs.updateColorMap(tagColours);
                return;
            }
        }
        throw new IllegalValueException("No such tag!");
    }

    @Override
    public HashMap<Tag, String> getTagColours() {
        return tagColours;
    }

    private void updateAllPersons(HashMap<Tag, String> allTagColours) {
        colourPrefs.updateColorMap(allTagColours);
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    /**
     * @param personToPin
     * @return updated Person with added pin to be added to the address book
     * @throws CommandException
     */
    private Person addPinTag(ReadOnlyPerson personToPin) throws CommandException {
        /**
         * Create a new UniqueTagList to add pin tag into the list.
         */
        UniqueTagList updatedTags = new UniqueTagList(personToPin.getTags());
        updatedTags.addPinTag();

        return new Person(personToPin.getName(), personToPin.getPhone(), personToPin.getBirthday(),
                personToPin.getEmail(), personToPin.getAddress(), updatedTags.toSet());
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    /**
     * @param personToUnpin
     * @return updated Person with removed pin to be added to the address book
     * @throws CommandException
     */
    private Person removePinTag(ReadOnlyPerson personToUnpin) throws CommandException {
        try {
            UniqueTagList updatedTags = new UniqueTagList(personToUnpin.getTags());
            updatedTags.removePinTag();
            return new Person(personToUnpin.getName(), personToUnpin.getPhone(),
                    personToUnpin.getBirthday(), personToUnpin.getEmail(), personToUnpin.getAddress(),
                    updatedTags.toSet());
        } catch (IllegalValueException ive) {
            throw new CommandException(Tag.MESSAGE_TAG_CONSTRAINTS);
        }
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    public Predicate<ReadOnlyPerson> getPredicateForTags(String arg) throws IllegalValueException {
        try {
            Tag targetTag = new Tag(arg);
            Predicate<ReadOnlyPerson> taggedPredicate = p -> p.getTags().contains(targetTag);
            return taggedPredicate;
        }  catch (IllegalValueException ive) {
            throw new IllegalValueException(Tag.MESSAGE_TAG_CONSTRAINTS);
        }
    }
    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code ReadOnlyPerson} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<ReadOnlyPerson> getFilteredPersonList() {
        return FXCollections.unmodifiableObservableList(filteredPersons);
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    @Override
    public ObservableList<ReadOnlyGroup> getGroupList() {
        return FXCollections.unmodifiableObservableList(filteredGroups);
    }

    @Override
    public void updateFilteredPersonList(Predicate<ReadOnlyPerson> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

```
###### /java/seedu/address/model/ModelManager.java
``` java
    @Override
    public void updateFilteredGroupList(Predicate<ReadOnlyGroup> predicate) {
        requireNonNull(predicate);
        filteredGroups.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return addressBook.equals(other.addressBook)
                && filteredPersons.equals(other.filteredPersons);
    }

    @Override
    public void sort(String sortType) throws DuplicatePersonException {
        switch (sortType) {
        case SortCommand.ARGUMENT_NAME:
            addressBook.setPersons(sortBy(COMPARATOR_SORT_BY_NAME));
            break;

        case SortCommand.ARGUMENT_PHONE:
            addressBook.setPersons(sortBy(COMPARATOR_SORT_BY_PHONE));
            break;

        case SortCommand.ARGUMENT_EMAIL:
            addressBook.setPersons(sortBy(COMPARATOR_SORT_BY_EMAIL));
            break;

        default:
            break;

        }
        indicateAddressBookChanged();
    }

    /**
     * Sort the addressbook by the comparator given
     * @return ArrayList<ReadOnlyPerson> sorted list</ReadOnlyPerson>
     */
    private ArrayList<ReadOnlyPerson> sortBy(Comparator<ReadOnlyPerson> comparator) {
        ArrayList<ReadOnlyPerson> newList = new ArrayList<>();
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        SortedList<ReadOnlyPerson> sortedList =
                getFilteredPersonList().filtered(PREDICATE_SHOW_PINNED_PERSONS).sorted(comparator);
        newList.addAll(sortedList);
        sortedList = getFilteredPersonList().filtered(PREDICATE_SHOW_UNPINNED_PERSONS).sorted(comparator);
        newList.addAll(sortedList);

        return newList;
    }

    @Subscribe
    private void handleGroupPanelSelectionChangedEvent(GroupPanelSelectionChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        updateFilteredPersonList(person -> event.getNewSelection().group.getGroupMembers().contains(person));
        raise(new NewPersonListEvent(getFilteredPersonList()));
    }
}
```
###### /java/seedu/address/model/tag/UniqueTagList.java
``` java
    /**
     * Creates a pin tag
     * @return a Pin Tag to be used to add or remove person to be pinned in the address book
     */
    private Tag createPinTag() {
        try {
            return new Tag("Pinned");
        } catch (IllegalValueException ive) {
            return null; //Will not reach here
        }

    }
    /**
     * Returns all tags in this list as a Set.
     * This set is mutable and change-insulated against the internal list.
     */
    public Set<Tag> toSet() {
        assert CollectionUtil.elementsAreUnique(internalList);
        return new HashSet<>(internalList);
    }

    /**
     * Replaces the Tags in this list with those in the argument tag list.
     */
    public void setTags(Set<Tag> tags) {
        requireAllNonNull(tags);
        internalList.setAll(tags);
        assert CollectionUtil.elementsAreUnique(internalList);
    }

    /**
     * Ensures every tag in the argument list exists in this object.
     */
    public void mergeFrom(UniqueTagList from) {
        final Set<Tag> alreadyInside = this.toSet();
        from.internalList.stream()
                .filter(tag -> !alreadyInside.contains(tag))
                .forEach(internalList::add);

        assert CollectionUtil.elementsAreUnique(internalList);
    }

    /**
     * Returns true if the list contains an equivalent Tag as the given argument.
     */
    public boolean contains(Tag toCheck) {
        requireNonNull(toCheck);
        return internalList.contains(toCheck);
    }

    /**
     * Adds a Tag to the list.
     *
     * @throws DuplicateTagException if the Tag to add is a duplicate of an existing Tag in the list.
     */
    public void add(Tag toAdd) throws DuplicateTagException {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateTagException();
        }
        internalList.add(toAdd);

        assert CollectionUtil.elementsAreUnique(internalList);
    }

```
###### /java/seedu/address/model/tag/UniqueTagList.java
``` java
    /**
     * Adds a pin tag to the tag list
     */
    public void addPinTag() {
        internalList.add(pinTag);
    }

```
###### /java/seedu/address/model/tag/UniqueTagList.java
``` java
    /**
     * Removes a pin tag from the tag list
     * @throws IllegalValueException
     */
    public void removePinTag() throws IllegalValueException {
        if (contains(pinTag)) {
            internalList.remove(pinTag);
        } else {
            throw new IllegalValueException("Unable to find tag");
        }
    }


    @Override
    public Iterator<Tag> iterator() {
        assert CollectionUtil.elementsAreUnique(internalList);
        return internalList.iterator();
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Tag> asObservableList() {
        assert CollectionUtil.elementsAreUnique(internalList);
        return FXCollections.unmodifiableObservableList(internalList);
    }

    /**
     * Searches the tag list to find Pinned Tag. Can always be found as the person in pinned already
     *
     * @param pinnedPerson
     * @return true is pin tag exists, false if no pin tag
     */
    public static boolean containsPinTag(ReadOnlyPerson pinnedPerson) {
        for (Tag tag : pinnedPerson.getTags()) {
            if ("Pinned".equals(tag.tagName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        assert CollectionUtil.elementsAreUnique(internalList);
        return other == this // short circuit if same object
                || (other instanceof UniqueTagList // instanceof handles nulls
                        && this.internalList.equals(((UniqueTagList) other).internalList));
    }

    /**
     * Returns true if the element in this list is equal to the elements in {@code other}.
     * The elements do not have to be in the same order.
     */
    public boolean equalsOrderInsensitive(UniqueTagList other) {
        assert CollectionUtil.elementsAreUnique(internalList);
        assert CollectionUtil.elementsAreUnique(other.internalList);
        return this == other || new HashSet<>(this.internalList).equals(new HashSet<>(other.internalList));
    }

    @Override
    public int hashCode() {
        assert CollectionUtil.elementsAreUnique(internalList);
        return internalList.hashCode();
    }

    /**
     * Signals that an operation would have violated the 'no duplicates' property of the list.
     */
    public static class DuplicateTagException extends DuplicateDataException {
        protected DuplicateTagException() {
            super("Operation would result in duplicate tags");
        }
    }

}
```
###### /java/seedu/address/storage/XmlAdaptedGroup.java
``` java
/**
 * JAXB-friendly version of the Group.
 */
public class XmlAdaptedGroup {

    @XmlElement(required = true)
    private String name;

    @XmlElement
    private List<XmlAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs an XmlAdaptedGroup.
     * This is the no-arg constructor that is required by JAXB.
     */
    public XmlAdaptedGroup() {}

    /**
     * Converts a given Group into this class for JAXB use.
     *
     * @param source future changes to this will not affect the created XmlAdaptedGroup
     */
    public XmlAdaptedGroup(ReadOnlyGroup source) {
        this.name = source.getGroupName().fullName;
        persons = new ArrayList<>();
        for (ReadOnlyPerson person: source.getGroupMembers()) {
            persons.add(new XmlAdaptedPerson(person));
        }
    }

    /**
     * Converts this jaxb-friendly adapted group object into the model's Group object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted group
     */
    public Group toModelType() throws IllegalValueException {
        final GroupName groupName = new GroupName(this.name);
        final List<ReadOnlyPerson> groupMembers = new ArrayList<>();
        for (XmlAdaptedPerson person: this.persons) {
            groupMembers.add(person.toModelType());
        }
        return new Group(groupName, groupMembers);
    }
}
```
###### /java/seedu/address/storage/XmlSerializableAddressBook.java
``` java
    @Override
    public ObservableList<ReadOnlyGroup> getGroupList() {
        final ObservableList<ReadOnlyGroup> groups = this.groups.stream().map(g -> {
            try {
                return g.toModelType();
            } catch (IllegalValueException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));
        return FXCollections.unmodifiableObservableList(groups);
    }

}
```
###### /java/seedu/address/ui/GroupCard.java
``` java
/**
 * An UI component that displays information of a {@code Group}.
 */
public class GroupCard extends UiPart<Region> {

    private static final String FXML = "GroupListCard.fxml";
    private static final int TAG_LIMIT = 3;

    public final ReadOnlyGroup group;
    private ObservableList<ReadOnlyPerson> personList;

    @FXML
    private HBox cardPane;
    @FXML
    private Text groupName;
    @FXML
    private Text id;
    @FXML
    private FlowPane members;

    public GroupCard(ReadOnlyGroup group, int displayedIndex, ObservableList<ReadOnlyPerson> personList) {
        super(FXML);
        this.group = group;
        this.personList = new FilteredList<>(personList);
        id.setText(displayedIndex + ". ");
        initMembers(group);
        bindListeners(group);
    }

    /**
     * Binds the individual UI elements to observe their respective {@code Group} properties
     * so that they will be notified of any changes.
     */
    private void bindListeners(ReadOnlyGroup group) {
        groupName.textProperty().bind(Bindings.convert(group.nameProperty()));
        group.membersProperty().addListener(((observable, oldValue, newValue) -> {
            members.getChildren().clear();
            initMembers(group);
        }));
    }

    /**
     * Initialises all members of the group into labels in flow pane
     * @param group
     */
    private void initMembers(ReadOnlyGroup group) {
        personList = personList.filtered(person -> group.getGroupMembers().contains(person));
        int groupSize = personList.size();
        String labelText = groupSize + " member";

        if (groupSize > 1) {
            labelText = labelText.concat("s");
        }

        Label groupLabel = new Label(labelText);
        members.getChildren().add(groupLabel);


    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof GroupCard)) {
            return false;
        }

        // state check
        GroupCard card = (GroupCard) other;
        return id.getText().equals(card.id.getText())
                && group.equals(card.group);
    }
}
```
###### /java/seedu/address/ui/GroupListPanel.java
``` java
/**
 * Panel containing a list of groups
 */
public class GroupListPanel extends UiPart<Region> {

    private static final String FXML = "GroupListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(GroupListPanel.class);

    @FXML
    private ListView<GroupCard> groupListView;

    public GroupListPanel(ObservableList<ReadOnlyGroup> groupList, ObservableList<ReadOnlyPerson> personList) {
        super(FXML);
        setConnections(groupList, personList);
        registerAsAnEventHandler(this);
    }
    private void setConnections(ObservableList<ReadOnlyGroup> groupList, ObservableList<ReadOnlyPerson> personList) {
        ObservableList<GroupCard> mappedList = EasyBind.map(
                groupList, (group) -> new GroupCard(group, groupList.indexOf(group) + 1, personList));
        groupListView.setItems(mappedList);
        groupListView.setCellFactory(listView -> new GroupListPanel.GroupListViewCell());
        setEventHandlerForSelectionChangeEvent();
    }

    /**
     * Scrolls to the {@code GroupCard} at the {@code index} and selects it.
     */
    private void scrollTo(int index) {
        Platform.runLater(() -> {
            groupListView.scrollTo(index);
            groupListView.getSelectionModel().clearAndSelect(index);
        });
    }

    private void setEventHandlerForSelectionChangeEvent() {
        groupListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        logger.fine("Selection in group list panel changed to : '" + newValue + "'");
                        raise(new GroupPanelSelectionChangedEvent(newValue));
                    }
                });
    }
    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        scrollTo(event.targetIndex);
    }

    @Subscribe
    private void handleNewGroupListEvent(NewGroupListEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        setConnections(event.getGroupsList(), event.getPersonsList());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code GroupCard}.
     */
    class GroupListViewCell extends ListCell<GroupCard> {

        @Override
        protected void updateItem(GroupCard group, boolean empty) {
            super.updateItem(group, empty);

            if (empty || group == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(group.getRoot());
            }
        }
    }
}
```
###### /java/seedu/address/ui/PersonCard.java
``` java
    private void setTagColour(Label tagLabel, Tag tag) {
        if (colourMap.containsKey(tag.tagName)) {
            tagLabel.setStyle("-fx-background-color: " + colourMap.get(tag.tagName));
        } else {
            tagLabel.setStyle(null);
        }
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PersonCard)) {
            return false;
        }

        // state check
        PersonCard card = (PersonCard) other;
        return id.getText().equals(card.id.getText())
                && person.equals(card.person);
    }

    private Image setPinIcon(ReadOnlyPerson person) {
        if (person.isPinned()) {
            return new Image(PIN_ICON);
        } else {
            return null;
        }
    }
}
```
###### /java/seedu/address/ui/PersonInfo.java
``` java
/**
 * Shows the Person's full contact information
 */
public class PersonInfo extends UiPart<Region> {

    private static final String FXML = "PersonInfoPanel.fxml";

    private static final Color[] colors = {Color.BLUE, Color.BROWN, Color.GREEN, Color.RED, Color.YELLOW, Color.PURPLE,
        Color.ORANGE, Color.CHOCOLATE, Color.AQUAMARINE, Color.INDIGO, Color.GRAY};

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    private HashMap<String, String> colourMap;

    @FXML
    private Circle circle;
    @FXML
    private Text initial;
    @FXML
    private Text name;
    @FXML
    private Text phone;
    @FXML
    private Label emailHeader;
    @FXML
    private Label addressHeader;
    @FXML
    private Label birthdayHeader;
    @FXML
    private Label tagsHeader;
    @FXML
    private Label email;
    @FXML
    private Label address;
    @FXML
    private Label birthday;
    @FXML
    private FlowPane tags;

    public PersonInfo(HashMap<String, String> colourMap) {
        super(FXML);

        this.colourMap = colourMap;

        // To prevent triggering events for typing inside the loaded Web page.
        getRoot().setOnKeyPressed(Event::consume);

        loadDefaultPage();
        registerAsAnEventHandler(this);
    }

    /**
     * Loads default welcome page
     */
    private void loadDefaultPage() {
        circle.setRadius(70);
        initial.setText("C");
        name.setText("Welcome to Circles!");
    }

    private void loadPersonPage(ReadOnlyPerson person) {
        loadPage(person);
    }

    /**
     * Loads the contact information of the specific person
     * @param person
     */
    public void loadPage(ReadOnlyPerson person) {
        circle.setRadius(70);
        initial.setText(person.getName().fullName.substring(0, 1));
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        circle.setFill(colors[initial.getText().hashCode() % colors.length]);
        emailHeader.setText("Email:");
        email.textProperty().bind(Bindings.convert(person.emailProperty()));
        addressHeader.setText("Address:");
        address.textProperty().bind(Bindings.convert(person.addressProperty()));
        birthdayHeader.setText("Birthday: ");
        birthday.textProperty().bind(Bindings.convert(person.birthdayProperty()));
        tagsHeader.setText("Tags:");
        initTags(person);
    }

    /**
     * Initializes tags for the person
     * @param person
     */
    private void initTags(ReadOnlyPerson person) {
        tags.getChildren().clear();
        person.getTags().forEach(tag -> {
            Label tagLabel = new Label(tag.tagName);
            setTagColour(tagLabel, tag);
            tags.getChildren().add(tagLabel);
        });
    }

    private void setTagColour(Label tagLabel, Tag tag) {
        if (colourMap.containsKey(tag.tagName)) {
            tagLabel.setStyle("-fx-background-color: " + colourMap.get(tag.tagName));
        } else {
            tagLabel.setStyle(null);
        }
    }

    @Subscribe
    private void handlePersonPanelSelectionChangedEvent(PersonPanelSelectionChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        loadPersonPage(event.getNewSelection().person);
    }
}
```
###### /resources/view/GroupListCard.fxml
``` fxml
<?import javafx.geometry.Insets?>
<?import javafx.scene.layout.FlowPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Pane?>
<?import javafx.scene.layout.StackPane?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.shape.Circle?>
<?import javafx.scene.text.Font?>
<?import javafx.scene.text.Text?>

<HBox id="cardPane" fx:id="cardPane" maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="100.0" prefWidth="320.0" xmlns="http://javafx.com/javafx/8.0.111" xmlns:fx="http://javafx.com/fxml/1">
   <children>
      <Pane HBox.hgrow="ALWAYS">
         <children>
            <VBox alignment="CENTER_LEFT" maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="100.0" prefWidth="320.0">
               <padding>
                  <Insets bottom="5" left="15" right="5" top="5" />
               </padding>
               <children>
                  <HBox alignment="CENTER_LEFT" prefHeight="91.0" prefWidth="400.0" spacing="5">
                     <children>
                        <StackPane>
                           <children>
                              <Circle fill="#545f6b" radius="16.0" stroke="BLACK" strokeType="INSIDE" />
                              <Text fx:id="id" fill="WHITE" strokeType="OUTSIDE" strokeWidth="0.0" text="1." textAlignment="CENTER" wrappingWidth="39.0">
                                 <font>
                                    <Font size="16.0" />
                                 </font>
                                 <StackPane.margin>
                                    <Insets left="5.0" />
                                 </StackPane.margin>
                              </Text>
                           </children>
                        </StackPane>
                        <Text fx:id="groupName" fill="WHITE" strokeType="OUTSIDE" strokeWidth="0.0" text="\$name">
                           <font>
                              <Font size="24.0" />
                           </font>
                        </Text>
                     </children>
                  </HBox>
                  <FlowPane fx:id="members">
                     <VBox.margin>
                        <Insets bottom="20.0" />
                     </VBox.margin>
                  </FlowPane>
               </children>
            </VBox>
         </children>
      </Pane>
   </children>
</HBox>
```
###### /resources/view/GroupListPanel.fxml
``` fxml
<?import javafx.scene.control.ListView?>
<?import javafx.scene.layout.VBox?>

<VBox maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="210.0" prefWidth="320.0" styleClass="background" stylesheets="@DarkTheme.css" xmlns="http://javafx.com/javafx/8.0.111" xmlns:fx="http://javafx.com/fxml/1">
   <children>
      <ListView fx:id="groupListView" prefHeight="210.0" prefWidth="320.0" styleClass="background" />
   </children>
</VBox>
```
###### /resources/view/PersonInfoPanel.fxml
``` fxml
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.ColumnConstraints?>
<?import javafx.scene.layout.FlowPane?>
<?import javafx.scene.layout.GridPane?>
<?import javafx.scene.layout.Pane?>
<?import javafx.scene.layout.RowConstraints?>
<?import javafx.scene.shape.Circle?>
<?import javafx.scene.text.Font?>
<?import javafx.scene.text.Text?>

<Pane maxHeight="-Infinity" maxWidth="-Infinity" minHeight="-Infinity" minWidth="-Infinity" prefHeight="400.0" prefWidth="600.0" styleClass="background" stylesheets="@DarkTheme.css" xmlns="http://javafx.com/javafx/8.0.111" xmlns:fx="http://javafx.com/fxml/1">
   <children>
      <Circle fx:id="circle" fill="DODGERBLUE" layoutX="308.0" layoutY="73.0" radius="70.0" stroke="BLACK" strokeType="INSIDE" />
      <Text fx:id="initial" fill="WHITE" layoutX="262.0" layoutY="105.0" strokeType="OUTSIDE" strokeWidth="0.0" textAlignment="CENTER" wrappingWidth="85.0">
         <font>
            <Font size="93.0" />
         </font>
      </Text>
      <GridPane layoutX="114.0" layoutY="244.0" prefHeight="134.0" prefWidth="479.0">
        <columnConstraints>
          <ColumnConstraints hgrow="SOMETIMES" maxWidth="241.0" minWidth="10.0" prefWidth="101.0" />
          <ColumnConstraints hgrow="SOMETIMES" maxWidth="382.0" minWidth="0.0" prefWidth="378.0" />
        </columnConstraints>
        <rowConstraints>
          <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
          <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
            <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
            <RowConstraints minHeight="10.0" prefHeight="30.0" vgrow="SOMETIMES" />
        </rowConstraints>
         <children>
            <Label fx:id="addressHeader" styleClass="label-bright" GridPane.rowIndex="1" />
            <Label fx:id="birthdayHeader" styleClass="label-bright" GridPane.rowIndex="2" />
            <Label fx:id="tagsHeader" styleClass="label-bright" GridPane.rowIndex="3" />
            <Label fx:id="birthday" styleClass="label-bright" GridPane.columnIndex="1" GridPane.rowIndex="2" />
            <Label fx:id="email" styleClass="label-bright" GridPane.columnIndex="1" />
            <Label fx:id="address" styleClass="label-bright" GridPane.columnIndex="1" GridPane.rowIndex="1" />
            <Label fx:id="emailHeader" styleClass="label-bright" />
            <FlowPane fx:id="tags" alignment="CENTER_LEFT" prefHeight="200.0" prefWidth="200.0" GridPane.columnIndex="1" GridPane.rowIndex="3" />
         </children>
      </GridPane>
      <Text fx:id="name" fill="WHITE" layoutX="20.0" layoutY="179.0" strokeType="OUTSIDE" strokeWidth="0.0" textAlignment="CENTER" wrappingWidth="560.0">
         <font>
            <Font size="35.0" />
         </font>
      </Text>
      <Text fx:id="phone" fill="WHITE" layoutX="4.0" layoutY="210.0" strokeType="OUTSIDE" strokeWidth="0.0" textAlignment="CENTER" wrappingWidth="591.0000050663948">
         <font>
            <Font size="28.0" />
         </font>
      </Text>
   </children>
</Pane>
```
