package gui.dialogs.auth

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label, TextField, PasswordField}
import javafx.event.ActionEvent
import scalafx.application.Platform
import scalafx.Includes._
import gui.utils.GuiUtils
import java.net.URL
import java.util.ResourceBundle

/**
 * Login screen controller
 * Connects to LoginAuthDialog.fxml
 */
class LoginAuthDialogController extends Initializable {
  
  // FXML component binding
  @FXML private var btnLoginBack: Button = _
  @FXML private var lblLoginTitle: Label = _
  @FXML private var txtLoginUsername: TextField = _
  @FXML private var lblLoginUsernameStatus: Label = _
  @FXML private var txtLoginPassword: PasswordField = _
  @FXML private var lblLoginPasswordStatus: Label = _
  @FXML private var lblLoginErrorStatus: Label = _
  @FXML private var btnLogin: Button = _
  @FXML private var lblLoginDemo: Label = _
  @FXML private var btnLoginToRegister: Button = _
  
  // Parent controller reference
  private var parentController: Option[AuthDialogController] = None
  
  // Community service instance
  private val communityService = service.CommunityEngagementService.getInstance
  
  /**
   * Initialization method
   */
  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    setupComponents()
    setupEventHandlers()
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
    // Clear status labels
    clearStatusLabels()
    
    // Set focus to username field
    Platform.runLater {
      txtLoginUsername.requestFocus()
    }
  }
  
  /**
   * Set event handlers
   */
  private def setupEventHandlers(): Unit = {
    // Username field change listener
    txtLoginUsername.text.onChange { (_, _, newValue) =>
      validateUsernameInput(newValue)
    }
    
    // Password field change listener
    txtLoginPassword.text.onChange { (_, _, newValue) =>
      validatePasswordInput(newValue)
    }
    
    // Enter key login
    txtLoginPassword.onAction = (_: ActionEvent) => handleLogin(null)
  }
  
  /**
   * Validate username input
   * @param username Username
   */
  private def validateUsernameInput(username: String): Unit = {
    if (username.trim.isEmpty) {
      lblLoginUsernameStatus.text = ""
      updateFieldStyle(txtLoginUsername, "normal")
    } else {
      lblLoginUsernameStatus.text = "✓ Username format is correct"
      updateStatusLabelStyle(lblLoginUsernameStatus, "success")
      updateFieldStyle(txtLoginUsername, "success")
    }
    clearErrorStatus()
  }
  
  /**
   * Validate password input
   * @param password Password
   */
  private def validatePasswordInput(password: String): Unit = {
    if (password.trim.isEmpty) {
      lblLoginPasswordStatus.text = ""
      updateFieldStyle(txtLoginPassword, "normal")
    } else {
      lblLoginPasswordStatus.text = "✓ Password entered"
      updateStatusLabelStyle(lblLoginPasswordStatus, "success")
      updateFieldStyle(txtLoginPassword, "success")
    }
    clearErrorStatus()
  }
  
  /**
   * Update field style
   * @param field Field control
   * @param status Status type
   */
  private def updateFieldStyle(field: TextField, status: String): Unit = {
    field.getStyleClass.removeAll("error-field", "success-field")
    status match {
      case "error" => field.getStyleClass.add("error-field")
      case "success" => field.getStyleClass.add("success-field")
      case _ => // Normal status does not add special styles
    }
  }
  
  /**
   * Update status label style
   * @param label Label control
   * @param status Status type
   */
  private def updateStatusLabelStyle(label: Label, status: String): Unit = {
    label.getStyleClass.removeAll("error-status", "success-status", "warning-status")
    status match {
      case "error" => label.getStyleClass.add("error-status")
      case "success" => label.getStyleClass.add("success-status")
      case "warning" => label.getStyleClass.add("warning-status")
      case _ => // Normal status does not add special styles
    }
  }
  
  /**
   * Clear error status
   */
  private def clearErrorStatus(): Unit = {
    lblLoginErrorStatus.text = ""
  }
  
  /**
   * Clear all status labels
   */
  private def clearStatusLabels(): Unit = {
    lblLoginUsernameStatus.text = ""
    lblLoginPasswordStatus.text = ""
    lblLoginErrorStatus.text = ""
  }
  
  /**
   * Handle back button click event
   */
  @FXML
  def handleLoginBack(event: ActionEvent): Unit = {
    parentController.foreach(_.showWelcomeMode())
  }
  
  /**
   * Handle login button click event
   */
  @FXML
  def handleLogin(event: ActionEvent): Unit = {
    val username = txtLoginUsername.text.value.trim
    val password = txtLoginPassword.text.value
    
    // Validate input
    if (username.isEmpty || password.isEmpty) {
      lblLoginErrorStatus.text = "✗ Please enter username and password"
      updateStatusLabelStyle(lblLoginErrorStatus, "error")
      return
    }
    
    // Attempt login
    try {
      val loginResult = communityService.login(username, password)
      
      if (loginResult.isDefined) {
        // Login successful
        lblLoginErrorStatus.text = "✓ Login successful!"
        updateStatusLabelStyle(lblLoginErrorStatus, "success")
        
        // Delay closing dialog
        Platform.runLater {
          Thread.sleep(500)
          parentController.foreach(_.setAuthResult(AuthResult.LoginSuccess))
        }
      } else {
        // Login failed
        lblLoginErrorStatus.text = "✗ Username or password incorrect"
        updateStatusLabelStyle(lblLoginErrorStatus, "error")
        updateFieldStyle(txtLoginUsername, "error")
        updateFieldStyle(txtLoginPassword, "error")
        
        // Clear password field
        txtLoginPassword.clear()
        txtLoginPassword.requestFocus()
      }
    } catch {
      case e: Exception =>
        lblLoginErrorStatus.text = s"✗ Login failed: ${e.getMessage}"
        updateStatusLabelStyle(lblLoginErrorStatus, "error")
        GuiUtils.showError("Login Error", s"Error occurred during login: ${e.getMessage}")
    }
  }
  
  /**
   * Handle jump to registration page button click event
   */
  @FXML
  def handleLoginToRegister(event: ActionEvent): Unit = {
    parentController.foreach(_.showRegisterMode())
  }
}