package gui.dialogs.auth

import javafx.scene.Scene
import scalafx.stage.{Stage, Modality, StageStyle}
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import scalafx.beans.property.ObjectProperty
import scalafx.Includes._
import javafx.stage.WindowEvent
import gui.utils.GuiUtils

/**
 * Authentication mode enumeration
 */
sealed trait AuthMode
object AuthMode {
  case object LoginMode extends AuthMode
  case object RegisterMode extends AuthMode
  case object WelcomeMode extends AuthMode
}

/**
 * Authentication result enumeration
 */
sealed trait AuthResult
object AuthResult {
  case object LoginSuccess extends AuthResult
  case object RegisterSuccess extends AuthResult
  case object ContinueAsGuest extends AuthResult
  case object Cancelled extends AuthResult
}

/**
 * Main authentication dialog controller
 * Manages Welcome, Login and Register three independent FXML dialogs
 */
class AuthDialogController(parentStage: Stage) {
  
  private val authResultProperty = ObjectProperty[Option[AuthResult]](None)
  
  // Main dialog window
  private val dialog = new Stage {
    title = "Community Platform - Authentication"
    initModality(Modality.ApplicationModal)
    initOwner(parentStage)
    initStyle(StageStyle.Decorated)
    resizable = false
    
    onCloseRequest = (event: WindowEvent) => {
      authResultProperty.value = Some(AuthResult.ContinueAsGuest)
    }
  }
  
  // Three sub-dialog controllers
  private var welcomeController: Option[WelcomeAuthDialogController] = None
  private var loginController: Option[LoginAuthDialogController] = None
  private var registerController: Option[RegisterAuthDialogController] = None
  
  /**
   * Show authentication dialog
   * @return Authentication result
   */
  def show(): AuthResult = {
    show(AuthMode.WelcomeMode)
  }
  
  /**
   * Show authentication dialog with specified initial mode
   * @param initialMode Initial display mode
   * @return Authentication result
   */
  def show(initialMode: AuthMode): AuthResult = {
    initialMode match {
      case AuthMode.WelcomeMode => showWelcomeMode()
      case AuthMode.LoginMode => showLoginMode()
      case AuthMode.RegisterMode => showRegisterMode()
    }
    
    dialog.centerOnScreen()
    dialog.showAndWait()
    authResultProperty.value.getOrElse(AuthResult.ContinueAsGuest)
  }
  
  /**
   * Show welcome screen
   */
  def showWelcomeMode(): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/WelcomeAuthDialog.fxml"))
      val root: Parent = loader.load()
      
      // Get controller and set callback
      welcomeController = Some(loader.getController[WelcomeAuthDialogController]())
      welcomeController.foreach { controller =>
        controller.setParentController(this)
      }
      
      dialog.scene = new Scene(root, 400, 550)
      dialog.title = "Community Platform - Welcome"
      
    } catch {
      case e: Exception =>
        GuiUtils.showError("Loading Error", s"Unable to load welcome screen: ${e.getMessage}")
        authResultProperty.value = Some(AuthResult.Cancelled)
    }
  }
  
  /**
   * Show login screen
   */
  def showLoginMode(): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/LoginAuthDialog.fxml"))
      val root: Parent = loader.load()
      
      // Get controller and set callback
      loginController = Some(loader.getController[LoginAuthDialogController]())
      loginController.foreach { controller =>
        controller.setParentController(this)
      }
      
      dialog.scene = new Scene(root, 400, 450)
      dialog.title = "Community Platform - Login"
      
    } catch {
      case e: Exception =>
        GuiUtils.showError("Loading Error", s"Unable to load login screen: ${e.getMessage}")
        authResultProperty.value = Some(AuthResult.Cancelled)
    }
  }
  
  /**
   * Show registration screen
   */
  def showRegisterMode(): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/RegisterAuthDialog.fxml"))
      val root: Parent = loader.load()
      
      // Get controller and set callback
      registerController = Some(loader.getController[RegisterAuthDialogController]())
      registerController.foreach { controller =>
        controller.setParentController(this)
      }
      
      dialog.scene = new Scene(root, 400, 650)
      dialog.title = "Community Platform - Register"
      
    } catch {
      case e: Exception =>
        GuiUtils.showError("Loading Error", s"Unable to load registration screen: ${e.getMessage}")
        authResultProperty.value = Some(AuthResult.Cancelled)
    }
  }
  
  /**
   * Set authentication result and close dialog
   * @param result Authentication result
   */
  def setAuthResult(result: AuthResult): Unit = {
    authResultProperty.value = Some(result)
    dialog.close()
  }
  
  /**
   * Get dialog window reference
   * @return Stage object
   */
  def getDialogStage: Stage = dialog
}