package gui.components.features.discussion

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.BaseTabComponent

/**
 * Discussion tab component for managing community discussions
 * 安全级别: PUBLIC/USER - 匿名用户可以查看，注册用户可以参与讨论
 */
class DiscussionTab(
  readOnlyMode: Boolean = false,
  onLoginPrompt: () => Unit = () => {}
) extends BaseTabComponent {
  
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
        if (readOnlyMode) {
          onLoginPrompt()
        } else {
          createTopicDialog()
        }
      }
    }
    
    val filterButton = new Button("Filter by Category") {
      onAction = (_: ActionEvent) => handleCategoryFilter()
    }
    
    val replyButton = new Button("Add Reply") {
      onAction = (_: ActionEvent) => {
        if (readOnlyMode) {
          onLoginPrompt()
        } else {
          handleAddReply()
        }
      }
    }
    
    val likeButton = new Button("Like Topic") {
      onAction = (_: ActionEvent) => {
        if (readOnlyMode) {
          onLoginPrompt()
        } else {
          handleLikeTopic()
        }
      }
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => handleSearchTopics()
    }
    
    val refreshButton = new Button("Refresh") {
      onAction = (_: ActionEvent) => refreshTopics()
    }
    
    // 在只读模式下修改按钮文本
    if (readOnlyMode) {
      createTopicButton.text = "🔒 Login to Create Topic"
      replyButton.text = "🔒 Login to Reply"
      likeButton.text = "🔒 Login to Like"
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(createTopicButton, categoryCombo, filterButton, replyButton, likeButton, refreshButton)
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
      text = if (readOnlyMode) "💬 Discussion Forum" else "Discussion Forum"
      content = mainContent
      closable = false
    }
  }
  
  override def refresh(): Unit = {
    refreshTopics()
  }
  
  override def initialize(): Unit = {
    refreshTopics()
  }
  
  private def refreshTopics(): Unit = {
    // Mock data for demonstration
    val mockTopics = Seq(
      "[NUTRITION] Healthy eating tips for families - 5 replies",
      "[FOOD_SECURITY] Community garden project update - 12 replies", 
      "[COOKING_TIPS] Easy recipes for busy schedules - 8 replies",
      "[GENERAL] Welcome to our discussion forum - 3 replies",
      "[SUSTAINABLE_AGRICULTURE] Urban farming techniques - 7 replies",
      "[COMMUNITY_GARDEN] Spring planting schedule - 15 replies"
    )
    topicsList.items = scalafx.collections.ObservableBuffer(mockTopics: _*)
  }
  
  private def handleCategoryFilter(): Unit = {
    val category = categoryCombo.value.value
    if (category == "All") {
      refreshTopics()
    } else {
      // Filter mock data by category
      val filteredTopics = Seq(s"[$category] Sample topic for $category - 2 replies")
      topicsList.items = scalafx.collections.ObservableBuffer(filteredTopics: _*)
    }
  }
  
  private def handleAddReply(): Unit = {
    val selectedIndex = topicsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      GuiUtils.showInfo("Add Reply", "Reply functionality will be available after login.")
    } else {
      GuiUtils.showWarning("No Selection", "Please select a topic to reply to.")
    }
  }
  
  private def handleLikeTopic(): Unit = {
    val selectedIndex = topicsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      GuiUtils.showInfo("Like Topic", "Like functionality will be available after login.")
    } else {
      GuiUtils.showWarning("No Selection", "Please select a topic to like.")
    }
  }
  
  private def handleSearchTopics(): Unit = {
    val searchTerm = searchField.text.value
    if (searchTerm.nonEmpty) {
      val filteredTopics = Seq(s"[GENERAL] Search result for '$searchTerm' - 1 reply")
      topicsList.items = scalafx.collections.ObservableBuffer(filteredTopics: _*)
    } else {
      refreshTopics()
    }
  }
  
  private def createTopicDialog(): Unit = {
    GuiUtils.showInfo("Create Topic", "Topic creation functionality will be available after login.")
  }
}
