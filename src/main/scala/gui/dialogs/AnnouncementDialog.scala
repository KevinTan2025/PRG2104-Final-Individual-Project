package gui.dialogs

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import service.CommunityEngagementService
import gui.utils.GuiUtils

/**
 * Dialog for creating announcements
 */
class AnnouncementDialog(onSuccess: () => Unit) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  
  def showAndWait(): Unit = {
    dialog.title = "Create Announcement"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = false
    
    val titleField = new TextField { promptText = "Announcement title" }
    val contentArea = new TextArea { 
      promptText = "Announcement content"
      prefRowCount = 5
    }
    
    val typeCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer("GENERAL", "FOOD_DISTRIBUTION", "EVENTS", "TIPS", "EMERGENCY")
      value = "GENERAL"
    }
    
    val createButton = new Button("Create") {
      onAction = _ => {
        import model.AnnouncementType
        val announcementType = typeCombo.value.value match {
          case "FOOD_DISTRIBUTION" => AnnouncementType.FOOD_DISTRIBUTION
          case "EVENTS" => AnnouncementType.EVENTS
          case "TIPS" => AnnouncementType.TIPS
          case "EMERGENCY" => AnnouncementType.EMERGENCY
          case _ => AnnouncementType.GENERAL
        }
        
        if (service.createAnnouncement(titleField.text.value, contentArea.text.value, announcementType).isDefined) {
          GuiUtils.showInfo("Success", "Announcement created successfully!")
          onSuccess()
          dialog.close()
        } else {
          GuiUtils.showError("Error", "Failed to create announcement.")
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
      add(new Label("Type:"), 0, 1)
      add(typeCombo, 1, 1)
      add(new Label("Content:"), 0, 2)
      add(contentArea, 1, 2)
      add(new HBox {
        spacing = 10
        children = Seq(createButton, cancelButton)
      }, 1, 3)
    }
    
    dialog.scene = new Scene(grid, 400, 300)
    dialog.showAndWait()
  }
}
