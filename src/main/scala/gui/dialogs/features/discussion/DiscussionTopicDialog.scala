package gui.dialogs.features.discussion

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import service.CommunityEngagementService
import gui.utils.GuiUtils
import gui.components.common.public.EnhancedTextField
import service.CommunityEngagementService
import model.DiscussionCategory

/**
 * Dialog for creating discussion topics
 */
class DiscussionTopicDialog(onSuccess: () => Unit) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  
  def showAndWait(): Unit = {
    dialog.title = "Create Discussion Topic"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = true
    dialog.minWidth = 550
    dialog.minHeight = 450
    
    val titleField = new EnhancedTextField("Discussion topic title") {
      prefWidth = 300
    }
    val contentArea = new TextArea { 
      promptText = "Topic content or question"
      prefRowCount = 5
      prefWidth = 300
      wrapText = true
    }
    
    val categoryCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer(
        "GENERAL", "NUTRITION", "SUSTAINABLE_AGRICULTURE", "FOOD_SECURITY", 
        "COMMUNITY_GARDEN", "COOKING_TIPS", "EVENTS", "ANNOUNCEMENTS"
      )
      value = "GENERAL"
      prefWidth = 300
    }
    
    val createButton = new Button("Create") {
      onAction = _ => {
        val title = titleField.text.value.trim
        val content = contentArea.text.value.trim
        val selectedCategory = categoryCombo.value.value
        
        if (title.isEmpty || content.isEmpty) {
          GuiUtils.showWarning("Missing Information", "Please fill in both title and content.")
        } else {
          service.currentUserInfo match {
            case Some(_) =>
              val category = selectedCategory match {
                case "NUTRITION" => DiscussionCategory.NUTRITION
                case "SUSTAINABLE_AGRICULTURE" => DiscussionCategory.SUSTAINABLE_AGRICULTURE
                case "FOOD_SECURITY" => DiscussionCategory.FOOD_SECURITY
                case "COMMUNITY_GARDEN" => DiscussionCategory.COMMUNITY_GARDEN
                case "COOKING_TIPS" => DiscussionCategory.COOKING_TIPS
                case "EVENTS" => DiscussionCategory.EVENTS
                case "ANNOUNCEMENTS" => DiscussionCategory.ANNOUNCEMENTS
                case _ => DiscussionCategory.GENERAL
              }
              
              service.createDiscussionTopic(title, content, category) match {
                case Some(_) =>
                  GuiUtils.showInfo("Success", "Discussion topic created successfully!")
                  onSuccess()
                  dialog.close()
                case None =>
                  GuiUtils.showError("Error", "Failed to create discussion topic.")
              }
            case None =>
              GuiUtils.showWarning("Login Required", "Please log in to create topics.")
          }
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
      
      add(new Label("Category:") {
        style = "-fx-font-weight: bold;"
      }, 0, 1)
      add(categoryCombo, 1, 1)
      
      add(new Label("Content:") {
        style = "-fx-font-weight: bold;"
      }, 0, 2)
      add(contentArea, 1, 2)
      
      add(new HBox {
        spacing = 10
        children = Seq(createButton, cancelButton)
      }, 1, 3)
    }
    
    dialog.scene = new Scene(grid, 550, 450)
    dialog.showAndWait()
  }
}
