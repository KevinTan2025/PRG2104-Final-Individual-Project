package gui.dialogs.features.events

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import service.CommunityEngagementService
import gui.utils.GuiUtils
import gui.components.common.public.EnhancedTextField
import java.time.LocalDateTime

/**
 * Dialog for creating events
 */
class EventDialog(onSuccess: () => Unit) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  
  def showAndWait(): Unit = {
    dialog.title = "创建活动 - Create Event"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = true
    dialog.minWidth = 550
    dialog.minHeight = 500
    
    val titleField = new EnhancedTextField("Event title")
    val descriptionArea = new TextArea { 
      promptText = "Description"
      prefRowCount = 3
    }
    val locationField = new EnhancedTextField("Event location")
    val startDateField = new EnhancedTextField("Start date (YYYY-MM-DD)")
    val startTimeField = new EnhancedTextField("Start time (HH:MM)")
    val endDateField = new EnhancedTextField("End date (YYYY-MM-DD)")
    val endTimeField = new EnhancedTextField("End time (HH:MM)")
    val maxParticipantsField = new EnhancedTextField("Max participants (optional)")
    
    val createButton = new Button("Create") {
      onAction = _ => {
        try {
          // Parse dates and times
          val startDateTime = LocalDateTime.parse(s"${startDateField.text.value}T${startTimeField.text.value}")
          val endDateTime = LocalDateTime.parse(s"${endDateField.text.value}T${endTimeField.text.value}")
          
          val maxParticipants = if (maxParticipantsField.text.value.nonEmpty) {
            Some(maxParticipantsField.text.value.toInt)
          } else None
          
          if (service.createEvent(
            titleField.text.value,
            descriptionArea.text.value,
            locationField.text.value,
            startDateTime,
            endDateTime,
            maxParticipants
          ).isDefined) {
            GuiUtils.showInfo("Success", "Event created successfully!")
            onSuccess()
            dialog.close()
          } else {
            GuiUtils.showError("Error", "Failed to create event.")
          }
        } catch {
          case e: Exception =>
            GuiUtils.showError("Error", s"Invalid date/time format: ${e.getMessage}")
        }
      }
    }
    
    val cancelButton = new Button("Cancel") {
      onAction = _ => dialog.close()
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Title:"), 0, 0)
      add(titleField, 1, 0)
      add(new Label("Location:"), 0, 1)
      add(locationField, 1, 1)
      add(new Label("Start Date:"), 0, 2)
      add(startDateField, 1, 2)
      add(new Label("Start Time:"), 0, 3)
      add(startTimeField, 1, 3)
      add(new Label("End Date:"), 0, 4)
      add(endDateField, 1, 4)
      add(new Label("End Time:"), 0, 5)
      add(endTimeField, 1, 5)
      add(new Label("Max Participants:"), 0, 6)
      add(maxParticipantsField, 1, 6)
      add(new Label("Description:"), 0, 7)
      add(descriptionArea, 1, 7)
      add(new HBox {
        spacing = 10
        children = Seq(createButton, cancelButton)
      }, 1, 8)
    }
    
    dialog.scene = new Scene(grid, 400, 500)
    dialog.showAndWait()
  }
}
