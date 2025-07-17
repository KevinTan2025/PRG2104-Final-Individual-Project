package gui.components

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.stage.Stage
import gui.utils.GuiUtils
import gui.dialogs.OTPVerificationDialog
import gui.components.EnhancedTextField

/**
 * Registration form component with OTP verification
 */
class RegisterComponent(
  onRegistrationSuccess: () => Unit,
  onBackClick: () => Unit
) extends BaseComponent {
  
  private val usernameField = new EnhancedTextField("Username")
  private val emailField = new EnhancedTextField("Email")
  private val nameField = new EnhancedTextField("Full Name")
  private val contactField = new EnhancedTextField("Contact Info")
  private val passwordField = new PasswordField { promptText = "Password (8+ chars, letter, digit, special char)" }
  private val confirmPasswordField = new PasswordField { promptText = "Confirm Password" }
  
  // Pending registration data
  private var pendingRegistration: Option[RegistrationData] = None
  
  case class RegistrationData(
    username: String,
    email: String,
    name: String,
    contact: String,
    password: String
  )
  
  override def build(): Region = {
    val registerButton = new Button("Register") {
      onAction = (_: ActionEvent) => handleRegistration()
    }
    
    val backButton = new Button("Back to Login") {
      onAction = (_: ActionEvent) => onBackClick()
    }
    
    val formBox = new VBox {
      spacing = 15
      alignment = Pos.Center
      children = Seq(
        new Label("Register New Account") {
          style = "-fx-font-size: 20px; -fx-font-weight: bold;"
        },
        usernameField,
        emailField,
        nameField,
        contactField,
        passwordField,
        confirmPasswordField,
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(registerButton, backButton)
        }
      )
      padding = Insets(50)
    }
    
    new BorderPane {
      center = formBox
      style = "-fx-background-color: #f5f5f5;"
    }
  }
  
  private def handleRegistration(): Unit = {
    val username = usernameField.getUserInput
    val email = emailField.getUserInput
    val name = nameField.getUserInput
    val contact = contactField.getUserInput
    val password = passwordField.text.value
    val confirmPassword = confirmPasswordField.text.value
    
    // Basic validation
    if (username.isEmpty || email.isEmpty || name.isEmpty || password.isEmpty) {
      GuiUtils.showWarning("Incomplete Information", "Please fill in all required fields.")
      return
    }
    
    // Email format validation
    if (!isValidEmail(email)) {
      GuiUtils.showError("Invalid Email", "Please enter a valid email address.")
      return
    }
    
    // Password validation
    if (password != confirmPassword) {
      GuiUtils.showError("Password Mismatch", "Password and confirmation do not match.")
      return
    }
    
    if (!util.PasswordHasher.isPasswordValid(password)) {
      GuiUtils.showError("Invalid Password", util.PasswordHasher.getPasswordRequirements)
      return
    }
    
    // Check for existing username
    if (!service.isUsernameAvailable(username)) {
      GuiUtils.showError("Username Taken", s"The username '$username' is already taken. Please choose a different one.")
      return
    }
    
    // Check for existing email
    if (!service.isEmailAvailable(email)) {
      GuiUtils.showError("Email Already Registered", s"The email '$email' is already registered. Please use a different email or try logging in.")
      return
    }
    
    // Store registration data and start OTP verification
    pendingRegistration = Some(RegistrationData(username, email, name, contact, password))
    showOTPVerification(email)
  }
  
  private def showOTPVerification(email: String): Unit = {
    // Get the parent stage from the scene
    val parentStage = usernameField.scene.value.window.value.asInstanceOf[Stage]
    
    val otpDialog = new OTPVerificationDialog(parentStage, email)
    otpDialog.show(
      onSuccess = () => completeRegistration(),
      onFailure = () => {
        pendingRegistration = None
        GuiUtils.showWarning("Registration Cancelled", "Email verification was cancelled. Please try again.")
      }
    )
  }
  
  private def completeRegistration(): Unit = {
    pendingRegistration match {
      case Some(data) =>
        if (service.registerUser(data.username, data.email, data.name, data.contact, data.password, isAdmin = false)) {
          GuiUtils.showInfo("Registration Successful", "Account created successfully! You can now log in.")
          clearForm()
          onRegistrationSuccess()
        } else {
          GuiUtils.showError("Registration Failed", "An error occurred during registration. Please try again.")
        }
        pendingRegistration = None
      case None =>
        GuiUtils.showError("Registration Error", "Registration data not found. Please try again.")
    }
  }
  
  private def clearForm(): Unit = {
    usernameField.clearInput()
    emailField.clearInput()
    nameField.clearInput()
    contactField.clearInput()
    passwordField.clear()
    confirmPasswordField.clear()
  }
  
  private def isValidEmail(email: String): Boolean = {
    val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
    emailRegex.matches(email)
  }
}
