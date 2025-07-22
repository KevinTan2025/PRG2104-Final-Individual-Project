package gui.components.features.anonymous

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._
import gui.components.common.public.BaseTabComponent
import gui.components.features.activityfeed.EnhancedActivityFeedComponent

/**
 * Anonymous Dashboard component with enhanced activity feed
 * 安全级别: PUBLIC - 匿名用户可以浏览的仪表板组件
 */
class AnonymousDashboardComponent(onLoginPrompt: () => Unit) extends BaseTabComponent {
  
  private var activityFeedComponent: EnhancedActivityFeedComponent = _
  
  override def build(): Tab = {
    // Anonymous home tab shows all activity types
    activityFeedComponent = new EnhancedActivityFeedComponent(service, () => refresh(), None)
    val activityFeed = activityFeedComponent.build()
    
    val mainContent = new HBox {
      spacing = 20
      padding = Insets(20)
      children = Seq(
        activityFeed,
        createSidePanel()
      )
    }
    
    val scrollContent = new ScrollPane {
      fitToWidth = true
      content = mainContent
    }
    
    new Tab {
      text = "🏠 Home"
      content = scrollContent
      closable = false
    }
  }
  
  private def createSidePanel(): VBox = {
    new VBox {
      spacing = 20
      prefWidth = 300
      
      children = Seq(
        createJoinPromptCard(),
        createStatsCard(),
        createTrendingCard(),
        createUpcomingEventsCard()
      )
    }
  }
  
  private def createJoinPromptCard(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: linear-gradient(to bottom, #e3f2fd, #f3e5f5); -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("👋 Welcome to our Community!") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        new Label("You're browsing as a guest. Join us to participate fully!") {
          style = "-fx-text-fill: #424242; -fx-font-size: 12px;"
          wrapText = true
        },
        new Button("Sign Up") {
          style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6; -fx-pref-width: 200;"
          onAction = _ => onLoginPrompt()
        }
      )
    }
  }
  
  private def createStatsCard(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("📊 Community Stats") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        createStatRow("Active Members", "245"),
        createStatRow("Posts Today", "18"),
        createStatRow("Food Shared", "12"),
        createStatRow("Events This Week", "7")
      )
    }
  }
  
  private def createTrendingCard(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("🔥 Trending Topics") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        createTrendingItem("#StudyGroups", "45 posts"),
        createTrendingItem("#FreeFood", "32 posts"),
        createTrendingItem("#MovieNight", "28 posts"),
        createTrendingItem("#FinalExams", "21 posts")
      )
    }
  }
  
  private def createUpcomingEventsCard(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("📅 Upcoming Events") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        createEventItem("Movie Night", "Tomorrow 7:00 PM"),
        createEventItem("Study Group", "Friday 2:00 PM"),
        createEventItem("Food Festival", "Next Monday"),
        new Button("View All Events") {
          style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6; -fx-pref-width: 200;"
          onAction = _ => onLoginPrompt()
        }
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
  
  private def createTrendingItem(hashtag: String, count: String): HBox = {
    new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new Label(hashtag) {
          style = "-fx-text-fill: #1877f2; -fx-font-weight: bold; -fx-font-size: 12px;"
        },
        new Region { HBox.setHgrow(this, scalafx.scene.layout.Priority.Always) },
        new Label(count) {
          style = "-fx-text-fill: #65676b; -fx-font-size: 11px;"
        }
      )
    }
  }
  
  private def createEventItem(eventName: String, time: String): HBox = {
    new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("📅") {
          style = "-fx-font-size: 12px;"
        },
        new VBox {
          spacing = 2
          children = Seq(
            new Label(eventName) {
              style = "-fx-text-fill: #1c1e21; -fx-font-weight: bold; -fx-font-size: 12px;"
            },
            new Label(time) {
              style = "-fx-text-fill: #65676b; -fx-font-size: 10px;"
            }
          )
        }
      )
    }
  }
  
  override def refresh(): Unit = {
    // Refresh anonymous dashboard activity feed to show latest data
    if (activityFeedComponent != null) {
      activityFeedComponent.refresh()
    }
  }
  
  override def initialize(): Unit = {
    // No initialization needed for anonymous dashboard
  }
}
