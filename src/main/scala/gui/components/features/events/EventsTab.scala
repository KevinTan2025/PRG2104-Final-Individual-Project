package gui.components.features.events

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.dialogs.features.events.EventDialog
import gui.components.common.public.BaseTabComponent
import service.CommunityEngagementService


// This tab allows users to create, view, RSVP, cancel RSVP, and search for events.
// It also supports read-only mode for users who are not logged in.
class EventsTab(
  readOnlyMode: Boolean = false,
  onLoginPrompt: () => Unit = () => {}
) extends BaseTabComponent {
  
  private val eventsList = new ListView[String]()
  private val searchField = new TextField {
    promptText = "Search events..."
  }
  
  override def build(): Tab = {
    refreshEvents()
    
    val createEventButton = new Button("Create Event") {
      onAction = (_: ActionEvent) => {
        if (readOnlyMode) {
          onLoginPrompt()
        } else {
          val dialog = new EventDialog(() => refreshEvents())
          dialog.showAndWait()
        }
      }
    }
    
    val rsvpButton = new Button("RSVP to Event") {
      onAction = (_: ActionEvent) => {
        if (readOnlyMode) {
          onLoginPrompt()
        } else {
          handleRSVP()
        }
      }
    }
    
    val cancelRsvpButton = new Button("Cancel RSVP") {
      onAction = (_: ActionEvent) => {
        if (readOnlyMode) {
          onLoginPrompt()
        } else {
          handleCancelRSVP()
        }
      }
    }
    
    val viewMyEventsButton = new Button("My Events") {
      onAction = (_: ActionEvent) => {
        if (readOnlyMode) {
          onLoginPrompt()
        } else {
          handleViewMyEvents()
        }
      }
    }
    
    val viewAllButton = new Button("All Events") {
      onAction = (_: ActionEvent) => refreshEvents()
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => handleSearchEvents()
    }
    
    val refreshButton = new Button("Refresh") {
      onAction = (_: ActionEvent) => refreshEvents()
    }
    
    // Reading mode adjustments
    if (readOnlyMode) {
      createEventButton.text = "🔒 Login to Create Event"
      rsvpButton.text = "🔒 Login to RSVP"
      cancelRsvpButton.text = "🔒 Login to Cancel RSVP"
      viewMyEventsButton.text = "🔒 Login to View My Events"
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(createEventButton, rsvpButton, cancelRsvpButton, viewMyEventsButton, viewAllButton, refreshButton)
    }
    
    val searchControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(searchField, searchButton)
    }
    
    val mainContent = new BorderPane {
      top = new VBox {
        children = Seq(topControls, searchControls)
      }
      center = eventsList
    }
    
    new Tab {
      text = if (readOnlyMode) "📅 Events" else "Events"
      content = mainContent
      closable = false
    }
  }
  
  override def refresh(): Unit = {
    refreshEvents()
  }
  
  override def initialize(): Unit = {
    // Initial setup if needed
  }
  
  private def refreshEvents(): Unit = {
    val events = service.getUpcomingEvents
    val items = events.map(e => s"${e.title} - ${e.location} (${e.startDateTime.toLocalDate}) - ${e.participants.size} participants")
    eventsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def handleRSVP(): Unit = {
    val selectedIndex = eventsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val events = service.getUpcomingEvents
      if (selectedIndex < events.length) {
        val event = events(selectedIndex)
        if (service.rsvpToEvent(event.eventId)) {
          GuiUtils.showInfo("Success", "RSVP successful!")
          refreshEvents()
        } else {
          GuiUtils.showWarning("Failed", "Could not RSVP. Event may be full or you may already be registered.")
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select an event to RSVP.")
    }
  }
  
  private def handleCancelRSVP(): Unit = {
    val selectedIndex = eventsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val events = service.getUpcomingEvents
      if (selectedIndex < events.length) {
        val event = events(selectedIndex)
        if (service.cancelRsvp(event.eventId)) {
          GuiUtils.showInfo("Success", "RSVP cancelled successfully!")
          refreshEvents()
        } else {
          GuiUtils.showWarning("Failed", "Could not cancel RSVP.")
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select an event to cancel RSVP.")
    }
  }
  
  private def handleViewMyEvents(): Unit = {
    service.getCurrentUser match {
      case Some(user) =>
        val myEvents = service.getMyEvents(user.userId)
        val items = myEvents.map(e => s"${e.title} - ${e.location} (${e.startDateTime.toLocalDate}) - ${e.participants.size} participants")
        eventsList.items = scalafx.collections.ObservableBuffer(items: _*)
      case None =>
        GuiUtils.showWarning("Not Logged In", "Please log in to view your events.")
    }
  }
  
  private def handleSearchEvents(): Unit = {
    val searchTerm = searchField.text.value
    if (searchTerm.nonEmpty) {
      val results = service.searchEvents(searchTerm)
      val items = results.map(e => s"${e.title} - ${e.location} (${e.startDateTime.toLocalDate}) - ${e.participants.size} participants")
      eventsList.items = scalafx.collections.ObservableBuffer(items: _*)
    } else {
      refreshEvents()
    }
  }
}
