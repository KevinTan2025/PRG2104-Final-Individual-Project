package gui.components.features.anonymous

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._
import gui.components.common.public.BaseTabComponent

/**
 * Anonymous Dashboard component with Facebook-style social media feed
 * 安全级别: PUBLIC - 匿名用户可以浏览的仪表板组件
 */
class AnonymousDashboardComponent(onLoginPrompt: () => Unit) extends BaseTabComponent {
  
  override def build(): Tab = {
    val mainContent = new HBox {
      spacing = 20
      padding = Insets(20)
      children = Seq(
        createActivityFeed(),
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
  
  private def createActivityFeed(): VBox = {
    val feedTitle = new Label("🔥 Community Activity Feed") {
      style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1877f2;"
    }
    
    val activityFeed = new VBox {
      spacing = 15
      padding = Insets(0, 20, 0, 0)
      prefWidth = 500
      
      children = Seq(
        feedTitle,
        createPostCard("📢 System Announcement", "Welcome Week activities start tomorrow!", "Community Admin", "2 hours ago"),
        createPostCard("🍕 Food Share", "Free pizza available in the main lobby - first come first served!", "Sarah Chen", "3 hours ago"),
        createPostCard("🎉 Event Reminder", "Movie night this Friday at 7 PM in Room 301. Popcorn provided!", "Events Team", "5 hours ago"),
        createPostCard("🍜 Food Share", "Homemade soup available - perfect for this cold weather!", "Mike Johnson", "6 hours ago"),
        createPostCard("📅 New Event", "Study group for final exams - all subjects welcome!", "Student Council", "1 day ago"),
        createJoinPromptCard()
      )
    }
    
    activityFeed
  }
  
  private def createPostCard(category: String, content: String, author: String, time: String): VBox = {
    val postCard = new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new HBox {
          spacing = 10
          alignment = Pos.CenterLeft
          children = Seq(
            new Label(category) {
              style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
            },
            new Region { HBox.setHgrow(this, Priority.Always) },
            new Label(time) {
              style = "-fx-text-fill: #65676b; -fx-font-size: 12px;"
            }
          )
        },
        new Label(content) {
          style = "-fx-font-size: 14px; -fx-text-fill: #050505;"
          wrapText = true
        },
        new HBox {
          spacing = 5
          alignment = Pos.CenterLeft
          children = Seq(
            new Label("by") {
              style = "-fx-text-fill: #65676b; -fx-font-size: 12px;"
            },
            new Label(author) {
              style = "-fx-text-fill: #1877f2; -fx-font-weight: bold; -fx-font-size: 12px;"
            }
          )
        },
        new HBox {
          spacing = 20
          alignment = Pos.CenterLeft
          children = Seq(
            new Button("👍 Like") {
              style = "-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-border-color: transparent;"
              onAction = _ => onLoginPrompt()
            },
            new Button("💬 Comment") {
              style = "-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-border-color: transparent;"
              onAction = _ => onLoginPrompt()
            },
            new Button("📤 Share") {
              style = "-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-border-color: transparent;"
              onAction = _ => onLoginPrompt()
            }
          )
        }
      )
    }
    
    postCard
  }
  
  private def createJoinPromptCard(): VBox = {
    val joinCard = new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: linear-gradient(to right, #1877f2, #42b883); -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("🌟 Join Our Community!") {
          style = "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;"
        },
        new Label("Create posts, share food, organize events, and connect with your community!") {
          style = "-fx-text-fill: white; -fx-font-size: 12px;"
          wrapText = true
        },
        new Button("Join Now - It's Free!") {
          style = "-fx-background-color: white; -fx-text-fill: #1877f2; -fx-font-weight: bold; -fx-background-radius: 6;"
          onAction = _ => onLoginPrompt()
        }
      )
    }
    
    joinCard
  }
  
  private def createSidePanel(): VBox = {
    new VBox {
      spacing = 20
      prefWidth = 300
      
      children = Seq(
        createWelcomeCard(),
        createStatsCard(),
        createTrendingCard(),
        createUpcomingEventsCard()
      )
    }
  }
  
  private def createWelcomeCard(): VBox = {
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
        new Region { HBox.setHgrow(this, Priority.Always) },
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
        new Region { HBox.setHgrow(this, Priority.Always) },
        new Label(count) {
          style = "-fx-text-fill: #65676b; -fx-font-size: 10px;"
        }
      )
    }
  }
  
  private def createEventItem(eventName: String, time: String): VBox = {
    new VBox {
      spacing = 3
      children = Seq(
        new Label(eventName) {
          style = "-fx-text-fill: #050505; -fx-font-weight: bold; -fx-font-size: 12px;"
        },
        new Label(time) {
          style = "-fx-text-fill: #65676b; -fx-font-size: 10px;"
        }
      )
    }
  }
  
  override def refresh(): Unit = {
    // Refresh feed content
  }
  
  override def initialize(): Unit = {
    // Initialize component
  }
}
