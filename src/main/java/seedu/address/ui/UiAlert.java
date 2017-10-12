package seedu.address.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;

public  class UiAlert {

    private static final String ICON = "/images/address_book_32.png";

    private static final String EXIT_TITLE = "Confirm Exit";
    private static final String EXIT_MESSAGE = "Are you sure you want to exit the AddressBook?";


    /**
     * Shows prompt to user before exiting the addressbook, from menu bar or command box
     * @return true if OK is pressed, otherwise returns false
     */
    public static boolean showExitAlert() {
        ButtonType ok = new ButtonType("OK");
        ButtonType cancel = new ButtonType("Cancel");

        Alert exitPrompt = new Alert(Alert.AlertType.WARNING, EXIT_MESSAGE , ok, cancel);
        exitPrompt.setHeaderText(EXIT_TITLE);
        exitPrompt.setTitle(EXIT_TITLE);

        Stage exitStage = (Stage) exitPrompt.getDialogPane().getScene().getWindow();
        exitStage.getIcons().add(new Image(ICON));

        Optional<ButtonType> result = exitPrompt.showAndWait();
        return result.isPresent() && result.get() == ok;
    }
}
