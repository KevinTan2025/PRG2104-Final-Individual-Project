package gui.components

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.stage.Stage
import scalafx.application.Platform
import gui.utils.GuiUtils
import gui.components.EnhancedTextField
import scala.util.Random

/**
 * Registration form component with integrated OTP verification
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
  
  // OTP verification components
  private val otpField = new TextField {
    promptText = "Enter 6-digit OTP"
    maxWidth = 150
    style = "-fx-font-size: 14px; -fx-text-alignment: center;"
    disable = true
  }
  
  private val sendOtpButton = new Button("Send OTP") {
    prefWidth = 100
    disable = true
  }
  
  private val verifyOtpButton = new Button("Verify") {
    prefWidth = 80
    disable = true
  }
  
  // Status labels
  private val emailStatusLabel = new Label("") {
    style = "-fx-font-size: 12px;"
  }
  
  private val otpStatusLabel = new Label("") {
    style = "-fx-font-size: 12px;"
  }
  
  // OTP state
  private var generatedOtp: Option[String] = None
  private var isEmailVerified = false
  
  // Initialize component
  setupEventHandlers()
  
  private def setupEventHandlers(): Unit = {
    // Email field changes
    emailField.text.onChange { (_, _, _) =>
      val email = emailField.getUserInput
      val isValidEmail = email.nonEmpty && isValidEmailFormat(email)
      sendOtpButton.disable = !isValidEmail
      
      if (isValidEmail) {
        emailStatusLabel.text = "✓ Valid email format"
        emailStatusLabel.style = "-fx-font-size: 12px; -fx-text-fill: green;"
      } else if (email.nonEmpty) {
        emailStatusLabel.text = "✗ Invalid email format"
        emailStatusLabel.style = "-fx-font-size: 12px; -fx-text-fill: red;"
      } else {
        emailStatusLabel.text = ""
      }
      
      if (!isValidEmail) {
        resetOTPState()
      }
    }
    
    // Send OTP button
    sendOtpButton.onAction = (_: ActionEvent) => sendOTP()
    
    // Verify OTP button
    verifyOtpButton.onAction = (_: ActionEvent) => verifyOTP()
    
    // OTP field changes
    otpField.text.onChange { (_, _, newValue) =>
      verifyOtpButton.disable = newValue.length != 6 || !newValue.forall(_.isDigit)
    }
  }
  
  override def build(): Region = {
    val registerButton = new Button("Register") {
      onAction = (_: ActionEvent) => handleRegistration()
    }
    
    val backButton = new Button("Back to Login") {
      onAction = (_: ActionEvent) => onBackClick()
    }
    
    // Email verification section
    val emailVerificationBox = new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("Email:"),
        emailField,
        sendOtpButton
      )
    }
    
    // OTP verification section
    val otpVerificationBox = new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("OTP:"),
        otpField,
        verifyOtpButton
      )
    }
    
    val formBox = new VBox {
      spacing = 15
      alignment = Pos.Center
      children = Seq(
        new Label("Register New Account") {
          style = "-fx-font-size: 20px; -fx-font-weight: bold;"
        },
        usernameField,
        nameField,
        contactField,
        passwordField,
        confirmPasswordField,
        new Separator(),
        new Label("Email Verification") {
          style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2196F3;"
        },
        emailVerificationBox,
        emailStatusLabel,
        otpVerificationBox,
        otpStatusLabel,
        new Separator(),
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(registerButton, backButton)
        }
      )
      padding = Insets(30)
    }
    
    new BorderPane {
      center = formBox
      style = "-fx-background-color: #f5f5f5;"
    }
  }
  
  private def sendOTP(): Unit = {
    val email = emailField.getUserInput
    
    if (!isValidEmailFormat(email)) {
      GuiUtils.showError("Invalid Email", "Please enter a valid email address.")
      return
    }
    
    // Check if email is already registered
    if (!service.isEmailAvailable(email)) {
      GuiUtils.showError("Email Already Registered", s"The email '$email' is already registered. Please use a different email or try logging in.")
      return
    }
    
    // Generate OTP
    generatedOtp = Some(generateOTP())
    
    // Enable OTP field and verify button
    otpField.disable = false
    verifyOtpButton.disable = false
    sendOtpButton.disable = true
    sendOtpButton.text = "Sent"
    
    // Show simulated notification
    Platform.runLater {
      val notification = new Alert(Alert.AlertType.Information) {
        title = "OTP Sent"
        headerText = "Verification Code Sent"
        contentText = s"A 6-digit OTP has been sent to $email\n\n[SIMULATED] Your OTP: ${generatedOtp.get}"
      }
      notification.showAndWait()
    }
    
    otpStatusLabel.text = "OTP sent to your email"
    otpStatusLabel.style = "-fx-font-size: 12px; -fx-text-fill: blue;"
    
    // Focus on OTP field
    Platform.runLater {
      otpField.requestFocus()
    }
  }
  
  private def verifyOTP(): Unit = {
    val enteredOtp = otpField.text.value.trim
    
    generatedOtp match {
      case Some(correctOtp) if enteredOtp == correctOtp =>
        isEmailVerified = true
        otpField.disable = true
        verifyOtpButton.disable = true
        otpStatusLabel.text = "✓ Email verified successfully!"
        otpStatusLabel.style = "-fx-font-size: 12px; -fx-text-fill: green;"
        
        GuiUtils.showInfo("Email Verified", "Your email has been verified successfully!")
        
      case Some(_) =>
        otpStatusLabel.text = "✗ Invalid OTP. Please try again."
        otpStatusLabel.style = "-fx-font-size: 12px; -fx-text-fill: red;"
        otpField.clear()
        otpField.requestFocus()
        
      case None =>
        GuiUtils.showError("No OTP", "Please send OTP first.")
    }
  }
  
  private def resetOTPState(): Unit = {
    generatedOtp = None
    isEmailVerified = false
    otpField.clear()
    otpField.disable = true
    verifyOtpButton.disable = true
    sendOtpButton.disable = true
    sendOtpButton.text = "Send OTP"
    emailStatusLabel.text = ""
    otpStatusLabel.text = ""
  }
  
  private def generateOTP(): String = {
    val random = new Random()
    (100000 + random.nextInt(900000)).toString
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
    if (!isValidEmailFormat(email)) {
      GuiUtils.showError("Invalid Email", "Please enter a valid email address.")
      return
    }
    
    // Email verification check
    if (!isEmailVerified) {
      GuiUtils.showWarning("Email Not Verified", "Please verify your email address first by clicking 'Send OTP' and entering the verification code.")
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
    
    // Register user (email availability was already checked during OTP sending)
    if (service.registerUser(username, email, name, contact, password, isAdmin = false)) {
      GuiUtils.showInfo("Registration Successful", "Account created successfully! You can now log in.")
      clearForm()
      onRegistrationSuccess()
    } else {
      GuiUtils.showError("Registration Failed", "An error occurred during registration. Please try again.")
    }
  }
  
  private def clearForm(): Unit = {
    usernameField.clearInput()
    emailField.clearInput()
    nameField.clearInput()
    contactField.clearInput()
    passwordField.clear()
    confirmPasswordField.clear()
    resetOTPState()
  }
  
  private def isValidEmailFormat(email: String): Boolean = {
    val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
    emailRegex.matches(email)
  }
}
