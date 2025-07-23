package gui.dialogs.features.food

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import scalafx.scene.text.{Font, FontWeight}
import model.{FoodPost, FoodPostStatus, FoodPostType}
import service.CommunityEngagementService
import gui.utils.GuiUtils
import java.time.format.DateTimeFormatter

/**
 * Dialog for displaying detailed information about a food post
 */
class FoodPostDetailsDialog(foodPost: FoodPost, onUpdate: () => Unit) {
  
  private val service = CommunityEngagementService.getInstance
  private val dialog = new Stage()
  
  def showAndWait(): Unit = {
    dialog.title = s"Food Post Details - ${foodPost.title}"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = true
    dialog.minWidth = 600
    dialog.minHeight = 500
    
    // Header with title and status
    val headerBox = new VBox {
      spacing = 10
      padding = Insets(20)
      style = "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;"
      children = Seq(
        new Label(foodPost.title) {
          font = Font.font("System", FontWeight.Bold, 24)
          style = "-fx-text-fill: #495057;"
        },
        new HBox {
          spacing = 15
          alignment = Pos.CenterLeft
          children = Seq(
            new Label(s"[${foodPost.postType}]") {
              style = s"-fx-background-color: ${if (foodPost.postType == FoodPostType.OFFER) "#28a745" else "#007bff"}; " +
                     "-fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15;"
              font = Font.font("System", FontWeight.Bold, 12)
            },
            new Label(s"Status: ${foodPost.status}") {
              style = s"-fx-background-color: ${getStatusColor(foodPost.status)}; " +
                     "-fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15;"
              font = Font.font("System", FontWeight.Bold, 12)
            }
          )
        }
      )
    }
    
    // Details section
    val detailsGrid = new GridPane {
      hgap = 15
      vgap = 15
      padding = Insets(20)
      
      add(createDetailLabel("Quantity:"), 0, 0)
      add(createDetailValue(foodPost.quantity), 1, 0)
      
      add(createDetailLabel("Location:"), 0, 1)
      add(createDetailValue(foodPost.location), 1, 1)
      
      add(createDetailLabel("Posted:"), 0, 2)
      add(createDetailValue(foodPost.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))), 1, 2)
      
      if (foodPost.expiryDate.isDefined) {
        add(createDetailLabel("Expires:"), 0, 3)
        add(createDetailValue(foodPost.expiryDate.get.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))), 1, 3)
      }
      
      if (foodPost.acceptedBy.isDefined) {
        add(createDetailLabel("Accepted by:"), 0, 4)
        add(createDetailValue(s"User ${foodPost.acceptedBy.get}"), 1, 4)
      }
      
      if (foodPost.acceptedDate.isDefined) {
        add(createDetailLabel("Accepted on:"), 0, 5)
        add(createDetailValue(foodPost.acceptedDate.get.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))), 1, 5)
      }
    }
    
    // Description section
    val descriptionBox = new VBox {
      spacing = 10
      padding = Insets(20)
      children = Seq(
        new Label("Description:") {
          font = Font.font("System", FontWeight.Bold, 16)
          style = "-fx-text-fill: #495057;"
        },
        new TextArea {
          text = foodPost.description
          editable = false
          wrapText = true
          prefRowCount = 4
          style = "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6;"
        }
      )
    }
    
    // Comments section
    val commentsBox = new VBox {
      spacing = 10
      padding = Insets(20)
      children = Seq(
        new Label("Comments:") {
          font = Font.font("System", FontWeight.Bold, 16)
          style = "-fx-text-fill: #495057;"
        }
      )
    }
    
    // Add comments to the box
    if (foodPost.comments.nonEmpty) {
      val commentsList = new ListView[String] {
        items = scalafx.collections.ObservableBuffer(
          foodPost.comments.map(c => s"${c.authorId}: ${c.content}"): _*
        )
        prefHeight = 150
      }
      commentsBox.children.add(commentsList)
    } else {
      commentsBox.children.add(new Label("No comments yet.") {
        style = "-fx-text-fill: #6c757d; -fx-font-style: italic;"
      })
    }
    
    // Action buttons
    val buttonBox = new HBox {
      spacing = 10
      padding = Insets(20)
      alignment = Pos.CenterRight
      children = createActionButtons()
    }
    
    val mainContent = new BorderPane {
      top = headerBox
      center = new ScrollPane {
        content = new VBox {
          children = Seq(detailsGrid, descriptionBox, commentsBox)
        }
        fitToWidth = true
      }
      bottom = buttonBox
    }
    
    dialog.scene = new Scene(mainContent, 600, 700)
    dialog.showAndWait()
  }
  
  private def createDetailLabel(text: String): Label = {
    new Label(text) {
      font = Font.font("System", FontWeight.Bold, 14)
      style = "-fx-text-fill: #495057;"
    }
  }
  
  private def createDetailValue(text: String): Label = {
    new Label(text) {
      font = Font.font("System", 14)
      style = "-fx-text-fill: #6c757d;"
      wrapText = true
    }
  }
  
  private def getStatusColor(status: FoodPostStatus): String = {
    status match {
      case FoodPostStatus.PENDING => "#ffc107"
      case FoodPostStatus.ACCEPTED => "#28a745"
      case FoodPostStatus.COMPLETED => "#6c757d"
      case FoodPostStatus.CANCELLED => "#dc3545"
    }
  }
  
  private def createActionButtons(): Seq[Button] = {
    val closeButton = new Button("Close") {
      onAction = _ => dialog.close()
      style = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 6;"
    }
    
    val buttons = scala.collection.mutable.ListBuffer[Button](closeButton)
    
    // Add Accept button if post is pending and user is logged in
    if (foodPost.status == FoodPostStatus.PENDING && service.getCurrentUser.isDefined) {
      val acceptButton = new Button("Accept Post") {
        onAction = _ => {
          if (service.acceptFoodPost(foodPost.postId)) {
            GuiUtils.showInfo("Success", "Food post accepted successfully!")
            onUpdate()
            dialog.close()
          } else {
            GuiUtils.showError("Error", "Failed to accept food post.")
          }
        }
        style = "-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 6;"
      }
      buttons.prepend(acceptButton)
    }
    
    // Add Complete button if post is accepted
    if (foodPost.status == FoodPostStatus.ACCEPTED && service.getCurrentUser.isDefined) {
      val completeButton = new Button("Mark Complete") {
        onAction = _ => {
          if (service.completeFoodPost(foodPost.postId)) {
            GuiUtils.showInfo("Success", "Food post marked as completed!")
            onUpdate()
            dialog.close()
          } else {
            GuiUtils.showError("Error", "Failed to complete food post.")
          }
        }
        style = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 6;"
      }
      buttons.insert(buttons.length - 1, completeButton)
    }
    
    buttons.toSeq
  }
}
