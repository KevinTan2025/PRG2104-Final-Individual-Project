package gui.dialogs.auth

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label, TextField, PasswordField}
import javafx.event.ActionEvent
import scalafx.application.Platform
import scalafx.Includes._
import gui.utils.GuiUtils
import java.net.URL
import java.util.ResourceBundle

/**
 * 登录界面控制器
 * 对接LoginAuthDialog.fxml
 */
class LoginAuthDialogController extends Initializable {
  
  // FXML组件绑定
  @FXML private var btnLoginBack: Button = _
  @FXML private var lblLoginTitle: Label = _
  @FXML private var txtLoginUsername: TextField = _
  @FXML private var lblLoginUsernameStatus: Label = _
  @FXML private var txtLoginPassword: PasswordField = _
  @FXML private var lblLoginPasswordStatus: Label = _
  @FXML private var lblLoginErrorStatus: Label = _
  @FXML private var btnLogin: Button = _
  @FXML private var lblLoginDemo: Label = _
  @FXML private var btnLoginToRegister: Button = _
  
  // 父控制器引用
  private var parentController: Option[AuthDialogController] = None
  
  // 社区服务实例
  private val communityService = service.CommunityEngagementService.getInstance
  
  /**
   * 初始化方法
   */
  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    setupComponents()
    setupEventHandlers()
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
    // 清空状态标签
    clearStatusLabels()
    
    // 设置焦点到用户名字段
    Platform.runLater {
      txtLoginUsername.requestFocus()
    }
  }
  
  /**
   * 设置事件处理器
   */
  private def setupEventHandlers(): Unit = {
    // 用户名字段变化监听
    txtLoginUsername.text.onChange { (_, _, newValue) =>
      validateUsernameInput(newValue)
    }
    
    // 密码字段变化监听
    txtLoginPassword.text.onChange { (_, _, newValue) =>
      validatePasswordInput(newValue)
    }
    
    // 回车键登录
    txtLoginPassword.onAction = (_: ActionEvent) => handleLogin(null)
  }
  
  /**
   * 验证用户名输入
   * @param username 用户名
   */
  private def validateUsernameInput(username: String): Unit = {
    if (username.trim.isEmpty) {
      lblLoginUsernameStatus.text = ""
      updateFieldStyle(txtLoginUsername, "normal")
    } else {
      lblLoginUsernameStatus.text = "✓ 用户名格式正确"
      updateStatusLabelStyle(lblLoginUsernameStatus, "success")
      updateFieldStyle(txtLoginUsername, "success")
    }
    clearErrorStatus()
  }
  
  /**
   * 验证密码输入
   * @param password 密码
   */
  private def validatePasswordInput(password: String): Unit = {
    if (password.trim.isEmpty) {
      lblLoginPasswordStatus.text = ""
      updateFieldStyle(txtLoginPassword, "normal")
    } else {
      lblLoginPasswordStatus.text = "✓ 密码已输入"
      updateStatusLabelStyle(lblLoginPasswordStatus, "success")
      updateFieldStyle(txtLoginPassword, "success")
    }
    clearErrorStatus()
  }
  
  /**
   * 更新字段样式
   * @param field 字段控件
   * @param status 状态类型
   */
  private def updateFieldStyle(field: TextField, status: String): Unit = {
    field.getStyleClass.removeAll("error-field", "success-field")
    status match {
      case "error" => field.getStyleClass.add("error-field")
      case "success" => field.getStyleClass.add("success-field")
      case _ => // normal状态不添加特殊样式
    }
  }
  
  /**
   * 更新状态标签样式
   * @param label 标签控件
   * @param status 状态类型
   */
  private def updateStatusLabelStyle(label: Label, status: String): Unit = {
    label.getStyleClass.removeAll("error-status", "success-status", "warning-status")
    status match {
      case "error" => label.getStyleClass.add("error-status")
      case "success" => label.getStyleClass.add("success-status")
      case "warning" => label.getStyleClass.add("warning-status")
      case _ => // normal状态不添加特殊样式
    }
  }
  
  /**
   * 清空错误状态
   */
  private def clearErrorStatus(): Unit = {
    lblLoginErrorStatus.text = ""
  }
  
  /**
   * 清空所有状态标签
   */
  private def clearStatusLabels(): Unit = {
    lblLoginUsernameStatus.text = ""
    lblLoginPasswordStatus.text = ""
    lblLoginErrorStatus.text = ""
  }
  
  /**
   * 处理返回按钮点击事件
   */
  @FXML
  def handleLoginBack(event: ActionEvent): Unit = {
    parentController.foreach(_.showWelcomeMode())
  }
  
  /**
   * 处理登录按钮点击事件
   */
  @FXML
  def handleLogin(event: ActionEvent): Unit = {
    val username = txtLoginUsername.text.value.trim
    val password = txtLoginPassword.text.value
    
    // 验证输入
    if (username.isEmpty || password.isEmpty) {
      lblLoginErrorStatus.text = "✗ 请输入用户名和密码"
      updateStatusLabelStyle(lblLoginErrorStatus, "error")
      return
    }
    
    // 尝试登录
    try {
      val loginResult = communityService.login(username, password)
      
      if (loginResult.isDefined) {
        // 登录成功
        lblLoginErrorStatus.text = "✓ 登录成功！"
        updateStatusLabelStyle(lblLoginErrorStatus, "success")
        
        // 延迟关闭对话框
        Platform.runLater {
          Thread.sleep(500)
          parentController.foreach(_.setAuthResult(AuthResult.LoginSuccess))
        }
      } else {
        // 登录失败
        lblLoginErrorStatus.text = "✗ 用户名或密码错误"
        updateStatusLabelStyle(lblLoginErrorStatus, "error")
        updateFieldStyle(txtLoginUsername, "error")
        updateFieldStyle(txtLoginPassword, "error")
        
        // 清空密码字段
        txtLoginPassword.clear()
        txtLoginPassword.requestFocus()
      }
    } catch {
      case e: Exception =>
        lblLoginErrorStatus.text = s"✗ 登录失败: ${e.getMessage}"
        updateStatusLabelStyle(lblLoginErrorStatus, "error")
        GuiUtils.showError("登录错误", s"登录过程中发生错误: ${e.getMessage}")
    }
  }
  
  /**
   * 处理跳转到注册页面按钮点击事件
   */
  @FXML
  def handleLoginToRegister(event: ActionEvent): Unit = {
    parentController.foreach(_.showRegisterMode())
  }
}