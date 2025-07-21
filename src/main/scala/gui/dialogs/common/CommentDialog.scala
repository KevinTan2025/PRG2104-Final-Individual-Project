package gui.dialogs.common

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}

/**
 * Enhanced dialog for adding comments with better UX
 */
class CommentDialog(itemTitle: String, onSuccess: (String) => Unit) {
  
  private val dialog = new Stage()
  
  def showAndWait(): Unit = {
    dialog.title = "Add Comment"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = false
    
    val contentArea = new TextArea { 
      promptText = "Share your thoughts... (Be respectful and constructive)"
      prefRowCount = 4
      prefWidth = 450
      wrapText = true
      style = "-fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;"
    }
    
    val charCountLabel = new Label("0/500 characters") {
      font = Font.font("System", 11)
      textFill = Color.web("#8a8d91")
    }
    
    // Update character count
    contentArea.text.onChange { (_, _, newValue) =>
      val length = newValue.length
      charCountLabel.text = s"$length/500 characters"
      charCountLabel.textFill = if (length > 500) Color.Red else Color.web("#8a8d91")
    }
    
    val addButton = new Button("💬 Post Comment") {
      style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16;"
      font = Font.font("System", FontWeight.Bold, 14)
      onAction = _ => {
        val comment = contentArea.text.value.trim
        if (comment.nonEmpty && comment.length <= 500) {
          onSuccess(comment)
          dialog.close()
        } else if (comment.isEmpty) {
          showValidationError("Please enter a comment before submitting.")
        } else {
          showValidationError("Comment is too long. Please keep it under 500 characters.")
        }
      }
    }
    
    val cancelButton = new Button("Cancel") {
      style = "-fx-background-color: #e4e6ea; -fx-text-fill: #65676b; -fx-background-radius: 6; -fx-padding: 8 16;"
      font = Font.font("System", 14)
      onAction = _ => dialog.close()
    }
    
    val content = new VBox {
      spacing = 15
      padding = Insets(25)
      children = Seq(
        new Label(s"💬 Comment on: $itemTitle") {
          font = Font.font("System", FontWeight.Bold, 16)
          textFill = Color.web("#1c1e21")
          wrapText = true
        },
        new Label("Share your thoughts and engage with the community:") {
          font = Font.font("System", 12)
          textFill = Color.web("#65676b")
        },
        contentArea,
        charCountLabel,
        new Label("💡 Tip: Be respectful and constructive in your comments") {
          font = Font.font("System", 11)
          textFill = Color.web("#65676b")
          style = "-fx-background-color: #f0f8ff; -fx-padding: 8; -fx-background-radius: 4;"
        },
        new HBox {
          spacing = 10
          alignment = Pos.CenterRight
          children = Seq(cancelButton, addButton)
        }
      )
    }
    
    dialog.scene = new Scene(content, 500, 400)
    dialog.showAndWait()
  }
  
  private def showValidationError(message: String): Unit = {
    val alert = new Alert(Alert.AlertType.Warning) {
      title = "Invalid Comment"
      headerText = ""
      contentText = message
    }
    alert.showAndWait()
  }
}
