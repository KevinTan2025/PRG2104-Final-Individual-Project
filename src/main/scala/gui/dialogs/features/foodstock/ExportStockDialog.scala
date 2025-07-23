package gui.dialogs.features.foodstock

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.stage.{FileChooser, Stage}
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.scene.input.{Clipboard, ClipboardContent}
import java.io.{File, FileWriter}
import model.FoodStock

/**
 * Dialog for exporting food stock data with copy and download options
 */
class ExportStockDialog(stocks: List[FoodStock]) {
  
  private val csvContent = generateCSVContent(stocks)
  
  private val dialog = new Dialog[Unit]() {
    title = "Export Stock Data"
    headerText = "Food Stock Export"
    
    // Create custom content
    val textArea = new TextArea {
      text = csvContent
      editable = false
      prefRowCount = 15
      prefColumnCount = 80
      wrapText = false
      style = "-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;"
    }
    
    val copyButton = new Button("📋 Copy to Clipboard") {
      prefWidth = 150
      onAction = (_: ActionEvent) => {
        val clipboard = Clipboard.systemClipboard
        val content = new ClipboardContent()
        content.putString(csvContent)
        clipboard.setContent(content)
        
        // Show feedback
        text = "✅ Copied!"
        style = "-fx-background-color: #28a745; -fx-text-fill: white;"
        
        // Reset button after 2 seconds
        val timeline = new scalafx.animation.Timeline {
          keyFrames = Seq(
            scalafx.animation.KeyFrame(
              scalafx.util.Duration(2000),
              onFinished = _ => {
                text = "📋 Copy to Clipboard"
                style = ""
              }
            )
          )
        }
        timeline.play()
      }
    }
    
    val downloadButton = new Button("💾 Download CSV") {
      prefWidth = 150
      onAction = (_: ActionEvent) => handleDownload()
    }
    
    val buttonBox = new HBox {
      spacing = 15
      alignment = Pos.Center
      padding = Insets(15, 0, 0, 0)
      children = Seq(copyButton, downloadButton)
    }
    
    val infoLabel = new Label(s"📊 Total ${stocks.length} items ready for export") {
      style = "-fx-text-fill: #666; -fx-font-size: 12px;"
    }
    
    val scrollPane = new ScrollPane {
      content = textArea
      fitToWidth = true
      fitToHeight = true
      prefViewportWidth = 600
      prefViewportHeight = 350
    }
    
    val mainContent = new VBox {
      spacing = 15
      padding = Insets(20)
      children = Seq(
        infoLabel,
        scrollPane,
        buttonBox
      )
    }
    
    dialogPane().content = mainContent
    dialogPane().buttonTypes = Seq(ButtonType.Close)
  }
  
  private def generateCSVContent(stocks: List[FoodStock]): String = {
    val header = "Food Name,Category,Quantity,Unit,Status,Location,Expiry Date"
    val rows = stocks.map { stock =>
      val expiryStr = stock.expiryDate.map(_.toString).getOrElse("No expiry")
      s"\"${stock.foodName}\",\"${stock.category}\",${stock.currentQuantity},\"${stock.unit}\"," +
      s"\"${stock.getStockStatus}\",\"${stock.location}\",\"$expiryStr\""
    }
    
    (header :: rows).mkString("\n")
  }
  
  private def handleDownload(): Unit = {
    val fileChooser = new FileChooser {
      title = "Save Stock Data"
      extensionFilters.addAll(
        new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
        new FileChooser.ExtensionFilter("All Files", "*.*")
      )
      initialFileName = s"food_stock_export_${java.time.LocalDate.now()}.csv"
    }
    
    val selectedFile = fileChooser.showSaveDialog(dialog.dialogPane().scene().window())
    
    if (selectedFile != null) {
      try {
        val writer = new FileWriter(selectedFile)
        writer.write(csvContent)
        writer.close()
        
        // Show success message
        val alert = new Alert(Alert.AlertType.Information) {
          title = "Export Successful"
          headerText = "File Saved Successfully"
          contentText = s"Stock data has been saved to:\n${selectedFile.getAbsolutePath}"
        }
        alert.showAndWait()
        
      } catch {
        case e: Exception =>
          val alert = new Alert(Alert.AlertType.Error) {
            title = "Export Failed"
            headerText = "Failed to Save File"
            contentText = s"Error saving file: ${e.getMessage}"
          }
          alert.showAndWait()
      }
    }
  }
  
  def showAndWait(): Unit = {
    dialog.showAndWait()
  }
}
