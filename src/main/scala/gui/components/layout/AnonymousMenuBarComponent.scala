package gui.components.layout

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Label, Menu, MenuBar, MenuItem}
import javafx.scene.layout.{BorderPane, HBox}
import javafx.event.ActionEvent
import scalafx.scene.layout.Region
import scalafx.Includes._
import gui.utils.{GuiUtils, ThemeManager}
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
  @FXML private var menuTheme: Menu = _
  @FXML private var menuHelp: Menu = _
  @FXML private var hboxStatusContainer: HBox = _
  @FXML private var lblAnonymousStatus: Label = _
  @FXML private var menuItemLoginRegister: MenuItem = _
  @FXML private var menuItemLogin: MenuItem = _
  @FXML private var menuItemRegister: MenuItem = _
  @FXML private var menuItemBackToLogin: MenuItem = _
  @FXML private var menuItemToggleTheme: MenuItem = _
  @FXML private var menuItemLightMode: MenuItem = _
  @FXML private var menuItemDarkMode: MenuItem = _
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
  
  // 主题相关事件处理方法 - Theme related event handlers
  @FXML
  private def handleToggleTheme(event: ActionEvent): Unit = {
    ThemeManager.toggleTheme()
    applyThemeToCurrentScene()
    GuiUtils.showInfo("Theme Changed", 
      s"Switched to ${if (ThemeManager.isDarkMode.value) "Dark" else "Light"} mode")
  }
  
  @FXML
  private def handleLightMode(event: ActionEvent): Unit = {
    ThemeManager.setDarkMode(false)
    applyThemeToCurrentScene()
    GuiUtils.showInfo("Theme Changed", "Switched to Light mode")
  }
  
  @FXML
  private def handleDarkMode(event: ActionEvent): Unit = {
    ThemeManager.setDarkMode(true)
    applyThemeToCurrentScene()
    GuiUtils.showInfo("Theme Changed", "Switched to Dark mode")
  }
  
  /**
   * 应用主题到当前场景
   * Apply theme to current scene
   */
  private def applyThemeToCurrentScene(): Unit = {
    GuiUtils.mainStage.foreach { stage =>
      val scene = stage.scene.value
      if (scene != null) {
        // 使用ThemeManager的CSS主题系统
        ThemeManager.applyThemeToScene(scene)
      }
    }
  }
  
  /**
   * 递归更新所有组件样式
   * Recursively update all component styles
   */
  private def updateAllComponentStyles(node: javafx.scene.Node): Unit = {
    updateAllComponentStyles(node, Set.empty, 0)
  }
  
  /**
   * 递归更新所有组件样式（带访问记录防止无限递归）
   * Recursively update all component styles with visited tracking
   */
  private def updateAllComponentStyles(node: javafx.scene.Node, visited: Set[javafx.scene.Node], depth: Int): Unit = {
    // 防止无限递归和过深递归
    if (visited.contains(node) || depth > 50) return
    val newVisited = visited + node
    
    // 更新当前节点样式
    node match {
      case region: javafx.scene.layout.Region =>
        // 设置背景色和文字颜色
        val backgroundStyle = ThemeManager.getComponentStyle("background")
        val textStyle = ThemeManager.getComponentStyle("label-primary")
        region.setStyle(s"$backgroundStyle $textStyle")
      case label: javafx.scene.control.Label =>
        // 更新标签文字颜色
        label.setStyle(ThemeManager.getComponentStyle("label-primary"))
      case button: javafx.scene.control.Button =>
        // 更新按钮样式
        button.setStyle(ThemeManager.getComponentStyle("button"))
      case menuBar: javafx.scene.control.MenuBar =>
        // 更新菜单栏样式
        val backgroundStyle = ThemeManager.getComponentStyle("background")
        val textStyle = ThemeManager.getComponentStyle("label-primary")
        menuBar.setStyle(s"$backgroundStyle $textStyle")
        // 更新菜单栏中的菜单项
        updateMenuBarTheme(menuBar)
        // MenuBar已经特殊处理，不需要继续递归子节点
        return
      case textField: javafx.scene.control.TextField =>
        // 更新文本框样式
        textField.setStyle(ThemeManager.getInputFieldStyle)
      case _ =>
    }
    
    // 递归更新子节点
    node match {
      case parent: javafx.scene.Parent =>
        import scala.jdk.CollectionConverters._
        parent.getChildrenUnmodifiable.asScala.foreach(child => 
          updateAllComponentStyles(child, newVisited, depth + 1)
        )
      case _ =>
    }
  }
  
  /**
   * 更新菜单栏主题
   * Update menu bar theme
   */
  private def updateMenuBarTheme(menuBar: javafx.scene.control.MenuBar): Unit = {
    import scala.jdk.CollectionConverters._
    menuBar.getMenus.asScala.foreach { menu =>
      // 更新菜单样式 - Menu不继承自Node，需要特殊处理
      menu.setStyle(ThemeManager.getComponentStyle("label-primary"))
      // 更新菜单项
      menu.getItems.asScala.foreach { menuItem =>
        menuItem.setStyle(ThemeManager.getComponentStyle("label-primary"))
      }
    }
  }
  
  /**
   * Show authentication dialog (Facebook-style) with all options
   */
  private def showAuthDialog(): Unit = {
    try {
      GuiUtils.mainStage match {
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
      GuiUtils.mainStage match {
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
