package gui.components.layout

import scalafx.scene.control._
import scalafx.scene.layout._
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
 * 安全级别: PUBLIC - 匿名用户的主界面布局
 */
class AnonymousMainTabPane(
  onLoginPrompt: () => Unit
) extends BaseComponent {
  
  override def build(): Region = {
    new TabPane {
      tabs = Seq(
        new AnonymousDashboardComponent(onLoginPrompt).build(),
        new AnnouncementsTab(readOnlyMode = true, onLoginPrompt).build(),
        new FoodSharingTab(readOnlyMode = true, onLoginPrompt).build(),
        new FoodStockTab().build(),
        new DiscussionTab(readOnlyMode = true, onLoginPrompt).build(),
        new EventsTab(readOnlyMode = true, onLoginPrompt).build(),
        new AppInfoTab().build()
      )
    }
  }
}
