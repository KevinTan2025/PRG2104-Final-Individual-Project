package gui.components.common.user

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.BaseTabComponent
import gui.dialogs._
import service.CommunityEngagementService
import model.User

/**
 * Enhanced Dashboard component for logged-in users with activity feed
 * 安全级别: USER - 注册用户可以查看的增强仪表板组件
 */
class DashboardComponent extends BaseTabComponent {
  
  override def build(): Tab = {
    val currentUser = service.getCurrentUser
    val userFeed = createUserActivityFeed(currentUser)
    val sidePanel = createUserSidePanel(currentUser)
    
    val mainContent = new HBox {
      spacing = 20
      padding = Insets(20)
      children = Seq(
        userFeed,
        sidePanel
      )
    }
    
    val scrollContent = new ScrollPane {
      fitToWidth = true
      content = mainContent
    }
    
    new Tab {
      text = "🏠 Dashboard"
      content = scrollContent
      closable = false
    }
  }
  
  private def createUserActivityFeed(currentUser: Option[User]): VBox = {
    val feedTitle = new Label("🔥 Community Activity Feed") {
      style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1877f2;"
    }
    
    val activityFeed = new VBox {
      spacing = 15
      padding = Insets(0, 20, 0, 0)
      prefWidth = 500
      
      children = Seq(
        feedTitle,
        createActivityPost("📢 System Announcement", "Welcome Week activities start tomorrow!", "Community Admin", "2 hours ago"),
        createActivityPost("🍕 Food Share", "Free pizza available in the main lobby", "Sarah Chen", "3 hours ago"),
        createActivityPost("🎉 Event Reminder", "Movie night this Friday at 7 PM in Room 301", "Events Team", "5 hours ago"),
        createActivityPost("🍜 Food Share", "Homemade soup available - perfect for cold weather!", "Mike Johnson", "6 hours ago"),
        createActivityPost("📅 New Event", "Study group for final exams - all welcome!", "Student Council", "1 day ago"),
        createQuickActionCard(currentUser)
      )
    }
    
    activityFeed
  }
  
  private def createActivityPost(category: String, content: String, author: String, time: String): VBox = {
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
              onAction = _ => println(s"Liked: $content")
            },
            new Button("💬 Comment") {
              style = "-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-border-color: transparent;"
              onAction = _ => println(s"Comment on: $content")
            },
            new Button("📤 Share") {
              style = "-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-border-color: transparent;"
              onAction = _ => println(s"Shared: $content")
            }
          )
        }
      )
    }
    
    postCard
  }
  
  private def createQuickActionCard(currentUser: Option[User]): VBox = {
    val actionCard = new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: #f0f2f5; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("✨ Quick Actions") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        new HBox {
          spacing = 10
          children = Seq(
            new Button("📢 New Post") {
              style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6;"
              onAction = _ => {
                val dialog = new AnnouncementDialog(() => refresh())
                dialog.showAndWait()
              }
            },
            new Button("🍕 Share Food") {
              style = "-fx-background-color: #42b883; -fx-text-fill: white; -fx-background-radius: 6;"
              onAction = _ => {
                val dialog = new FoodPostDialog(() => refresh())
                dialog.showAndWait()
              }
            },
            new Button("📅 Create Event") {
              style = "-fx-background-color: #e1306c; -fx-text-fill: white; -fx-background-radius: 6;"
              onAction = _ => {
                val dialog = new EventDialog(() => refresh())
                dialog.showAndWait()
              }
            }
          )
        }
      )
    }
    
    actionCard
  }
  
  private def createUserSidePanel(currentUser: Option[User]): VBox = {
    new VBox {
      spacing = 20
      prefWidth = 300
      
      children = Seq(
        createUserInfoCard(currentUser),
        createMyStatsCard(),
        createTrendingCard(),
        createUpcomingEventsCard()
      )
    }
  }
  
  private def createUserInfoCard(currentUser: Option[User]): VBox = {
    val userInfo = currentUser match {
      case Some(user) => 
        new VBox {
          spacing = 10
          padding = Insets(15)
          style = "-fx-background-color: linear-gradient(to bottom, #1877f2, #42b883); -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);"
          
          children = Seq(
            new Label(s"👤 ${user.name}") {
              style = "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;"
            },
            new Label(s"✉️ ${user.email}") {
              style = "-fx-text-fill: white; -fx-font-size: 12px;"
            },
            new Label(s"🎯 Role: ${user.getUserRole}") {
              style = "-fx-text-fill: white; -fx-font-size: 12px;"
            },
            new Label(s"🗓️ Member since: ${user.registrationDate.toLocalDate}") {
              style = "-fx-text-fill: white; -fx-font-size: 10px;"
            }
          )
        }
      case None => 
        new VBox {
          spacing = 10
          padding = Insets(15)
          style = "-fx-background-color: #f0f2f5; -fx-background-radius: 8;"
          
          children = Seq(
            new Label("👤 Guest User") {
              style = "-fx-font-weight: bold; -fx-text-fill: #65676b; -fx-font-size: 16px;"
            }
          )
        }
    }
    
    userInfo
  }
  
  private def createMyStatsCard(): VBox = {
    val stats = service.getDashboardStatistics
    
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("📊 My Activity") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        createStatRow("Posts Created", "12"),
        createStatRow("Events Joined", "5"),
        createStatRow("Food Shared", "8"),
        createStatRow("Comments Made", "24")
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
          onAction = _ => println("View all events")
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
    // Refresh dashboard data if needed
  }
  
  override def initialize(): Unit = {
    // Initial setup if needed
  }
}
