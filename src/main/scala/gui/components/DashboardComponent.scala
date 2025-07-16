package gui.components

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.dialogs._

/**
 * Dashboard component showing platform statistics and quick actions
 */
class DashboardComponent extends BaseTabComponent {
  
  override def build(): Tab = {
    val stats = service.getDashboardStatistics
    
    val statsGrid = new GridPane {
      hgap = 20
      vgap = 15
      padding = Insets(20)
      
      // Row 0
      add(createStatCard("Total Users", stats("totalUsers").toString), 0, 0)
      add(createStatCard("Community Members", stats("communityMembers").toString), 1, 0)
      add(createStatCard("Admin Users", stats("adminUsers").toString), 2, 0)
      
      // Row 1
      add(createStatCard("Active Announcements", stats("activeAnnouncements").toString), 0, 1)
      add(createStatCard("Active Food Posts", stats("activeFoodPosts").toString), 1, 1)
      add(createStatCard("Upcoming Events", stats("upcomingEvents").toString), 2, 1)
      
      // Row 2
      add(createStatCard("Completed Food Posts", stats("completedFoodPosts").toString), 0, 2)
      add(createStatCard("Total Events", stats("totalEvents").toString), 1, 2)
      add(createStatCard("Unread Notifications", stats("unreadNotifications").toString), 2, 2)
    }
    
    val welcomeLabel = service.getCurrentUser match {
      case Some(user) => 
        new Label(s"Welcome to the Community Engagement Platform, ${user.name}!") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        }
      case None => 
        new Label("Welcome to the Community Engagement Platform!") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        }
    }
    
    val quickActionsPanel = createQuickActionsPanel()
    val adminPanel = createAdminPanel()
    
    val dashboardContent = new VBox {
      spacing = 20
      padding = Insets(20)
      children = Seq(
        welcomeLabel,
        new Label("Platform Statistics:") {
          style = "-fx-font-size: 16px; -fx-font-weight: bold;"
        },
        statsGrid,
        quickActionsPanel
      ) ++ (if (service.isCurrentUserAdmin) Seq(adminPanel) else Seq.empty)
    }
    
    new Tab {
      text = "Dashboard"
      content = new ScrollPane {
        content = dashboardContent
        fitToWidth = true
      }
      closable = false
    }
  }
  
  private def createStatCard(title: String, value: String): VBox = {
    new VBox {
      spacing = 5
      alignment = Pos.Center
      children = Seq(
        new Label(title) {
          style = "-fx-font-size: 12px; -fx-text-fill: gray;"
        },
        new Label(value) {
          style = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2196F3;"
        }
      )
      style = "-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;"
      padding = Insets(15)
      prefWidth = 150
      prefHeight = 80
    }
  }
  
  private def createQuickActionsPanel(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(20)
      style = "-fx-background-color: #e8f5e8; -fx-border-color: #4caf50; -fx-border-radius: 5;"
      children = Seq(
        new Label("Quick Actions") {
          style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;"
        },
        new HBox {
          spacing = 10
          children = Seq(
            new Button("Create Announcement") {
              onAction = (_: ActionEvent) => {
                val dialog = new AnnouncementDialog(() => refresh())
                dialog.showAndWait()
              }
            },
            new Button("Create Food Post") {
              onAction = (_: ActionEvent) => {
                val dialog = new FoodPostDialog(() => refresh())
                dialog.showAndWait()
              }
            },
            new Button("Create Event") {
              onAction = (_: ActionEvent) => {
                val dialog = new EventDialog(() => refresh())
                dialog.showAndWait()
              }
            }
          )
        }
      )
    }
  }
  
  private def createAdminPanel(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(20)
      style = "-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-radius: 5;"
      children = Seq(
        new Label("Admin Panel") {
          style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;"
        },
        new HBox {
          spacing = 10
          children = Seq(
            new Button("View All Users") {
              onAction = (_: ActionEvent) => AdminDialogs.showUsersDialog()
            },
            new Button("System Stats") {
              onAction = (_: ActionEvent) => AdminDialogs.showSystemStatsDialog()
            },
            new Button("Moderate Content") {
              onAction = (_: ActionEvent) => AdminDialogs.showModerationDialog()
            }
          )
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
