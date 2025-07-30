package gui.components.features.food

import scalafx.scene.control._
import scalafx.scene.layout.{VBox, HBox}
import scalafx.geometry.Insets
import scalafx.Includes._
import gui.components.common.public.{BaseTabComponent, SimpleComponentBuilder}
import gui.dialogs.features.food.FoodPostDialog
import gui.utils.GuiUtils
import service.CommunityEngagementService


// Minimalist Food Sharing tab component
class SimpleFoodSharingTab extends BaseTabComponent {
  
  // Customized list view for food posts
  private val foodPostsList = SimpleComponentBuilder.listView[String](650, 450)
  private val searchField = SimpleComponentBuilder.searchBox("Search food posts...", 250)
  private val typeFilterBox = SimpleComponentBuilder.filterBox("All", "OFFER", "REQUEST")
  
  // Button Definitions - Unified API
  private val createBtn = SimpleComponentBuilder.button("Create Post", handleCreate)
  private val refreshBtn = SimpleComponentBuilder.button("Refresh", handleRefresh)
  private val searchBtn = SimpleComponentBuilder.button("Search", handleSearch)
  private val acceptBtn = SimpleComponentBuilder.button("Accept", handleAccept)
  private val filterBtn = SimpleComponentBuilder.button("Filter", handleFilter)
  
  override def build(): Tab = {
    // Load initial food posts
    handleRefresh()

    // Button layout - including filter
    val buttons = new HBox(10) {
      children = List(createBtn, refreshBtn, typeFilterBox, filterBtn, acceptBtn)
      padding = Insets(10)
    }
    
    val searchRow = new HBox(10) {
      children = List(searchField, searchBtn)
      padding = Insets(10)
    }
    
    val layoutContent = new VBox(10) {
      children = List(buttons, searchRow, foodPostsList)
      padding = Insets(10)
    }
    
    new Tab {
      text = "Food Sharing"
      content = layoutContent
      closable = false
    }
  }
  
  // Handles the refresh action to load food posts
  
  private def handleRefresh(): Unit = {
    val posts = service.getFoodPosts
    val items = posts.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
    foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
  }
  
  private def handleCreate(): Unit = {
    val dialog = new FoodPostDialog(() => handleRefresh())
    dialog.showAndWait()
  }
  
  private def handleSearch(): Unit = {
    val searchTerm = searchField.text.value
    if (searchTerm.nonEmpty) {
      val results = service.searchFoodPosts(searchTerm)
      val items = results.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
      foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
    } else {
      handleRefresh()
    }
  }
  
  private def handleFilter(): Unit = {
    val filterType = typeFilterBox.value.value
    if (filterType == "All") {
      handleRefresh()
    } else {
      import model.FoodPostType
      val postType = if (filterType == "OFFER") FoodPostType.OFFER else FoodPostType.REQUEST
      val filtered = service.getFoodPostsByType(postType)
      val items = filtered.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
      foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
    }
  }
  
  private def handleAccept(): Unit = {
    val selectedIndex = foodPostsList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val posts = service.getFoodPosts
      if (selectedIndex < posts.length) {
        val post = posts(selectedIndex)
        if (service.acceptFoodPost(post.postId)) {
          GuiUtils.showInfo("Success", "Food post accepted successfully!")
          handleRefresh()
        } else {
          GuiUtils.showWarning("Failed", "Could not accept food post.")
        }
      }
    } else {
      GuiUtils.showWarning("No Selection", "Please select a food post to accept.")
    }
  }
  
  override def refresh(): Unit = handleRefresh()
  override def initialize(): Unit = {}
}
