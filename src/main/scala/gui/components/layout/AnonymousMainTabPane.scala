package gui.components.layout

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Label, TabPane}
import javafx.scene.layout.VBox
import scalafx.scene.layout.Region
import scalafx.Includes._
import gui.components.common.public.BaseComponent
import gui.components.features.announcements.AnnouncementsTab
import gui.components.features.food.FoodSharingTab
import gui.components.features.foodstock.FoodStockTab
import gui.components.features.discussion.DiscussionTab
import gui.components.features.events.EventsTab
import gui.components.features.info.AppInfoTab
import gui.components.features.anonymous.AnonymousDashboardComponent

/**
 * Main tab pane component for anonymous users
 * Security Level: PUBLIC - Main interface layout for anonymous users
 */
class AnonymousMainTabPane(
  onLoginPrompt: () => Unit
) extends BaseComponent {
  
  // FXML injected controls
  @FXML private var tabPaneAnonymous: TabPane = _
  @FXML private var tabAnonymousDashboard = null
  @FXML private var tabAnonymousAnnouncements = null
  @FXML private var tabAnonymousFoodSharing = null
  @FXML private var tabAnonymousFoodStock = null
  @FXML private var tabAnonymousDiscussion = null
  @FXML private var tabAnonymousEvents = null
  @FXML private var tabAnonymousAppInfo = null
  
  // Content containers
  @FXML private var vboxAnonymousDashboardContent: VBox = _
  @FXML private var vboxAnonymousAnnouncementsContent: VBox = _
  @FXML private var vboxAnonymousFoodSharingContent: VBox = _
  @FXML private var vboxAnonymousFoodStockContent: VBox = _
  @FXML private var vboxAnonymousDiscussionContent: VBox = _
  @FXML private var vboxAnonymousEventsContent: VBox = _
  @FXML private var vboxAnonymousAppInfoContent: VBox = _
  
  // Title labels
  @FXML private var lblAnonymousDashboardTitle: Label = _
  @FXML private var lblAnonymousAnnouncementsTitle: Label = _
  @FXML private var lblAnonymousFoodSharingTitle: Label = _
  @FXML private var lblAnonymousFoodStockTitle: Label = _
  @FXML private var lblAnonymousDiscussionTitle: Label = _
  @FXML private var lblAnonymousEventsTitle: Label = _
  @FXML private var lblAnonymousAppInfoTitle: Label = _
  
  // Placeholder labels
  @FXML private var lblAnonymousDashboardPlaceholder: Label = _
  @FXML private var lblAnonymousAnnouncementsPlaceholder: Label = _
  @FXML private var lblAnonymousFoodSharingPlaceholder: Label = _
  @FXML private var lblAnonymousFoodStockPlaceholder: Label = _
  @FXML private var lblAnonymousDiscussionPlaceholder: Label = _
  @FXML private var lblAnonymousEventsPlaceholder: Label = _
  @FXML private var lblAnonymousAppInfoPlaceholder: Label = _
  
  override def build(): Region = {
    val loader = new FXMLLoader(getClass.getResource("/gui/components/layout/AnonymousMainTabPane.fxml"))
    loader.setController(this)
    val root = loader.load[TabPane]()
    
    // Load actual content for each tab
    loadTabContent()
    
    // Convert JavaFX TabPane to ScalaFX Region
    new Region(root)
  }
  
  /**
   * Load actual content for each tab
   */
  private def loadTabContent(): Unit = {
    try {
      // Load dashboard content
      val dashboardComponent = new AnonymousDashboardComponent(onLoginPrompt)
      vboxAnonymousDashboardContent.getChildren.clear()
      vboxAnonymousDashboardContent.getChildren.add(dashboardComponent.build().content.value.delegate)
      
      // Load announcements content
      val announcementsTab = new AnnouncementsTab(readOnlyMode = true, onLoginPrompt)
      vboxAnonymousAnnouncementsContent.getChildren.clear()
      vboxAnonymousAnnouncementsContent.getChildren.add(announcementsTab.build().delegate.getContent)
      
      // Load food sharing content
      val foodSharingTab = new FoodSharingTab(readOnlyMode = true, onLoginPrompt)
      vboxAnonymousFoodSharingContent.getChildren.clear()
      vboxAnonymousFoodSharingContent.getChildren.add(foodSharingTab.build().delegate.getContent)
      
      // Load food stock content
      val foodStockTab = new FoodStockTab()
      vboxAnonymousFoodStockContent.getChildren.clear()
      vboxAnonymousFoodStockContent.getChildren.add(foodStockTab.build().delegate.getContent)
      
      // Load discussion content
      val discussionTab = new DiscussionTab(readOnlyMode = true, onLoginPrompt)
      vboxAnonymousDiscussionContent.getChildren.clear()
      vboxAnonymousDiscussionContent.getChildren.add(discussionTab.build().delegate.getContent)
      
      // Load events content
      val eventsTab = new EventsTab(readOnlyMode = true, onLoginPrompt)
      vboxAnonymousEventsContent.getChildren.clear()
      vboxAnonymousEventsContent.getChildren.add(eventsTab.build().delegate.getContent)
      
      // Load app info content
      val appInfoTab = new AppInfoTab()
      vboxAnonymousAppInfoContent.getChildren.clear()
      vboxAnonymousAppInfoContent.getChildren.add(appInfoTab.build().delegate.getContent)
      
    } catch {
      case e: Exception =>
        // If any component fails to load, keep the placeholder content
        println(s"Warning: Failed to load some tab content: ${e.getMessage}")
    }
  }
}
