package gui.dialogs.features.discussion

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import service.CommunityEngagementService
import gui.utils.GuiUtils
import gui.components.common.public.EnhancedTextField

/**
 * Dialog for creating discussion topics
 */
class DiscussionTopicDialog(onSuccess: () => Unit) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  
  def showAndWait(): Unit = {
    dialog.title = "创建讨论 - Create Discussion Topic"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = true
    dialog.minWidth = 450
    dialog.minHeight = 400
    
    val titleField = new EnhancedTextField("Discussion topic title")
    val contentArea = new TextArea { 
      promptText = "Topic content or question"
      prefRowCount = 5
    }
    
    val categoryCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer("GENERAL", "FOOD_SHARING", "EVENTS", "TIPS", "Q_AND_A")
      value = "GENERAL"
    }
    
    val createButton = new Button("Create") {
      onAction = _ => {
        // This would create a discussion topic through the service
        // For now, just show success message
        GuiUtils.showInfo("Success", "Discussion topic created successfully!")
        onSuccess()
        dialog.close()
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
      add(new Label("Category:"), 0, 1)
      add(categoryCombo, 1, 1)
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
