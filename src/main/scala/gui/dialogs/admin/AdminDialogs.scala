package gui.dialogs.admin

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import service.CommunityEngagementService

/**
 * Admin dialogs for administrative functions
 */
object AdminDialogs {
  
  private val service = CommunityEngagementService.getInstance
  
  /**
   * Show users management dialog (admin only)
   */
  def showUsersDialog(): Unit = {
    val dialog = new Dialog[Unit]()
    dialog.title = "User Management"
    dialog.headerText = "All registered users"
    
    val usersList = new ListView[String]()
    val users = service.getAllUsers
    val items = users.map(u => s"${u.username} (${u.name}) - ${u.getUserRole} - ${if (u.isActive) "Active" else "Inactive"}")
    usersList.items = scalafx.collections.ObservableBuffer(items: _*)
    
    val vbox = new VBox {
      spacing = 10
      padding = Insets(20)
      children = Seq(
        new Label(s"Total Users: ${users.length}"),
        usersList
      )
      prefWidth = 500
      prefHeight = 400
    }
    
    dialog.dialogPane().content = vbox
    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    
    dialog.showAndWait()
  }
  
  /**
   * Show system statistics dialog (admin only)
   */
  def showSystemStatsDialog(): Unit = {
    val dialog = new Dialog[Unit]()
    dialog.title = "System Statistics"
    dialog.headerText = "Detailed platform statistics"
    
    val stats = service.getDashboardStatistics
    val detailedStats = service.getDetailedStatistics
    
    val statsText = s"""
Platform Overview:
- Total Users: ${stats("totalUsers")}
- Community Members: ${stats("communityMembers")}
- Administrators: ${stats("adminUsers")}

Content Statistics:
- Active Announcements: ${stats("activeAnnouncements")}
- Total Food Posts: ${stats("totalFoodPosts")}
- Active Food Posts: ${stats("activeFoodPosts")}
- Completed Food Posts: ${stats("completedFoodPosts")}

Events:
- Total Events: ${stats("totalEvents")}
- Upcoming Events: ${stats("upcomingEvents")}
- Completed Events: ${stats("completedEvents")}

User Activity:
- Total Notifications Sent: ${detailedStats("totalNotifications")}
- Total Comments: ${detailedStats("totalComments")}
- Total Likes: ${detailedStats("totalLikes")}
"""
    
    val textArea = new TextArea {
      text = statsText
      editable = false
      prefRowCount = 15
      prefWidth = 400
      wrapText = true
    }
    
    dialog.dialogPane().content = textArea
    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    
    dialog.showAndWait()
  }
  
  /**
   * Show content moderation dialog (admin only)
   */
  def showModerationDialog(): Unit = {
    val dialog = new Dialog[Unit]()
    dialog.title = "Content Moderation"
    dialog.headerText = "Moderate platform content"
    
    val contentList = new ListView[String]()
    val moderationItems = service.getContentForModeration
    val items = moderationItems.map { case (id, contentType, title) =>
      s"[$contentType] $title (ID: $id)"
    }
    contentList.items = scalafx.collections.ObservableBuffer(items: _*)
    
    val moderateButton = new Button("Moderate Selected") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = contentList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val moderationItems = service.getContentForModeration
          if (selectedIndex < moderationItems.length) {
            val (id, contentType, title) = moderationItems(selectedIndex)
            if (service.moderateContent(id, contentType)) {
              GuiUtils.showInfo("Success", s"Content '$title' moderated successfully!")
              // Refresh the list
              val updatedItems = service.getContentForModeration
              val newItems = updatedItems.map { case (id, contentType, title) =>
                s"[$contentType] $title (ID: $id)"
              }
              contentList.items = scalafx.collections.ObservableBuffer(newItems: _*)
            } else {
              GuiUtils.showError("Failed", "Could not moderate content.")
            }
          }
        } else {
          GuiUtils.showWarning("No Selection", "Please select content to moderate.")
        }
      }
    }
    
    val refreshButton = new Button("Refresh") {
      onAction = (_: ActionEvent) => {
        val moderationItems = service.getContentForModeration
        val items = moderationItems.map { case (id, contentType, title) =>
          s"[$contentType] $title (ID: $id)"
        }
        contentList.items = scalafx.collections.ObservableBuffer(items: _*)
      }
    }
    
    val buttonBox = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(moderateButton, refreshButton)
    }
    
    val vbox = new VBox {
      spacing = 10
      padding = Insets(20)
      children = Seq(
        new Label(s"Content requiring moderation: ${moderationItems.length}") {
          style = "-fx-font-weight: bold;"
        },
        contentList,
        buttonBox
      )
      prefWidth = 500
      prefHeight = 400
    }
    
    dialog.dialogPane().content = vbox
    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    
    dialog.showAndWait()
  }
}
