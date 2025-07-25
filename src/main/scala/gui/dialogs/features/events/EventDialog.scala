package gui.dialogs.features.events

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import service.CommunityEngagementService
import gui.utils.GuiUtils
import gui.components.common.public.EnhancedTextField
import gui.components.common.datepicker.{EnhancedDatePicker, EnhancedTimePicker}
import java.time.LocalDateTime

/**
 * Dialog for creating events
 */
class EventDialog(onSuccess: () => Unit) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  
  def showAndWait(): Unit = {
    dialog.title = "Create Event"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = true
    dialog.minWidth = 600
    dialog.minHeight = 600
    
    val titleField = new EnhancedTextField("Event title") {
      prefWidth = 350
    }
    val descriptionArea = new TextArea { 
      promptText = "Description"
      prefRowCount = 3
      prefWidth = 350
      wrapText = true
    }
    val locationField = new EnhancedTextField("Event location") {
      prefWidth = 350
    }
    val startDatePicker = new EnhancedDatePicker()
    val startTimePicker = new EnhancedTimePicker()
    val endDatePicker = new EnhancedDatePicker()
    val endTimePicker = new EnhancedTimePicker()
    val maxParticipantsField = new EnhancedTextField("Max participants (optional)") {
      prefWidth = 350
    }
    
    val createButton = new Button("Create") {
      onAction = _ => {
        try {
          // Validate that dates and times are selected
          val startDate = startDatePicker.getValue
          val startTime = startTimePicker.getValue
          val endDate = endDatePicker.getValue
          val endTime = endTimePicker.getValue
          
          if (startDate.isEmpty || startTime.isEmpty || endDate.isEmpty || endTime.isEmpty) {
            GuiUtils.showError("Missing Information", "Please select all dates and times.")
          } else {
            // Combine dates and times
            val startDateTime = startDate.get.atTime(startTime.get)
            val endDateTime = endDate.get.atTime(endTime.get)
            
            // Validate that end time is after start time
            if (!endDateTime.isAfter(startDateTime)) {
              GuiUtils.showError("Invalid Time", "End time must be after start time.")
            } else {
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
            }
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
      vgap = 15
      padding = Insets(20)
      
      add(new Label("Title:") {
        style = "-fx-font-weight: bold;"
      }, 0, 0)
      add(titleField, 1, 0)
      
      add(new Label("Location:") {
        style = "-fx-font-weight: bold;"
      }, 0, 1)
      add(locationField, 1, 1)
      
      add(new Label("Start Date:") {
        style = "-fx-font-weight: bold;"
      }, 0, 2)
      add(startDatePicker.control, 1, 2)
      
      add(new Label("Start Time:") {
        style = "-fx-font-weight: bold;"
      }, 0, 3)
      add(startTimePicker.control, 1, 3)
      
      add(new Label("End Date:") {
        style = "-fx-font-weight: bold;"
      }, 0, 4)
      add(endDatePicker.control, 1, 4)
      
      add(new Label("End Time:") {
        style = "-fx-font-weight: bold;"
      }, 0, 5)
      add(endTimePicker.control, 1, 5)
      
      add(new Label("Max Participants:") {
        style = "-fx-font-weight: bold;"
      }, 0, 6)
      add(maxParticipantsField, 1, 6)
      
      add(new Label("Description:") {
        style = "-fx-font-weight: bold;"
      }, 0, 7)
      add(descriptionArea, 1, 7)
      
      add(new HBox {
        spacing = 10
        children = Seq(createButton, cancelButton)
      }, 1, 8)
    }
    
    dialog.scene = new Scene(grid, 600, 600)
    dialog.showAndWait()
  }
}
