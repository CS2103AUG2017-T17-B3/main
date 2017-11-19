# mavistoh

###### \java\seedu\address\logic\commands\EditCommand.java
``` java
        public void setBirthday(Birthday birthday) {
            this.birthday = birthday;
        }

        public Optional<Birthday> getBirthday() {
            return Optional.ofNullable(birthday);
        }
```
###### \java\seedu\address\model\person\Address.java
``` java
    /**
     * Validates given address.
     *
     * @throws IllegalValueException if given address string is invalid.
     */
    public Address(String address) throws IllegalValueException {
        if (address == null) {
            this.value = ADDRESS_EMPTY;
        } else {
            if (!isValidAddress(address)) {
                throw new IllegalValueException(MESSAGE_ADDRESS_CONSTRAINTS);
            }
            this.value = address;
        }
    }

    /**
     * Returns true if a given string is a valid person email.
     */
    public static boolean isValidAddress(String test) {
        return test.matches(ADDRESS_VALIDATION_REGEX) || test.matches(ADDRESS_EMPTY);
    }
```
###### \java\seedu\address\model\person\Birthday.java
``` java
/**
 * Represents Person's birthday in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidBirthday(String)}
 */
public class Birthday {


    public static final String MESSAGE_BIRTHDAY_CONSTRAINTS =
            "Birthday must be in the format DD.MM.YYYY, DD/MM/YYYY or DD-MM-YYYY";
    //public static final String BIRTHDAY_VALIDATION_REGEX = "(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)
    // (\\/|-|\\.)(?:0?[1,3-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]
    // \\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.
    // )(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})";
    //public static final String BIRTHDAY_VALIDATION_REGEX = "(^(((0[1-9]|1[0-9]|2[0-8])[\/](0[1-9]|1[012]))|((29|30|31)
    // [\/](0[13578]|1[02]))|((29|30)[\/](0[4,6,9]|11)))[\/](19|[2-9][0-9])\d\d$)|(^29[\/]02[\/](19|[2-9][0-9])(00|04
    // |08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)$)";
    public static final String BIRTHDAY_VALIDATION_REGEX = "^(?=\\d{2}([-.,\\/])\\d{2}\\1\\d{4}$)"
            + "(?:0[1-9]|1\\d|[2][0-8]|29(?!.02.(?!(?!(?:[02468][1-35-79]|[13579][0-13-57-9])00)\\d{2}"
            + "(?:[02468][048]|[13579][26])))|30(?!.02)|31(?=.(?:0[13578]|10|12))).(?:0[1-9]|1[012]).\\d{4}$";
    public static final String BIRTHDAY_EMPTY = "-";
    public final String value;


    /**
     * Validates given birthday.
     *
     * @throws IllegalValueException if given birthday is invalid.
     */
    public Birthday(String birthday) throws IllegalValueException {
        if (birthday == null) {
            this.value = BIRTHDAY_EMPTY;
        } else {
            String trimmedBirthday = birthday.trim();
            if (!isValidBirthday(trimmedBirthday)) {
                throw new IllegalValueException(MESSAGE_BIRTHDAY_CONSTRAINTS);
            }
            this.value = trimmedBirthday;
        }
    }

    /**
     * Returns true if a given string is a valid person birthday.
     */
    public static boolean isValidBirthday(String test) {
        return test.matches(BIRTHDAY_VALIDATION_REGEX) || test.matches(BIRTHDAY_EMPTY);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Birthday // instanceof handles nulls
                && this.value.equals(((Birthday) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
```
###### \java\seedu\address\model\person\Email.java
``` java
    /**
     * Validates given email.
     *
     * @throws IllegalValueException if given email address string is invalid.
     */
    public Email(String email) throws IllegalValueException {
        if (EMAIL_EMPTY.equals(email) || email == null) {
            this.value = EMAIL_EMPTY;
        } else {
            String trimmedEmail = email.trim();
            if (!isValidEmail(trimmedEmail)) {
                throw new IllegalValueException(MESSAGE_EMAIL_CONSTRAINTS);
            }
            this.value = trimmedEmail;
            String[] splitEmail = trimmedEmail.split("@");
            userName = splitEmail[0];
            domainName = splitEmail[1];
        }
    }

    /**
     * Returns if a given string is a valid person email.
     */
    public static boolean isValidEmail(String test) {
        return test.matches(EMAIL_VALIDATION_REGEX) || test.matches(EMAIL_EMPTY);
    }
```
###### \java\seedu\address\model\person\Person.java
``` java
    @Override
    public ObjectProperty<Name> nameProperty() {
        return name;
    }

    @Override
    public Name getName() {
        return name.get();
    }

    public void setPhone(Phone phone) {
        this.phone.set(phone);
    }

    @Override
    public ObjectProperty<Phone> phoneProperty() {
        return phone;
    }

    @Override
    public Phone getPhone() {
        return phone.get();
    }

    public void setBirthday(Birthday birthday) {
        this.birthday.set(birthday);
    }

    @Override
    public ObjectProperty<Birthday> birthdayProperty() {
        return birthday;
    }

    @Override
    public Birthday getBirthday() {
        return birthday.get();
    }

    public void setEmail(Email email) {
        this.email.set(email);
    }

    @Override
    public ObjectProperty<Email> emailProperty() {
        return email;
    }

    @Override
    public Email getEmail() {
        return email.get();
    }

    public void setAddress(Address address) {
        this.address.set(address);
    }

    @Override
    public ObjectProperty<Address> addressProperty() {
        return address;
    }

    @Override
    public Address getAddress() {
        return address.get();
    }

```
###### \java\seedu\address\model\person\Phone.java
``` java
    /**
     * Validates given phone number.
     *
     * @throws IllegalValueException if given phone string is invalid.
     */
    public Phone(String phone) throws IllegalValueException {
        if (phone == null) {
            this.value = PHONE_EMPTY;
        } else {
            String trimmedPhone = phone.trim();
            if (!isValidPhone(trimmedPhone)) {
                throw new IllegalValueException(MESSAGE_PHONE_CONSTRAINTS);
            }
            this.value = trimmedPhone;
        }
    }

    /**
     * Returns true if a given string is a valid person phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.matches(PHONE_VALIDATION_REGEX) || test.matches(PHONE_EMPTY);
    }
```
