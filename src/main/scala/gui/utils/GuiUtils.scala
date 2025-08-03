package gui.utils

import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.stage.Stage
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty

/**
 * Utility class for common GUI operations
 */
object GuiUtils {
  
  private val mainStageProperty = ObjectProperty[Option[Stage]](None)
  
  def mainStage_=(stage: Stage): Unit = {
    mainStageProperty.value = Some(stage)
  }
  
  def mainStage: Option[Stage] = mainStageProperty.value
  
  /**
   * Show an information alert
   */
  def showInfo(title: String, message: String): Unit = {
    showAlert(AlertType.Information, title, message)
  }
  
  /**
   * Show an error alert
   */
  def showError(title: String, message: String): Unit = {
    showAlert(AlertType.Error, title, message)
  }
  
  /**
   * Show a warning alert
   */
  def showWarning(title: String, message: String): Unit = {
    showAlert(AlertType.Warning, title, message)
  }
  
  /**
   * Show a confirmation dialog and return user's choice
   */
  def showConfirmation(alertTitle: String, message: String): Boolean = {
    val alert = new Alert(AlertType.Confirmation) {
      title = alertTitle
      contentText = message
    }
    
    val result = alert.showAndWait()
    result.contains(scalafx.scene.control.ButtonType.OK)
  }
  
  /**
   * Show a login prompt dialog for anonymous users
   */
  def showLoginPrompt(onLoginClick: () => Unit, onRegisterClick: () => Unit): Unit = {
    import scalafx.scene.control.ButtonType
    import scalafx.scene.control.ButtonBar.ButtonData
    
    val loginButton = new ButtonType("Login", ButtonData.OKDone)
    val registerButton = new ButtonType("Register", ButtonData.Other)
    val cancelButton = new ButtonType("Cancel", ButtonData.CancelClose)
    
    val alert = new Alert(AlertType.Confirmation) {
      title = "Login Required"
      headerText = "🔐 Account Required"
      contentText = "This feature requires an account. Would you like to login or create a new account?"
      buttonTypes = Seq(loginButton, registerButton, cancelButton)
    }
    
    val result = alert.showAndWait()
    result match {
      case Some(btn) if btn == loginButton => onLoginClick()
      case Some(btn) if btn == registerButton => onRegisterClick()
      case _ => // Cancel - do nothing
    }
  }
  
  /**
   * Generic alert method
   */
  private def showAlert(alertType: AlertType, alertTitle: String, message: String): Unit = {
    val alert = new Alert(alertType) {
      title = alertTitle
      contentText = message
    }
    alert.showAndWait()
  }
}
