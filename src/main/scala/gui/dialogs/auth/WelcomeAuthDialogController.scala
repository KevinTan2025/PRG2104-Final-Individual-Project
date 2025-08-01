package gui.dialogs.auth

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label}
import javafx.event.ActionEvent
import scalafx.Includes._
import java.net.URL
import java.util.ResourceBundle
import java.util.concurrent.atomic.AtomicReference

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
  
  // Parent controller reference using AtomicReference for thread-safe mutable state
  private val parentController: AtomicReference[Option[AuthDialogController]] = new AtomicReference(None)
  
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
    parentController.set(Some(controller))
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
    parentController.get().foreach(_.showLoginMode())
  }
  
  /**
   * Handle register button click event
   */
  @FXML
  def handleWelcomeRegister(event: ActionEvent): Unit = {
    parentController.get().foreach(_.showRegisterMode())
  }
  
  /**
   * Handle guest mode button click event
   */
  @FXML
  def handleWelcomeGuest(event: ActionEvent): Unit = {
    parentController.get().foreach(_.setAuthResult(AuthResult.ContinueAsGuest))
  }
}