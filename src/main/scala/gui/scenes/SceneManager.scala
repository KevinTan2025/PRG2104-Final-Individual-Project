package gui.scenes

import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane
import scalafx.stage.Stage
import gui.components.layout.{MenuBarComponent, MainTabPane, AnonymousMenuBarComponent, AnonymousMainTabPane}
import gui.utils.GuiUtils
import service.CommunityEngagementService

/**
 * Scene manager for handling different application scenes
 */
class SceneManager(stage: Stage, private val service: CommunityEngagementService) {
  
  GuiUtils.mainStage = stage
  
  def showAnonymousScene(): Unit = {
    // Enable anonymous mode in service
    service.enableAnonymousMode()
    
    val menuBar = new AnonymousMenuBarComponent(
      onLoginClick = () => {}, // These are now handled internally by AnonymousMenuBarComponent
      onRegisterClick = () => {}, // These are now handled internally by AnonymousMenuBarComponent
      onExitAnonymousMode = () => {
        service.disableAnonymousMode()
        // Show auth dialog when exiting anonymous mode
        val authDialog = new gui.dialogs.auth.FacebookStyleAuthDialog(stage)
        authDialog.show() match {
          case gui.dialogs.auth.AuthResult.LoginSuccess =>
            showMainScene()
          case gui.dialogs.auth.AuthResult.RegisterSuccess =>
            showMainScene()
          case _ =>
            // Stay in anonymous mode if cancelled
            showAnonymousScene()
        }
      }
    )
    
    val tabPane = new AnonymousMainTabPane(
      onLoginPrompt = () => {
        // Show auth dialog when login is prompted
        val authDialog = new gui.dialogs.auth.FacebookStyleAuthDialog(stage)
        authDialog.show() match {
          case gui.dialogs.auth.AuthResult.LoginSuccess =>
            showMainScene()
          case gui.dialogs.auth.AuthResult.RegisterSuccess =>
            showMainScene()
          case _ =>
            // Stay in anonymous mode if cancelled
            ()
        }
      }
    )
    
    val rootPane = new BorderPane {
      top = menuBar.build()
      center = tabPane.build()
    }
    
    val scene = new Scene(rootPane, 1000, 700)
    stage.scene = scene
  }
  
  def showMainScene(): Unit = {
    val menuBar = new MenuBarComponent(onLogout = () => {
      service.logout()
      // After logout, show anonymous scene
      showAnonymousScene()
    })
    val tabPane = new MainTabPane()
    
    val rootPane = new BorderPane {
      top = menuBar.build()
      center = tabPane.build()
    }
    
    val scene = new Scene(rootPane, 1000, 700)
    stage.scene = scene
  }
}
