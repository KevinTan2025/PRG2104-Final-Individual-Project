package gui.dialogs.auth

import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label, TextField}
import javafx.event.ActionEvent
import scalafx.application.Platform
import scalafx.stage.{Stage, Modality}
import javafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.Includes._
import gui.utils.GuiUtils
import java.net.URL
import java.util.ResourceBundle
import scala.util.Random

/**
 * OTP验证对话框控制器
 * 对接OTPVerificationDialog.fxml
 */
class OTPVerificationDialogController extends Initializable {
  
  // FXML组件绑定
  @FXML private var lblEmailIcon: Label = _
  @FXML private var lblTitle: Label = _
  @FXML private var lblEmailPrompt: Label = _
  @FXML private var lblUserEmail: Label = _
  @FXML private var lblOtpPrompt: Label = _
  @FXML private var txtOtpField: TextField = _
  @FXML private var lblOtpInputStatus: Label = _
  @FXML private var btnVerify: Button = _
  @FXML private var btnResend: Button = _
  @FXML private var lblVerificationStatus: Label = _
  @FXML private var btnCancel: Button = _
  
  // 验证状态
  private val isVerifiedProperty = BooleanProperty(false)
  private val onVerificationSuccessProperty = ObjectProperty[() => Unit](() => {})
  private val onVerificationFailureProperty = ObjectProperty[() => Unit](() => {})
  
  // OTP相关
  private var otpCode: String = _
  private var userEmail: String = _
  private var parentStage: Stage = _
  
  /**
   * 初始化方法
   */
  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    setupComponents()
    setupEventHandlers()
  }
  
  /**
   * 设置用户邮箱和父窗口
   * @param email 用户邮箱
   * @param parent 父窗口
   */
  def setEmailAndParent(email: String, parent: Stage): Unit = {
    userEmail = email
    parentStage = parent
    lblUserEmail.text = email
    generateNewOTP()
  }
  
  /**
   * 设置验证回调函数
   * @param onSuccess 成功回调
   * @param onFailure 失败回调
   */
  def setVerificationCallbacks(onSuccess: () => Unit, onFailure: () => Unit): Unit = {
    onVerificationSuccessProperty.value = onSuccess
    onVerificationFailureProperty.value = onFailure
  }
  
  /**
   * 设置组件初始状态
   */
  private def setupComponents(): Unit = {
    // 清空状态标签
    clearStatusLabels()
    
    // 设置焦点到OTP输入字段
    Platform.runLater {
      txtOtpField.requestFocus()
    }
  }
  
  /**
   * 设置事件处理器
   */
  private def setupEventHandlers(): Unit = {
    // OTP字段变化监听
    txtOtpField.text.onChange { (_, _, newValue) =>
      validateOTPInput(newValue)
    }
    
    // 回车键验证
    txtOtpField.onAction = (_: ActionEvent) => handleVerification(null)
    
    // 限制输入只能是数字，最多6位
    txtOtpField.textFormatter = new scalafx.scene.control.TextFormatter[String](
      (change: scalafx.scene.control.TextFormatter.Change) => {
        val newText = change.getControlNewText
        if (newText.matches("\\d{0,6}")) change else null
      }
    )
  }
  
  /**
   * 验证OTP输入
   * @param otp OTP输入值
   */
  private def validateOTPInput(otp: String): Unit = {
    if (otp.isEmpty) {
      lblOtpInputStatus.text = ""
      updateFieldStyle(txtOtpField, "normal")
    } else if (otp.length < 6) {
      lblOtpInputStatus.text = "请输入6位验证码"
      updateStatusLabelStyle(lblOtpInputStatus, "warning")
      updateFieldStyle(txtOtpField, "warning")
    } else if (otp.length == 6 && otp.forall(_.isDigit)) {
      lblOtpInputStatus.text = "✓ 格式正确"
      updateStatusLabelStyle(lblOtpInputStatus, "success")
      updateFieldStyle(txtOtpField, "success")
    } else {
      lblOtpInputStatus.text = "✗ 请输入6位数字"
      updateStatusLabelStyle(lblOtpInputStatus, "error")
      updateFieldStyle(txtOtpField, "error")
    }
  }
  
  /**
   * 生成新的OTP
   */
  private def generateNewOTP(): Unit = {
    val random = new Random()
    otpCode = (100000 + random.nextInt(900000)).toString
  }
  
  /**
   * 模拟发送邮件通知
   */
  private def simulateEmailNotification(): Unit = {
    Platform.runLater {
      val emailDialog = new Stage {
        title = "📧 邮件通知"
        initModality(Modality.ApplicationModal)
        initOwner(parentStage)
        resizable = false
      }
      
      try {
        val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/EmailNotificationDialog.fxml"))
        val root: Parent = loader.load()
        
        // 如果有专门的邮件通知控制器，可以在这里设置
        // val controller = loader.getController[EmailNotificationDialogController]()
        // controller.setOTPCode(otpCode)
        // controller.setUserEmail(userEmail)
        
        emailDialog.scene = new Scene(root, 500, 600)
        emailDialog.centerOnScreen()
        emailDialog.showAndWait()
        
      } catch {
        case _: Exception =>
          // 如果没有专门的邮件通知FXML，使用简单的信息对话框
          GuiUtils.showInfo(
            "📧 邮件已发送",
            s"验证码已发送到: $userEmail\n\n" +
            s"您的验证码是: $otpCode\n\n" +
            "(这是演示模式，实际应用中验证码会通过邮件发送)"
          )
      }
    }
  }
  
  /**
   * 更新字段样式
   * @param field 字段控件
   * @param status 状态类型
   */
  private def updateFieldStyle(field: TextField, status: String): Unit = {
    field.getStyleClass.removeAll("error-field", "success-field", "warning-field")
    status match {
      case "error" => field.getStyleClass.add("error-field")
      case "success" => field.getStyleClass.add("success-field")
      case "warning" => field.getStyleClass.add("warning-field")
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
   * 清空状态标签
   */
  private def clearStatusLabels(): Unit = {
    lblOtpInputStatus.text = ""
    lblVerificationStatus.text = ""
  }
  
  /**
   * 处理验证按钮点击事件
   */
  @FXML
  def handleVerification(event: ActionEvent): Unit = {
    val enteredOTP = txtOtpField.text.value.trim
    
    if (enteredOTP.isEmpty) {
      lblVerificationStatus.text = "✗ 请输入验证码"
      updateStatusLabelStyle(lblVerificationStatus, "error")
      return
    }
    
    if (enteredOTP.length != 6 || !enteredOTP.forall(_.isDigit)) {
      lblVerificationStatus.text = "✗ 验证码必须是6位数字"
      updateStatusLabelStyle(lblVerificationStatus, "error")
      return
    }
    
    if (enteredOTP == otpCode) {
      // 验证成功
      isVerifiedProperty.value = true
      lblVerificationStatus.text = "✓ 验证成功！"
      updateStatusLabelStyle(lblVerificationStatus, "success")
      
      // 延迟关闭对话框并调用成功回调
      Platform.runLater {
        Thread.sleep(500)
        onVerificationSuccessProperty.value()
        closeDialog()
      }
    } else {
      // 验证失败
      lblVerificationStatus.text = "✗ 验证码错误，请重新输入"
      updateStatusLabelStyle(lblVerificationStatus, "error")
      updateFieldStyle(txtOtpField, "error")
      
      // 清空输入字段并重新聚焦
      txtOtpField.clear()
      txtOtpField.requestFocus()
    }
  }
  
  /**
   * 处理重新发送邮件按钮点击事件
   */
  @FXML
  def handleResendEmail(event: ActionEvent): Unit = {
    // 生成新的OTP
    generateNewOTP()
    
    // 清空输入和状态
    txtOtpField.clear()
    clearStatusLabels()
    
    // 模拟发送邮件
    simulateEmailNotification()
    
    // 显示重新发送成功信息
    lblVerificationStatus.text = "📧 验证码已重新发送"
    updateStatusLabelStyle(lblVerificationStatus, "success")
    
    // 聚焦到输入字段
    txtOtpField.requestFocus()
  }
  
  /**
   * 处理取消按钮点击事件
   */
  @FXML
  def handleCancel(event: ActionEvent): Unit = {
    onVerificationFailureProperty.value()
    closeDialog()
  }
  
  /**
   * 关闭对话框
   */
  private def closeDialog(): Unit = {
    val stage = btnCancel.scene.value.window.value.asInstanceOf[javafx.stage.Stage]
    Platform.runLater {
      stage.close()
    }
  }
  
  /**
   * 显示OTP验证对话框
   * @param email 用户邮箱
   * @param parent 父窗口
   * @param onSuccess 成功回调
   * @param onFailure 失败回调
   */
  def showOTPDialog(email: String, parent: Stage, onSuccess: () => Unit, onFailure: () => Unit): Unit = {
    setEmailAndParent(email, parent)
    setVerificationCallbacks(onSuccess, onFailure)
    
    // 自动发送第一次OTP
    simulateEmailNotification()
  }
  
  /**
   * 获取验证状态
   * @return 是否已验证
   */
  def isOTPVerified: Boolean = isVerifiedProperty.value
}