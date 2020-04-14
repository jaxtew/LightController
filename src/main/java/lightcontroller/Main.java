package lightcontroller;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortIOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lightcontroller.serial.SerialConnection;
import lightcontroller.settings.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application
{
  public static final boolean DEBUG = false;

  private static final long CONNECTION_TIMEOUT = 10000;
  private static boolean timedOut = false;

  private static SerialConnection serialConnection = null;

  public static void main(String[] args)
  {
    launch();
  }

  @Override
  public void start(Stage primaryStage) throws Exception
  {
    attemptStart(primaryStage);
  }

  public static void attemptStart(Stage primaryStage) throws Exception
  {
    timedOut = false;
    System.out.print("Establishing serial connection... ");
    List<SerialConnection> possibleConnections = new ArrayList<>();
    for (SerialPort port : SerialPort.getCommPorts())
    {
      try
      {
        possibleConnections.add(new SerialConnection(port));
      }
      catch (Exception e)
      {
        // silently ignore
        e.printStackTrace();
      }
    }

    /*
      WAIT FOR SERIAL CONNECTIONS TO RESPOND
     */
    long startTime = System.currentTimeMillis();
    AtomicBoolean connected = new AtomicBoolean(false);
    while (!connected.get())
    {
      // handle timeout
      if ((System.currentTimeMillis() - startTime) > CONNECTION_TIMEOUT)
      {
        timedOut = true;
        System.out.println("Timed out. Make sure Arduino is connected and serial monitor is off");
        possibleConnections.forEach(sc ->
        {
          try
          {
            sc.close();
          }
          catch (SerialPortIOException e)
          {
            e.printStackTrace();
          }
        });
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("timeout.fxml"));
        Parent root = loader.load();
        TimeoutController cont = loader.getController();
        cont.setStage(primaryStage);
        primaryStage.setTitle("Bedroom Light Controller");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
        return;
      }

      // check if a port is connected
      possibleConnections.forEach(sc ->
      {
        if (sc.isConnected())
        {
          serialConnection = sc;
          connected.set(true);
        }
      });
    }

    /*
      CLOSE OTHER PORTS
     */
    possibleConnections.stream().filter(sc -> !sc.equals(serialConnection)).forEach(sc ->
    {
      try
      {
        sc.close();
      }
      catch (SerialPortIOException e)
      {
        e.printStackTrace();
      }
    });

    System.out.println("Done.");
    System.out.println("Loading settings... ");
    Settings.load();
    System.out.println("Done.");
    System.out.println("Launching...");

    FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("main.fxml"));
    Parent root = loader.load();
    WindowController cont = loader.getController();
    primaryStage.setTitle("Bedroom Light Controller");
    primaryStage.setScene(new Scene(root));
    primaryStage.setOnHidden(e -> cont.shutdown());
    primaryStage.setResizable(false);
    primaryStage.show();
  }

  @Override
  public void stop() throws Exception
  {
    if (timedOut)
    {
      System.exit(0);
    }
    serialConnection.close();
    Settings.save(false);
    System.exit(0);
  }

  public static SerialConnection getSerialConnection()
  {
    return serialConnection;
  }
}
