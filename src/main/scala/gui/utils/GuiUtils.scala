package gui.utils

import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.stage.Stage

/**
 * Utility class for common GUI operations
 */
object GuiUtils {
  
  private var mainStage: Option[Stage] = None
  
  def setMainStage(stage: Stage): Unit = {
    mainStage = Some(stage)
  }
  
  def getMainStage: Option[Stage] = mainStage
  
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
