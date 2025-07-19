package gui.components.layout

import scalafx.scene.control._
import scalafx.scene.layout._
import gui.components.common.public.BaseComponent
import gui.components.common.user.DashboardComponent
import gui.components.features.announcements.AnnouncementsTab
import gui.components.features.food.FoodSharingTab
import gui.components.features.discussion.DiscussionTab
import gui.components.features.events.EventsTab
import gui.components.features.notifications.NotificationsTab
import gui.components.features.info.AppInfoTab
import service.CommunityEngagementService

/**
 * Main tab pane component - 主标签页组件
 * 安全级别: USER - 注册用户的主界面布局
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
