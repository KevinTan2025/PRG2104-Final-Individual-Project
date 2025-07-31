package gui.components.layout

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Menu, MenuBar, MenuItem}
import javafx.event.ActionEvent
import scalafx.scene.layout.Region
import scalafx.Includes._
import gui.utils.GuiUtils
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
  @FXML private var menuUser: Menu = _
  @FXML private var menuItemLogout: MenuItem = _
  @FXML private var menuItemExit: MenuItem = _
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
    val userInfo = service.getCurrentUser match {
      case Some(user) => s"${user.name} (${user.getUserRole})"
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
    GuiUtils.getMainStage.foreach(_.close())
  }
  
  @FXML
  private def handleProfile(event: ActionEvent): Unit = {
    new ProfileDialog().showAndWait()
  }
  
  @FXML
  private def handleSettings(event: ActionEvent): Unit = {
    GuiUtils.showInfo("Settings", "Settings coming soon!")
  }
}
