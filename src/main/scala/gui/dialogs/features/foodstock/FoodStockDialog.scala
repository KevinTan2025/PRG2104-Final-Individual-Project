package gui.dialogs.features.foodstock

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import scalafx.event.ActionEvent
import scalafx.Includes._
import service.CommunityEngagementService
import gui.utils.GuiUtils
import gui.components.common.public.EnhancedTextField
import gui.components.common.datepicker.EnhancedDatePicker
import model.{FoodCategory, FoodStock}
import java.time.LocalDateTime
import java.util.UUID

/**
 * Dialog for creating or editing food stock items
 */
class FoodStockDialog(
  existingStock: Option[FoodStock] = None,
  onSuccess: () => Unit
) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  private val isEditing = existingStock.isDefined
  
  def showAndWait(): Unit = {
    dialog.title = if (isEditing) "Edit Food Stock Item" else "添加新库存 - Add New Food Stock"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = true
    dialog.minWidth = 650
    dialog.minHeight = 700
    
    val foodNameField = new EnhancedTextField("Food item name") { 
      text = existingStock.map(_.foodName).getOrElse("")
      prefWidth = 350
    }
    
    val categoryCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer(FoodCategory.values.map(_.toString): _*)
      value = existingStock.map(_.category.toString).getOrElse("OTHER")
      prefWidth = 350
    }
    
    val quantityField = new EnhancedTextField("Current quantity") { 
      text = existingStock.map(_.currentQuantity.toString).getOrElse("")
      prefWidth = 350
    }
    
    val unitField = new EnhancedTextField("Unit (kg, pieces, bottles, etc.)") { 
      text = existingStock.map(_.unit).getOrElse("")
      prefWidth = 350
    }
    
    val thresholdField = new EnhancedTextField("Minimum threshold") { 
      text = existingStock.map(_.minimumThreshold.toString).getOrElse("")
      prefWidth = 350
    }
    
    val locationField = new EnhancedTextField("Storage location") {
      text = existingStock.map(_.location).getOrElse("Main Storage")
      prefWidth = 350
    }
    
    val isPackagedCheck = new CheckBox("Is packaged food") {
      selected = existingStock.map(_.isPackaged).getOrElse(false)
      style = "-fx-font-size: 14px;"
    }
    
    val hasExpiryCheck = new CheckBox("Has expiry date") {
      selected = existingStock.flatMap(_.expiryDate).isDefined
      style = "-fx-font-size: 14px;"
    }
    
    val expiryDatePicker = new EnhancedDatePicker()
    expiryDatePicker.control.disable = !hasExpiryCheck.selected.value
    // Set existing expiry date if available
    existingStock.flatMap(_.expiryDate).foreach { expiry =>
      expiryDatePicker.setValue(expiry.toLocalDate)
    }
    
    hasExpiryCheck.onAction = _ => {
      expiryDatePicker.control.disable = !hasExpiryCheck.selected.value
    }
    
    val notesArea = new TextArea {
      promptText = "Notes (optional)"
      prefRowCount = 3
      prefWidth = 350
      wrapText = true
      text = if (isEditing) "Stock item updated" else "Initial stock entry"
    }
    
    def handleSave(): Unit = {
      try {
        val foodName = foodNameField.text.value.trim
        val category = FoodCategory.valueOf(categoryCombo.value.value)
        val quantity = quantityField.text.value.toDouble
        val unit = unitField.text.value.trim
        val threshold = thresholdField.text.value.toDouble
        val location = locationField.text.value.trim
        val isPackaged = isPackagedCheck.selected.value
        val notes = notesArea.text.value.trim
        
        // Validate inputs
        if (foodName.isEmpty || unit.isEmpty || location.isEmpty) {
          GuiUtils.showError("Validation Error", "Please fill in all required fields.")
          return
        }
        
        if (quantity < 0 || threshold < 0) {
          GuiUtils.showError("Validation Error", "Quantity and threshold must be non-negative.")
          return
        }
        
        val expiryDate = if (hasExpiryCheck.selected.value && expiryDatePicker.getValue.isDefined) {
          Some(expiryDatePicker.getValue.get.atStartOfDay())
        } else None
        
        val success = if (isEditing) {
          val currentStock = existingStock.get
          val updatedStock = currentStock.copy(
            foodName = foodName,
            category = category,
            unit = unit,
            minimumThreshold = threshold,
            expiryDate = expiryDate,
            isPackaged = isPackaged,
            location = location
          )
          service.updateFoodStock(updatedStock)
        } else {
          val foodStock = FoodStock(
            stockId = UUID.randomUUID().toString,
            foodName = foodName,
            category = category,
            currentQuantity = quantity,
            unit = unit,
            minimumThreshold = threshold,
            expiryDate = expiryDate,
            isPackaged = isPackaged,
            location = location,
            lastModifiedBy = None,
            lastModifiedDate = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
          )
          service.createFoodStock(foodStock)
        }
        
        if (success) {
          GuiUtils.showInfo("Success", 
            if (isEditing) "Stock item updated successfully!" 
            else "Stock item added successfully!"
          )
          onSuccess()
          dialog.close()
        } else {
          GuiUtils.showError("Error", "Failed to save stock item.")
        }
        
      } catch {
        case e: NumberFormatException =>
          GuiUtils.showError("Validation Error", "Please enter valid numbers for quantity and threshold.")
        case e: IllegalArgumentException =>
          GuiUtils.showError("Validation Error", "Please select a valid category.")
        case e: Exception =>
          GuiUtils.showError("Error", s"An error occurred: ${e.getMessage}")
      }
    }

    val saveButton = new Button(if (isEditing) "Update" else "Add") {
      onAction = _ => handleSave()
      style = "-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 6;"
      prefWidth = 100
    }
    
    val cancelButton = new Button("Cancel") {
      onAction = _ => dialog.close()
      style = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 6;"
      prefWidth = 100
    }
    
    val grid = new GridPane {
      hgap = 15
      vgap = 15
      padding = Insets(25)
      
      // Helper function to create styled labels
      def createLabel(text: String): Label = new Label(text) {
        style = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #495057;"
        prefWidth = 180
      }
      
      add(createLabel("Food Name:"), 0, 0)
      add(foodNameField, 1, 0)
      
      add(createLabel("Category:"), 0, 1)
      add(categoryCombo, 1, 1)
      
      add(createLabel("Current Quantity:"), 0, 2)
      add(quantityField, 1, 2)
      
      add(createLabel("Unit:"), 0, 3)
      add(unitField, 1, 3)
      
      add(createLabel("Minimum Threshold:"), 0, 4)
      add(thresholdField, 1, 4)
      
      add(createLabel("Location:"), 0, 5)
      add(locationField, 1, 5)
      
      add(createLabel("Packaging:"), 0, 6)
      add(isPackagedCheck, 1, 6)
      
      add(createLabel("Expiry Date:"), 0, 7)
      add(new VBox {
        spacing = 5
        children = Seq(hasExpiryCheck, expiryDatePicker.control)
      }, 1, 7)
      
      add(createLabel("Notes:"), 0, 8)
      add(notesArea, 1, 8)
      
      add(new Label(), 0, 9) // Empty cell for spacing
      add(new HBox {
        spacing = 15
        alignment = scalafx.geometry.Pos.CenterRight
        children = Seq(saveButton, cancelButton)
      }, 1, 9)
    }
    
    dialog.scene = new Scene(grid, 650, 700)
    dialog.showAndWait()
  }
}
