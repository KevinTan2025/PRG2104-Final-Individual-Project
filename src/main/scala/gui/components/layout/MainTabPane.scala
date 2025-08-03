package gui.components.layout

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Label, Tab, TabPane}
import javafx.scene.layout.VBox
import scalafx.scene.layout.Region
import scalafx.Includes._
import gui.components.common.public.BaseComponent
import gui.components.common.user.DashboardComponent
import gui.components.features.announcements.AnnouncementsTab
import gui.components.features.food.FoodSharingTab
import gui.components.features.foodstock.FoodStockTab
import gui.components.features.discussion.DiscussionTab
import gui.components.features.events.EventsTab
import gui.components.features.notifications.NotificationsTab
import gui.components.features.info.AppInfoTab
import service.CommunityEngagementService

/**
 * Main tab pane component
 * Security Level: USER - Main interface layout for registered users
 */
class MainTabPane extends BaseComponent {
  
  // FXML injected controls
  @FXML private var tabPaneMain: TabPane = _
  @FXML private var tabDashboard: Tab = _
  @FXML private var tabAnnouncements: Tab = _
  @FXML private var tabFoodSharing: Tab = _
  @FXML private var tabFoodStock: Tab = _
  @FXML private var tabDiscussion: Tab = _
  @FXML private var tabEvents: Tab = _
  @FXML private var tabNotifications: Tab = _
  @FXML private var tabAppInfo: Tab = _
  
  // Content containers
  @FXML private var vboxDashboardContent: VBox = _
  @FXML private var vboxAnnouncementsContent: VBox = _
  @FXML private var vboxFoodSharingContent: VBox = _
  @FXML private var vboxFoodStockContent: VBox = _
  @FXML private var vboxDiscussionContent: VBox = _
  @FXML private var vboxEventsContent: VBox = _
  @FXML private var vboxNotificationsContent: VBox = _
  @FXML private var vboxAppInfoContent: VBox = _
  
  // Title labels
  @FXML private var lblDashboardTitle: Label = _
  @FXML private var lblAnnouncementsTitle: Label = _
  @FXML private var lblFoodSharingTitle: Label = _
  @FXML private var lblFoodStockTitle: Label = _
  @FXML private var lblDiscussionTitle: Label = _
  @FXML private var lblEventsTitle: Label = _
  @FXML private var lblNotificationsTitle: Label = _
  @FXML private var lblAppInfoTitle: Label = _
  
  // Placeholder labels
  @FXML private var lblDashboardPlaceholder: Label = _
  @FXML private var lblAnnouncementsPlaceholder: Label = _
  @FXML private var lblFoodSharingPlaceholder: Label = _
  @FXML private var lblFoodStockPlaceholder: Label = _
  @FXML private var lblDiscussionPlaceholder: Label = _
  @FXML private var lblEventsPlaceholder: Label = _
  @FXML private var lblNotificationsPlaceholder: Label = _
  @FXML private var lblAppInfoPlaceholder: Label = _
  
  override def build(): Region = {
    val loader = new FXMLLoader(getClass.getResource("/gui/components/layout/MainTabPane.fxml"))
    loader.setController(this)
    val root = loader.load[TabPane]()
    
    // Load actual content for each tab
    loadTabContent()
    
    // Convert JavaFX TabPane to ScalaFX Region
    new Region(root)
  }
  
  /**
   * Loads the actual content for each tab by replacing placeholder content
   */
  private def loadTabContent(): Unit = {
    try {
      // Load dashboard content
      val dashboardComponent = new DashboardComponent().build()
      vboxDashboardContent.getChildren.clear()
      vboxDashboardContent.getChildren.add(dashboardComponent.delegate.getContent)
      
      // Load announcements content
      val announcementsComponent = new AnnouncementsTab().build()
      vboxAnnouncementsContent.getChildren.clear()
      vboxAnnouncementsContent.getChildren.add(announcementsComponent.delegate.getContent)
      
      // Load food sharing content
      val foodSharingComponent = new FoodSharingTab().build()
      vboxFoodSharingContent.getChildren.clear()
      vboxFoodSharingContent.getChildren.add(foodSharingComponent.delegate.getContent)
      
      // Load food stock content
      val foodStockComponent = new FoodStockTab().build()
      vboxFoodStockContent.getChildren.clear()
      vboxFoodStockContent.getChildren.add(foodStockComponent.delegate.getContent)
      
      // Load discussion content
      val discussionComponent = new DiscussionTab().build()
      vboxDiscussionContent.getChildren.clear()
      vboxDiscussionContent.getChildren.add(discussionComponent.delegate.getContent)
      
      // Load events content
      val eventsComponent = new EventsTab().build()
      vboxEventsContent.getChildren.clear()
      vboxEventsContent.getChildren.add(eventsComponent.delegate.getContent)
      
      // Load notifications content
      val notificationsComponent = new NotificationsTab().build()
      vboxNotificationsContent.getChildren.clear()
      vboxNotificationsContent.getChildren.add(notificationsComponent.delegate.getContent)
      
      // Load app info content
      val appInfoComponent = new AppInfoTab().build()
      vboxAppInfoContent.getChildren.clear()
      vboxAppInfoContent.getChildren.add(appInfoComponent.delegate.getContent)
      
    } catch {
      case e: Exception =>
        // If any component fails to load, keep the placeholder content
        println(s"Warning: Failed to load some tab content: ${e.getMessage}")
    }
  }
}
