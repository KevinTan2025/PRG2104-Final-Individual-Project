package gui.dialogs.auth

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
    promptText = "Enter 6-digit code"
    maxWidth = 180
    style = "-fx-font-size: 20px; -fx-text-alignment: center; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-background-color: #fff3cd; -fx-border-color: #ffc107; -fx-border-width: 2;"
  }
  
  private val dialog = new Stage {
    title = "📧 Email Verification - Community Platform"
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
   * Simulate email notification with realistic email interface
   */
  private def simulateEmailNotification(): Unit = {
    Platform.runLater {
      val emailDialog = new Stage {
        title = "📧 Email Notification"
        initModality(Modality.ApplicationModal)
        initOwner(dialog)
        resizable = false
      }
      
      val copyOTPButton = new Button("📋 Click to Copy OTP") {
        style = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;"
        onAction = (_: ActionEvent) => {
          // Simulate copying to clipboard
          val clipboard = scalafx.scene.input.Clipboard.systemClipboard
          val content = new scalafx.scene.input.ClipboardContent()
          content.putString(otpCode)
          clipboard.setContent(content)
          text = "✅ Copied!"
          style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"
        }
      }
      
      val emailContent = new VBox {
        spacing = 15
        padding = Insets(20)
        style = "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;"
        children = Seq(
          // Email Header
          new HBox {
            spacing = 10
            alignment = Pos.CenterLeft
            children = Seq(
              new Label("📧") { style = "-fx-font-size: 24px;" },
              new VBox {
                spacing = 2
                children = Seq(
                  new Label("Community Engagement Platform") {
                    style = "-fx-font-size: 16px; -fx-font-weight: bold;"
                  },
                  new Label("noreply@community-platform.com") {
                    style = "-fx-font-size: 12px; -fx-text-fill: #6c757d;"
                  }
                )
              }
            )
          },
          
          new Separator(),
          
          // Email Details
          new VBox {
            spacing = 8
            children = Seq(
              new HBox {
                spacing = 10
                children = Seq(
                  new Label("To:") { style = "-fx-font-weight: bold; -fx-min-width: 60;" },
                  new Label(userEmail) { style = "-fx-text-fill: #2196F3;" }
                )
              },
              new HBox {
                spacing = 10
                children = Seq(
                  new Label("From:") { style = "-fx-font-weight: bold; -fx-min-width: 60;" },
                  new Label("Community Platform Security") { style = "-fx-text-fill: #666;" }
                )
              },
              new HBox {
                spacing = 10
                children = Seq(
                  new Label("Subject:") { style = "-fx-font-weight: bold; -fx-min-width: 60;" },
                  new Label("🔐 Your Email Verification Code") { style = "-fx-text-fill: #666;" }
                )
              }
            )
          },
          
          new Separator(),
          
          // Email Body
          new VBox {
            spacing = 15
            padding = Insets(10)
            style = "-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1;"
            children = Seq(
              new Label("Email Verification Required") {
                style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
              },
              new Label("Hello,\n\nWelcome to Community Engagement Platform! To complete your registration, please use the verification code below:") {
                style = "-fx-font-size: 14px; -fx-text-fill: #333; -fx-wrap-text: true;"
                maxWidth = 400
              },
              new VBox {
                spacing = 10
                alignment = Pos.Center
                padding = Insets(20, 10, 20, 10)
                style = "-fx-background-color: #f8f9fa; -fx-border-color: #6c757d; -fx-border-width: 2; -fx-border-style: dashed;"
                children = Seq(
                  new Label("Your Verification Code:") {
                    style = "-fx-font-size: 12px; -fx-text-fill: #6c757d;"
                  },
                  new Label(otpCode) {
                    style = "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-font-family: 'Courier New';"
                  },
                  copyOTPButton
                )
              },
              new Label("This code will expire in 10 minutes. If you didn't request this verification, please ignore this email.") {
                style = "-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-wrap-text: true;"
                maxWidth = 400
              },
              new Label("Best regards,\nCommunity Platform Team") {
                style = "-fx-font-size: 14px; -fx-text-fill: #333; -fx-wrap-text: true;"
                maxWidth = 400
              }
            )
          },
          
          // Footer
          new VBox {
            spacing = 5
            alignment = Pos.Center
            children = Seq(
              new Separator(),
              new Label("🔒 This is a simulated email for demonstration purposes") {
                style = "-fx-font-size: 11px; -fx-text-fill: #6c757d; -fx-font-style: italic;"
              }
            )
          }
        )
      }
      
      val closeButton = new Button("Close Email") {
        prefWidth = 120
        style = "-fx-background-color: #6c757d; -fx-text-fill: white;"
        onAction = (_: ActionEvent) => emailDialog.close()
      }
      
      val mainContainer = new VBox {
        spacing = 15
        padding = Insets(10)
        children = Seq(emailContent, closeButton)
      }
      
      emailDialog.scene = new Scene(new ScrollPane {
        content = mainContainer
        fitToWidth = true
        style = "-fx-background-color: white;"
      }, 500, 600)
      
      emailDialog.centerOnScreen()
      emailDialog.showAndWait()
    }
  }
  
  /**
   * Show the OTP verification dialog
   */
  def show(onSuccess: () => Unit, onFailure: () => Unit): Unit = {
    onVerificationSuccess = onSuccess
    onVerificationFailure = onFailure
    
    // Simulate sending OTP
    simulateEmailNotification()
    
    val verifyButton = new Button("🔐 Verify Code") {
      prefWidth = 120
      style = "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => handleVerification()
    }
    
    val resendButton = new Button("📧 Resend Email") {
      prefWidth = 120
      style = "-fx-background-color: #17a2b8; -fx-text-fill: white;"
      onAction = (_: ActionEvent) => {
        simulateEmailNotification()
        showInfo("📧 Email Resent", "A new verification email has been sent to your inbox!")
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
      style = "-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);"
      children = Seq(
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(
            new Label("📧") { style = "-fx-font-size: 32px;" },
            new Label("Email Verification") {
              style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
            }
          )
        },
        new VBox {
          spacing = 5
          alignment = Pos.Center
          children = Seq(
            new Label("Check your email for verification code") {
              style = "-fx-font-size: 12px; -fx-text-fill: #6c757d;"
            },
            new Label(userEmail) {
              style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2196F3;"
            }
          )
        },
        new VBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(
            new Label("Enter 6-digit verification code:") {
              style = "-fx-font-size: 12px; -fx-text-fill: #495057;"
            },
            otpField
          )
        },
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(verifyButton, resendButton)
        },
        new Separator(),
        cancelButton
      )
    }
    
    dialog.scene = new Scene(contentBox, 400, 350)
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
      showError("❌ Missing Code", "Please enter the verification code from your email.")
      return
    }
    
    if (enteredOTP.length != 6 || !enteredOTP.forall(_.isDigit)) {
      showError("❌ Invalid Format", "Verification code must be exactly 6 digits.\nPlease check your email for the correct format.")
      return
    }
    
    if (enteredOTP == otpCode) {
      isVerified = true
      dialog.close()
      showInfo("✅ Verification Successful", "🎉 Your email has been verified successfully!\nWelcome to Community Platform!")
      onVerificationSuccess()
    } else {
      showError("❌ Invalid Code", "The verification code you entered is incorrect.\nPlease check your email and try again.")
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
