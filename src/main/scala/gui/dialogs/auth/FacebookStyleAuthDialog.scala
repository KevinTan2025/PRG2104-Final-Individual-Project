package gui.dialogs.auth

import scalafx.stage.{Stage, Modality}
import javafx.stage.WindowEvent

/**
 * Facebook-style authentication popup dialog
 * Refactored to use independent FXML files and controller architecture
 */

class FacebookStyleAuthDialog(parentStage: Stage) {
  
  // Use new AuthDialogController to manage authentication flow
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
   * Show authentication dialog, starting from welcome screen by default
   * @return Authentication result
   */
  def show(): AuthResult = {
    show(AuthMode.WelcomeMode)
  }
  
  /**
   * Show authentication dialog, starting from specified mode
   * @param initialMode Initial authentication mode
   * @return Authentication result
   */
  def show(initialMode: AuthMode): AuthResult = {
    // Use AuthDialogController to show dialog
    authController.show(initialMode)
  }

  

  

  

  

  

  

  

  

  

  

}
