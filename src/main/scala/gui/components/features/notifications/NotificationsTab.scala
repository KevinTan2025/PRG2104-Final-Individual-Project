package gui.components.features.notifications

import scalafx.scene.control.{Tab => ScalaFXTab, TabPane => ScalaFXTabPane, Button, Label, ListView, ScrollPane, TextField, ComboBox, CheckBox, TextArea, Separator, Dialog, ButtonType}
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.BaseTabComponent
import service.CommunityEngagementService

/**
 * Notifications tab component for managing user notifications
 * 安全级别: USER - 注册用户可以查看和管理通知
 */
class NotificationsTab extends BaseTabComponent {
  
  private val notificationsList = new ListView[String]()
  
  override def build(): ScalaFXTab = {
    refreshNotifications()
    
    val markReadButton = new Button("Mark as Read") {
      onAction = (_: ActionEvent) => handleMarkAsRead()
    }
    
    val markAllReadButton = new Button("Mark All as Read") {
      onAction = (_: ActionEvent) => handleMarkAllAsRead()
    }
    
    val viewUnreadButton = new Button("Unread Only") {
      onAction = (_: ActionEvent) => handleViewUnread()
    }
    
    val viewAllButton = new Button("All Notifications") {
      onAction = (_: ActionEvent) => refreshNotifications()
    }
    
    val deleteButton = new Button("Delete Selected") {
      onAction = (_: ActionEvent) => handleDeleteNotification()
    }
    
    val viewDetailsButton = new Button("View Details") {
      onAction = (_: ActionEvent) => handleViewDetails()
    }
    
    val refreshButton = new Button("Refresh") {
      onAction = (_: ActionEvent) => refreshNotifications()
    }
    
    // Stats display
    val unreadCount = service.getUnreadNotificationCount
    val statsLabel = new Label(s"Unread notifications: $unreadCount") {
      style = "-fx-font-weight: bold; -fx-text-fill: #2196F3;"
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(markReadButton, markAllReadButton, viewUnreadButton, viewAllButton, deleteButton, viewDetailsButton, refreshButton)
    }
    
    val statsBox = new HBox {
      padding = Insets(10)
      children = Seq(statsLabel)
    }
    
    val mainContent = new BorderPane {
      top = new VBox {
        children = Seq(statsBox, topControls)
      }
      center = notificationsList
    }
    
    new ScalaFXTab {
      text = s"Notifications ($unreadCount)"
      content = mainContent
      closable = false
    }
  }
  
  override def refresh(): Unit = {
    refreshNotifications()
  }
  
  override def initialize(): Unit = {
    // Initial setup if needed
  }
  
  private def refreshNotifications(): Unit = {
    val notifications = service.getNotifications
    val items = notifications.map { n =>
      val readStatus = if (n.isRead) "✓" else "●"
      s"$readStatus [${n.notificationType}] ${n.title} - ${n.timestamp.toLocalDate}"
    }
    notificationsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def handleMarkAsRead(): Unit = {
    val selectedIndex = notificationsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val notifications = service.getNotifications
      if (selectedIndex < notifications.length) {
        val notification = notifications(selectedIndex)
        if (service.markNotificationAsRead(notification.notificationId)) {
          refreshNotifications()
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select a notification to mark as read.")
    }
  }
  
  private def handleMarkAllAsRead(): Unit = {
    service.getCurrentUser match {
      case Some(user) =>
        val count = service.markAllNotificationsAsRead
        GuiUtils.showInfo("Success", s"Marked $count notifications as read.")
        refreshNotifications()
      case None =>
        GuiUtils.showWarning("Not Logged In", "Please log in to mark notifications as read.")
    }
  }
  
  private def handleViewUnread(): Unit = {
    val unread = service.getUnreadNotifications
    val items = unread.map { n =>
      val readStatus = if (n.isRead) "✓" else "●"
      s"$readStatus [${n.notificationType}] ${n.title} - ${n.timestamp.toLocalDate}"
    }
    notificationsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def handleDeleteNotification(): Unit = {
    val selectedIndex = notificationsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val notifications = service.getNotifications
      if (selectedIndex < notifications.length) {
        val notification = notifications(selectedIndex)
        if (service.deleteNotification(notification.notificationId)) {
          GuiUtils.showInfo("Success", "Notification deleted.")
          refreshNotifications()
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select a notification to delete.")
    }
  }
  
  private def handleViewDetails(): Unit = {
    val selectedIndex = notificationsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val notifications = service.getNotifications
      if (selectedIndex < notifications.length) {
        val notification = notifications(selectedIndex)
        showNotificationDetails(notification)
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select a notification to view details.")
    }
  }
  
  /**
   * Show notification details dialog
   */
  private def showNotificationDetails(notification: model.Notification): Unit = {
    val dialog = new Dialog[Unit]()
    dialog.title = "Notification Details"
    dialog.headerText = notification.title
    
    val detailsText = s"""
Type: ${notification.notificationType}
From: ${notification.senderId.getOrElse("System")}
Date: ${notification.timestamp}
Read: ${if (notification.isRead) "Yes" else "No"}
${if (notification.readAt.isDefined) s"Read at: ${notification.readAt.get}" else ""}

Message:
${notification.message}
"""
    
    val textArea = new TextArea {
      text = detailsText
      editable = false
      prefRowCount = 10
      prefWidth = 400
      wrapText = true
    }
    
    dialog.dialogPane().content = textArea
    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    
    dialog.showAndWait()
  }
}
