package gui.dialogs.features.food

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import service.CommunityEngagementService
import gui.utils.GuiUtils
import java.time.LocalDateTime

/**
 * Dialog for creating food posts
 */
class FoodPostDialog(onSuccess: () => Unit) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  
  def showAndWait(): Unit = {
    dialog.title = "Create Food Post"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = false
    
    val titleField = new TextField { promptText = "Post title" }
    val descriptionArea = new TextArea { 
      promptText = "Description"
      prefRowCount = 3
    }
    val quantityField = new TextField { promptText = "Quantity" }
    val locationField = new TextField { promptText = "Location" }
    
    val typeCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer("OFFER", "REQUEST")
      value = "OFFER"
    }
    
    val expiryCheck = new CheckBox("Has expiry date")
    val expiryPicker = new TextField { 
      promptText = "Days until expiry"
      disable = true
    }
    
    expiryCheck.onAction = _ => {
      expiryPicker.disable = !expiryCheck.selected.value
    }
    
    val createButton = new Button("Create") {
      onAction = _ => {
        import model.FoodPostType
        
        val postType = if (typeCombo.value.value == "OFFER") FoodPostType.OFFER else FoodPostType.REQUEST
        val expiryDate = if (expiryCheck.selected.value && expiryPicker.text.value.nonEmpty) {
          try {
            Some(LocalDateTime.now().plusDays(expiryPicker.text.value.toLong))
          } catch {
            case _: Exception => None
          }
        } else None
        
        if (service.createFoodPost(
          titleField.text.value,
          descriptionArea.text.value,
          postType,
          quantityField.text.value,
          locationField.text.value,
          expiryDate
        ).isDefined) {
          GuiUtils.showInfo("Success", "Food post created successfully!")
          onSuccess()
          dialog.close()
        } else {
          GuiUtils.showError("Error", "Failed to create food post.")
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
      add(new Label("Quantity:"), 0, 2)
      add(quantityField, 1, 2)
      add(new Label("Location:"), 0, 3)
      add(locationField, 1, 3)
      add(new Label("Description:"), 0, 4)
      add(descriptionArea, 1, 4)
      add(expiryCheck, 0, 5)
      add(expiryPicker, 1, 5)
      add(new HBox {
        spacing = 10
        children = Seq(createButton, cancelButton)
      }, 1, 6)
    }
    
    dialog.scene = new Scene(grid, 400, 400)
    dialog.showAndWait()
  }
}
