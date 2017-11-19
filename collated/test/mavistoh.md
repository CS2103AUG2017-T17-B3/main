# mavistoh
###### /java/seedu/address/model/person/BirthdayTest.java
``` java
public class BirthdayTest {

    @Test
    public void isValidBirthday() {
        //invalid birthdays
        assertFalse(Birthday.isValidBirthday(" ")); // spaces only
        assertFalse(Birthday.isValidBirthday("91")); // less than 3 numbers
        assertFalse(Birthday.isValidBirthday("phone")); // non-numeric
        assertFalse(Birthday.isValidBirthday("9011p041")); // alphabets within digits
        assertFalse(Birthday.isValidBirthday("9312 1534")); // spaces within digits
        assertFalse(Birthday.isValidBirthday("29-02-1995")); // not a leap year
        assertFalse(Birthday.isValidBirthday("31-02-1995")); // feb cannot take 30/31
        assertFalse(Birthday.isValidBirthday("29-02-1995")); // not a leap year
        assertFalse(Birthday.isValidBirthday("31-09-1989")); // no 31st in sept
        assertFalse(Birthday.isValidBirthday("02.09-1989")); // separators not consistent

        // valid birthdays
        assertTrue(Birthday.isValidBirthday("-"));
        assertTrue(Birthday.isValidBirthday("02-03-1995")); // follow regex
        assertTrue(Birthday.isValidBirthday("02.03.1995")); // follow regex
        assertTrue(Birthday.isValidBirthday("02/03/1995")); // follow regex
    }
}
```
###### /java/seedu/address/testutil/EditPersonDescriptorBuilder.java
``` java
    /**
     * Sets the {@code Birthday} of the {@code EditBirthdayDescriptor} that we are building.
     */
    public EditPersonDescriptorBuilder withBirthday(String birthday) {
        try {
            ParserUtil.parseBirthday(Optional.of(birthday)).ifPresent(descriptor::setBirthday);
        } catch (IllegalValueException ive) {
            throw new IllegalArgumentException("birthday is expected to be unique.");
        }
        return this;
    }
```
###### /java/seedu/address/testutil/PersonBuilder.java
``` java
    /**
     * Sets the {@code Birthday} of the {@code Person} that we are building.
     */
    public PersonBuilder withBirthday(String birthday) {
        try {
            this.person.setBirthday(new Birthday(birthday));
        } catch (IllegalValueException ive) {
            throw new IllegalArgumentException("birthday is expected to be unique.");
        }
        return this;
    }
```
###### /java/seedu/address/testutil/TypicalPersons.java
``` java
    public static final ReadOnlyPerson HOON = new PersonBuilder().withName("Hoon Meier").withPhone("8482424")
            .withBirthday("08-08-1998").withEmail("stefan@example.com").withAddress("little india").build();
    public static final ReadOnlyPerson IDA = new PersonBuilder().withName("Ida Mueller").withPhone("8482131")
            .withBirthday("09-09-1999").withEmail("hans@example.com").withAddress("chicago ave").build();
    public static final ReadOnlyPerson PERSON_WITHOUT_PHONE = new PersonBuilder().withName("Sally")
            .withBirthday("02-03-1995").withEmail("nophone@example.com").withAddress("Jurong").build();
    public static final ReadOnlyPerson PERSON_WITHOUT_BIRTHDAY = new PersonBuilder().withName("Kelly")
            .withPhone("97273912").withEmail("nobirthday@example.com").withAddress("Bukit Batok").build();
    public static final ReadOnlyPerson PERSON_WITHOUT_EMAIL = new PersonBuilder().withName("Dilly")
            .withPhone("91827384").withBirthday("02-06-1995").withAddress("Bukit Gombak").build();
    public static final ReadOnlyPerson PERSON_WITHOUT_ADDRESS = new PersonBuilder().withName("Dally")
            .withPhone("98760293").withBirthday("09-10-1995").withEmail("noaddress@example.com").build();
```
###### /java/systemtests/AddCommandSystemTest.java
``` java
        /* Case: missing name -> rejected */
        command = AddCommand.COMMAND_WORD + PHONE_DESC_AMY + BIRTHDAY_DESC_AMY + EMAIL_DESC_AMY
                + ADDRESS_DESC_AMY;
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: missing phone -> accepted */
        assertCommandSuccess(PERSON_WITHOUT_PHONE);

        /* Case: missing birthday -> accepted */
        assertCommandSuccess(PERSON_WITHOUT_BIRTHDAY);

        /* Case: missing email -> accepted */
        assertCommandSuccess(PERSON_WITHOUT_EMAIL);

        /* Case: missing address -> accepted */
        assertCommandSuccess(PERSON_WITHOUT_ADDRESS);
```
