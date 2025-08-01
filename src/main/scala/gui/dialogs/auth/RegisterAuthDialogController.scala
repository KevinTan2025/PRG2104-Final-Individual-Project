package gui.dialogs.auth

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label, TextField, PasswordField}
import javafx.event.ActionEvent
import scalafx.application.Platform
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.Includes._
import gui.utils.GuiUtils
import java.net.URL
import java.util.ResourceBundle
import scala.util.matching.Regex

/**
 * 注册界面控制器
 * 对接RegisterAuthDialog.fxml
 */
class RegisterAuthDialogController extends Initializable {
  
  // FXML组件绑定
  @FXML private var btnRegisterBack: Button = _
  @FXML private var lblRegisterTitle: Label = _
  @FXML private var txtRegUsername: TextField = _
  @FXML private var lblUsernameStatus: Label = _
  @FXML private var txtRegName: TextField = _
  @FXML private var lblNameStatus: Label = _
  @FXML private var txtRegContact: TextField = _
  @FXML private var lblContactStatus: Label = _
  @FXML private var txtRegEmail: TextField = _
  @FXML private var lblEmailStatus: Label = _
  @FXML private var btnSendOtp: Button = _
  @FXML private var lblOtpStatus: Label = _
  @FXML private var txtRegPassword: PasswordField = _
  @FXML private var lblPasswordStatus: Label = _
  @FXML private var txtRegConfirmPassword: PasswordField = _
  @FXML private var lblConfirmPasswordStatus: Label = _
  @FXML private var btnRegister: Button = _
  @FXML private var btnRegisterToLogin: Button = _
  
  // 父控制器引用
  private var parentController: Option[AuthDialogController] = None
  
  // 社区服务实例
  private val communityService = service.CommunityEngagementService.getInstance
  
  // OTP验证状态
  private val isEmailVerifiedProperty = BooleanProperty(false)
  private val generatedOtpProperty = ObjectProperty[Option[String]](None)
  
  // 邮箱格式验证正则表达式
  private val emailRegex: Regex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
  
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
    clearAllStatusLabels()
    
    // 禁用发送OTP按钮
    btnSendOtp.disable = true
    
    // 设置焦点到用户名字段
    Platform.runLater {
      txtRegUsername.requestFocus()
    }
  }
  
  /**
   * 设置事件处理器
   */
  private def setupEventHandlers(): Unit = {
    // 用户名字段变化监听
    txtRegUsername.text.onChange { (_, _, newValue) =>
      validateUsername(newValue)
    }
    
    // 姓名字段变化监听
    txtRegName.text.onChange { (_, _, newValue) =>
      validateName(newValue)
    }
    
    // 联系方式字段变化监听
    txtRegContact.text.onChange { (_, _, newValue) =>
      validateContact(newValue)
    }
    
    // 邮箱字段变化监听
    txtRegEmail.text.onChange { (_, _, newValue) =>
      validateEmail(newValue)
    }
    
    // 密码字段变化监听
    txtRegPassword.text.onChange { (_, _, newValue) =>
      validatePassword(newValue)
      // 重新验证确认密码
      if (txtRegConfirmPassword.text.value.nonEmpty) {
        validateConfirmPassword(newValue, txtRegConfirmPassword.text.value)
      }
    }
    
    // 确认密码字段变化监听
    txtRegConfirmPassword.text.onChange { (_, _, newValue) =>
      validateConfirmPassword(txtRegPassword.text.value, newValue)
    }
  }
  
  /**
   * 验证用户名
   * @param username 用户名
   */
  private def validateUsername(username: String): Unit = {
    if (username.trim.isEmpty) {
      lblUsernameStatus.text = ""
      updateFieldStyle(txtRegUsername, "normal")
      return
    }
    
    if (username.length < 3) {
      lblUsernameStatus.text = "✗ 用户名至少需要3个字符"
      updateStatusLabelStyle(lblUsernameStatus, "error")
      updateFieldStyle(txtRegUsername, "error")
    } else if (!isValidUsernameFormat(username)) {
      lblUsernameStatus.text = "✗ 用户名只能包含字母、数字和下划线"
      updateStatusLabelStyle(lblUsernameStatus, "error")
      updateFieldStyle(txtRegUsername, "error")
    } else if (!communityService.isUsernameAvailable(username)) {
      lblUsernameStatus.text = "✗ 用户名已被占用"
      updateStatusLabelStyle(lblUsernameStatus, "error")
      updateFieldStyle(txtRegUsername, "error")
    } else {
      lblUsernameStatus.text = "✓ 用户名可用"
      updateStatusLabelStyle(lblUsernameStatus, "success")
      updateFieldStyle(txtRegUsername, "success")
    }
  }
  
  /**
   * 验证姓名
   * @param name 姓名
   */
  private def validateName(name: String): Unit = {
    if (name.trim.isEmpty) {
      lblNameStatus.text = ""
      updateFieldStyle(txtRegName, "normal")
    } else if (name.trim.length < 2) {
      lblNameStatus.text = "✗ 姓名至少需要2个字符"
      updateStatusLabelStyle(lblNameStatus, "error")
      updateFieldStyle(txtRegName, "error")
    } else {
      lblNameStatus.text = "✓ 姓名格式正确"
      updateStatusLabelStyle(lblNameStatus, "success")
      updateFieldStyle(txtRegName, "success")
    }
  }
  
  /**
   * 验证联系方式
   * @param contact 联系方式
   */
  private def validateContact(contact: String): Unit = {
    if (contact.trim.isEmpty) {
      lblContactStatus.text = ""
      updateFieldStyle(txtRegContact, "normal")
    } else if (contact.trim.length < 5) {
      lblContactStatus.text = "✗ 联系方式至少需要5个字符"
      updateStatusLabelStyle(lblContactStatus, "error")
      updateFieldStyle(txtRegContact, "error")
    } else {
      lblContactStatus.text = "✓ 联系方式格式正确"
      updateStatusLabelStyle(lblContactStatus, "success")
      updateFieldStyle(txtRegContact, "success")
    }
  }
  
  /**
   * 验证邮箱
   * @param email 邮箱地址
   */
  private def validateEmail(email: String): Unit = {
    val isValidEmail = email.nonEmpty && isValidEmailFormat(email)
    btnSendOtp.disable = !isValidEmail
    
    if (email.trim.isEmpty) {
      lblEmailStatus.text = ""
      updateFieldStyle(txtRegEmail, "normal")
      resetOTPState()
    } else if (isValidEmail) {
      if (!communityService.isEmailAvailable(email)) {
        lblEmailStatus.text = "✗ 邮箱已被注册"
        updateStatusLabelStyle(lblEmailStatus, "error")
        updateFieldStyle(txtRegEmail, "error")
        btnSendOtp.disable = true
      } else {
        lblEmailStatus.text = "✓ 邮箱格式正确"
        updateStatusLabelStyle(lblEmailStatus, "success")
        updateFieldStyle(txtRegEmail, "success")
      }
    } else {
      lblEmailStatus.text = "✗ 邮箱格式无效"
      updateStatusLabelStyle(lblEmailStatus, "error")
      updateFieldStyle(txtRegEmail, "error")
    }
    
    if (!isValidEmail) {
      resetOTPState()
    }
  }
  
  /**
   * 验证密码
   * @param password 密码
   */
  private def validatePassword(password: String): Unit = {
    if (password.isEmpty) {
      lblPasswordStatus.text = ""
      updateFieldStyle(txtRegPassword, "normal")
      return
    }
    
    val requirements = getPasswordRequirements(password)
    val missingCount = requirements.count(!_._2)
    
    if (missingCount == 0) {
      lblPasswordStatus.text = "✓ 密码符合所有要求"
      updateStatusLabelStyle(lblPasswordStatus, "success")
      updateFieldStyle(txtRegPassword, "success")
    } else {
      val missing = requirements.filter(!_._2).map(_._1)
      lblPasswordStatus.text = s"✗ 缺少: ${missing.mkString(", ")}"
      updateStatusLabelStyle(lblPasswordStatus, "error")
      updateFieldStyle(txtRegPassword, "error")
    }
  }
  
  /**
   * 验证确认密码
   * @param password 原密码
   * @param confirmPassword 确认密码
   */
  private def validateConfirmPassword(password: String, confirmPassword: String): Unit = {
    if (confirmPassword.isEmpty) {
      lblConfirmPasswordStatus.text = ""
      updateFieldStyle(txtRegConfirmPassword, "normal")
      return
    }
    
    if (password == confirmPassword) {
      lblConfirmPasswordStatus.text = "✓ 密码匹配"
      updateStatusLabelStyle(lblConfirmPasswordStatus, "success")
      updateFieldStyle(txtRegConfirmPassword, "success")
    } else {
      lblConfirmPasswordStatus.text = "✗ 密码不匹配"
      updateStatusLabelStyle(lblConfirmPasswordStatus, "error")
      updateFieldStyle(txtRegConfirmPassword, "error")
    }
  }
  
  /**
   * 获取密码要求检查结果
   * @param password 密码
   * @return 要求列表和是否满足
   */
  private def getPasswordRequirements(password: String): List[(String, Boolean)] = {
    List(
      ("8位以上", password.length >= 8),
      ("大写字母", password.exists(_.isUpper)),
      ("小写字母", password.exists(_.isLower)),
      ("数字", password.exists(_.isDigit)),
      ("特殊字符", password.exists(c => !c.isLetterOrDigit))
    )
  }
  
  /**
   * 检查用户名格式是否有效
   * @param username 用户名
   * @return 是否有效
   */
  private def isValidUsernameFormat(username: String): Boolean = {
    username.matches("^[a-zA-Z0-9_]+$")
  }
  
  /**
   * 检查邮箱格式是否有效
   * @param email 邮箱地址
   * @return 是否有效
   */
  private def isValidEmailFormat(email: String): Boolean = {
    emailRegex.findFirstIn(email).isDefined
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
   * 重置OTP状态
   */
  private def resetOTPState(): Unit = {
    isEmailVerifiedProperty.value = false
    generatedOtpProperty.value = None
    lblOtpStatus.text = ""
  }
  
  /**
   * 清空所有状态标签
   */
  private def clearAllStatusLabels(): Unit = {
    lblUsernameStatus.text = ""
    lblNameStatus.text = ""
    lblContactStatus.text = ""
    lblEmailStatus.text = ""
    lblOtpStatus.text = ""
    lblPasswordStatus.text = ""
    lblConfirmPasswordStatus.text = ""
  }
  
  /**
   * 处理返回按钮点击事件
   */
  @FXML
  def handleRegisterBack(event: ActionEvent): Unit = {
    parentController.foreach(_.showWelcomeMode())
  }
  
  /**
   * 处理发送OTP按钮点击事件
   */
  @FXML
  def handleSendOtp(event: ActionEvent): Unit = {
    val email = txtRegEmail.text.value.trim
    
    if (email.isEmpty || !isValidEmailFormat(email)) {
      GuiUtils.showWarning("邮箱错误", "请输入有效的邮箱地址")
      return
    }
    
    // 创建并显示OTP验证对话框
    val otpDialog = new OTPVerificationDialog(
      parentController.map(_.getDialogStage).getOrElse(new scalafx.stage.Stage()),
      email
    )
    
    otpDialog.show(
      onSuccess = () => {
        isEmailVerifiedProperty.value = true
        lblOtpStatus.text = "✓ 邮箱验证成功"
        updateStatusLabelStyle(lblOtpStatus, "success")
        btnSendOtp.text = "✓ 已验证"
        btnSendOtp.disable = true
      },
      onFailure = () => {
        isEmailVerifiedProperty.value = false
        lblOtpStatus.text = "✗ 邮箱验证失败"
        updateStatusLabelStyle(lblOtpStatus, "error")
      }
    )
  }
  
  /**
   * 处理注册按钮点击事件
   */
  @FXML
  def handleRegister(event: ActionEvent): Unit = {
    // 获取所有字段值
    val username = txtRegUsername.text.value.trim
    val name = txtRegName.text.value.trim
    val contact = txtRegContact.text.value.trim
    val email = txtRegEmail.text.value.trim
    val password = txtRegPassword.text.value
    val confirmPassword = txtRegConfirmPassword.text.value
    
    // 验证所有必填字段
    if (!validateAllFields(username, name, contact, email, password, confirmPassword)) {
      return
    }
    
    // 检查邮箱是否已验证
    if (!isEmailVerifiedProperty.value) {
      GuiUtils.showWarning("邮箱未验证", "请先验证您的邮箱地址")
      return
    }
    
    // 尝试注册
    try {
      val registrationResult = communityService.registerUser(
        username = username,
        email = email,
        name = name,
        contactInfo = contact,
        password = password,
        isAdmin = false
      )
      
      if (registrationResult) {
        GuiUtils.showInfo("注册成功", "🎉 账户创建成功！欢迎加入社区平台！")
        parentController.foreach(_.setAuthResult(AuthResult.RegisterSuccess))
      } else {
        GuiUtils.showError("注册失败", "注册过程中发生错误，请稍后重试")
      }
    } catch {
      case e: Exception =>
        GuiUtils.showError("注册错误", s"注册过程中发生错误: ${e.getMessage}")
    }
  }
  
  /**
   * 验证所有字段
   * @return 是否所有字段都有效
   */
  private def validateAllFields(username: String, name: String, contact: String, 
                               email: String, password: String, confirmPassword: String): Boolean = {
    var isValid = true
    
    // 验证用户名
    if (username.isEmpty) {
      lblUsernameStatus.text = "✗ 请输入用户名"
      updateStatusLabelStyle(lblUsernameStatus, "error")
      isValid = false
    }
    
    // 验证姓名
    if (name.isEmpty) {
      lblNameStatus.text = "✗ 请输入姓名"
      updateStatusLabelStyle(lblNameStatus, "error")
      isValid = false
    }
    
    // 验证联系方式
    if (contact.isEmpty) {
      lblContactStatus.text = "✗ 请输入联系方式"
      updateStatusLabelStyle(lblContactStatus, "error")
      isValid = false
    }
    
    // 验证邮箱
    if (email.isEmpty) {
      lblEmailStatus.text = "✗ 请输入邮箱地址"
      updateStatusLabelStyle(lblEmailStatus, "error")
      isValid = false
    }
    
    // 验证密码
    if (password.isEmpty) {
      lblPasswordStatus.text = "✗ 请输入密码"
      updateStatusLabelStyle(lblPasswordStatus, "error")
      isValid = false
    }
    
    // 验证确认密码
    if (confirmPassword.isEmpty) {
      lblConfirmPasswordStatus.text = "✗ 请确认密码"
      updateStatusLabelStyle(lblConfirmPasswordStatus, "error")
      isValid = false
    } else if (password != confirmPassword) {
      lblConfirmPasswordStatus.text = "✗ 密码不匹配"
      updateStatusLabelStyle(lblConfirmPasswordStatus, "error")
      isValid = false
    }
    
    isValid
  }
  
  /**
   * 处理跳转到登录页面按钮点击事件
   */
  @FXML
  def handleRegisterToLogin(event: ActionEvent): Unit = {
    parentController.foreach(_.showLoginMode())
  }
}