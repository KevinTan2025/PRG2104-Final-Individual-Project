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
    dialog.title = if (isEditing) "编辑库存商品 - Edit Food Stock Item" else "添加新库存 - Add New Food Stock"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = true
    dialog.minWidth = 500
    dialog.minHeight = 600
    
    val foodNameField = new EnhancedTextField("Food item name") { 
      text = existingStock.map(_.foodName).getOrElse("")
    }
    
    val categoryCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer(FoodCategory.values.map(_.toString): _*)
      value = existingStock.map(_.category.toString).getOrElse("OTHER")
    }
    
    val quantityField = new EnhancedTextField("Current quantity") { 
      text = existingStock.map(_.currentQuantity.toString).getOrElse("")
    }
    
    val unitField = new EnhancedTextField("Unit (kg, pieces, bottles, etc.)") { 
      text = existingStock.map(_.unit).getOrElse("")
    }
    
    val thresholdField = new EnhancedTextField("Minimum threshold") { 
      text = existingStock.map(_.minimumThreshold.toString).getOrElse("")
    }
    
    val locationField = new EnhancedTextField("Storage location") {
      text = existingStock.map(_.location).getOrElse("Main Storage")
    }
    
    val isPackagedCheck = new CheckBox("Is packaged food") {
      selected = existingStock.map(_.isPackaged).getOrElse(false)
    }
    
    val hasExpiryCheck = new CheckBox("Has expiry date") {
      selected = existingStock.flatMap(_.expiryDate).isDefined
    }
    
    val expiryDaysField = new EnhancedTextField("Days until expiry") { 
      disable = !hasExpiryCheck.selected.value
      text = existingStock.flatMap(_.expiryDate).map { expiry =>
        val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiry)
        math.max(0, days).toString
      }.getOrElse("")
    }
    
    hasExpiryCheck.onAction = _ => {
      expiryDaysField.disable = !hasExpiryCheck.selected.value
    }
    
    val notesArea = new TextArea {
      promptText = "Notes (optional)"
      prefRowCount = 2
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
        
        val expiryDate = if (hasExpiryCheck.selected.value && expiryDaysField.text.value.nonEmpty) {
          try {
            val days = expiryDaysField.text.value.toLong
            Some(LocalDateTime.now().plusDays(days))
          } catch {
            case _: NumberFormatException => 
              GuiUtils.showError("Validation Error", "Invalid expiry days.")
              return
          }
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
    }
    
    val cancelButton = new Button("Cancel") {
      onAction = _ => dialog.close()
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Food Name:"), 0, 0)
      add(foodNameField, 1, 0)
      add(new Label("Category:"), 0, 1)
      add(categoryCombo, 1, 1)
      add(new Label("Current Quantity:"), 0, 2)
      add(quantityField, 1, 2)
      add(new Label("Unit:"), 0, 3)
      add(unitField, 1, 3)
      add(new Label("Minimum Threshold:"), 0, 4)
      add(thresholdField, 1, 4)
      add(new Label("Location:"), 0, 5)
      add(locationField, 1, 5)
      add(isPackagedCheck, 0, 6)
      add(new Label(""), 1, 6)
      add(hasExpiryCheck, 0, 7)
      add(expiryDaysField, 1, 7)
      add(new Label("Notes:"), 0, 8)
      add(notesArea, 1, 8)
      add(new HBox {
        spacing = 10
        children = Seq(saveButton, cancelButton)
      }, 1, 9)
    }
    
    dialog.scene = new Scene(grid, 550, 600)
    dialog.showAndWait()
  }
}
