package gui.components.features.announcements

import scalafx.scene.control.{Tab => ScalaFXTab, TabPane => ScalaFXTabPane, Button, Label, ListView, ScrollPane, TextField, ComboBox, CheckBox, TextArea, Separator}
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.dialogs.features.announcements.AnnouncementDialog
import gui.dialogs.common.CommentDialog
import gui.components.common.public.{BaseTabComponent, SimpleComponentBuilder}
import service.CommunityEngagementService



/**
  * Minimalist Announcements Tab Component
  * Provides a simple interface for viewing, creating, searching, commenting, and liking announcements.
  * Uses SimpleComponentBuilder for consistent UI components.
  */

class SimpleAnnouncementsTab extends BaseTabComponent {
  
  // Customized list view for announcements
  private val announcementsList = SimpleComponentBuilder.listView[String](650, 450)
  private val searchField = SimpleComponentBuilder.searchBox("Search announcements...", 250)

  // Button Definitions - Unified API
  private val createBtn = SimpleComponentBuilder.button("Create", handleCreate)
  private val refreshBtn = SimpleComponentBuilder.button("Refresh", handleRefresh)
  private val searchBtn = SimpleComponentBuilder.button("Search", handleSearch)
  private val commentBtn = SimpleComponentBuilder.button("Comment", handleComment)
  private val likeBtn = SimpleComponentBuilder.button("Like", handleLike)
  
  override def build(): ScalaFXTab = {
    // Initial data loading
    handleRefresh()

    // Create layout
    val buttons = new HBox(10) {
      children = List(createBtn, refreshBtn, commentBtn, likeBtn)
      padding = Insets(10)
    }
    
    val searchRow = new HBox(10) {
      children = List(searchField, searchBtn)
      padding = Insets(10)
    }
    
    val layoutContent = new VBox(10) {
      children = List(buttons, searchRow, announcementsList)
      padding = Insets(10)
    }
    
    new ScalaFXTab {
      text = "Announcements"
      content = layoutContent
      closable = false
    }
  }
  
  // Handles the refresh action to load announcements

  private def handleRefresh(): Unit = {
    val announcements = service.getAnnouncements
    val items = announcements.map(a => s"[${a.announcementType}] ${a.title} - ${a.timestamp}")
    announcementsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def handleCreate(): Unit = {
    val dialog = new AnnouncementDialog(() => handleRefresh())
    dialog.showAndWait()
  }
  
  private def handleSearch(): Unit = {
    val searchTerm = searchField.text.value
    if (searchTerm.nonEmpty) {
      val results = service.searchAnnouncements(searchTerm)
      val items = results.map(a => s"[${a.announcementType}] ${a.title} - ${a.timestamp}")
      announcementsList.items = scalafx.collections.ObservableBuffer(items: _*)
    } else {
      handleRefresh()
    }
  }
  
  private def handleComment(): Unit = {
    val selectedIndex = announcementsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val announcements = service.getAnnouncements
      if (selectedIndex < announcements.length) {
        val announcement = announcements(selectedIndex)
        val dialog = new CommentDialog(announcement.title, (_: String) => handleRefresh())
        dialog.showAndWait()
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select an announcement to comment on.")
    }
  }
  
  private def handleLike(): Unit = {
    val selectedIndex = announcementsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val announcements = service.getAnnouncements
      if (selectedIndex < announcements.length) {
        val announcement = announcements(selectedIndex)
        if (service.likeAnnouncement(announcement.announcementId)) {
          GuiUtils.showInfo("Success", "Announcement liked!")
          handleRefresh()
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select an announcement to like.")
    }
  }
  
  override def refresh(): Unit = handleRefresh()
  override def initialize(): Unit = {}
}
