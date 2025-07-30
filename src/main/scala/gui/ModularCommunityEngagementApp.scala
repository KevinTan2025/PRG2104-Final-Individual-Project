package gui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import gui.scenes.SceneManager
import gui.utils.GuiUtils
import scalafx.application.Platform
import javafx.animation.{PauseTransition}
import javafx.util.Duration

/**
 * Modular main GUI application class for the Community Engagement Platform
 * This replaces the monolithic CommunityEngagementApp with a component-based architecture
 * Now shows dashboard first, then welcome dialog
 */
object ModularCommunityEngagementApp extends JFXApp3 {
  
  override def start(): Unit = {
    stage = new PrimaryStage {
      title = "Community Engagement Platform"
      minWidth = 800
      minHeight = 600
      resizable = true
    }
    
    try {
      val communityService = new service.CommunityEngagementService()
      val sceneManager = new SceneManager(stage, communityService)
      
      // Check if user is already logged in, if so go directly to dashboard
      if (communityService.isLoggedIn) {
        sceneManager.showMainScene()
      } else {
        // First, show the dashboard (anonymous mode)
        sceneManager.showAnonymousScene()
        
        // Show Facebook-style auth dialog after a short delay to let user see the dashboard
        val delay = new PauseTransition(Duration.millis(1000)) // 1 second delay
        delay.setOnFinished { _ =>
          Platform.runLater {
            val authDialog = new gui.dialogs.auth.FacebookStyleAuthDialog(stage)
            authDialog.show() match {
              case gui.dialogs.auth.AuthResult.LoginSuccess =>
                sceneManager.showMainScene()
              case gui.dialogs.auth.AuthResult.RegisterSuccess =>
                sceneManager.showMainScene()
              case gui.dialogs.auth.AuthResult.ContinueAsGuest =>
                // Already showing anonymous scene, just continue
                ()
              case _ =>
                // Handle other cases (like Cancelled) as guest mode
                ()
            }
          }
        }
        delay.play()
      }
      
      stage.centerOnScreen()
    } catch {
      case e: Exception =>
        GuiUtils.showError("Startup Error", s"Failed to start application: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}
