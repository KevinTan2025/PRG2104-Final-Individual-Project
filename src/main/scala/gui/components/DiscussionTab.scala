package gui.components

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.dialogs.CommentDialog

/**
 * Discussion Forum tab component for managing discussion topics
 */
class DiscussionTab extends BaseTabComponent {
  
  private val topicsList = new ListView[String]()
  private val categoryCombo = new ComboBox[String] {
    items = scalafx.collections.ObservableBuffer(
      "All", "NUTRITION", "SUSTAINABLE_AGRICULTURE", "FOOD_SECURITY", 
      "COMMUNITY_GARDEN", "COOKING_TIPS", "GENERAL"
    )
    value = "All"
  }
  private val searchField = new TextField {
    promptText = "Search topics..."
  }
  
  override def build(): Tab = {
    refreshTopics()
    
    val createTopicButton = new Button("Create Topic") {
      onAction = (_: ActionEvent) => {
        val dialog = createTopicDialog()
        dialog.showAndWait()
      }
    }
    
    val filterButton = new Button("Filter by Category") {
      onAction = (_: ActionEvent) => handleCategoryFilter()
    }
    
    val replyButton = new Button("Add Reply") {
      onAction = (_: ActionEvent) => handleAddReply()
    }
    
    val likeButton = new Button("Like Topic") {
      onAction = (_: ActionEvent) => handleLikeTopic()
    }
    
    // Admin-only moderation button
    val moderateButton = new Button("Moderate Topic") {
      visible = service.isCurrentUserAdmin
      onAction = (_: ActionEvent) => handleModerateTopic()
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => handleSearchTopics()
    }
    
    val refreshButton = new Button("Refresh") {
      onAction = (_: ActionEvent) => refreshTopics()
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(createTopicButton, categoryCombo, filterButton, replyButton, likeButton, moderateButton, refreshButton)
    }
    
    val searchControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(searchField, searchButton)
    }
    
    val mainContent = new BorderPane {
      top = new VBox {
        children = Seq(topControls, searchControls)
      }
      center = topicsList
    }
    
    new Tab {
      text = "Discussion Forum"
      content = mainContent
      closable = false
    }
  }
  
  override def refresh(): Unit = {
    refreshTopics()
  }
  
  override def initialize(): Unit = {
    // Initial setup if needed
  }
  
  private def refreshTopics(): Unit = {
    val topics = service.getDiscussionTopics
    val items = topics.map(t => s"[${t.category}] ${t.title} - ${t.getReplyCount} replies")
    topicsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def handleCategoryFilter(): Unit = {
    val category = categoryCombo.value.value
    if (category == "All") {
      refreshTopics()
    } else {
      import model.DiscussionCategory
      val cat = category match {
        case "NUTRITION" => DiscussionCategory.NUTRITION
        case "SUSTAINABLE_AGRICULTURE" => DiscussionCategory.SUSTAINABLE_AGRICULTURE
        case "FOOD_SECURITY" => DiscussionCategory.FOOD_SECURITY
        case "COMMUNITY_GARDEN" => DiscussionCategory.COMMUNITY_GARDEN
        case "COOKING_TIPS" => DiscussionCategory.COOKING_TIPS
        case _ => DiscussionCategory.GENERAL
      }
      val filtered = service.getTopicsByCategory(cat)
      val items = filtered.map(t => s"[${t.category}] ${t.title} - ${t.getReplyCount} replies")
      topicsList.items = scalafx.collections.ObservableBuffer(items: _*)
    }
  }
  
  private def handleAddReply(): Unit = {
    val selectedIndex = topicsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val topics = getCurrentTopics()
      
      if (selectedIndex < topics.length) {
        val topic = topics(selectedIndex)
        val dialog = createReplyDialog(topic.title)
        dialog.showAndWait() match {
          case Some(content) => 
            if (service.addReplyToTopic(topic.topicId, content.toString)) {
              GuiUtils.showInfo("Success", "Reply added successfully!")
              refreshTopics()
            } else {
              GuiUtils.showError("Failed", "Could not add reply.")
            }
          case None => // User cancelled
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select a topic to reply to.")
    }
  }
  
  private def handleLikeTopic(): Unit = {
    val selectedIndex = topicsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val topics = getCurrentTopics()
      
      if (selectedIndex < topics.length) {
        val topic = topics(selectedIndex)
        if (service.likeTopic(topic.topicId)) {
          GuiUtils.showInfo("Success", "Topic liked!")
          refreshTopics()
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select a topic to like.")
    }
  }
  
  private def handleModerateTopic(): Unit = {
    val selectedIndex = topicsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val topics = service.getDiscussionTopics
      if (selectedIndex < topics.length) {
        val topic = topics(selectedIndex)
        if (service.moderateContent(topic.topicId, "topic")) {
          GuiUtils.showInfo("Success", "Topic moderated successfully!")
          refreshTopics()
        }
      }
    }
  }
  
  private def handleSearchTopics(): Unit = {
    val searchTerm = searchField.text.value
    if (searchTerm.nonEmpty) {
      val results = service.searchTopics(searchTerm)
      val items = results.map(t => s"[${t.category}] ${t.title} - ${t.getReplyCount} replies")
      topicsList.items = scalafx.collections.ObservableBuffer(items: _*)
    } else {
      refreshTopics()
    }
  }
  
  private def getCurrentTopics() = {
    if (categoryCombo.value.value == "All") {
      service.getDiscussionTopics
    } else {
      import model.DiscussionCategory
      val cat = categoryCombo.value.value match {
        case "NUTRITION" => DiscussionCategory.NUTRITION
        case "SUSTAINABLE_AGRICULTURE" => DiscussionCategory.SUSTAINABLE_AGRICULTURE
        case "FOOD_SECURITY" => DiscussionCategory.FOOD_SECURITY
        case "COMMUNITY_GARDEN" => DiscussionCategory.COMMUNITY_GARDEN
        case "COOKING_TIPS" => DiscussionCategory.COOKING_TIPS
        case _ => DiscussionCategory.GENERAL
      }
      service.getTopicsByCategory(cat)
    }
  }
  
  private def createTopicDialog(): Dialog[String] = {
    val dialog = new Dialog[String]()
    dialog.title = "Create Discussion Topic"
    dialog.headerText = "Create a new discussion topic"
    
    val titleField = new TextField { promptText = "Topic title" }
    val descriptionArea = new TextArea { 
      promptText = "Topic description"
      prefRowCount = 5
    }
    
    val categoryCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer(
        "NUTRITION", "SUSTAINABLE_AGRICULTURE", "FOOD_SECURITY", 
        "COMMUNITY_GARDEN", "COOKING_TIPS", "GENERAL"
      )
      value = "GENERAL"
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Title:"), 0, 0)
      add(titleField, 1, 0)
      add(new Label("Category:"), 0, 1)
      add(categoryCombo, 1, 1)
      add(new Label("Description:"), 0, 2)
      add(descriptionArea, 1, 2)
    }
    
    dialog.dialogPane().content = grid
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    
    dialog.resultConverter = dialogButton => {
      if (dialogButton == ButtonType.OK) {
        import model.DiscussionCategory
        val category = categoryCombo.value.value match {
          case "NUTRITION" => DiscussionCategory.NUTRITION
          case "SUSTAINABLE_AGRICULTURE" => DiscussionCategory.SUSTAINABLE_AGRICULTURE
          case "FOOD_SECURITY" => DiscussionCategory.FOOD_SECURITY
          case "COMMUNITY_GARDEN" => DiscussionCategory.COMMUNITY_GARDEN
          case "COOKING_TIPS" => DiscussionCategory.COOKING_TIPS
          case _ => DiscussionCategory.GENERAL
        }
        
        service.createDiscussionTopic(titleField.text.value, descriptionArea.text.value, category)
        "created"
      } else {
        null
      }
    }
    
    dialog
  }
  
  private def createReplyDialog(topicTitle: String): Dialog[String] = {
    val dialog = new Dialog[String]()
    dialog.title = "Add Reply"
    dialog.headerText = s"Reply to: $topicTitle"
    
    val contentArea = new TextArea { 
      promptText = "Your reply..."
      prefRowCount = 5
      prefWidth = 400
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Reply:"), 0, 0)
      add(contentArea, 0, 1)
    }
    
    dialog.dialogPane().content = grid
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    
    dialog.resultConverter = dialogButton => {
      if (dialogButton == ButtonType.OK) {
        contentArea.text()
      } else {
        null
      }
    }
    
    dialog
  }
}
