package gui.dialogs.common

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene

/**
 * Dialog for adding comments
 */
class CommentDialog(itemTitle: String, onSuccess: (String) => Unit) {
  
  private val dialog = new Stage()
  
  def showAndWait(): Unit = {
    dialog.title = "Add Comment"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = false
    
    val contentArea = new TextArea { 
      promptText = "Your comment..."
      prefRowCount = 3
      prefWidth = 400
    }
    
    val addButton = new Button("Add Comment") {
      onAction = _ => {
        val comment = contentArea.text.value
        if (comment.nonEmpty) {
          onSuccess(comment)
          dialog.close()
        }
      }
    }
    
    val cancelButton = new Button("Cancel") {
      onAction = _ => dialog.close()
    }
    
    val content = new VBox {
      spacing = 10
      padding = Insets(20)
      children = Seq(
        new Label(s"Comment on: $itemTitle") {
          style = "-fx-font-weight: bold;"
        },
        contentArea,
        new HBox {
          spacing = 10
          children = Seq(addButton, cancelButton)
        }
      )
    }
    
    dialog.scene = new Scene(content, 450, 200)
    dialog.showAndWait()
  }
}
