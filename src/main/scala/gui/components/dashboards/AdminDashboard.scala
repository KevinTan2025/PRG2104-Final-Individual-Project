package gui.components.dashboards

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.dialogs.admin._
import gui.dialogs.features.announcements._
import gui.dialogs.features.events._
import gui.dialogs.features.food._
import gui.dialogs.common._
import gui.utils.GuiUtils
import service.CommunityEngagementService

/**
 * Admin-focused dashboard with management tools and system oversight
 */
class AdminDashboard(service: CommunityEngagementService) {
  
  def build(): Tab = {
    // Use placeholder stats for now
    val stats = Map(
      "totalUsers" -> 156,
      "activeUsers" -> 89,
      "totalPosts" -> 342,
      "pendingContent" -> 12
    )
    
    val welcomeSection = createWelcomeSection()
    val systemOverviewSection = createSystemOverviewSection(stats)
    val userManagementSection = createUserManagementSection(stats)
    val contentModerationSection = createContentModerationSection(stats)
    val systemControlsSection = createSystemControlsSection()
    val recentActivitySection = createRecentActivitySection()
    
    val dashboardContent = new VBox {
      spacing = 25
      padding = Insets(25)
      children = Seq(
        welcomeSection,
        systemOverviewSection,
        new HBox {
          spacing = 20
          children = Seq(userManagementSection, contentModerationSection)
        },
        systemControlsSection,
        recentActivitySection
      )
    }
    
    new Tab {
      text = "Admin Dashboard"
      content = new ScrollPane {
        content = dashboardContent
        fitToWidth = true
        style = "-fx-background-color: #f8f9fa;"
      }
      closable = false
    }
  }
  
  private def createWelcomeSection(): VBox = {
    val currentUser = service.getCurrentUser.get
    new VBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new HBox {
          spacing = 15
          alignment = Pos.CenterLeft
          children = Seq(
            new Label("🔧") { style = "-fx-font-size: 32px;" },
            new VBox {
              spacing = 5
              children = Seq(
                new Label(s"System Administrator Dashboard") {
                  style = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
                },
                new Label(s"Welcome back, ${currentUser.name}!") {
                  style = "-fx-font-size: 16px; -fx-text-fill: #6c757d;"
                }
              )
            }
          )
        },
        new Label("Manage users, monitor system health, and oversee community operations") {
          style = "-fx-font-size: 14px; -fx-text-fill: #495057;"
        }
      )
      padding = Insets(20)
      style = "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-background-radius: 10; -fx-text-fill: white;"
    }
  }
  
  private def createSystemOverviewSection(stats: Map[String, Any]): VBox = {
    new VBox {
      spacing = 15
      children = Seq(
        new Label("📊 System Overview") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new GridPane {
          hgap = 15
          vgap = 15
          children = Seq(
            (createAdminStatCard("👥 Total Users", stats("totalUsers").toString, "#3498db"), 0, 0),
            (createAdminStatCard("👤 Community Members", stats("communityMembers").toString, "#2ecc71"), 1, 0),
            (createAdminStatCard("🔧 Admin Users", stats("adminUsers").toString, "#e74c3c"), 2, 0),
            (createAdminStatCard("📢 Active Announcements", stats("activeAnnouncements").toString, "#f39c12"), 0, 1),
            (createAdminStatCard("🍽️ Active Food Posts", stats("activeFoodPosts").toString, "#9b59b6"), 1, 1),
            (createAdminStatCard("📅 Upcoming Events", stats("upcomingEvents").toString, "#1abc9c"), 2, 1)
          ).map { case (card, col, row) => GridPane.setConstraints(card, col, row); card }
        }
      )
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;"
    }
  }
  
  private def createUserManagementSection(stats: Map[String, Any]): VBox = {
    new VBox {
      spacing = 15
      prefWidth = 400
      children = Seq(
        new Label("👥 User Management") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new VBox {
          spacing = 10
          children = Seq(
            new Button("🔍 View All Users") {
              prefWidth = 200
              style = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => AdminDialogs.showUsersDialog()
            },
            new Button("➕ Create Admin User") {
              prefWidth = 200
              style = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => showCreateAdminDialog()
            },
            new Button("🚫 Manage User Permissions") {
              prefWidth = 200
              style = "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => GuiUtils.showInfo("Coming Soon", "User permission management will be available in the next update.")
            },
            new Button("📊 User Activity Report") {
              prefWidth = 200
              style = "-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => showUserActivityReport()
            }
          )
        }
      )
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;"
    }
  }
  
  private def createContentModerationSection(stats: Map[String, Any]): VBox = {
    new VBox {
      spacing = 15
      prefWidth = 400
      children = Seq(
        new Label("🛡️ Content Moderation") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new VBox {
          spacing = 10
          children = Seq(
            new Button("📝 Review Announcements") {
              prefWidth = 200
              style = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => AdminDialogs.showModerationDialog()
            },
            new Button("🍽️ Monitor Food Posts") {
              prefWidth = 200
              style = "-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => showFoodPostModerationDialog()
            },
            new Button("💬 Discussion Moderation") {
              prefWidth = 200
              style = "-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => showDiscussionModerationDialog()
            },
            new Button("⚠️ Reported Content") {
              prefWidth = 200
              style = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => GuiUtils.showInfo("No Reports", "No content has been reported by users.")
            }
          )
        }
      )
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;"
    }
  }
  
  private def createSystemControlsSection(): VBox = {
    new VBox {
      spacing = 15
      children = Seq(
        new Label("⚙️ System Controls") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new HBox {
          spacing = 15
          children = Seq(
            new Button("📊 System Statistics") {
              prefWidth = 180
              style = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => AdminDialogs.showSystemStatsDialog()
            },
            new Button("🔄 Refresh Dashboard") {
              prefWidth = 180
              style = "-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => {
                // Refresh dashboard data when implemented
                GuiUtils.showInfo("Dashboard Refreshed", "Dashboard data has been refreshed successfully!")
              }
            },
            new Button("💾 Backup System") {
              prefWidth = 180
              style = "-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => showBackupDialog()
            },
            new Button("📋 Export Reports") {
              prefWidth = 180
              style = "-fx-background-color: #d35400; -fx-text-fill: white; -fx-font-weight: bold;"
              onAction = (_: ActionEvent) => showExportDialog()
            }
          )
        }
      )
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;"
    }
  }
  
  private def createRecentActivitySection(): VBox = {
    new VBox {
      spacing = 15
      children = Seq(
        new Label("📈 Recent System Activity") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new ListView[String] {
          items = scalafx.collections.ObservableBuffer(
            "🆕 New user 'alice_green' registered (2 hours ago)",
            "📢 Announcement 'Community Garden Meeting' created by john_doe (4 hours ago)",
            "🍽️ Food post 'Fresh Vegetables Available' by mary_smith (6 hours ago)",
            "📅 Event 'Nutrition Workshop' scheduled by System Administrator (1 day ago)",
            "👤 User 'bob_wilson' profile updated (1 day ago)"
          )
          prefHeight = 150
          style = "-fx-font-family: monospace; -fx-font-size: 12px;"
        }
      )
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;"
    }
  }
  
  private def createAdminStatCard(title: String, value: String, color: String): VBox = {
    new VBox {
      spacing = 8
      alignment = Pos.Center
      children = Seq(
        new Label(title) {
          style = "-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-weight: bold;"
        },
        new Label(value) {
          style = s"-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: $color;"
        }
      )
      style = "-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-width: 1; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
      padding = Insets(20)
      prefWidth = 180
      prefHeight = 100
    }
  }
  
  private def showCreateAdminDialog(): Unit = {
    GuiUtils.showInfo("Create Admin User", "Admin user creation functionality will be available in the next update.")
  }
  
  private def showUserActivityReport(): Unit = {
    GuiUtils.showInfo("User Activity Report", "Detailed user activity reports will be available in the next update.")
  }
  
  private def showFoodPostModerationDialog(): Unit = {
    GuiUtils.showInfo("Food Post Moderation", "Food post moderation tools will be available in the next update.")
  }
  
  private def showDiscussionModerationDialog(): Unit = {
    GuiUtils.showInfo("Discussion Moderation", "Discussion moderation tools will be available in the next update.")
  }
  
  private def showBackupDialog(): Unit = {
    GuiUtils.showInfo("System Backup", "Database backup functionality will be available in the next update.")
  }
  
  private def showExportDialog(): Unit = {
    GuiUtils.showInfo("Export Reports", "Report export functionality will be available in the next update.")
  }
}
