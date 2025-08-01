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
 * 认证模式枚举
 */
sealed trait AuthMode
object AuthMode {
  case object LoginMode extends AuthMode
  case object RegisterMode extends AuthMode
  case object WelcomeMode extends AuthMode
}

/**
 * 认证结果枚举
 */
sealed trait AuthResult
object AuthResult {
  case object LoginSuccess extends AuthResult
  case object RegisterSuccess extends AuthResult
  case object ContinueAsGuest extends AuthResult
  case object Cancelled extends AuthResult
}

/**
 * 主认证对话框控制器
 * 管理Welcome、Login和Register三个独立的FXML对话框
 */
class AuthDialogController(parentStage: Stage) {
  
  private val authResultProperty = ObjectProperty[Option[AuthResult]](None)
  
  // 主对话框窗口
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
  
  // 三个子对话框控制器
  private var welcomeController: Option[WelcomeAuthDialogController] = None
  private var loginController: Option[LoginAuthDialogController] = None
  private var registerController: Option[RegisterAuthDialogController] = None
  
  /**
   * 显示认证对话框
   * @return 认证结果
   */
  def show(): AuthResult = {
    show(AuthMode.WelcomeMode)
  }
  
  /**
   * 显示认证对话框，指定初始模式
   * @param initialMode 初始显示模式
   * @return 认证结果
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
   * 显示欢迎界面
   */
  def showWelcomeMode(): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/WelcomeAuthDialog.fxml"))
      val root: Parent = loader.load()
      
      // 获取控制器并设置回调
      welcomeController = Some(loader.getController[WelcomeAuthDialogController]())
      welcomeController.foreach { controller =>
        controller.setParentController(this)
      }
      
      dialog.scene = new Scene(root, 400, 550)
      dialog.title = "Community Platform - Welcome"
      
    } catch {
      case e: Exception =>
        GuiUtils.showError("加载错误", s"无法加载欢迎界面: ${e.getMessage}")
        authResultProperty.value = Some(AuthResult.Cancelled)
    }
  }
  
  /**
   * 显示登录界面
   */
  def showLoginMode(): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/LoginAuthDialog.fxml"))
      val root: Parent = loader.load()
      
      // 获取控制器并设置回调
      loginController = Some(loader.getController[LoginAuthDialogController]())
      loginController.foreach { controller =>
        controller.setParentController(this)
      }
      
      dialog.scene = new Scene(root, 400, 450)
      dialog.title = "Community Platform - Login"
      
    } catch {
      case e: Exception =>
        GuiUtils.showError("加载错误", s"无法加载登录界面: ${e.getMessage}")
        authResultProperty.value = Some(AuthResult.Cancelled)
    }
  }
  
  /**
   * 显示注册界面
   */
  def showRegisterMode(): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/RegisterAuthDialog.fxml"))
      val root: Parent = loader.load()
      
      // 获取控制器并设置回调
      registerController = Some(loader.getController[RegisterAuthDialogController]())
      registerController.foreach { controller =>
        controller.setParentController(this)
      }
      
      dialog.scene = new Scene(root, 400, 650)
      dialog.title = "Community Platform - Register"
      
    } catch {
      case e: Exception =>
        GuiUtils.showError("加载错误", s"无法加载注册界面: ${e.getMessage}")
        authResultProperty.value = Some(AuthResult.Cancelled)
    }
  }
  
  /**
   * 设置认证结果并关闭对话框
   * @param result 认证结果
   */
  def setAuthResult(result: AuthResult): Unit = {
    authResultProperty.value = Some(result)
    dialog.close()
  }
  
  /**
   * 获取对话框窗口引用
   * @return Stage对象
   */
  def getDialogStage: Stage = dialog
}