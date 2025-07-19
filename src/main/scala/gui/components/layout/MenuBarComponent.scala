package gui.components.layout

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.BaseComponent
import gui.dialogs.{ProfileDialog, AdminDialogs}
import service.CommunityEngagementService

/**
 * Menu bar component with user info and logout functionality
 * 安全级别: USER - 注册用户的菜单栏
 */
class MenuBarComponent(
  onLogout: () => Unit
) extends BaseComponent {
  
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
