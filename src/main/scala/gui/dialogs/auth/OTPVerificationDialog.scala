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
import scala.util.Random

/**
 * OTP Verification Dialog for user registration
 * 使用FXML界面的包装类
 */
class OTPVerificationDialog(parentStage: Stage, userEmail: String) {
  
  // 对话框窗口
  private val dialog = new Stage {
    title = "📧 Email Verification - Community Platform"
    initModality(Modality.ApplicationModal)
    initOwner(parentStage)
    resizable = false
  }
  
  // 控制器引用
  private var controller: Option[OTPVerificationDialogController] = None
  
  // 验证状态
  private val isVerifiedProperty = BooleanProperty(false)
  
  /**
   * 显示OTP验证对话框
   * @param onSuccess 验证成功回调
   * @param onFailure 验证失败回调
   */
  def show(onSuccess: () => Unit, onFailure: () => Unit): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/gui/dialogs/auth/OTPVerificationDialog.fxml"))
      val root: Parent = loader.load()
      
      // 获取控制器并设置参数
      controller = Some(loader.getController[OTPVerificationDialogController]())
      controller.foreach { ctrl =>
        ctrl.setEmailAndParent(userEmail, parentStage)
        ctrl.setVerificationCallbacks(onSuccess, onFailure)
        ctrl.showOTPDialog(userEmail, parentStage, onSuccess, onFailure)
      }
      
      dialog.scene = new Scene(root, 400, 350)
      dialog.centerOnScreen()
      dialog.showAndWait()
      
    } catch {
      case e: Exception =>
        GuiUtils.showError("加载错误", s"无法加载OTP验证界面: ${e.getMessage}")
        onFailure()
    }
  }
  /**
   * 获取验证状态
   * @return 是否已验证
   */
  def isOTPVerified: Boolean = {
    controller.map(_.isOTPVerified).getOrElse(false)
  }
  

}
