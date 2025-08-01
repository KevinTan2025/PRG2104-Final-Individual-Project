package gui.dialogs.auth

import scalafx.stage.{Stage, Modality}
import javafx.stage.WindowEvent

/**
 * Facebook-style authentication popup dialog
 * 重构为使用独立的FXML文件和控制器架构
 */

class FacebookStyleAuthDialog(parentStage: Stage) {
  
  // 使用新的AuthDialogController来管理认证流程
  private val authController = new AuthDialogController(parentStage)
  
  // Main dialog
  private val dialog = new Stage {
    title = "Community Platform - Authentication"
    initModality(Modality.ApplicationModal)
    initOwner(parentStage)
    resizable = false
    
    onCloseRequest = (event: WindowEvent) => {
      authController.setAuthResult(AuthResult.ContinueAsGuest)
    }
  }

  
  /**
   * 显示认证对话框，默认从欢迎界面开始
   * @return 认证结果
   */
  def show(): AuthResult = {
    show(AuthMode.WelcomeMode)
  }
  
  /**
   * 显示认证对话框，从指定模式开始
   * @param initialMode 初始认证模式
   * @return 认证结果
   */
  def show(initialMode: AuthMode): AuthResult = {
    // 使用AuthDialogController显示对话框
    authController.show(initialMode)
  }

  

  

  

  

  

  

  

  

  

  

}
