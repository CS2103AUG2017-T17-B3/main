package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.AddressBookChangedEvent;
import seedu.address.commons.events.ui.GroupPanelSelectionChangedEvent;
import seedu.address.commons.events.ui.NewGroupListEvent;
import seedu.address.commons.events.ui.NewPersonListEvent;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.group.ReadOnlyGroup;
import seedu.address.model.group.exceptions.DuplicateGroupException;
import seedu.address.model.group.exceptions.GroupNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.ReadOnlyPerson;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final FilteredList<ReadOnlyPerson> filteredPersons;
    private final FilteredList<ReadOnlyGroup> filteredGroups;
    private HashMap<Tag, String> tagColours = new HashMap<>();
    private UserPrefs colourPrefs;


    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, UserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        filteredGroups = new FilteredList<>(this.addressBook.getGroupList());
        colourPrefs = userPrefs;
        HashMap<String, String> stringColourMap = userPrefs.getColourMap();
        if (stringColourMap != null) {
            try {
                for (HashMap.Entry<String, String> entry : stringColourMap.entrySet()) {
                    tagColours.put(new Tag(entry.getKey()), entry.getValue());
                }

            } catch (IllegalValueException ive) {
                //it shouldn't ever reach here
            }
        }
        updateAllPersons(tagColours);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    @Override
    public void resetData(ReadOnlyAddressBook newData) {
        addressBook.resetData(newData);
        indicateAddressBookChanged();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    /**
     * Raises an event to indicate the model has changed
     */
    private void indicateAddressBookChanged() {
        raise(new AddressBookChangedEvent(addressBook));
    }

    @Override
    public synchronized void deletePerson(ReadOnlyPerson target) throws PersonNotFoundException {
        addressBook.removePerson(target);
        raise(new NewGroupListEvent(getGroupList()));
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void addPerson(ReadOnlyPerson person) throws DuplicatePersonException {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void addGroup(ReadOnlyGroup group) throws DuplicateGroupException {
        addressBook.addGroup(group);
        indicateAddressBookChanged();
    }

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

    @Override
    public ObservableList<ReadOnlyGroup> getGroupList() {
        return FXCollections.unmodifiableObservableList(filteredGroups);
    }

    @Override
    public void updateFilteredPersonList(Predicate<ReadOnlyPerson> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

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
