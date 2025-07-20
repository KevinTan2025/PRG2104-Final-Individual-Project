package gui.dialogs.auth

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.{Stage, Modality, StageStyle}
import scalafx.scene.paint.Color
import scalafx.animation.{FadeTransition}
import scalafx.util.Duration
import javafx.stage.WindowEvent
import gui.utils.GuiUtils
import gui.components.common.public.EnhancedTextField
import scala.util.Random

/**
 * Facebook-style authentication popup dialog
 * Combines login and registration in a modern overlay design
 */
sealed trait AuthMode
object AuthMode {
  case object LoginMode extends AuthMode
  case object RegisterMode extends AuthMode
  case object WelcomeMode extends AuthMode
}

sealed trait AuthResult
object AuthResult {
  case object LoginSuccess extends AuthResult
  case object RegisterSuccess extends AuthResult
  case object ContinueAsGuest extends AuthResult
  case object Cancelled extends AuthResult
}

class FacebookStyleAuthDialog(parentStage: Stage) {
  
  private var currentMode: AuthMode = AuthMode.WelcomeMode
  private var authResult: Option[AuthResult] = None
  private val communityService = service.CommunityEngagementService.getInstance
  
  // Main dialog
  private val dialog = new Stage {
    title = "Community Platform - Authentication"
    initModality(Modality.ApplicationModal)
    initOwner(parentStage)
    initStyle(StageStyle.Decorated) // Use standard window decorations
    resizable = false
    
    onCloseRequest = (event: WindowEvent) => {
      authResult = Some(AuthResult.ContinueAsGuest)
    }
  }
  
  // Login form components
  private val loginUsernameField = new TextField {
    promptText = "Username or Email"
    prefWidth = 300
    prefHeight = 40
    style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd; -fx-prompt-text-fill: #999999;"
  }
  
  private val loginPasswordField = new PasswordField {
    promptText = "Password"
    prefWidth = 300
    prefHeight = 40
    style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd; -fx-prompt-text-fill: #999999;"
  }
  
  // Registration form components
  private val regUsernameField = new EnhancedTextField("Username") {
    prefWidth = 300
    prefHeight = 40
    style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd;"
  }
  private val regEmailField = new EnhancedTextField("Email") {
    prefWidth = 300
    prefHeight = 40
    style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd;"
  }
  private val regNameField = new EnhancedTextField("Full Name") {
    prefWidth = 300
    prefHeight = 40
    style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd;"
  }
  private val regContactField = new EnhancedTextField("Contact Info") {
    prefWidth = 300
    prefHeight = 40
    style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd;"
  }
  private val regPasswordField = new PasswordField { 
    promptText = "Password (8+ chars, letter, digit, special char)" 
    prefWidth = 300
    prefHeight = 40
    style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd; -fx-prompt-text-fill: #999999;"
  }
  private val regConfirmPasswordField = new PasswordField { 
    promptText = "Confirm Password" 
    prefWidth = 300
    prefHeight = 40
    style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd; -fx-prompt-text-fill: #999999;"
  }
  
  // OTP verification components
  private val sendOtpButton = new Button("📧 Send Verification Email") {
    prefWidth = 200
    disable = true
    style = "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12px;"
  }
  
  private val usernameStatusLabel = new Label("") {
    style = "-fx-font-size: 11px; -fx-padding: 2 0 0 5;"
  }
  
  private val emailStatusLabel = new Label("") {
    style = "-fx-font-size: 11px; -fx-padding: 2 0 0 5;"
  }
  
  private val passwordStatusLabel = new Label("") {
    style = "-fx-font-size: 11px; -fx-padding: 2 0 0 5;"
  }
  
  private val confirmPasswordStatusLabel = new Label("") {
    style = "-fx-font-size: 11px; -fx-padding: 2 0 0 5;"
  }
  
  private val otpStatusLabel = new Label("") {
    style = "-fx-font-size: 11px; -fx-padding: 2 0 0 5;"
  }
  
  // OTP state
  private var generatedOtp: Option[String] = None
  private var isEmailVerified = false
  
  // Main container for switching between modes
  private val contentContainer = new StackPane()
  
  def show(): AuthResult = {
    show(AuthMode.WelcomeMode)
  }
  
  def show(initialMode: AuthMode): AuthResult = {
    setupEventHandlers()
    dialog.scene = createScene()
    initialMode match {
      case AuthMode.WelcomeMode => showWelcomeMode()
      case AuthMode.LoginMode => showLoginMode()
      case AuthMode.RegisterMode => showRegisterMode()
    }
    dialog.centerOnScreen()
    dialog.showAndWait()
    authResult.getOrElse(AuthResult.ContinueAsGuest)
  }
  
  private def createScene(): Scene = {
    // Main dialog container
    val mainContainer = new VBox {
      maxWidth = 400
      maxHeight = 550
      style = "-fx-background-color: white; -fx-padding: 20;"
    }
    
    // Content area
    contentContainer.children = Seq(createWelcomeContent())
    
    mainContainer.children = Seq(contentContainer)
    
    new Scene(mainContainer, 400, 550) {
      fill = Color.White
    }
  }
  
  private def createWelcomeContent(): Region = {
    val titleLabel = new Label("Community Platform") {
      style = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1877f2;"
    }
    
    val welcomeText = new Label("Connect with your community") {
      style = "-fx-font-size: 16px; -fx-text-fill: #666; -fx-text-alignment: center;"
      wrapText = true
    }
    
    val loginButton = new Button("Log In") {
      prefWidth = 280
      prefHeight = 45
      style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 6;"
      onAction = _ => showLoginMode()
    }
    
    val registerButton = new Button("Create New Account") {
      prefWidth = 280
      prefHeight = 45
      style = "-fx-background-color: #42b883; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 6;"
      onAction = _ => showRegisterMode()
    }
    
    val guestButton = new Button("Continue as Guest") {
      prefWidth = 280
      prefHeight = 35
      style = "-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-font-size: 14px; -fx-border-color: #1877f2; -fx-border-radius: 6; -fx-background-radius: 6;"
      onAction = _ => {
        authResult = Some(AuthResult.ContinueAsGuest)
        dialog.close()
      }
    }
    
    new VBox {
      spacing = 20
      alignment = Pos.Center
      padding = Insets(20, 30, 30, 30)
      children = Seq(
        titleLabel,
        new Label("🏠") {
          style = "-fx-font-size: 48px;"
        },
        welcomeText,
        new Separator {
          maxWidth = 200
          style = "-fx-background-color: #e1e8ed;"
        },
        loginButton,
        registerButton,
        new Separator {
          maxWidth = 150
          style = "-fx-background-color: #e1e8ed;"
        },
        guestButton
      )
    }
  }
  
  private def createLoginContent(): Region = {
    val backButton = new Button("← Back") {
      style = "-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
      onAction = _ => showWelcomeMode()
    }
    
    val loginButton = new Button("Log In") {
      prefWidth = 280
      prefHeight = 45
      style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 6;"
      onAction = _ => handleLogin()
    }
    
    val registerLinkButton = new Button("Don't have an account? Sign up") {
      style = "-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-font-size: 12px;"
      onAction = _ => showRegisterMode()
    }
    
    new VBox {
      spacing = 15
      alignment = Pos.Center
      padding = Insets(20, 40, 40, 40)
      children = Seq(
        new HBox {
          alignment = Pos.CenterLeft
          children = Seq(backButton)
        },
        new Label("Log in to your account") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;"
        },
        loginUsernameField,
        loginPasswordField,
        loginButton,
        new Label("Demo: admin/Admin123* or john_doe/Password123!") {
          style = "-fx-font-size: 11px; -fx-text-fill: #666; -fx-text-alignment: center;"
          wrapText = true
        },
        registerLinkButton
      )
    }
  }
  
  private def createRegisterContent(): Region = {
    val backButton = new Button("← Back") {
      style = "-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
      onAction = _ => showWelcomeMode()
    }
    
    val registerButton = new Button("Create Account") {
      prefWidth = 280
      prefHeight = 45
      style = "-fx-background-color: #42b883; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 6;"
      onAction = _ => handleRegistration()
    }
    
    val loginLinkButton = new Button("Already have an account? Log in") {
      style = "-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-font-size: 12px;"
      onAction = _ => showLoginMode()
    }
    
    // Email verification section
    val emailSection = new VBox {
      spacing = 5
      children = Seq(
        regEmailField,
        new HBox {
          spacing = 10
          alignment = Pos.CenterLeft
          children = Seq(
            sendOtpButton
          )
        },
        emailStatusLabel,
        otpStatusLabel
      )
    }
    
    new ScrollPane {
      content = new VBox {
        spacing = 12
        alignment = Pos.Center
        padding = Insets(20, 40, 40, 40)
        children = Seq(
          new HBox {
            alignment = Pos.CenterLeft
            children = Seq(backButton)
          },
          new Label("Create a new account") {
            style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;"
          },
          new VBox {
            spacing = 3
            children = Seq(usernameStatusLabel, regUsernameField)
          },
          regNameField,
          regContactField,
          emailSection,
          new VBox {
            spacing = 3
            children = Seq(regPasswordField, passwordStatusLabel)
          },
          new VBox {
            spacing = 3
            children = Seq(regConfirmPasswordField, confirmPasswordStatusLabel)
          },
          registerButton,
          loginLinkButton
        )
      }
      fitToWidth = true
      hbarPolicy = ScrollPane.ScrollBarPolicy.Never
      vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
      style = "-fx-background-color: white; -fx-background: white;"
    }
  }
  
  private def showWelcomeMode(): Unit = {
    currentMode = AuthMode.WelcomeMode
    val content = createWelcomeContent()
    animateContentChange(content)
  }
  
  private def showLoginMode(): Unit = {
    currentMode = AuthMode.LoginMode
    val content = createLoginContent()
    animateContentChange(content)
  }
  
  private def showRegisterMode(): Unit = {
    currentMode = AuthMode.RegisterMode
    val content = createRegisterContent()
    animateContentChange(content)
  }
  
  private def animateContentChange(newContent: Region): Unit = {
    if (contentContainer.children.nonEmpty) {
      val fadeOut = new FadeTransition(Duration(150), contentContainer.children.head)
      fadeOut.fromValue = 1.0
      fadeOut.toValue = 0.0
      fadeOut.onFinished = _ => {
        contentContainer.children = Seq(newContent)
        val fadeIn = new FadeTransition(Duration(150), newContent)
        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0
        fadeIn.play()
      }
      fadeOut.play()
    } else {
      contentContainer.children = Seq(newContent)
    }
  }
  
  private def setupEventHandlers(): Unit = {
    // Username field changes for registration
    regUsernameField.text.onChange { (_, _, _) =>
      val username = regUsernameField.getUserInput
      if (username.nonEmpty) {
        if (username.length < 3) {
          usernameStatusLabel.text = "✗ Username must be at least 3 characters"
          usernameStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #E53E3E; -fx-padding: 2 0 0 5;"
          regUsernameField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #E53E3E; -fx-pref-height: 40;"
        } else if (!isValidUsernameFormat(username)) {
          usernameStatusLabel.text = "✗ Username can only contain letters, numbers, and underscore"
          usernameStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #E53E3E; -fx-padding: 2 0 0 5;"
          regUsernameField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #E53E3E; -fx-pref-height: 40;"
        } else if (!communityService.isUsernameAvailable(username)) {
          usernameStatusLabel.text = "✗ Username is already taken"
          usernameStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #E53E3E; -fx-padding: 2 0 0 5;"
          regUsernameField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #E53E3E; -fx-pref-height: 40;"
        } else {
          usernameStatusLabel.text = "✓ Username is available"
          usernameStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #38A169; -fx-padding: 2 0 0 5;"
          regUsernameField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #38A169; -fx-pref-height: 40;"
        }
      } else {
        usernameStatusLabel.text = ""
        regUsernameField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd; -fx-pref-height: 40;"
      }
    }
    
    // Email field changes for registration
    regEmailField.text.onChange { (_, _, _) =>
      val email = regEmailField.getUserInput
      val isValidEmail = email.nonEmpty && isValidEmailFormat(email)
      sendOtpButton.disable = !isValidEmail
      
      if (email.nonEmpty) {
        if (isValidEmail) {
          if (!communityService.isEmailAvailable(email)) {
            emailStatusLabel.text = "✗ Email is already registered"
            emailStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #E53E3E; -fx-padding: 2 0 0 5;"
            regEmailField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #E53E3E; -fx-pref-height: 40;"
          } else {
            emailStatusLabel.text = "✓ Valid email format"
            emailStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #38A169; -fx-padding: 2 0 0 5;"
            regEmailField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #38A169; -fx-pref-height: 40;"
          }
        } else {
          emailStatusLabel.text = "✗ Invalid email format"
          emailStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #E53E3E; -fx-padding: 2 0 0 5;"
          regEmailField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #E53E3E; -fx-pref-height: 40;"
        }
      } else {
        emailStatusLabel.text = ""
        regEmailField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd; -fx-pref-height: 40;"
      }
      
      if (!isValidEmail) {
        resetOTPState()
      }
    }
    
    // Send OTP button
    sendOtpButton.onAction = (_: ActionEvent) => sendOTP()
    
    // Password field changes for registration
    regPasswordField.text.onChange { (_, _, _) =>
      val password = regPasswordField.text.value
      validatePassword(password)
      // Also revalidate confirm password when main password changes
      val confirmPassword = regConfirmPasswordField.text.value
      if (confirmPassword.nonEmpty) {
        validateConfirmPassword(password, confirmPassword)
      }
    }
    
    // Confirm password field changes for registration
    regConfirmPasswordField.text.onChange { (_, _, _) =>
      val password = regPasswordField.text.value
      val confirmPassword = regConfirmPasswordField.text.value
      validateConfirmPassword(password, confirmPassword)
    }
  }
  
  private def validatePassword(password: String): Unit = {
    if (password.isEmpty) {
      passwordStatusLabel.text = ""
      regPasswordField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd; -fx-prompt-text-fill: #999999; -fx-pref-height: 40;"
      return
    }
    
    val requirements = getPasswordRequirements(password)
    val missingCount = requirements.count(!_._2)
    
    if (missingCount == 0) {
      passwordStatusLabel.text = "✓ Password meets all requirements"
      passwordStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #38A169; -fx-padding: 2 0 0 5;"
      regPasswordField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #38A169; -fx-prompt-text-fill: #999999; -fx-pref-height: 40;"
    } else {
      val missing = requirements.filter(!_._2).map(_._1)
      passwordStatusLabel.text = s"✗ Missing: ${missing.mkString(", ")}"
      passwordStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #E53E3E; -fx-padding: 2 0 0 5;"
      regPasswordField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #E53E3E; -fx-prompt-text-fill: #999999; -fx-pref-height: 40;"
    }
  }
  
  private def validateConfirmPassword(password: String, confirmPassword: String): Unit = {
    if (confirmPassword.isEmpty) {
      confirmPasswordStatusLabel.text = ""
      regConfirmPasswordField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ddd; -fx-prompt-text-fill: #999999; -fx-pref-height: 40;"
      return
    }
    
    if (password == confirmPassword) {
      confirmPasswordStatusLabel.text = "✓ Passwords match"
      confirmPasswordStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #38A169; -fx-padding: 2 0 0 5;"
      regConfirmPasswordField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #38A169; -fx-prompt-text-fill: #999999; -fx-pref-height: 40;"
    } else {
      confirmPasswordStatusLabel.text = "✗ Passwords do not match"
      confirmPasswordStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #E53E3E; -fx-padding: 2 0 0 5;"
      regConfirmPasswordField.style = "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #E53E3E; -fx-prompt-text-fill: #999999; -fx-pref-height: 40;"
    }
  }
  
  private def getPasswordRequirements(password: String): List[(String, Boolean)] = {
    List(
      ("8+ chars", password.length >= 8),
      ("uppercase", password.exists(_.isUpper)),
      ("lowercase", password.exists(_.isLower)),
      ("digit", password.exists(_.isDigit)),
      ("special char", password.exists(c => !c.isLetterOrDigit))
    )
  }
  
  private def handleLogin(): Unit = {
    val username = loginUsernameField.text.value
    val password = loginPasswordField.text.value
    
    if (username.isEmpty || password.isEmpty) {
      GuiUtils.showWarning("Missing Information", "Please enter both username and password.")
      return
    }
    
    communityService.login(username, password) match {
      case Some(user) =>
        GuiUtils.showInfo("Login Successful", s"Welcome back, ${user.name}!")
        authResult = Some(AuthResult.LoginSuccess)
        dialog.close()
      case None =>
        GuiUtils.showError("Login Failed", "Invalid username or password.")
        loginPasswordField.text = ""
    }
  }
  
  private def sendOTP(): Unit = {
    val email = regEmailField.getUserInput
    
    if (!isValidEmailFormat(email)) {
      GuiUtils.showError("Invalid Email", "Please enter a valid email address.")
      return
    }
    
    // Check if email is already registered
    if (!communityService.isEmailAvailable(email)) {
      GuiUtils.showError("Email Already Registered", s"The email '$email' is already registered. Please use a different email or try logging in.")
      return
    }
    
    // Get parent stage for dialog
    val parentStage = dialog
    
    // Create and show the enhanced OTP dialog
    val otpDialog = new OTPVerificationDialog(parentStage, email)
    
    otpDialog.show(
      onSuccess = () => {
        // OTP verification successful
        isEmailVerified = true
        generatedOtp = Some("verified") // Mark as verified
        sendOtpButton.disable = true
        sendOtpButton.text = "✅ Verified"
        sendOtpButton.style = "-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12px;"
        otpStatusLabel.text = "✓ Email verified successfully!"
        otpStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #38A169; -fx-padding: 2 0 0 5;"
      },
      onFailure = () => {
        // OTP verification failed or cancelled
        resetOTPState()
        otpStatusLabel.text = "Verification cancelled"
        otpStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #E53E3E; -fx-padding: 2 0 0 5;"
      }
    )
    
    // Update UI to show OTP is being sent
    sendOtpButton.disable = true
    sendOtpButton.text = "Sending..."
    otpStatusLabel.text = "Sending verification email..."
    otpStatusLabel.style = "-fx-font-size: 11px; -fx-text-fill: #3182CE; -fx-padding: 2 0 0 5;"
  }
  
  private def handleRegistration(): Unit = {
    val username = regUsernameField.getUserInput
    val email = regEmailField.getUserInput
    val name = regNameField.getUserInput
    val contact = regContactField.getUserInput
    val password = regPasswordField.text.value
    val confirmPassword = regConfirmPasswordField.text.value
    
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
      GuiUtils.showWarning("Email Not Verified", "Please verify your email address first by clicking 'Send Verification Email' and entering the verification code.")
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
    if (!communityService.isUsernameAvailable(username)) {
      GuiUtils.showError("Username Taken", s"The username '$username' is already taken. Please choose a different one.")
      return
    }
    
    // Register user
    if (communityService.registerUser(username, email, name, contact, password, isAdmin = false)) {
      GuiUtils.showInfo("Registration Successful", "Account created successfully! You can now log in.")
      authResult = Some(AuthResult.RegisterSuccess)
      dialog.close()
    } else {
      GuiUtils.showError("Registration Failed", "An error occurred during registration. Please try again.")
    }
  }
  
  private def resetOTPState(): Unit = {
    generatedOtp = None
    isEmailVerified = false
    sendOtpButton.disable = true
    sendOtpButton.text = "📧 Send Verification Email"
    sendOtpButton.style = "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 12px;"
    emailStatusLabel.text = ""
    otpStatusLabel.text = ""
  }
  
  private def isValidEmailFormat(email: String): Boolean = {
    val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
    emailRegex.matches(email)
  }
  
  private def isValidUsernameFormat(username: String): Boolean = {
    val usernameRegex = """^[a-zA-Z0-9_]+$""".r
    usernameRegex.matches(username)
  }
}
