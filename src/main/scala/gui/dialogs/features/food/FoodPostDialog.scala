package gui.dialogs.features.food

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import service.CommunityEngagementService
import gui.utils.GuiUtils
import gui.components.common.public.EnhancedTextField
import gui.components.common.datepicker.EnhancedDatePicker
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
    dialog.resizable = true
    dialog.minWidth = 550
    dialog.minHeight = 500
    
    val titleField = new EnhancedTextField("Post title") {
      prefWidth = 300
    }
    val descriptionArea = new TextArea { 
      promptText = "Description"
      prefRowCount = 3
      prefWidth = 300
      wrapText = true
    }
    val quantityField = new EnhancedTextField("Quantity") {
      prefWidth = 300
    }
    val locationField = new EnhancedTextField("Location") {
      prefWidth = 300
    }
    
    val typeCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer("OFFER", "REQUEST")
      value = "OFFER"
      prefWidth = 300
    }
    
    val expiryCheck = new CheckBox("Has expiry date")
    val expiryDatePicker = new EnhancedDatePicker()
    expiryDatePicker.control.disable = true
    
    expiryCheck.onAction = _ => {
      expiryDatePicker.control.disable = !expiryCheck.selected.value
    }
    
    val createButton = new Button("Create") {
      onAction = _ => {
        import model.FoodPostType
        
        val postType = if (typeCombo.value.value == "OFFER") FoodPostType.OFFER else FoodPostType.REQUEST
        val expiryDate = if (expiryCheck.selected.value && expiryDatePicker.getValue.isDefined) {
          Some(expiryDatePicker.getValue.get.atStartOfDay())
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
      vgap = 15
      padding = Insets(20)
      
      add(new Label("Title:") {
        style = "-fx-font-weight: bold;"
      }, 0, 0)
      add(titleField, 1, 0)
      
      add(new Label("Type:") {
        style = "-fx-font-weight: bold;"
      }, 0, 1)
      add(typeCombo, 1, 1)
      
      add(new Label("Quantity:") {
        style = "-fx-font-weight: bold;"
      }, 0, 2)
      add(quantityField, 1, 2)
      
      add(new Label("Location:") {
        style = "-fx-font-weight: bold;"
      }, 0, 3)
      add(locationField, 1, 3)
      
      add(new Label("Description:") {
        style = "-fx-font-weight: bold;"
      }, 0, 4)
      add(descriptionArea, 1, 4)
      
      add(expiryCheck, 0, 5)
      add(expiryDatePicker.control, 1, 5)
      
      add(new HBox {
        spacing = 10
        children = Seq(createButton, cancelButton)
      }, 1, 6)
    }
    
    dialog.scene = new Scene(grid, 550, 500)
    dialog.showAndWait()
  }
}
