package seedu.address.ui;

import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.PersonPanelSelectionChangedEvent;
import seedu.address.model.person.ReadOnlyPerson;
import seedu.address.model.tag.Tag;

/**
 * The Browser Panel of the App.
 */
public class BrowserPanel extends UiPart<Region> {

    public static final String DEFAULT_PAGE = "default.html";

    private static final String FXML = "BrowserPanel.fxml";

    private static final Color[] colors = {Color.BLUE, Color.BROWN, Color.GREEN, Color.RED, Color.YELLOW, Color.PURPLE,
        Color.ORANGE, Color.CHOCOLATE, Color.AQUAMARINE, Color.INDIGO, Color.GRAY};

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    @FXML
    private Circle circle;
    @FXML
    private Label name;
    @FXML
    private Label nameHeader;
    @FXML
    private Label phone;
    @FXML
    private Label phoneHeader;
    @FXML
    private Label email;
    @FXML
    private Label emailHeader;
    @FXML
    private Label address;
    @FXML
    private Label addressHeader;
    @FXML
    private Label birthdayHeader;
    @FXML
    private Label birthday;
    @FXML
    private Text initials;
    @FXML
    private FlowPane tags;
    @FXML
    private Label tagsHeader;


    public BrowserPanel() {
        super(FXML);
        loadDefaultPage();
        // To prevent triggering events for typing inside the loaded Web page.
        getRoot().setOnKeyPressed(Event::consume);
        registerAsAnEventHandler(this);
    }

    private void loadDefaultPage() {
        circle.setRadius(0);
    }

    private void loadPersonPage(ReadOnlyPerson person) {
        loadPage(person);
    }


    /**
     * Loads the contact information of the specific person
     * @param person
     */
    public void loadPage(ReadOnlyPerson person) {
        circle.setRadius(75);
        initials.setText(person.getName().fullName.substring(0, 1));
        circle.setFill(colors[initials.getText().hashCode() % colors.length]);
        nameHeader.setText("Name:");
        name.textProperty().bind(Bindings.convert(person.nameProperty()));
        phoneHeader.setText("Phone:");
        emailHeader.setText("Email:");
        email.textProperty().bind(Bindings.convert(person.emailProperty()));
        phone.textProperty().bind(Bindings.convert(person.phoneProperty()));
        addressHeader.setText("Address:");
        address.textProperty().bind(Bindings.convert(person.addressProperty()));
        birthdayHeader.setText("Birthday: ");
        birthday.setText("TBD");
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
            setTagColour(tagLabel, tag, person);
            tags.getChildren().add(tagLabel);
        });
    }

    private void setTagColour(Label tagLabel, Tag tag, ReadOnlyPerson person) {
        if (person.getTagColours().containsKey(tag)) {
            tagLabel.setStyle("-fx-background-color: " + person.getTagColours().get(tag));
        } else {
            tagLabel.setStyle("-fx-background-color: blue");
        }
    }

    public void freeResources() {
        loadDefaultPage();
    }


    @Subscribe
    private void handlePersonPanelSelectionChangedEvent(PersonPanelSelectionChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        loadPersonPage(event.getNewSelection().person);
    }
}
