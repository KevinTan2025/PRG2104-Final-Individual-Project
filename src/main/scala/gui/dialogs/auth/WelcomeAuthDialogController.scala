package gui.dialogs.auth

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label}
import javafx.event.ActionEvent
import java.net.URL
import java.util.ResourceBundle

/**
 * 欢迎界面控制器
 * 对接WelcomeAuthDialog.fxml
 */
class WelcomeAuthDialogController extends Initializable {
  
  // FXML组件绑定
  @FXML private var lblTitle: Label = _
  @FXML private var lblWelcomeIcon: Label = _
  @FXML private var lblWelcomeText: Label = _
  @FXML private var btnWelcomeLogin: Button = _
  @FXML private var btnWelcomeRegister: Button = _
  @FXML private var btnWelcomeGuest: Button = _
  
  // 父控制器引用
  private var parentController: Option[AuthDialogController] = None
  
  /**
   * 初始化方法
   */
  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    // 设置组件的初始状态
    setupComponents()
  }
  
  /**
   * 设置父控制器
   * @param controller 父控制器实例
   */
  def setParentController(controller: AuthDialogController): Unit = {
    parentController = Some(controller)
  }
  
  /**
   * 设置组件初始状态
   */
  private def setupComponents(): Unit = {
    // 可以在这里设置组件的初始状态
    // 例如设置文本、样式等
  }
  
  /**
   * 处理登录按钮点击事件
   */
  @FXML
  def handleWelcomeLogin(event: ActionEvent): Unit = {
    parentController.foreach(_.showLoginMode())
  }
  
  /**
   * 处理注册按钮点击事件
   */
  @FXML
  def handleWelcomeRegister(event: ActionEvent): Unit = {
    parentController.foreach(_.showRegisterMode())
  }
  
  /**
   * 处理访客模式按钮点击事件
   */
  @FXML
  def handleWelcomeGuest(event: ActionEvent): Unit = {
    parentController.foreach(_.setAuthResult(AuthResult.ContinueAsGuest))
  }
}