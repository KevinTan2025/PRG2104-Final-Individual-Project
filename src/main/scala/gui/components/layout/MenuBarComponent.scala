package gui.components.layout

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Menu, MenuBar, MenuItem}
import javafx.event.ActionEvent
import scalafx.scene.layout.Region
import scalafx.Includes._
import gui.utils.{GuiUtils, ThemeManager}
import gui.components.common.public.BaseComponent
import gui.dialogs.common.ProfileDialog
import gui.dialogs.admin.AdminDialogs
import service.CommunityEngagementService

/**
 * Menu bar component with user info and logout functionality
 * Security Level: USER - Menu bar for registered users
 */
class MenuBarComponent(
  onLogout: () => Unit
) extends BaseComponent {
  
  // FXML injected controls
  @FXML private var menuBar: MenuBar = _
  @FXML private var menuFile: Menu = _
  @FXML private var menuTheme: Menu = _
  @FXML private var menuUser: Menu = _
  @FXML private var menuItemLogout: MenuItem = _
  @FXML private var menuItemExit: MenuItem = _
  @FXML private var menuItemToggleTheme: MenuItem = _
  @FXML private var menuItemLightMode: MenuItem = _
  @FXML private var menuItemDarkMode: MenuItem = _
  @FXML private var menuItemProfile: MenuItem = _
  @FXML private var menuItemSettings: MenuItem = _
  
  override def build(): Region = {
    val loader = new FXMLLoader(getClass.getResource("/gui/components/layout/MenuBarComponent.fxml"))
    loader.setController(this)
    val root = loader.load[MenuBar]()
    
    // Update user menu text with current user info
    updateUserInfo()
    
    // Convert JavaFX MenuBar to ScalaFX Region
    new Region(root)
  }
  
  /**
   * Updates the user menu text with current user information
   */
  private def updateUserInfo(): Unit = {
    val userInfo = service.currentUserInfo match {
      case Some(user) => s"${user.name} (${user.userRole})"
      case None => "Guest"
    }
    menuUser.setText(userInfo)
  }
  
  // FXML Event Handlers
  @FXML
  private def handleLogout(event: ActionEvent): Unit = {
    service.logout()
    GuiUtils.showInfo("Logged Out", "You have been logged out successfully.")
    onLogout()
  }
  
  @FXML
  private def handleExit(event: ActionEvent): Unit = {
    GuiUtils.mainStage.foreach(_.close())
  }
  
  @FXML
  private def handleProfile(event: ActionEvent): Unit = {
    new ProfileDialog().showAndWait()
  }
  
  @FXML
  private def handleSettings(event: ActionEvent): Unit = {
    GuiUtils.showInfo("Settings", "Settings coming soon!")
  }
  
  // Theme related event handlers
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
   * Apply theme to current scene
   */
  private def applyThemeToCurrentScene(): Unit = {
    GuiUtils.mainStage.foreach { stage =>
      val scene = stage.scene.value
      if (scene != null) {
        // Use ThemeManager's CSS theme system
        ThemeManager.applyThemeToScene(scene)
      }
    }
  }
  
  /**
   * Recursively update all component styles
   */
  private def updateAllComponentStyles(node: javafx.scene.Node): Unit = {
    updateAllComponentStyles(node, Set.empty, 0)
  }
  
  /**
   * Recursively update all component styles with visited tracking
   */
  private def updateAllComponentStyles(node: javafx.scene.Node, visited: Set[javafx.scene.Node], depth: Int): Unit = {
    // Prevent infinite recursion and excessive depth
    if (visited.contains(node) || depth > 50) return
    val newVisited = visited + node
    
    // Update current node style
    node match {
      case region: javafx.scene.layout.Region =>
        // Set background color and text color
        val backgroundStyle = ThemeManager.getComponentStyle("background")
        val textStyle = ThemeManager.getComponentStyle("label-primary")
        region.setStyle(s"$backgroundStyle $textStyle")
      case label: javafx.scene.control.Label =>
        // Update label text color
        label.setStyle(ThemeManager.getComponentStyle("label-primary"))
      case button: javafx.scene.control.Button =>
        // Update button style
        button.setStyle(ThemeManager.getComponentStyle("button"))
      case menuBar: javafx.scene.control.MenuBar =>
        // Update menu bar style
        val backgroundStyle = ThemeManager.getComponentStyle("background")
        val textStyle = ThemeManager.getComponentStyle("label-primary")
        menuBar.setStyle(s"$backgroundStyle $textStyle")
        // Update menu items in menu bar
        updateMenuBarTheme(menuBar)
        // MenuBar already specially handled, no need to continue recursing child nodes
        return
      case textField: javafx.scene.control.TextField =>
        // Update text field style
        textField.setStyle(ThemeManager.getInputFieldStyle)
      case _ =>
    }
    
    // Recursively update child nodes
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
   * Update menu bar theme
   */
  private def updateMenuBarTheme(menuBar: javafx.scene.control.MenuBar): Unit = {
    import scala.jdk.CollectionConverters._
    menuBar.getMenus.asScala.foreach { menu =>
      // Update menu style - Menu doesn't inherit from Node, needs special handling
      menu.setStyle(ThemeManager.getComponentStyle("label-primary"))
      // Update menu items
      menu.getItems.asScala.foreach { menuItem =>
        menuItem.setStyle(ThemeManager.getComponentStyle("label-primary"))
      }
    }
  }
}
