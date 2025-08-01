package gui.dialogs.auth

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label, TextField}
import javafx.event.ActionEvent
import scalafx.application.Platform
import scalafx.stage.{Stage, Modality}
import javafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.Includes._
import gui.utils.GuiUtils
import java.net.URL
import java.util.ResourceBundle
import java.util.concurrent.atomic.AtomicReference
import scala.util.Random

/**
 * OTP verification dialog controller
 * Connects to OTPVerificationDialog.fxml
 */
class OTPVerificationDialogController extends Initializable {
  
  // FXML component binding
  @FXML private var lblEmailIcon: Label = _
  @FXML private var lblTitle: Label = _
  @FXML private var lblEmailPrompt: Label = _
  @FXML private var lblUserEmail: Label = _
  @FXML private var lblOtpPrompt: Label = _
  @FXML private var txtOtpField: TextField = _
  @FXML private var lblOtpInputStatus: Label = _
  @FXML private var btnVerify: Button = _
  @FXML private var btnResend: Button = _
  @FXML private var lblVerificationStatus: Label = _
  @FXML private var btnCancel: Button = _
  
  // Verification status
  private val isVerifiedProperty = BooleanProperty(false)
  private val onVerificationSuccessProperty = ObjectProperty[() => Unit](() => {})
  private val onVerificationFailureProperty = ObjectProperty[() => Unit](() => {})
  
  // OTP related using AtomicReference for thread-safe mutable state
  private val otpCode: AtomicReference[Option[String]] = new AtomicReference(None)
  private val userEmail: AtomicReference[Option[String]] = new AtomicReference(None)
  private val parentStage: AtomicReference[Option[Stage]] = new AtomicReference(None)
  
  /**
   * Initialization method
   */
  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    setupComponents()
    setupEventHandlers()
  }
  
  /**
   * Set user email and parent window
   * @param email User email
   * @param parent Parent window
   */
  def setEmailAndParent(email: String, parent: Stage): Unit = {
    userEmail.set(Some(email))
    parentStage.set(Some(parent))
    lblUserEmail.text = email
    generateNewOTP()
  }
  
  /**
   * Set verification callback functions
   * @param onSuccess Success callback
   * @param onFailure Failure callback
   */
  def setVerificationCallbacks(onSuccess: () => Unit, onFailure: () => Unit): Unit = {
    onVerificationSuccessProperty.value = onSuccess
    onVerificationFailureProperty.value = onFailure
  }
  
  /**
   * Set initial state of components
   */
  private def setupComponents(): Unit = {
    // Clear status labels
    clearStatusLabels()
    
    // Set focus to OTP input field
    Platform.runLater {
      txtOtpField.requestFocus()
    }
  }
  
  /**
   * Set event handlers
   */
  private def setupEventHandlers(): Unit = {
    // OTP field change listener
    txtOtpField.text.onChange { (_, _, newValue) =>
      validateOTPInput(newValue)
    }
    
    // Enter key verification
    txtOtpField.onAction = (_: ActionEvent) => handleVerification(null)
    
    // Limit input to numbers only, maximum 6 digits
    txtOtpField.textFormatter = new scalafx.scene.control.TextFormatter[String](
      (change: scalafx.scene.control.TextFormatter.Change) => {
        val newText = change.getControlNewText
        if (newText.matches("\\d{0,6}")) change else null
      }
    )
  }
  
  /**
   * Validate OTP input
   * @param otp OTP input value
   */
  private def validateOTPInput(otp: String): Unit = {
    if (otp.isEmpty) {
      lblOtpInputStatus.text = ""
      updateFieldStyle(txtOtpField, "normal")
    } else if (otp.length < 6) {
      lblOtpInputStatus.text = "Please enter 6-digit verification code"
      updateStatusLabelStyle(lblOtpInputStatus, "warning")
      updateFieldStyle(txtOtpField, "warning")
    } else if (otp.length == 6 && otp.forall(_.isDigit)) {
      lblOtpInputStatus.text = "✓ Format correct"
      updateStatusLabelStyle(lblOtpInputStatus, "success")
      updateFieldStyle(txtOtpField, "success")
    } else {
      lblOtpInputStatus.text = "✗ Please enter 6 digits"
      updateStatusLabelStyle(lblOtpInputStatus, "error")
      updateFieldStyle(txtOtpField, "error")
    }
  }
  
  /**
   * Generate new OTP
   */
  private def generateNewOTP(): Unit = {
    val random = new Random()
    otpCode.set(Some((100000 + random.nextInt(900000)).toString))
  }
  
  /**
   * Simulate email notification
   */
  private def simulateEmailNotification(): Unit = {
    Platform.runLater {
      val emailDialog = new Stage {
        title = "📧 Email Notification"
        initModality(Modality.ApplicationModal)
        parentStage.get().foreach(initOwner(_))
        resizable = false
      }
      
      try {
        val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/EmailNotificationDialog.fxml"))
        val root: Parent = loader.load()
        
        // If there is a dedicated email notification controller, it can be set here
        // val controller = loader.getController[EmailNotificationDialogController]()
        // controller.setOTPCode(otpCode)
        // controller.setUserEmail(userEmail)
        
        emailDialog.scene = new Scene(root, 500, 600)
        emailDialog.centerOnScreen()
        emailDialog.showAndWait()
        
      } catch {
        case _: Exception =>
          // If there is no dedicated email notification FXML, use simple info dialog
          GuiUtils.showInfo(
            "📧 Email Sent",
            s"Verification code sent to: $userEmail\n\n" +
            s"Your verification code is: $otpCode\n\n" +
            "(This is demo mode, in actual application verification code would be sent via email)"
          )
      }
    }
  }
  
  /**
   * Update field style
   * @param field Field control
   * @param status Status type
   */
  private def updateFieldStyle(field: TextField, status: String): Unit = {
    field.getStyleClass.removeAll("error-field", "success-field", "warning-field")
    status match {
      case "error" => field.getStyleClass.add("error-field")
      case "success" => field.getStyleClass.add("success-field")
      case "warning" => field.getStyleClass.add("warning-field")
      case _ => // normal status does not add special style
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
      case _ => // normal status does not add special style
    }
  }
  
  /**
   * Clear status labels
   */
  private def clearStatusLabels(): Unit = {
    lblOtpInputStatus.text = ""
    lblVerificationStatus.text = ""
  }
  
  /**
   * Handle verification button click event
   */
  @FXML
  def handleVerification(event: ActionEvent): Unit = {
    val enteredOTP = txtOtpField.text.value.trim
    
    if (enteredOTP.isEmpty) {
      lblVerificationStatus.text = "✗ Please enter verification code"
      updateStatusLabelStyle(lblVerificationStatus, "error")
      return
    }
    
    if (enteredOTP.length != 6 || !enteredOTP.forall(_.isDigit)) {
      lblVerificationStatus.text = "✗ Verification code must be 6 digits"
      updateStatusLabelStyle(lblVerificationStatus, "error")
      return
    }
    
    if (otpCode.get().contains(enteredOTP)) {
      // Verification successful
      isVerifiedProperty.value = true
      lblVerificationStatus.text = "✓ Verification successful!"
      updateStatusLabelStyle(lblVerificationStatus, "success")
      
      // Delay closing dialog and call success callback
      Platform.runLater {
        Thread.sleep(500)
        onVerificationSuccessProperty.value()
        closeDialog()
      }
    } else {
      // Verification failed
      lblVerificationStatus.text = "✗ Verification code incorrect, please re-enter"
      updateStatusLabelStyle(lblVerificationStatus, "error")
      updateFieldStyle(txtOtpField, "error")
      
      // Clear input field and refocus
      txtOtpField.clear()
      txtOtpField.requestFocus()
    }
  }
  
  /**
   * Handle resend email button click event
   */
  @FXML
  def handleResendEmail(event: ActionEvent): Unit = {
    // Generate new OTP
    generateNewOTP()
    
    // Clear input and status
    txtOtpField.clear()
    clearStatusLabels()
    
    // Simulate sending email
    simulateEmailNotification()
    
    // Show resend success message
    lblVerificationStatus.text = "📧 Verification code resent"
    updateStatusLabelStyle(lblVerificationStatus, "success")
    
    // Focus on input field
    txtOtpField.requestFocus()
  }
  
  /**
   * Handle cancel button click event
   */
  @FXML
  def handleCancel(event: ActionEvent): Unit = {
    onVerificationFailureProperty.value()
    closeDialog()
  }
  
  /**
   * Close dialog
   */
  private def closeDialog(): Unit = {
    val stage = btnCancel.scene.value.window.value.asInstanceOf[javafx.stage.Stage]
    Platform.runLater {
      stage.close()
    }
  }
  
  /**
   * Show OTP verification dialog
   * @param email User email
   * @param parent Parent window
   * @param onSuccess Success callback
   * @param onFailure Failure callback
   */
  def showOTPDialog(email: String, parent: Stage, onSuccess: () => Unit, onFailure: () => Unit): Unit = {
    setEmailAndParent(email, parent)
    setVerificationCallbacks(onSuccess, onFailure)
    
    // Automatically send first OTP
    simulateEmailNotification()
  }
  
  /**
   * Get verification status
   * @return Whether verified
   */
  def isOTPVerified: Boolean = isVerifiedProperty.value
}