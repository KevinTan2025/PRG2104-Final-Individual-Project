package gui.components.features.food

import scalafx.scene.control.{Tab => ScalaFXTab, _}
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import gui.utils.GuiUtils
import gui.dialogs.features.food.FoodPostDialog
import gui.dialogs.features.food.FoodPostDetailsDialog
import gui.dialogs.common.CommentDialog
import gui.components.common.public.BaseTabComponent
import service.CommunityEngagementService



// This tab allows users to create, view, filter, search, and accept food posts.
// It also supports read-only mode for users who are not logged in.
class FoodSharingTab(
  readOnlyMode: Boolean = false,
  onLoginPrompt: () => Unit = () => {}
) extends BaseTabComponent {
  
  private val foodPostsList = new ListView[String]()
  private val currentPostsProperty = ObjectProperty[List[model.FoodPost]](List.empty[model.FoodPost])
  private val filterCombo = new ComboBox[String] {
    items = scalafx.collections.ObservableBuffer("All", "OFFER", "REQUEST")
    value = "All"
  }
  private val searchField = new TextField {
    promptText = "Search food posts..."
  }
  
  override def build(): ScalaFXTab = {
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
          currentPostsProperty.value = service.foodPostsByType(postType)
          updateListView()
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
    
    // Reading mode adjustments
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
    
    // Add double-click handler to show details
    foodPostsList.onMouseClicked = (event) => {
      if (event.clickCount == 2) {
        val selectedIndex = foodPostsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0 && selectedIndex < currentPostsProperty.value.length) {
          val selectedPost = currentPostsProperty.value(selectedIndex)
          val detailsDialog = new FoodPostDetailsDialog(selectedPost, () => refreshFoodPosts())
          detailsDialog.showAndWait()
        }
      }
    }
    
    new ScalaFXTab {
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
    currentPostsProperty.value = service.foodPosts
    updateListView()
  }
  
  private def updateListView(): Unit = {
    val items = currentPostsProperty.value.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
    foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def handleAcceptFoodPost(): Unit = {
    val selectedIndex = foodPostsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0 && selectedIndex < currentPostsProperty.value.length) {
      val post = currentPostsProperty.value(selectedIndex)
      // Check if post is still pending
      if (post.status == model.FoodPostStatus.PENDING) {
        if (service.acceptFoodPost(post.postId)) {
          GuiUtils.showInfo("Success", "Food post accepted successfully! Status changed to ACCEPTED.")
          refreshFoodPosts()
        } else {
          GuiUtils.showWarning("Failed", "Could not accept food post. Please try again.")
        }
      } else {
        GuiUtils.showWarning("Cannot Accept", s"This post is already ${post.status}.")
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select a food post to accept.")
    }
  }
  
  private def handleSearchFoodPosts(): Unit = {
    val searchTerm = searchField.text.value
    if (searchTerm.nonEmpty) {
      currentPostsProperty.value = service.searchFoodPosts(searchTerm)
      updateListView()
    } else {
      refreshFoodPosts()
    }
  }
}
