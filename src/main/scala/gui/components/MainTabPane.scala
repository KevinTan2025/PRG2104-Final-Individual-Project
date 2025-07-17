package gui.components

import scalafx.scene.control._
import scalafx.scene.layout.Region

/**
 * Main tab container for the application
 */
class MainTabPane extends BaseComponent {
  
  override def build(): Region = {
    new TabPane {
      tabs = Seq(
        new DashboardComponent().build(),
        new AnnouncementsTab().build(),
        new FoodSharingTab().build(),
        new DiscussionTab().build(),
        new EventsTab().build(),
        new NotificationsTab().build(),
        new AppInfoTab().build()
      )
    }
  }
}
