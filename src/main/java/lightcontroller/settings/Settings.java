package lightcontroller.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javafx.beans.property.*;
import lightcontroller.ColorUtil;
import lightcontroller.Main;
import lightcontroller.Mode;
import lightcontroller.serial.CommandEncoder;
import org.hildan.fxgson.FxGson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Settings
{
  private static Path SETTINGS_PATH;

  static
  {
    SETTINGS_PATH = Paths.get(System.getProperty("user.dir")).resolve("settings.json");
  }

  //SINGLETON YAY
  private static Settings instance;

  private BooleanProperty state;
  private ObjectProperty<Mode> mode;
  private DoubleProperty brightness;
  private BooleanProperty white;

  // MUSIC
  private IntegerProperty musicHueStart;
  private IntegerProperty musicHueStep;

  // CHANNEL
  private IntegerProperty channelChannel;
  private IntegerProperty channelHue;

  // AMBIENT
  private IntegerProperty ambientHue;

  // DYNAMIC
  private IntegerProperty dynamicFlowSpeed;

  // RANDOM
  private IntegerProperty randomSpeed;

  private Settings(boolean withDefaults)
  {
    if(withDefaults)
    {
      state = new SimpleBooleanProperty(false);
      mode = new SimpleObjectProperty<>(Mode.MUSIC);
      brightness = new SimpleDoubleProperty(1.0);
      white = new SimpleBooleanProperty(false);

      musicHueStart = new SimpleIntegerProperty(ColorUtil.HSV_BLUE);
      musicHueStep = new SimpleIntegerProperty(3);

      channelChannel = new SimpleIntegerProperty(0);
      channelHue = new SimpleIntegerProperty(ColorUtil.HSV_BLUE);

      ambientHue = new SimpleIntegerProperty(ColorUtil.HSV_BLUE);

      dynamicFlowSpeed = new SimpleIntegerProperty(5);

      randomSpeed = new SimpleIntegerProperty(40);
    }else
    {
      state = new SimpleBooleanProperty();
      mode = new SimpleObjectProperty<>();
      brightness = new SimpleDoubleProperty();
      white = new SimpleBooleanProperty();

      musicHueStart = new SimpleIntegerProperty();
      musicHueStep = new SimpleIntegerProperty();

      channelChannel = new SimpleIntegerProperty();
      channelHue = new SimpleIntegerProperty();

      ambientHue = new SimpleIntegerProperty();

      dynamicFlowSpeed = new SimpleIntegerProperty();

      randomSpeed = new SimpleIntegerProperty();
    }
  }

  public static BooleanProperty stateProperty()
  {
    return instance.state;
  }

  public static ObjectProperty<Mode> modeProperty()
  {
    return instance.mode;
  }

  public static DoubleProperty brightnessProperty()
  {
    return instance.brightness;
  }

  public static BooleanProperty whiteProperty()
  {
    return instance.white;
  }

  public static IntegerProperty musicHueStartProperty()
  {
    return instance.musicHueStart;
  }

  public static IntegerProperty musicHueStepProperty()
  {
    return instance.musicHueStep;
  }

  public static IntegerProperty channelChannelProperty()
  {
    return instance.channelChannel;
  }

  public static IntegerProperty channelHueProperty()
  {
    return instance.channelHue;
  }

  public static IntegerProperty ambientHueProperty()
  {
    return instance.ambientHue;
  }

  public static IntegerProperty dynamicFlowSpeedProperty()
  {
    return instance.dynamicFlowSpeed;
  }

  public static IntegerProperty randomSpeedProperty()
  {
    return instance.randomSpeed;
  }

  public static void save(boolean reset)
  {
    try (BufferedWriter writer = Files.newBufferedWriter(SETTINGS_PATH))
    {
      Gson gson = FxGson.addFxSupport(new GsonBuilder()).setPrettyPrinting().create();
      writer.write(gson.toJson(reset ? new Settings(true) : instance));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public static void load()
  {
    JsonObject jsonObject = null;
    if (Files.notExists(SETTINGS_PATH))
    {
      save(true);
    }
    try (BufferedReader reader = Files.newBufferedReader(SETTINGS_PATH))
    {
      jsonObject = FxGson.create().fromJson(reader, JsonObject.class);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    instance = new Settings(false);

    /*
      ADD LISTENERS TO PROPERTIES
     */
    instance.state            .addListener((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setState(newValue)));
    instance.mode             .addListener((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setMode(newValue)));
    instance.brightness       .addListener((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setBrightness((double) Math.round(newValue.doubleValue() * 10) / 10)));
    instance.white            .addListener(((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setWhite(newValue))));

    // mode specific
    instance.musicHueStart    .addListener(((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setModeSetting(Mode.MUSIC, "hue_start",
                              newValue.intValue()))));
    instance.musicHueStep     .addListener(((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setModeSetting(Mode.MUSIC, "hue_step",
                              newValue.intValue()))));

    instance.channelChannel   .addListener(((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setModeSetting(Mode.CHANNEL, "channel",
                              newValue.intValue()))));
    instance.channelHue       .addListener(((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setModeSetting(Mode.CHANNEL, "hue",
                              newValue.intValue()))));

    instance.ambientHue       .addListener(((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setModeSetting(Mode.AMBIENT, "hue",
                              newValue.intValue()))));

    instance.dynamicFlowSpeed .addListener(((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setModeSetting(Mode.DYNAMIC, "flow_speed",
                              newValue.intValue()))));

    instance.randomSpeed      .addListener(((observable, oldValue, newValue) -> Main.getSerialConnection().write(CommandEncoder.setModeSetting(Mode.RANDOM, "speed",
                              newValue.intValue()))));

    /*
      SET PROPERTIES FROM STORED CONFIGURATION
     */
//    System.out.println(instance.state);
//    System.out.println(instance.mode);
//    System.out.println(instance.brightness);
//    System.out.println(instance.white);

    instance.state            .set(jsonObject.get("state").getAsBoolean());
    instance.mode             .set(FxGson.create().fromJson(jsonObject.get("mode"), Mode.class));
    instance.brightness       .set(jsonObject.get("brightness").getAsDouble());
    instance.white            .set(jsonObject.get("white").getAsBoolean());

    // mode specific
    instance.musicHueStart    .set(jsonObject.get("musicHueStart").getAsInt());
    instance.musicHueStep     .set(jsonObject.get("musicHueStep").getAsInt());

    instance.channelChannel   .set(jsonObject.get("channelChannel").getAsInt());
    instance.channelHue       .set(jsonObject.get("channelHue").getAsInt());

    instance.ambientHue       .set(jsonObject.get("ambientHue").getAsInt());

    instance.dynamicFlowSpeed .set(jsonObject.get("dynamicFlowSpeed").getAsInt());

    instance.randomSpeed      .set(jsonObject.get("randomSpeed").getAsInt());
  }
}
