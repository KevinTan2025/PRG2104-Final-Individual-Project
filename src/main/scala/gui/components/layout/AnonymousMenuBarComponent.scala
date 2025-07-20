package gui.components.layout

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.BaseComponent

/**
 * Menu bar component for anonymous users
 * 安全级别: PUBLIC - 匿名用户的菜单栏
 */
class AnonymousMenuBarComponent(
  onLoginClick: () => Unit,
  onRegisterClick: () => Unit,
  onExitAnonymousMode: () => Unit
) extends BaseComponent {
  
  override def build(): Region = {
    val accountMenu = new Menu("👤 Account") {
      items = Seq(
        new MenuItem("✨ Login / Register") {
          onAction = (_: ActionEvent) => showAuthDialog()
        },
        new SeparatorMenuItem(),
        new MenuItem("🔐 Login") {
          onAction = (_: ActionEvent) => showAuthDialog()
        },
        new MenuItem("📝 Register") {
          onAction = (_: ActionEvent) => showRegisterDialog()
        },
        new SeparatorMenuItem(),
        new MenuItem("🏠 Back to Login") {
          onAction = (_: ActionEvent) => onExitAnonymousMode()
        }
      )
    }
    
    val helpMenu = new Menu("ℹ️ Help") {
      items = Seq(
        new MenuItem("About Anonymous Mode") {
          onAction = (_: ActionEvent) => {
            GuiUtils.showInfo("Anonymous Mode", 
              "You are browsing in anonymous mode with limited features.\n\n" +
              "Available features:\n" +
              "• View announcements\n" +
              "• Browse food sharing posts\n" +
              "• Read discussions\n" +
              "• Check events\n" +
              "• Learn about the platform\n\n" +
              "To unlock full features like posting, commenting, and notifications,\n" +
              "please create an account or login."
            )
          }
        },
        new MenuItem("Why Register?") {
          onAction = (_: ActionEvent) => {
            GuiUtils.showInfo("Benefits of Registration", 
              "Create an account to unlock:\n\n" +
              "🍕 Food Sharing: Post and request food items\n" +
              "💬 Community Discussions: Join conversations\n" +
              "📅 Events: Create and manage events\n" +
              "🔔 Notifications: Stay updated\n" +
              "👤 Profile: Personalize your experience\n" +
              "🛡️ Security: Secure and private interactions\n\n" +
              "Join our community today!"
            )
          }
        }
      )
    }
    
    val statusLabel = new Label("🔍 Anonymous Mode - Limited Features") {
      style = "-fx-text-fill: #6c757d; -fx-font-style: italic; -fx-font-size: 12px;"
    }
    
    val menuBar = new MenuBar {
      menus = Seq(accountMenu, helpMenu)
    }
    
    new BorderPane {
      left = menuBar
      right = new HBox {
        alignment = scalafx.geometry.Pos.CenterRight
        children = Seq(statusLabel)
        padding = Insets(5, 10, 5, 0)
      }
    }
  }
  
  /**
   * Show authentication dialog (Facebook-style) with all options
   */
  private def showAuthDialog(): Unit = {
    try {
      GuiUtils.getMainStage match {
        case Some(mainStage) =>
          val authDialog = new gui.dialogs.auth.FacebookStyleAuthDialog(mainStage)
          authDialog.show() match {
            case gui.dialogs.auth.AuthResult.LoginSuccess =>
              // User logged in successfully, need to refresh the scene
              val sceneManager = new gui.scenes.SceneManager(mainStage, service)
              sceneManager.showMainScene()
            case gui.dialogs.auth.AuthResult.RegisterSuccess =>
              // User registered successfully, need to refresh the scene
              val sceneManager = new gui.scenes.SceneManager(mainStage, service)
              sceneManager.showMainScene()
            case _ =>
              // User cancelled or continued as guest, stay in anonymous mode
              ()
          }
        case None =>
          GuiUtils.showError("Error", "Main stage not available.")
      }
    } catch {
      case e: Exception =>
        GuiUtils.showError("Authentication Error", s"Failed to show authentication dialog: ${e.getMessage}")
        e.printStackTrace()
    }
  }
  
  /**
   * Show authentication dialog (Facebook-style) directly in register mode
   */
  private def showRegisterDialog(): Unit = {
    try {
      GuiUtils.getMainStage match {
        case Some(mainStage) =>
          val authDialog = new gui.dialogs.auth.FacebookStyleAuthDialog(mainStage)
          authDialog.show(gui.dialogs.auth.AuthMode.RegisterMode) match {
            case gui.dialogs.auth.AuthResult.LoginSuccess =>
              // User logged in successfully, need to refresh the scene
              val sceneManager = new gui.scenes.SceneManager(mainStage, service)
              sceneManager.showMainScene()
            case gui.dialogs.auth.AuthResult.RegisterSuccess =>
              // User registered successfully, need to refresh the scene
              val sceneManager = new gui.scenes.SceneManager(mainStage, service)
              sceneManager.showMainScene()
            case _ =>
              // User cancelled or continued as guest, stay in anonymous mode
              ()
          }
        case None =>
          GuiUtils.showError("Error", "Main stage not available.")
      }
    } catch {
      case e: Exception =>
        GuiUtils.showError("Authentication Error", s"Failed to show authentication dialog: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}
