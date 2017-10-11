package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddCommandParser;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;
import seedu.address.logic.Logic;

import java.util.HashSet;
import java.util.Set;

public class AddCommandWindow {

    private final static String ADD_WINDOW_TITLE = "Add Person";
    private final static String ADD_SUCCESS_HEADER = "Add Successful";
    private final static String ADD_SUCCESS_MESSAGE = "Person added Successfully";
    private final static String ADD_FAILED = "Add Unsuccessful";
    private final static String ADD_ERROR_MESSAGE = "An unexpected error has occurred. Please try again.";

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField tagsField;

    private Stage addCommandStage;
    private Name name;
    private Phone phone;
    private Email email;
    private Address address;
    private Set<Tag> tags;
    private boolean okClicked = false;
    private Logic logic;
    private CommandResult result;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param addCommandStage
     */
    public void setAddStage(Stage addCommandStage, Logic logic) {
        this.addCommandStage = addCommandStage;
        this.logic = logic;
    }



    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    public CommandResult getCommandResult() {
        return result;
    }


    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() throws Exception{
        try {
            if(isInputValid()) {
                name = new Name(nameField.getText().trim());
                phone = new Phone(phoneField.getText().trim());
                email = new Email(emailField.getText().trim());
                address = new Address(addressField.getText().trim());

                //Get tags as per normal if tagsField has text in it. Otherwise, if tagsField is empty return empty list
                if(tagsField.getText().trim().length() != 0) {
                    tags = new HashSet<>(AddCommandParser.getTagsFromArgs(tagsField.getText().trim()));
                } else {
                    tags = new HashSet<>();
                }

                okClicked = true;
                Person newPerson = new Person(name, phone, email, address, tags);
                AddCommand adder = new AddCommand(newPerson);
                CommandResult result = logic.execute(adder);

                if (result.feedbackToUser.equals(String.format(AddCommand.MESSAGE_SUCCESS, newPerson))) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.getDialogPane().setMinSize(200, 175);
                    alert.setTitle("AddressBook Add Command");
                    alert.setHeaderText("Add Successful");
                    alert.setContentText("Person added successfully");

                    alert.showAndWait();
                    addCommandStage.close();

                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.getDialogPane().setMinSize(200, 175);
                    alert.setTitle("AddressBook Add Command");
                    alert.setHeaderText("Error");
                    alert.setContentText("An unexpected error has occurred. Please try again.");

                    alert.showAndWait();
                }

                //Stores the CommandResult in Gui for MainWindow to retrieve
                this.result = result;
            }
        } catch (UniqueTagList.DuplicateTagException dte) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.getDialogPane().setMinSize(200, 175);
            alert.setTitle(ADD_WINDOW_TITLE);
            alert.setHeaderText("Error");
            alert.setContentText("Duplicate Tags found. Remove Duplicate Tags and try again.");

            alert.showAndWait();
        } catch (CommandException ce) {
            if(ce.getMessage().equals(AddCommand.MESSAGE_DUPLICATE_PERSON)) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.getDialogPane().setMinSize(200, 175);
                alert.setTitle(ADD_WINDOW_TITLE);
                alert.setHeaderText(ADD_FAILED);
                alert.setContentText(ce.getMessage());
                alert.showAndWait();
            }
        }

    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        addCommandStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (!Name.isValidName(nameField.getText().trim())) {
            errorMessage += Name.MESSAGE_NAME_CONSTRAINTS + "\n";
        }
        if (!Phone.isValidPhone(phoneField.getText().trim())) {
            errorMessage += Phone.MESSAGE_PHONE_CONSTRAINTS + "\n";
        }
        if (!Email.isValidEmail(emailField.getText().trim()))
            errorMessage += Email.MESSAGE_EMAIL_CONSTRAINTS + "\n";

        if (!Address.isValidAddress(addressField.getText().trim())) {
            errorMessage += Address.MESSAGE_ADDRESS_CONSTRAINTS + "\n";
        }
        //Check whether tagsField is valid if there is text inside
        if(tagsField.getText().trim().length() != 0 && !Tag.isValidTags(tagsField.getText().trim())) {
            errorMessage += Tag.MESSAGE_TAG_CONSTRAINTS + "\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        }
        else {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(addCommandStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }


}
