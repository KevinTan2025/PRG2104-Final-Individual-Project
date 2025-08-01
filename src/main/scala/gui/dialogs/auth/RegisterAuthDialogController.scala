package gui.dialogs.auth

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label, TextField, PasswordField}
import javafx.event.ActionEvent
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.Includes._
import gui.utils.GuiUtils
import java.net.URL
import java.util.ResourceBundle
import java.util.concurrent.atomic.AtomicReference
import scala.util.matching.Regex

/**
 * Registration screen controller
 * Connects to RegisterAuthDialog.fxml
 */
class RegisterAuthDialogController extends Initializable {
  
  // FXML component binding
  @FXML private var btnRegisterBack: Button = _
  @FXML private var lblRegisterTitle: Label = _
  @FXML private var txtRegUsername: TextField = _
  @FXML private var lblUsernameStatus: Label = _
  @FXML private var txtRegName: TextField = _
  @FXML private var lblNameStatus: Label = _
  @FXML private var txtRegContact: TextField = _
  @FXML private var lblContactStatus: Label = _
  @FXML private var txtRegEmail: TextField = _
  @FXML private var lblEmailStatus: Label = _
  @FXML private var btnSendOtp: Button = _
  @FXML private var lblOtpStatus: Label = _
  @FXML private var txtRegPassword: PasswordField = _
  @FXML private var lblPasswordStatus: Label = _
  @FXML private var txtRegConfirmPassword: PasswordField = _
  @FXML private var lblConfirmPasswordStatus: Label = _
  @FXML private var btnRegister: Button = _
  @FXML private var btnRegisterToLogin: Button = _
  
  // Parent controller reference using AtomicReference for thread-safe mutable state
  private val parentController: AtomicReference[Option[AuthDialogController]] = new AtomicReference(None)
  
  // Community service instance
  private val communityService = service.CommunityEngagementService.getInstance
  
  // OTP verification status
  private val isEmailVerifiedProperty = BooleanProperty(false)
  private val generatedOtpProperty = ObjectProperty[Option[String]](None)
  
  // Email format validation regex
  private val emailRegex: Regex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
  
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
    parentController.set(Some(controller))
  }
  
  /**
   * Set initial state of components
   */
  private def setupComponents(): Unit = {
    // Clear status labels
    clearAllStatusLabels()
    
    // Disable send OTP button
    btnSendOtp.disable = true
    
    // Set focus to username field
    Platform.runLater {
      txtRegUsername.requestFocus()
    }
  }
  
  /**
   * Set event handlers
   */
  private def setupEventHandlers(): Unit = {
    // Username field change listener
    txtRegUsername.text.onChange { (_, _, newValue) =>
      validateUsername(newValue)
    }
    
    // Name field change listener
    txtRegName.text.onChange { (_, _, newValue) =>
      validateName(newValue)
    }
    
    // Contact field change listener
    txtRegContact.text.onChange { (_, _, newValue) =>
      validateContact(newValue)
    }
    
    // Email field change listener
    txtRegEmail.text.onChange { (_, _, newValue) =>
      validateEmail(newValue)
    }
    
    // Password field change listener
    txtRegPassword.text.onChange { (_, _, newValue) =>
      validatePassword(newValue)
      // Re-validate confirm password
      if (txtRegConfirmPassword.text.value.nonEmpty) {
        validateConfirmPassword(newValue, txtRegConfirmPassword.text.value)
      }
    }
    
    // Confirm password field change listener
    txtRegConfirmPassword.text.onChange { (_, _, newValue) =>
      validateConfirmPassword(txtRegPassword.text.value, newValue)
    }
  }
  
  /**
   * Validate username
   * @param username Username
   */
  private def validateUsername(username: String): Unit = {
    if (username.trim.isEmpty) {
      lblUsernameStatus.text = ""
      updateFieldStyle(txtRegUsername, "normal")
      return
    }
    
    if (username.length < 3) {
      lblUsernameStatus.text = "✗ Username must be at least 3 characters"
      updateStatusLabelStyle(lblUsernameStatus, "error")
      updateFieldStyle(txtRegUsername, "error")
    } else if (!isValidUsernameFormat(username)) {
      lblUsernameStatus.text = "✗ Username can only contain letters, numbers and underscores"
      updateStatusLabelStyle(lblUsernameStatus, "error")
      updateFieldStyle(txtRegUsername, "error")
    } else if (!communityService.isUsernameAvailable(username)) {
      lblUsernameStatus.text = "✗ Username is already taken"
      updateStatusLabelStyle(lblUsernameStatus, "error")
      updateFieldStyle(txtRegUsername, "error")
    } else {
      lblUsernameStatus.text = "✓ Username is available"
      updateStatusLabelStyle(lblUsernameStatus, "success")
      updateFieldStyle(txtRegUsername, "success")
    }
  }
  
  /**
   * Validate name
   * @param name Name
   */
  private def validateName(name: String): Unit = {
    if (name.trim.isEmpty) {
      lblNameStatus.text = ""
      updateFieldStyle(txtRegName, "normal")
    } else if (name.trim.length < 2) {
      lblNameStatus.text = "✗ Name must be at least 2 characters"
      updateStatusLabelStyle(lblNameStatus, "error")
      updateFieldStyle(txtRegName, "error")
    } else {
      lblNameStatus.text = "✓ Name format is correct"
      updateStatusLabelStyle(lblNameStatus, "success")
      updateFieldStyle(txtRegName, "success")
    }
  }
  
  /**
   * Validate contact information
   * @param contact Contact information
   */
  private def validateContact(contact: String): Unit = {
    if (contact.trim.isEmpty) {
      lblContactStatus.text = ""
      updateFieldStyle(txtRegContact, "normal")
    } else if (contact.trim.length < 5) {
      lblContactStatus.text = "✗ Contact information must be at least 5 characters"
      updateStatusLabelStyle(lblContactStatus, "error")
      updateFieldStyle(txtRegContact, "error")
    } else {
      lblContactStatus.text = "✓ Contact information format is correct"
      updateStatusLabelStyle(lblContactStatus, "success")
      updateFieldStyle(txtRegContact, "success")
    }
  }
  
  /**
   * Validate email
   * @param email Email address
   */
  private def validateEmail(email: String): Unit = {
    val isValidEmail = email.nonEmpty && isValidEmailFormat(email)
    btnSendOtp.disable = !isValidEmail
    
    if (email.trim.isEmpty) {
      lblEmailStatus.text = ""
      updateFieldStyle(txtRegEmail, "normal")
      resetOTPState()
    } else if (isValidEmail) {
      if (!communityService.isEmailAvailable(email)) {
        lblEmailStatus.text = "✗ Email is already registered"
        updateStatusLabelStyle(lblEmailStatus, "error")
        updateFieldStyle(txtRegEmail, "error")
        btnSendOtp.disable = true
      } else {
        lblEmailStatus.text = "✓ Email format is correct"
        updateStatusLabelStyle(lblEmailStatus, "success")
        updateFieldStyle(txtRegEmail, "success")
      }
    } else {
      lblEmailStatus.text = "✗ Invalid email format"
      updateStatusLabelStyle(lblEmailStatus, "error")
      updateFieldStyle(txtRegEmail, "error")
    }
    
    if (!isValidEmail) {
      resetOTPState()
    }
  }
  
  /**
   * Validate password
   * @param password Password
   */
  private def validatePassword(password: String): Unit = {
    if (password.isEmpty) {
      lblPasswordStatus.text = ""
      updateFieldStyle(txtRegPassword, "normal")
      return
    }
    
    val requirements = getPasswordRequirements(password)
    val missingCount = requirements.count(!_._2)
    
    if (missingCount == 0) {
      lblPasswordStatus.text = "✓ Password meets all requirements"
      updateStatusLabelStyle(lblPasswordStatus, "success")
      updateFieldStyle(txtRegPassword, "success")
    } else {
      val missing = requirements.filter(!_._2).map(_._1)
      lblPasswordStatus.text = s"✗ Missing: ${missing.mkString(", ")}"
      updateStatusLabelStyle(lblPasswordStatus, "error")
      updateFieldStyle(txtRegPassword, "error")
    }
  }
  
  /**
   * Validate confirm password
   * @param password Original password
   * @param confirmPassword Confirm password
   */
  private def validateConfirmPassword(password: String, confirmPassword: String): Unit = {
    if (confirmPassword.isEmpty) {
      lblConfirmPasswordStatus.text = ""
      updateFieldStyle(txtRegConfirmPassword, "normal")
      return
    }
    
    if (password == confirmPassword) {
      lblConfirmPasswordStatus.text = "✓ Passwords match"
      updateStatusLabelStyle(lblConfirmPasswordStatus, "success")
      updateFieldStyle(txtRegConfirmPassword, "success")
    } else {
      lblConfirmPasswordStatus.text = "✗ Passwords do not match"
      updateStatusLabelStyle(lblConfirmPasswordStatus, "error")
      updateFieldStyle(txtRegConfirmPassword, "error")
    }
  }
  
  /**
   * Get password requirement check results
   * @param password Password
   * @return Requirement list and whether they are met
   */
  private def getPasswordRequirements(password: String): List[(String, Boolean)] = {
    List(
      ("8+ characters", password.length >= 8),
      ("uppercase letter", password.exists(_.isUpper)),
      ("lowercase letter", password.exists(_.isLower)),
      ("number", password.exists(_.isDigit)),
      ("special character", password.exists(c => !c.isLetterOrDigit))
    )
  }
  
  /**
   * Check if username format is valid
   * @param username Username
   * @return Whether it is valid
   */
  private def isValidUsernameFormat(username: String): Boolean = {
    username.matches("^[a-zA-Z0-9_]+$")
  }
  
  /**
   * Check if email format is valid
   * @param email Email address
   * @return Whether it is valid
   */
  private def isValidEmailFormat(email: String): Boolean = {
    emailRegex.findFirstIn(email).isDefined
  }
  
  /**
   * Update field style
   * @param field Field
   * @param status Status
   */
  private def updateFieldStyle(field: TextField, status: String): Unit = {
    field.getStyleClass.removeAll("error-field", "success-field")
    status match {
      case "error" => field.getStyleClass.add("error-field")
      case "success" => field.getStyleClass.add("success-field")
      case _ => // normal状态不添加特殊样式
    }
  }
  
  /**
   * Update status label style
   * @param label Label
   * @param status Status
   */
  private def updateStatusLabelStyle(label: Label, status: String): Unit = {
    label.getStyleClass.removeAll("error-status", "success-status", "warning-status")
    status match {
      case "error" => label.getStyleClass.add("error-status")
      case "success" => label.getStyleClass.add("success-status")
      case "warning" => label.getStyleClass.add("warning-status")
      case _ => // normal状态不添加特殊样式
    }
  }
  
  /**
   * Reset OTP status
   */
  private def resetOTPState(): Unit = {
    isEmailVerifiedProperty.value = false
    generatedOtpProperty.value = None
    lblOtpStatus.text = ""
  }
  
  /**
   * Clear all status labels
   */
  private def clearAllStatusLabels(): Unit = {
    lblUsernameStatus.text = ""
    lblNameStatus.text = ""
    lblContactStatus.text = ""
    lblEmailStatus.text = ""
    lblOtpStatus.text = ""
    lblPasswordStatus.text = ""
    lblConfirmPasswordStatus.text = ""
  }
  
  /**
   * Handle back button click event
   */
  @FXML
  def handleRegisterBack(event: ActionEvent): Unit = {
    parentController.get().foreach(_.showWelcomeMode())
  }
  
  /**
   * Handle send OTP button click event
   */
  @FXML
  def handleSendOtp(event: ActionEvent): Unit = {
    val email = txtRegEmail.text.value.trim
    
    if (email.isEmpty || !isValidEmailFormat(email)) {
      GuiUtils.showWarning("Email Error", "Please enter a valid email address")
      return
    }
    
    // Create and show OTP verification dialog
    val otpDialog = new OTPVerificationDialog(
      parentController.get().map(_.dialogStage).getOrElse(new scalafx.stage.Stage()),
      email
    )
    
    otpDialog.show(
      onSuccess = () => {
        isEmailVerifiedProperty.value = true
        lblOtpStatus.text = "✓ Email verification successful"
        updateStatusLabelStyle(lblOtpStatus, "success")
        btnSendOtp.text = "✓ Verified"
        btnSendOtp.disable = true
      },
      onFailure = () => {
        isEmailVerifiedProperty.value = false
        lblOtpStatus.text = "✗ Email verification failed"
        updateStatusLabelStyle(lblOtpStatus, "error")
      }
    )
  }
  
  /**
   * Handle register button click event
   */
  @FXML
  def handleRegister(event: ActionEvent): Unit = {
    // Get all field values
    val username = txtRegUsername.text.value.trim
    val name = txtRegName.text.value.trim
    val contact = txtRegContact.text.value.trim
    val email = txtRegEmail.text.value.trim
    val password = txtRegPassword.text.value
    val confirmPassword = txtRegConfirmPassword.text.value
    
    // Validate all required fields
    if (!validateAllFields(username, name, contact, email, password, confirmPassword)) {
      return
    }
    
    // Check if email is verified
    if (!isEmailVerifiedProperty.value) {
      GuiUtils.showWarning("Email Not Verified", "Please verify your email address first")
      return
    }
    
    // Attempt registration
    try {
      val registrationResult = communityService.registerUser(
        username = username,
        email = email,
        name = name,
        contactInfo = contact,
        password = password,
        isAdmin = false
      )
      
      if (registrationResult) {
        GuiUtils.showInfo("Registration Successful", "🎉 Account created successfully! Welcome to the community platform!")
        parentController.get().foreach(_.setAuthResult(AuthResult.RegisterSuccess))
      } else {
        GuiUtils.showError("Registration Failed", "An error occurred during registration, please try again later")
      }
    } catch {
      case e: Exception =>
        GuiUtils.showError("Registration Error", s"An error occurred during registration: ${e.getMessage}")
    }
  }
  
  /**
   * Validate all fields
   * @return Whether all fields are valid
   */
  private def validateAllFields(username: String, name: String, contact: String, 
                               email: String, password: String, confirmPassword: String): Boolean = {
    
    // Define validation functions that return true if valid
    val validations = List(
      () => {
        if (username.isEmpty) {
          lblUsernameStatus.text = "✗ Please enter username"
          updateStatusLabelStyle(lblUsernameStatus, "error")
          false
        } else true
      },
      () => {
        if (name.isEmpty) {
          lblNameStatus.text = "✗ Please enter name"
          updateStatusLabelStyle(lblNameStatus, "error")
          false
        } else true
      },
      () => {
        if (contact.isEmpty) {
          lblContactStatus.text = "✗ Please enter contact information"
          updateStatusLabelStyle(lblContactStatus, "error")
          false
        } else true
      },
      () => {
        if (email.isEmpty) {
          lblEmailStatus.text = "✗ Please enter email address"
          updateStatusLabelStyle(lblEmailStatus, "error")
          false
        } else true
      },
      () => {
        if (password.isEmpty) {
          lblPasswordStatus.text = "✗ Please enter password"
          updateStatusLabelStyle(lblPasswordStatus, "error")
          false
        } else true
      },
      () => {
        if (confirmPassword.isEmpty) {
          lblConfirmPasswordStatus.text = "✗ Please confirm password"
          updateStatusLabelStyle(lblConfirmPasswordStatus, "error")
          false
        } else if (password != confirmPassword) {
          lblConfirmPasswordStatus.text = "✗ Passwords do not match"
          updateStatusLabelStyle(lblConfirmPasswordStatus, "error")
          false
        } else true
      }
    )
    
    // Execute all validations and return true only if all are valid
    validations.map(_.apply()).forall(identity)
  }
  
  /**
   * Handle navigate to login page button click event
   */
  @FXML
  def handleRegisterToLogin(event: ActionEvent): Unit = {
    parentController.get().foreach(_.showLoginMode())
  }
}