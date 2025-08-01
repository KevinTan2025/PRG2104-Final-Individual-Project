package gui.dialogs.auth

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label}
import javafx.event.ActionEvent
import java.net.URL
import java.util.ResourceBundle

/**
 * Welcome screen controller
 * Connects to WelcomeAuthDialog.fxml
 */
class WelcomeAuthDialogController extends Initializable {
  
  // FXML component binding
  @FXML private var lblTitle: Label = _
  @FXML private var lblWelcomeIcon: Label = _
  @FXML private var lblWelcomeText: Label = _
  @FXML private var btnWelcomeLogin: Button = _
  @FXML private var btnWelcomeRegister: Button = _
  @FXML private var btnWelcomeGuest: Button = _
  
  // Parent controller reference
  private var parentController: Option[AuthDialogController] = None
  
  /**
   * Initialization method
   */
  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    // Set initial state of components
    setupComponents()
  }
  
  /**
   * Set parent controller
   * @param controller Parent controller instance
   */
  def setParentController(controller: AuthDialogController): Unit = {
    parentController = Some(controller)
  }
  
  /**
   * Set initial state of components
   */
  private def setupComponents(): Unit = {
    // Can set initial state of components here
    // For example, set text, styles, etc.
  }
  
  /**
   * Handle login button click event
   */
  @FXML
  def handleWelcomeLogin(event: ActionEvent): Unit = {
    parentController.foreach(_.showLoginMode())
  }
  
  /**
   * Handle register button click event
   */
  @FXML
  def handleWelcomeRegister(event: ActionEvent): Unit = {
    parentController.foreach(_.showRegisterMode())
  }
  
  /**
   * Handle guest mode button click event
   */
  @FXML
  def handleWelcomeGuest(event: ActionEvent): Unit = {
    parentController.foreach(_.setAuthResult(AuthResult.ContinueAsGuest))
  }
}