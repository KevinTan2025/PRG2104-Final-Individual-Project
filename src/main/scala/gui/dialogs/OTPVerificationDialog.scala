package gui.dialogs

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.{Stage, Modality}
import scalafx.application.Platform
import scala.util.Random
import java.util.concurrent.{Executors, TimeUnit}

/**
 * OTP Verification Dialog for user registration
 */
class OTPVerificationDialog(parentStage: Stage, userEmail: String) {
  
  private val otpCode = generateOTP()
  private val otpField = new TextField {
    promptText = "Enter 6-digit OTP"
    maxWidth = 150
    style = "-fx-font-size: 18px; -fx-text-alignment: center;"
  }
  
  private val dialog = new Stage {
    title = "OTP Verification"
    initModality(Modality.ApplicationModal)
    initOwner(parentStage)
    resizable = false
  }
  
  private var isVerified = false
  private var onVerificationSuccess: () => Unit = () => {}
  private var onVerificationFailure: () => Unit = () => {}
  
  /**
   * Generate a random 6-digit OTP
   */
  private def generateOTP(): String = {
    val random = new Random()
    (100000 + random.nextInt(900000)).toString
  }
  
  /**
   * Simulate sending OTP notification
   */
  private def simulateOTPNotification(): Unit = {
    // Simulate notification popup
    Platform.runLater {
      val notificationDialog = new Alert(Alert.AlertType.Information) {
        initOwner(dialog)
        title = "OTP Sent"
        headerText = "Verification Code Sent"
        contentText = s"A 6-digit OTP has been sent to $userEmail\n\n[SIMULATED] Your OTP: $otpCode"
      }
      notificationDialog.showAndWait()
    }
  }
  
  /**
   * Show the OTP verification dialog
   */
  def show(onSuccess: () => Unit, onFailure: () => Unit): Unit = {
    onVerificationSuccess = onSuccess
    onVerificationFailure = onFailure
    
    // Simulate sending OTP
    simulateOTPNotification()
    
    val verifyButton = new Button("Verify") {
      prefWidth = 120
      onAction = (_: ActionEvent) => handleVerification()
    }
    
    val resendButton = new Button("Resend OTP") {
      prefWidth = 120
      onAction = (_: ActionEvent) => {
        simulateOTPNotification()
        showInfo("OTP Resent", "A new OTP has been sent to your email.")
      }
    }
    
    val cancelButton = new Button("Cancel") {
      prefWidth = 120
      onAction = (_: ActionEvent) => {
        dialog.close()
        onVerificationFailure()
      }
    }
    
    val contentBox = new VBox {
      spacing = 20
      alignment = Pos.Center
      padding = Insets(30)
      children = Seq(
        new Label("Email Verification") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        },
        new Label(s"Please enter the 6-digit code sent to:") {
          style = "-fx-font-size: 12px;"
        },
        new Label(userEmail) {
          style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2196F3;"
        },
        otpField,
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(verifyButton, resendButton)
        },
        cancelButton
      )
    }
    
    dialog.scene = new Scene(contentBox, 350, 280)
    dialog.centerOnScreen()
    dialog.show()
    
    // Auto-focus on OTP field
    Platform.runLater {
      otpField.requestFocus()
    }
  }
  
  private def handleVerification(): Unit = {
    val enteredOTP = otpField.text.value.trim
    
    if (enteredOTP.isEmpty) {
      showError("Invalid OTP", "Please enter the OTP code.")
      return
    }
    
    if (enteredOTP.length != 6 || !enteredOTP.forall(_.isDigit)) {
      showError("Invalid OTP", "OTP must be exactly 6 digits.")
      return
    }
    
    if (enteredOTP == otpCode) {
      isVerified = true
      dialog.close()
      showInfo("Verification Successful", "Your email has been verified successfully!")
      onVerificationSuccess()
    } else {
      showError("Invalid OTP", "The OTP code you entered is incorrect. Please try again.")
      otpField.clear()
      otpField.requestFocus()
    }
  }
  
  private def showError(alertTitle: String, message: String): Unit = {
    val alert = new Alert(Alert.AlertType.Error) {
      initOwner(dialog)
      title = alertTitle
      headerText = ""
      contentText = message
    }
    alert.showAndWait()
  }
  
  private def showInfo(alertTitle: String, message: String): Unit = {
    val alert = new Alert(Alert.AlertType.Information) {
      initOwner(dialog)
      title = alertTitle
      headerText = ""
      contentText = message
    }
    alert.showAndWait()
  }
  
  def isOTPVerified: Boolean = isVerified
}
