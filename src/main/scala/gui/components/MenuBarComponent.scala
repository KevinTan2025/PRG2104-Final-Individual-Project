package gui.components

import scalafx.scene.control._
import scalafx.scene.layout.Region
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.dialogs.{ProfileDialog, AdminDialogs}

/**
 * Menu bar component
 */
class MenuBarComponent(onLogout: () => Unit) extends BaseComponent {
  
  override def build(): Region = {
    val fileMenu = new Menu("File") {
      items = Seq(
        new MenuItem("Logout") {
          onAction = (_: ActionEvent) => {
            service.logout()
            GuiUtils.showInfo("Logged Out", "You have been logged out successfully.")
            onLogout()
          }
        },
        new SeparatorMenuItem(),
        new MenuItem("Exit") {
          onAction = (_: ActionEvent) => {
            GuiUtils.getMainStage.foreach(_.close())
          }
        }
      )
    }
    
    val userInfo = service.getCurrentUser match {
      case Some(user) => s"${user.name} (${user.getUserRole})"
      case None => "Guest"
    }
    
    val userMenu = new Menu(userInfo) {
      items = Seq(
        new MenuItem("Profile") {
          onAction = (_: ActionEvent) => {
            new ProfileDialog().showAndWait()
          }
        },
        new MenuItem("Settings") {
          onAction = (_: ActionEvent) => {
            GuiUtils.showInfo("Settings", "Settings coming soon!")
          }
        }
      )
    }
    
    new MenuBar {
      menus = Seq(fileMenu, userMenu)
    }
  }
}
