package lightcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TimeoutController
{
  private Stage stage;

  @FXML
  private Button closeButton;

  @FXML
  private Button retryButton;

  @FXML
  private Label retryLabel;

  public void setStage(Stage stage)
  {
    this.stage = stage;
  }

  @FXML
  private void onCloseButtonClicked()
  {
    System.exit(0);
  }

  @FXML
  private void onRetryButtonClicked()
  {
    retryLabel.setVisible(true);
    try
    {
      Main.attemptStart(this.stage);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
