package gui.components.features.activityfeed

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import model._
import service.{CommunityEngagementService, ActivityFeedService}
import gui.dialogs.common.CommentDialog
import gui.utils.GuiUtils
import java.time.format.DateTimeFormatter

/**
 * Enhanced Activity Feed Component with real database integration
 * Displays all content in a unified feed without duplicate filtering
 */
class EnhancedActivityFeedComponent(
  private val service: CommunityEngagementService,
  private val onRefresh: () => Unit = () => {},
  private val contentFilter: Option[ActivityFeedType] = None
) {
  
  private val activityFeedService = new ActivityFeedService()
  private var feedContainer: VBox = _
  
  def build(): VBox = {
    val mainContainer = new VBox {
      spacing = 20
      padding = Insets(20)
      prefWidth = 600
    }
    
    val header = createHeader()
    feedContainer = new VBox {
      spacing = 15
      id = "feed-container"
    }
    
    // Initial load
    refreshFeed(feedContainer)
    
    mainContainer.children = Seq(header, feedContainer)
    mainContainer
  }
  
  /**
   * Public method to refresh the activity feed
   */
  def refresh(): Unit = {
    if (feedContainer != null) {
      refreshFeed(feedContainer)
    }
  }
  
  private def createHeader(): VBox = {
    val titleText = contentFilter match {
      case Some(ActivityFeedType.ANNOUNCEMENT) => "📢 Announcements"
      case Some(ActivityFeedType.FOOD_SHARING) => "🍕 Food Sharing Posts"
      case Some(ActivityFeedType.EVENT) => "📅 Community Events"
      case Some(ActivityFeedType.DISCUSSION) => "💬 Discussion Topics"
      case _ => "🔥 Community Activity Feed"
    }
    
    val subtitleText = contentFilter match {
      case Some(ActivityFeedType.ANNOUNCEMENT) => "Latest community announcements and updates"
      case Some(ActivityFeedType.FOOD_SHARING) => "Share and discover food in your community"
      case Some(ActivityFeedType.EVENT) => "Upcoming events and activities"
      case Some(ActivityFeedType.DISCUSSION) => "Join the conversation in our forums"
      case _ => "Stay connected with your community"
    }
    
    new VBox {
      spacing = 10
      children = Seq(
        new Label(titleText) {
          font = Font.font("System", FontWeight.Bold, 20)
          textFill = Color.web("#1877f2")
        },
        new Label(subtitleText) {
          font = Font.font("System", 14)
          textFill = Color.web("#65676b")
        }
      )
    }
  }
  
  private def refreshFeed(container: VBox): Unit = {
    container.children.clear()
    
    try {
      val feedItems = contentFilter match {
        case Some(filterType) => activityFeedService.getActivityFeedByType(filterType, 20, service.getCurrentUser.map(_.userId))
        case None => activityFeedService.getActivityFeed(20, service.getCurrentUser.map(_.userId))
      }
      
      if (feedItems.isEmpty) {
        container.children.add(createEmptyState())
      } else {
        feedItems.foreach { item =>
          container.children.add(createActivityFeedCard(item))
        }
      }
    } catch {
      case e: Exception =>
        println(s"Error refreshing feed: ${e.getMessage}")
        container.children.add(createErrorState())
    }
  }
  
  private def createActivityFeedCard(item: ActivityFeedItem): VBox = {
    val card = new VBox {
      spacing = 12
      padding = Insets(16)
      style = "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);"
    }
    
    // Header with category and time
    val header = new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new Label(s"${item.getCategoryIcon} ${item.getCategoryName}") {
          font = Font.font("System", FontWeight.Bold, 13)
          textFill = Color.web(item.getCategoryColor)
        },
        new Region { HBox.setHgrow(this, scalafx.scene.layout.Priority.Always) },
        new Label(item.getTimeAgo) {
          font = Font.font("System", 11)
          textFill = Color.web("#8a8d91")
        }
      )
    }
    
    // Title
    val title = new Label(item.title) {
      font = Font.font("System", FontWeight.Bold, 16)
      textFill = Color.web("#1c1e21")
      wrapText = true
    }
    
    // Content
    val content = new Label(item.content) {
      font = Font.font("System", 14)
      textFill = Color.web("#050505")
      wrapText = true
    }
    
    // Author info
    val authorInfo = new HBox {
      spacing = 5
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("by") {
          font = Font.font("System", 12)
          textFill = Color.web("#65676b")
        },
        new Label(item.authorName) {
          font = Font.font("System", FontWeight.Bold, 12)
          textFill = Color.web("#1877f2")
        }
      )
    }
    
    // Additional info based on type
    val additionalInfo = createAdditionalInfo(item)
    
    // Interaction buttons
    val interactions = createInteractionButtons(item)
    
    // Add all components
    val components = Seq(header, title, content, authorInfo) ++ 
      (if (additionalInfo.children.nonEmpty) Seq(additionalInfo) else Seq.empty) ++
      Seq(interactions)
    
    card.children = components
    card
  }
  
  private def createAdditionalInfo(item: ActivityFeedItem): VBox = {
    val info = new VBox {
      spacing = 5
    }
    
    item.feedType match {
      case ActivityFeedType.EVENT =>
        item.eventDateTime.foreach { dateTime =>
          val eventInfo = new Label(s"📅 ${dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))}") {
            font = Font.font("System", 12)
            textFill = Color.web("#e74c3c")
          }
          info.children.add(eventInfo)
        }
        item.location.foreach { location =>
          val locationInfo = new Label(s"📍 $location") {
            font = Font.font("System", 12)
            textFill = Color.web("#65676b")
          }
          info.children.add(locationInfo)
        }
        
      case ActivityFeedType.FOOD_SHARING =>
        item.location.foreach { location =>
          val locationInfo = new Label(s"📍 $location") {
            font = Font.font("System", 12)
            textFill = Color.web("#65676b")
          }
          info.children.add(locationInfo)
        }
        item.expiryDate.foreach { expiry =>
          val expiryInfo = new Label(s"⏰ Expires: ${expiry.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))}") {
            font = Font.font("System", 12)
            textFill = Color.web("#f39c12")
          }
          info.children.add(expiryInfo)
        }
        
      case _ => // No additional info for announcements and discussions
    }
    
    info
  }
  
  private def createInteractionButtons(item: ActivityFeedItem): HBox = {
    val currentUserId = service.getCurrentUser.map(_.userId)
    val isLiked = currentUserId.exists(id => item.isLikedBy(id))
    
    val likeButton = new Button(s"👍 ${item.likes}") {
      style = if (isLiked) {
        "-fx-background-color: #e3f2fd; -fx-text-fill: #1877f2; -fx-border-color: #1877f2; -fx-background-radius: 6;"
      } else {
        "-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-border-color: transparent; -fx-background-radius: 6;"
      }
      
      onAction = _ => handleLike(item, this)
    }
    
    val commentButton = new Button(s"💬 ${item.comments.length}") {
      style = "-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-border-color: transparent; -fx-background-radius: 6;"
      onAction = _ => handleComment(item)
    }
    
    val shareButton = new Button("📤 Share") {
      style = "-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-border-color: transparent; -fx-background-radius: 6;"
      onAction = _ => handleShare(item)
    }
    
    new HBox {
      spacing = 15
      alignment = Pos.CenterLeft
      children = Seq(likeButton, commentButton, shareButton)
    }
  }
  
  private def handleLike(item: ActivityFeedItem, button: Button): Unit = {
    service.getCurrentUser match {
      case Some(user) =>
        val wasLiked = item.isLikedBy(user.userId)
        val success = if (wasLiked) {
          activityFeedService.unlikeItem(item.id, user.userId, item.feedType)
        } else {
          activityFeedService.likeItem(item.id, user.userId, item.feedType)
        }
        
        if (success) {
          val liked = item.toggleLike(user.userId)
          button.text = s"👍 ${item.likes}"
          button.style = if (liked) {
            "-fx-background-color: #e3f2fd; -fx-text-fill: #1877f2; -fx-border-color: #1877f2; -fx-background-radius: 6;"
          } else {
            "-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-border-color: transparent; -fx-background-radius: 6;"
          }
          
          // Show success feedback
          showFeedback(if (liked) "👍 Liked!" else "👍 Unliked!", success = true)
        } else {
          showFeedback("❌ Failed to update like", success = false)
        }
        
      case None =>
        showFeedback("⚠️ Please log in to like posts", success = false)
    }
  }
  
  private def handleComment(item: ActivityFeedItem): Unit = {
    service.getCurrentUser match {
      case Some(user) =>
        val dialog = new CommentDialog(
          s"Comment on ${item.title}",
          (comment: String) => {
            if (comment.trim.nonEmpty) {
              val success = activityFeedService.addComment(item.id, user.userId, comment.trim, item.feedType)
              if (success) {
                showFeedback("💬 Comment added!", success = true)
                onRefresh() // Refresh to show new comment count
              } else {
                showFeedback("❌ Failed to add comment", success = false)
              }
            }
          }
        )
        dialog.showAndWait()
        
      case None =>
        showFeedback("⚠️ Please log in to comment", success = false)
    }
  }
  
  private def handleShare(item: ActivityFeedItem): Unit = {
    // For now, just copy to clipboard or show share dialog
    showFeedback(s"📤 Shared: ${item.title}", success = true)
  }
  
  private def showFeedback(message: String, success: Boolean): Unit = {
    val alert = new Alert(if (success) Alert.AlertType.Information else Alert.AlertType.Warning) {
      title = "Activity Feed"
      headerText = ""
      contentText = message
    }
    alert.showAndWait()
  }
  
  private def createEmptyState(): VBox = {
    new VBox {
      spacing = 10
      alignment = Pos.Center
      padding = Insets(40)
      children = Seq(
        new Label("📭") {
          font = Font.font("System", 48)
        },
        new Label("No activity yet") {
          font = Font.font("System", FontWeight.Bold, 16)
          textFill = Color.web("#65676b")
        },
        new Label("Be the first to share something with the community!") {
          font = Font.font("System", 14)
          textFill = Color.web("#8a8d91")
        }
      )
    }
  }
  
  private def createErrorState(): VBox = {
    new VBox {
      spacing = 10
      alignment = Pos.Center
      padding = Insets(40)
      children = Seq(
        new Label("⚠️") {
          font = Font.font("System", 48)
        },
        new Label("Unable to load activity feed") {
          font = Font.font("System", FontWeight.Bold, 16)
          textFill = Color.web("#e74c3c")
        },
        new Button("Try Again") {
          style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6;"
          onAction = _ => onRefresh()
        }
      )
    }
  }
}
