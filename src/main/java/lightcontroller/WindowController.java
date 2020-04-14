package lightcontroller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import lightcontroller.settings.Settings;

import java.util.Arrays;

public class WindowController
{
  @FXML
  private AnchorPane mainPane;
  @FXML
  private RadioButton stateButton;
  @FXML
  private Label brightnessLabel;
  @FXML
  private Slider brightnessSlider;
  @FXML
  private Label modeLabel;
  @FXML
  private ChoiceBox<Mode> modeChoiceBox;
  @FXML
  private Label settingsLabel;
  @FXML
  private TabPane settingsPane;
  @FXML
  private CheckBox whiteCheckbox;

  // MUSIC
  @FXML
  private Slider musicHueStartSlider;
  @FXML
  private Slider musicHueStepSlider;

  // CHANNEL
  @FXML
  private Slider channelChannelSlider;
  @FXML
  private Slider channelHueSlider;

  // AMBIENT
  @FXML
  private Slider ambientHueSlider;

  // DYNAMIC
  @FXML
  private Slider dynamicFlowSpeedSlider;

  // RANDOM
  @FXML
  private Slider randomSpeedSlider;

  @FXML
  private void initialize()
  {
    settingsPane.tabMinWidthProperty().bind(mainPane.widthProperty().subtract(65).divide(5));

    /*
      SET COMPONENTS TO STORED CONFIGURATION
     */
    modeChoiceBox       .setItems(FXCollections.observableList(Arrays.asList(Mode.values())));
    modeChoiceBox       .setValue(Settings.modeProperty().getValue());
    brightnessSlider    .valueProperty().set(Settings.brightnessProperty().doubleValue());
    whiteCheckbox       .selectedProperty().set(Settings.whiteProperty().getValue());
    stateButton         .selectedProperty().set(Settings.stateProperty().getValue());

    // disable other components based on state
    modeChoiceBox         .disableProperty().bind(Settings.stateProperty().not());
    settingsPane          .disableProperty().bind(Settings.stateProperty().not());
    brightnessSlider      .disableProperty().bind(Settings.stateProperty().not());
    modeLabel             .disableProperty().bind(Settings.stateProperty().not());
    brightnessLabel       .disableProperty().bind(Settings.stateProperty().not());
    settingsLabel         .disableProperty().bind(Settings.stateProperty().not());
    whiteCheckbox         .disableProperty().bind(Settings.stateProperty().not());

    // mode specific settings
    musicHueStartSlider   .valueProperty().set(Settings.musicHueStartProperty().getValue());
    musicHueStepSlider    .valueProperty().set(Settings.musicHueStepProperty().getValue());

    channelChannelSlider  .valueProperty().set(Settings.channelChannelProperty().getValue());
    channelHueSlider      .valueProperty().set(Settings.channelHueProperty().getValue());

    ambientHueSlider      .valueProperty().set(Settings.ambientHueProperty().getValue());

    dynamicFlowSpeedSlider.valueProperty().set(Settings.dynamicFlowSpeedProperty().getValue());

    randomSpeedSlider     .valueProperty().set(Settings.randomSpeedProperty().getValue());

    /*
      BIND COMPONENTS TO CONFIGURATION VALUE PROPERTIES
     */
    Settings.modeProperty()             .bind(modeChoiceBox.valueProperty());
    Settings.brightnessProperty()       .bind(brightnessSlider.valueProperty());
    Settings.whiteProperty()            .bindBidirectional(whiteCheckbox.selectedProperty());
    Settings.stateProperty()            .bind(stateButton.selectedProperty());

    // mode specific
    Settings.musicHueStartProperty()    .bind(musicHueStartSlider.valueProperty());
    Settings.musicHueStepProperty()     .bind(musicHueStepSlider.valueProperty());

    Settings.channelChannelProperty()   .bind(channelChannelSlider.valueProperty());
    Settings.channelHueProperty()       .bind(channelHueSlider.valueProperty());

    Settings.ambientHueProperty()       .bind(ambientHueSlider.valueProperty());

    Settings.dynamicFlowSpeedProperty() .bind(dynamicFlowSpeedSlider.valueProperty());

    Settings.randomSpeedProperty()      .bind(randomSpeedSlider.valueProperty());
  }

  public void shutdown()
  {
    stateButton.selectedProperty().set(false); // turn lights off when shutting down
  }
}
