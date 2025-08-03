package gui.dialogs.auth

import javafx.scene.Scene
import scalafx.stage.{Stage, Modality}
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import javafx.scene.control.{Label, TextField, Button}
import scalafx.scene.layout.{VBox, HBox}
import scalafx.geometry.{Pos, Insets}
import scalafx.Includes._
import gui.utils.GuiUtils
import java.util.concurrent.atomic.AtomicReference
import scala.util.Random

/**
 * OTP Verification Dialog for user registration
 * Wrapper class using FXML interface
 */
class OTPVerificationDialog(parentStage: Stage, userEmail: String) {
  
  // Dialog window
  private val dialog = new Stage {
    title = "📧 Email Verification - Community Platform"
    initModality(Modality.ApplicationModal)
    initOwner(parentStage)
    resizable = false
  }
  
  // Controller reference using AtomicReference for thread-safe mutable state
  private val controller: AtomicReference[Option[OTPVerificationDialogController]] = new AtomicReference(None)
  
  // Verification status
  private val isVerifiedProperty = BooleanProperty(false)
  
  /**
   * Show OTP verification dialog
   * @param onSuccess Verification success callback
   * @param onFailure Verification failure callback
   */
  def show(onSuccess: () => Unit, onFailure: () => Unit): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/OTPVerificationDialog.fxml"))
      val root: Parent = loader.load()
      
      // Get controller and set parameters
      controller.set(Some(loader.getController[OTPVerificationDialogController]()))
      controller.get().foreach { ctrl =>
        ctrl.setEmailAndParent(userEmail, parentStage)
        ctrl.setVerificationCallbacks(onSuccess, onFailure)
        ctrl.showOTPDialog(userEmail, parentStage, onSuccess, onFailure)
      }
      
      dialog.scene = new Scene(root, 400, 350)
      dialog.centerOnScreen()
      dialog.showAndWait()
      
    } catch {
      case e: Exception =>
        GuiUtils.showError("Loading Error", s"Unable to load OTP verification interface: ${e.getMessage}")
        onFailure()
    }
  }
  /**
   * Get verification status
   * @return Whether verified
   */
  def isOTPVerified: Boolean = {
    controller.get().map(_.isOTPVerified).getOrElse(false)
  }
  

}
