package gui.components.features.announcements

import scalafx.scene.control.{Tab => ScalaFXTab, TabPane => ScalaFXTabPane, Button, Label, ListView, ScrollPane, TextField, ComboBox, CheckBox, TextArea, Separator}
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.dialogs.features.announcements.AnnouncementDialog
import gui.components.common.public.BaseTabComponent
import gui.components.features.activityfeed.EnhancedActivityFeedComponent
import model.ActivityFeedType
import service.CommunityEngagementService

/**
 * Announcements tab component with activity feed integration
 * 安全级别: PUBLIC/USER - 匿名用户可以查看，注册用户可以创建公告
 */
class AnnouncementsTab(
  readOnlyMode: Boolean = false,
  onLoginPrompt: () => Unit = () => {}
) extends BaseTabComponent {
  
  private lazy val activityFeedComponent: EnhancedActivityFeedComponent = 
    new EnhancedActivityFeedComponent(
      service, 
      () => refresh(), 
      Some(ActivityFeedType.ANNOUNCEMENT)
    )
  
  override def build(): ScalaFXTab = {
    // Create activity feed component filtered for announcements only
    val activityFeed = activityFeedComponent.build()
    
    val sidePanel = createAnnouncementsSidePanel()
    
    val mainContent = new HBox {
      spacing = 20
      padding = Insets(20)
      children = Seq(
        activityFeed,
        sidePanel
      )
    }
    
    val scrollContent = new ScrollPane {
      fitToWidth = true
      content = mainContent
    }
    
    new ScalaFXTab {
      text = "📢 Announcements"
      content = scrollContent
      closable = false
    }
  }
  
  private def createAnnouncementsSidePanel(): VBox = {
    new VBox {
      spacing = 20
      prefWidth = 300
      
      children = Seq(
        createQuickActionsCard(),
        createAnnouncementStatsCard(),
        createRecentAnnouncementsCard()
      )
    }
  }
  
  private def createQuickActionsCard(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: #f0f2f5; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("📢 Quick Actions") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        new Button("📝 Create Announcement") {
          style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6; -fx-pref-width: 250;"
          onAction = _ => {
            if (readOnlyMode) {
              onLoginPrompt()
            } else {
              val dialog = new AnnouncementDialog(() => refresh())
              dialog.showAndWait()
            }
          }
        }
      )
    }
  }
  
  private def createAnnouncementStatsCard(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("📊 Announcement Stats") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        createStatRow("Total Announcements", service.getAnnouncements.size.toString),
        createStatRow("This Week", service.getAnnouncements.count(_.timestamp.isAfter(java.time.LocalDateTime.now().minusWeeks(1))).toString),
        createStatRow("This Month", service.getAnnouncements.count(_.timestamp.isAfter(java.time.LocalDateTime.now().minusMonths(1))).toString)
      )
    }
  }
  
  private def createRecentAnnouncementsCard(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("📋 Categories") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        createCategoryItem("🚨 Emergency", "Urgent notifications"),
        createCategoryItem("📅 Events", "Event announcements"),
        createCategoryItem("🍕 Food Distribution", "Food sharing info"),
        createCategoryItem("💡 Tips", "Helpful community tips"),
        createCategoryItem("📢 General", "General announcements")
      )
    }
  }
  
  private def createStatRow(label: String, value: String): HBox = {
    new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new Label(label) {
          style = "-fx-text-fill: #65676b; -fx-font-size: 12px;"
        },
        new Region { HBox.setHgrow(this, scalafx.scene.layout.Priority.Always) },
        new Label(value) {
          style = "-fx-text-fill: #1877f2; -fx-font-weight: bold; -fx-font-size: 12px;"
        }
      )
    }
  }
  
  private def createCategoryItem(name: String, description: String): VBox = {
    new VBox {
      spacing = 2
      children = Seq(
        new Label(name) {
          style = "-fx-text-fill: #1c1e21; -fx-font-weight: bold; -fx-font-size: 12px;"
        },
        new Label(description) {
          style = "-fx-text-fill: #65676b; -fx-font-size: 10px;"
        }
      )
    }
  }
  
  override def refresh(): Unit = {
    // Refresh the activity feed to show latest announcements
    if (activityFeedComponent != null) {
      activityFeedComponent.refresh()
    }
  }
  
  override def initialize(): Unit = {
    // No special initialization needed
  }
}
