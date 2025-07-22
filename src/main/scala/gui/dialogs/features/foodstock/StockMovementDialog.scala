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
import model.{FoodStock, StockActionType}

/**
 * Dialog for managing stock movements (add/remove stock)
 */
class StockMovementDialog(
  stock: FoodStock,
  onSuccess: () => Unit
) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  
  def getStatusColor(status: String): String = status match {
    case "IN_STOCK" => "#28a745"
    case "LOW_STOCK" => "#ffc107"
    case "OUT_OF_STOCK" => "#dc3545"
    case "EXPIRED" => "#6c757d"
    case _ => "#000000"
  }
  
  def showAndWait(): Unit = {
    dialog.title = s"Manage Stock: ${stock.foodName}"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = false
    
    // Current stock info
    val currentStockLabel = new Label(s"Current Stock: ${stock.currentQuantity} ${stock.unit}")
    currentStockLabel.style = "-fx-font-weight: bold; -fx-font-size: 14px;"
    
    val stockStatusLabel = new Label(s"Status: ${stock.getStockStatus}")
    stockStatusLabel.style = s"-fx-text-fill: ${getStatusColor(stock.getStockStatus.toString)};"
    
    val actionCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer("Add Stock", "Remove Stock", "Adjust Stock")
      value = "Add Stock"
    }
    
    val quantityField = new TextField { 
      promptText = "Quantity"
    }
    
    val notesArea = new TextArea {
      promptText = "Notes (optional)"
      prefRowCount = 3
    }
    
    // Update labels based on action type
    actionCombo.onAction = _ => {
      actionCombo.value.value match {
        case "Add Stock" => 
          quantityField.promptText = "Quantity to add"
        case "Remove Stock" => 
          quantityField.promptText = "Quantity to remove"
        case "Adjust Stock" => 
          quantityField.promptText = "New total quantity"
      }
    }
    
    def handleStockMovement(): Unit = {
      try {
        val quantity = quantityField.text.value.toDouble
        val notes = notesArea.text.value.trim
        val action = actionCombo.value.value
        
        if (quantity < 0) {
          GuiUtils.showError("Validation Error", "Quantity must be non-negative.")
          return
        }
        
        service.getCurrentUser match {
          case Some(user) =>
            val success = action match {
              case "Add Stock" =>
                service.addStock(stock.stockId, quantity, user.userId, notes)
                
              case "Remove Stock" =>
                if (quantity > stock.currentQuantity) {
                  val confirm = GuiUtils.showConfirmation(
                    "Insufficient Stock",
                    s"Requested quantity ($quantity) exceeds current stock (${stock.currentQuantity}). " +
                    "This will result in negative stock. Continue anyway?"
                  )
                  if (!confirm) return
                }
                service.removeStock(stock.stockId, quantity, user.userId, notes)
                
              case "Adjust Stock" =>
                if (quantity < stock.currentQuantity) {
                  val diff = stock.currentQuantity - quantity
                  val confirm = GuiUtils.showConfirmation(
                    "Stock Reduction",
                    s"This will reduce stock by $diff ${stock.unit}. Continue?"
                  )
                  if (!confirm) return
                }
                service.adjustStock(stock.stockId, quantity, user.userId, notes)
                
              case _ => false
            }
            
            if (success) {
              GuiUtils.showInfo("Success", s"Stock movement executed successfully!")
              onSuccess()
              dialog.close()
            } else {
              GuiUtils.showError("Error", "Failed to execute stock movement.")
            }
            
          case None =>
            GuiUtils.showError("Error", "No current user found.")
        }
      } catch {
        case e: NumberFormatException =>
          GuiUtils.showError("Validation Error", "Please enter a valid number for quantity.")
        case e: Exception =>
          GuiUtils.showError("Error", s"An error occurred: ${e.getMessage}")
      }
    }

    val executeButton = new Button("Execute") {
      onAction = _ => handleStockMovement()
    }
    
    val cancelButton = new Button("Cancel") {
      onAction = _ => dialog.close()
    }
    
    val stockInfoBox = new VBox {
      spacing = 5
      padding = Insets(10)
      style = "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;"
      children = Seq(
        new Label(s"Food Item: ${stock.foodName}") {
          style = "-fx-font-weight: bold;"
        },
        currentStockLabel,
        stockStatusLabel,
        new Label(s"Minimum Threshold: ${stock.minimumThreshold} ${stock.unit}"),
        new Label(s"Location: ${stock.location}"),
        stock.expiryDate.map { expiry =>
          val daysLeft = stock.getDaysUntilExpiry.getOrElse(0)
          new Label(s"Expires in: $daysLeft days") {
            style = if (daysLeft <= 7) "-fx-text-fill: #dc3545;" else "-fx-text-fill: #28a745;"
          }
        }.getOrElse(new Label("No expiry date"))
      )
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(stockInfoBox, 0, 0)
      GridPane.setColumnSpan(stockInfoBox, 2)
      
      add(new Label("Action:"), 0, 1)
      add(actionCombo, 1, 1)
      add(new Label("Quantity:"), 0, 2)
      add(quantityField, 1, 2)
      add(new Label("Notes:"), 0, 3)
      add(notesArea, 1, 3)
      add(new HBox {
        spacing = 10
        children = Seq(executeButton, cancelButton)
      }, 1, 4)
    }
    
    dialog.scene = new Scene(grid, 450, 400)
    dialog.showAndWait()
  }
}
