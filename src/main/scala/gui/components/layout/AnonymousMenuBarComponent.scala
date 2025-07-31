package gui.components.layout

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Label, Menu, MenuBar, MenuItem}
import javafx.scene.layout.{BorderPane, HBox}
import javafx.event.ActionEvent
import scalafx.scene.layout.Region
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.BaseComponent

/**
 * Menu bar component for anonymous users
 * Security Level: PUBLIC - Menu bar for anonymous users
 */
class AnonymousMenuBarComponent(
  onLoginClick: () => Unit,
  onRegisterClick: () => Unit,
  onExitAnonymousMode: () => Unit
) extends BaseComponent {
  
  // FXML injected controls
  @FXML private var bpMainLayout: BorderPane = _
  @FXML private var menuBar: MenuBar = _
  @FXML private var menuAccount: Menu = _
  @FXML private var menuHelp: Menu = _
  @FXML private var hboxStatusContainer: HBox = _
  @FXML private var lblAnonymousStatus: Label = _
  @FXML private var menuItemLoginRegister: MenuItem = _
  @FXML private var menuItemLogin: MenuItem = _
  @FXML private var menuItemRegister: MenuItem = _
  @FXML private var menuItemBackToLogin: MenuItem = _
  @FXML private var menuItemAboutAnonymous: MenuItem = _
  @FXML private var menuItemWhyRegister: MenuItem = _
  
  override def build(): Region = {
    val loader = new FXMLLoader(getClass.getResource("/gui/components/layout/AnonymousMenuBarComponent.fxml"))
    loader.setController(this)
    val root = loader.load[BorderPane]()
    
    // Convert JavaFX BorderPane to ScalaFX Region
    new Region(root)
  }
  
  // FXML Event Handlers
  @FXML
  private def handleLoginRegister(event: ActionEvent): Unit = {
    showAuthDialog()
  }
  
  @FXML
  private def handleLogin(event: ActionEvent): Unit = {
    showAuthDialog()
  }
  
  @FXML
  private def handleRegister(event: ActionEvent): Unit = {
    showRegisterDialog()
  }
  
  @FXML
  private def handleBackToLogin(event: ActionEvent): Unit = {
    onExitAnonymousMode()
  }
  
  @FXML
  private def handleAboutAnonymous(event: ActionEvent): Unit = {
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
  
  @FXML
  private def handleWhyRegister(event: ActionEvent): Unit = {
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
