package gui.dialogs.features.discussion

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.stage.{Modality, Stage}
import scalafx.event.ActionEvent
import scalafx.beans.property.ObjectProperty
import scalafx.Includes._
import model.{DiscussionTopic, Reply}
import service.CommunityEngagementService
import gui.utils.GuiUtils
import java.time.format.DateTimeFormatter

/**
 * Dialog for displaying discussion topic details with replies and interactions
 */
class DiscussionTopicDetailsDialog(topic: DiscussionTopic, onUpdate: () => Unit = () => {}) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  private val currentTopicProperty = ObjectProperty[DiscussionTopic](topic)
  
  def showAndWait(): Unit = {
    initializeDialog()
    dialog.showAndWait()
  }
  
  private def initializeDialog(): Unit = {
    dialog.title = s"Discussion: ${currentTopicProperty.value.title}"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = true
    dialog.minWidth = 600
    dialog.minHeight = 500
    
    // Header with topic info
    val headerBox = new VBox {
      spacing = 10
      padding = Insets(20)
      style = "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;"
      
      children = Seq(
        new Label(currentTopicProperty.value.title) {
          style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        },
        new Label(s"Category: ${currentTopicProperty.value.category}") {
          style = "-fx-text-fill: #6c757d; -fx-font-size: 12px;"
        },
        new Label(s"Posted by: ${currentTopicProperty.value.authorId} on ${currentTopicProperty.value.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}") {
          style = "-fx-text-fill: #6c757d; -fx-font-size: 12px;"
        },
        new Label(s"👍 ${currentTopicProperty.value.likes} likes • 💬 ${currentTopicProperty.value.getReplyCount} replies") {
          style = "-fx-text-fill: #495057; -fx-font-weight: bold;"
        }
      )
    }
    
    // Topic description
    val descriptionBox = new VBox {
      spacing = 10
      padding = Insets(20)
      children = Seq(
        new Label("Description:") {
          style = "-fx-font-weight: bold; -fx-font-size: 14px;"
        },
        new Label(currentTopicProperty.value.description) {
          wrapText = true
          style = "-fx-font-size: 13px; -fx-text-fill: #212529;"
        }
      )
    }
    
    // Replies section
    val repliesBox = new VBox {
      spacing = 15
      padding = Insets(20)
    }
    
    repliesBox.children.add(new Label(s"Replies (${currentTopicProperty.value.getReplyCount}):") {
      style = "-fx-font-weight: bold; -fx-font-size: 14px;"
    })
    
    if (currentTopicProperty.value.replies.nonEmpty) {
      val repliesScrollPane = new ScrollPane {
        prefHeight = 200
        fitToWidth = true
        
        content = new VBox {
          spacing = 10
          children = currentTopicProperty.value.replies.reverse.map(createReplyItem)
        }
      }
      repliesBox.children.add(repliesScrollPane)
    } else {
      repliesBox.children.add(new Label("No replies yet. Be the first to reply!") {
        style = "-fx-text-fill: #6c757d; -fx-font-style: italic;"
      })
    }
    
    // Action buttons
    val buttonBox = createActionButtons()
    
    val mainContent = new BorderPane {
      top = headerBox
      center = new ScrollPane {
        content = new VBox {
          children = Seq(descriptionBox, new Separator(), repliesBox)
        }
        fitToWidth = true
      }
      bottom = buttonBox
    }
    
    dialog.scene = new Scene(mainContent, 650, 700)
  }
  
  private def refreshDialog(): Unit = {
    // Update current topic data
    service.discussionTopics.find(_.topicId == currentTopicProperty.value.topicId) match {
      case Some(updatedTopic) =>
        currentTopicProperty.value = updatedTopic
        initializeDialog() // Reinitialize the dialog with updated data
      case None =>
        GuiUtils.showError("Error", "Topic not found.")
    }
  }
  
  private def createReplyItem(reply: Reply): VBox = {
    new VBox {
      spacing = 5
      padding = Insets(10)
      style = "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;"
      
      children = Seq(
        new HBox {
          spacing = 10
          alignment = Pos.CenterLeft
          children = Seq(
            new Label(reply.authorId) {
              style = "-fx-font-weight: bold; -fx-font-size: 12px;"
            },
            new Label(reply.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) {
              style = "-fx-text-fill: #6c757d; -fx-font-size: 11px;"
            },
            new Label(s"👍 ${reply.likes}") {
              style = "-fx-text-fill: #495057; -fx-font-size: 11px;"
            }
          )
        },
        new Label(reply.content) {
          wrapText = true
          style = "-fx-font-size: 13px; -fx-text-fill: #212529;"
        }
      )
    }
  }
  
  private def createActionButtons(): HBox = {
    val likeButton = new Button("👍 Like Topic") {
      onAction = _ => {
        service.getCurrentUser match {
          case Some(_) =>
            if (service.likeTopic(currentTopicProperty.value.topicId)) {
              GuiUtils.showInfo("Success", "Topic liked!")
              onUpdate()
              refreshDialog()
            } else {
              GuiUtils.showError("Error", "Failed to like topic.")
            }
          case None =>
            GuiUtils.showWarning("Login Required", "Please log in to like topics.")
        }
      }
      style = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 6;"
    }
    
    val replyButton = new Button("💬 Add Reply") {
      onAction = _ => {
        service.getCurrentUser match {
          case Some(_) =>
            showReplyDialog()
          case None =>
            GuiUtils.showWarning("Login Required", "Please log in to add replies.")
        }
      }
      style = "-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 6;"
    }
    
    val refreshButton = new Button("🔄 Refresh") {
      onAction = _ => {
        onUpdate()
        refreshDialog()
      }
      style = "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 6;"
    }
    
    val closeButton = new Button("Close") {
      onAction = _ => dialog.close()
      style = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 6;"
    }
    
    new HBox {
      spacing = 10
      padding = Insets(20)
      alignment = Pos.CenterRight
      children = Seq(likeButton, replyButton, refreshButton, closeButton)
    }
  }
  
  private def showReplyDialog(): Unit = {
    val replyDialog = new Dialog[String]()
    replyDialog.title = "Add Reply"
    replyDialog.headerText = s"Reply to: ${currentTopicProperty.value.title}"
    
    val contentArea = new TextArea {
      promptText = "Your reply..."
      prefRowCount = 5
      prefWidth = 400
      wrapText = true
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Reply:"), 0, 0)
      add(contentArea, 0, 1)
    }
    
    replyDialog.dialogPane().content = grid
    replyDialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    
    replyDialog.resultConverter = dialogButton => {
      if (dialogButton == ButtonType.OK && contentArea.text.value.trim.nonEmpty) {
        contentArea.text.value.trim
      } else {
        null
      }
    }
    
    replyDialog.showAndWait() match {
      case Some(replyContent: String) =>
        if (service.addReplyToTopic(currentTopicProperty.value.topicId, replyContent)) {
          GuiUtils.showInfo("Success", "Reply added successfully!")
          onUpdate()
          refreshDialog()
        } else {
          GuiUtils.showError("Error", "Failed to add reply.")
        }
      case Some(_) => // Invalid type returned
        GuiUtils.showError("Error", "Invalid reply content.")
      case None => // User cancelled or entered empty content
    }
  }
}
