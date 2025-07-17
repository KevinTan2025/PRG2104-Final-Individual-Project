package gui.components

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.dialogs.{AnnouncementDialog, CommentDialog}
import gui.utils.GuiUtils

/**
 * Announcements tab component
 */
class AnnouncementsTab extends BaseTabComponent {
  
  private val announcementsList = new ListView[String]()
  
  override def build(): Tab = {
    // 初始加载数据
    refreshAnnouncements()
    
    val createButton = new Button("Create Announcement") {
      onAction = (_: ActionEvent) => {
        val dialog = new AnnouncementDialog(() => refreshAnnouncements())
        dialog.showAndWait()
      }
    }
    
    val searchField = new TextField {
      promptText = "Search announcements..."
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => searchAnnouncements()
    }
    
    val addCommentButton = new Button("Add Comment") {
      onAction = (_: ActionEvent) => addComment()
    }
    
    val likeButton = new Button("Like") {
      onAction = (_: ActionEvent) => likeAnnouncement()
    }
    
    val refreshButton = new Button("Refresh") {
      onAction = (_: ActionEvent) => refreshAnnouncements()
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(createButton, searchField, searchButton, addCommentButton, likeButton, refreshButton)
    }
    
    val tabContent = new BorderPane {
      top = topControls
      center = announcementsList
    }
    
    new Tab {
      text = "Announcements"
      content = tabContent
      closable = false
    }
  }
  
  override def refresh(): Unit = {
    refreshAnnouncements()
  }
  
  private def refreshAnnouncements(): Unit = {
    val announcements = service.getAnnouncements
    val items = announcements.map(a => s"[${a.announcementType}] ${a.title} - ${a.timestamp.toLocalDate}")
    announcementsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def searchAnnouncements(): Unit = {
    // Implementation for search
  }
  
  private def addComment(): Unit = {
    val selectedIndex = announcementsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val announcements = service.getAnnouncements
      if (selectedIndex < announcements.length) {
        val announcement = announcements(selectedIndex)
        val dialog = new CommentDialog(announcement.title, (comment) => {
          if (service.addCommentToAnnouncement(announcement.announcementId, comment)) {
            GuiUtils.showInfo("Success", "Comment added successfully!")
          } else {
            GuiUtils.showError("Failed", "Could not add comment.")
          }
        })
        dialog.showAndWait()
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select an announcement to comment on.")
    }
  }
  
  private def likeAnnouncement(): Unit = {
    val selectedIndex = announcementsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val announcements = service.getAnnouncements
      if (selectedIndex < announcements.length) {
        val announcement = announcements(selectedIndex)
        if (service.likeAnnouncement(announcement.announcementId)) {
          GuiUtils.showInfo("Success", "Announcement liked!")
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select an announcement to like.")
    }
  }
  
  override def initialize(): Unit = {
    refreshAnnouncements()
  }
}
