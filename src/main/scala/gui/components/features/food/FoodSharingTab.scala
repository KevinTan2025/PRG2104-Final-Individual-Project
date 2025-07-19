package gui.components.features.food

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.dialogs.{FoodPostDialog, CommentDialog}
import gui.components.common.public.BaseTabComponent
import service.CommunityEngagementService

/**
 * Food Sharing tab component for managing food posts
 * 安全级别: PUBLIC/USER - 匿名用户可以查看，注册用户可以分享和请求食物
 */
class FoodSharingTab(
  readOnlyMode: Boolean = false,
  onLoginPrompt: () => Unit = () => {}
) extends BaseTabComponent {
  
  private val foodPostsList = new ListView[String]()
  private val filterCombo = new ComboBox[String] {
    items = scalafx.collections.ObservableBuffer("All", "OFFER", "REQUEST")
    value = "All"
  }
  private val searchField = new TextField {
    promptText = "Search food posts..."
  }
  
  override def build(): Tab = {
    refreshFoodPosts()
    
    val createButton = new Button("Create Food Post") {
      onAction = (_: ActionEvent) => {
        if (readOnlyMode) {
          onLoginPrompt()
        } else {
          val dialog = new FoodPostDialog(() => refreshFoodPosts())
          dialog.showAndWait()
        }
      }
    }
    
    val filterButton = new Button("Filter") {
      onAction = (_: ActionEvent) => {
        val filterType = filterCombo.value.value
        if (filterType == "All") {
          refreshFoodPosts()
        } else {
          import model.FoodPostType
          val postType = if (filterType == "OFFER") FoodPostType.OFFER else FoodPostType.REQUEST
          val filtered = service.getFoodPostsByType(postType)
          val items = filtered.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
          foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
        }
      }
    }
    
    val acceptButton = new Button("Accept Selected") {
      onAction = (_: ActionEvent) => {
        if (readOnlyMode) {
          onLoginPrompt()
        } else {
          handleAcceptFoodPost()
        }
      }
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => handleSearchFoodPosts()
    }
    
    val refreshButton = new Button("Refresh") {
      onAction = (_: ActionEvent) => refreshFoodPosts()
    }
    
    // 在只读模式下修改按钮文本
    if (readOnlyMode) {
      createButton.text = "🔒 Login to Post"
      acceptButton.text = "🔒 Login to Accept"
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(createButton, filterCombo, filterButton, acceptButton, refreshButton)
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
      center = foodPostsList
    }
    
    new Tab {
      text = if (readOnlyMode) "🍕 Food Sharing" else "Food Sharing"
      content = mainContent
      closable = false
    }
  }
  
  override def refresh(): Unit = {
    refreshFoodPosts()
  }
  
  override def initialize(): Unit = {
    // Initial setup if needed
  }
  
  private def refreshFoodPosts(): Unit = {
    val posts = service.getFoodPosts
    val items = posts.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
    foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def handleAcceptFoodPost(): Unit = {
    val selectedIndex = foodPostsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val posts = if (filterCombo.value.value == "All") {
        service.getFoodPosts
      } else {
        import model.FoodPostType
        val postType = if (filterCombo.value.value == "OFFER") FoodPostType.OFFER else FoodPostType.REQUEST
        service.getFoodPostsByType(postType)
      }
      if (selectedIndex < posts.length) {
        val post = posts(selectedIndex)
        if (service.acceptFoodPost(post.postId)) {
          GuiUtils.showInfo("Success", "Food post accepted successfully!")
          refreshFoodPosts()
        } else {
          GuiUtils.showWarning("Failed", "Could not accept food post. It may already be accepted.")
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select a food post to accept.")
    }
  }
  
  private def handleSearchFoodPosts(): Unit = {
    val searchTerm = searchField.text.value
    if (searchTerm.nonEmpty) {
      val results = service.searchFoodPosts(searchTerm)
      val items = results.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
      foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
    } else {
      refreshFoodPosts()
    }
  }
}
