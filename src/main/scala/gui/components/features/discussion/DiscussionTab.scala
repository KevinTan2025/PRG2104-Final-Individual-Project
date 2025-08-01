package gui.components.features.discussion

import scalafx.scene.control.{Tab => ScalaFXTab, _}
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import gui.utils.GuiUtils
import gui.components.common.public.BaseTabComponent
import gui.dialogs.features.discussion.{DiscussionTopicDialog, DiscussionTopicDetailsDialog}
import service.CommunityEngagementService
import model.{DiscussionCategory, DiscussionTopic}


// This tab allows users to create, view, filter, search, and interact with discussion topics.
// It also supports read-only mode for users who are not logged in.
class DiscussionTab(
  readOnlyMode: Boolean = false,
  onLoginPrompt: () => Unit = () => {}
) extends BaseTabComponent {
  
  private val service = CommunityEngagementService.getInstance
  private val topicsList = new ListView[String]()
  private val currentTopicsProperty = ObjectProperty[List[DiscussionTopic]](List.empty[DiscussionTopic])
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
  
  override def build(): ScalaFXTab = {
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
    
    // Reading mode adjustments
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
    
    // Add double-click handler to show topic details
    topicsList.onMouseClicked = (event) => {
      if (event.clickCount == 2) {
        val selectedIndex = topicsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0 && selectedIndex < currentTopicsProperty.value.length) {
          val selectedTopic = currentTopicsProperty.value(selectedIndex)
          val detailsDialog = new DiscussionTopicDetailsDialog(selectedTopic, () => refreshTopics())
          detailsDialog.showAndWait()
        }
      }
    }
    
    new ScalaFXTab {
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
    currentTopicsProperty.value = service.discussionTopics
    updateListView()
  }
  
  private def updateListView(): Unit = {
    val items = currentTopicsProperty.value.map(t => s"[${t.category}] ${t.title} - ${t.getReplyCount} replies • ${t.likes} likes")
    topicsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def handleCategoryFilter(): Unit = {
    val category = categoryCombo.value.value
    if (category == "All") {
      currentTopicsProperty.value = service.discussionTopics
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
      currentTopicsProperty.value = service.topicsByCategory(cat)
    }
    updateListView()
  }
  
  private def handleAddReply(): Unit = {
    val selectedIndex = topicsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0 && selectedIndex < currentTopicsProperty.value.length) {
      val selectedTopic = currentTopicsProperty.value(selectedIndex)
      val detailsDialog = new DiscussionTopicDetailsDialog(selectedTopic, () => refreshTopics())
      detailsDialog.showAndWait()
    } else {
      GuiUtils.showWarning("No Selection", "Please select a topic to reply to.")
    }
  }
  
  private def handleLikeTopic(): Unit = {
    val selectedIndex = topicsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0 && selectedIndex < currentTopicsProperty.value.length) {
      val selectedTopic = currentTopicsProperty.value(selectedIndex)
      service.getCurrentUser match {
        case Some(_) =>
          if (service.likeTopic(selectedTopic.topicId)) {
            GuiUtils.showInfo("Success", "Topic liked!")
            refreshTopics()
          } else {
            GuiUtils.showError("Error", "Failed to like topic.")
          }
        case None =>
          GuiUtils.showWarning("Login Required", "Please log in to like topics.")
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select a topic to like.")
    }
  }
  
  private def handleSearchTopics(): Unit = {
    val searchTerm = searchField.text.value
    if (searchTerm.nonEmpty) {
      currentTopicsProperty.value = service.searchTopics(searchTerm)
      updateListView()
    } else {
      refreshTopics()
    }
  }
  
  private def createTopicDialog(): Unit = {
    val dialog = new DiscussionTopicDialog(() => refreshTopics())
    dialog.showAndWait()
  }
}
