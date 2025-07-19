package gui.components.dashboards

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.dialogs._
import gui.utils.GuiUtils
import service.CommunityEngagementService

/**
 * User-focused dashboard with personal stats and community interaction
 */
class UserDashboard(service: CommunityEngagementService) {

  def build(): Tab = {
    // Use placeholder stats for now
    val stats = Map(
      "postsShared" -> 23,
      "helpedFamilies" -> 8,
      "eventsJoined" -> 5,
      "communityPoints" -> 245
    )
    
    val personalStatsSection = createPersonalStatsSection(stats)
    val communityHighlightsSection = createCommunityHighlightsSection()
    val personalActivitySection = createPersonalActivitySection()
    val communityInteractionSection = createCommunityInteractionSection()
    
    val dashboardContent = new VBox {
      spacing = 20
      padding = Insets(20)
      children = Seq(
        new Label("🏠 User Dashboard") {
          style = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        personalStatsSection,
        new HBox {
          spacing = 20
          children = Seq(communityHighlightsSection, personalActivitySection)
        },
        communityInteractionSection
      )
    }
    
    new Tab {
      text = "Dashboard"
      content = new ScrollPane {
        content = dashboardContent
        fitToWidth = true
        hbarPolicy = ScrollPane.ScrollBarPolicy.Never
        vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
      }
      closable = false
    }
  }

  private def createPersonalStatsSection(stats: Map[String, Any]): HBox = {
    new HBox {
      spacing = 20
      children = Seq(
        createStatCard("📦 Posts Shared", stats("postsShared").toString, "#e8f5e8"),
        createStatCard("👨‍👩‍👧‍👦 Families Helped", stats("helpedFamilies").toString, "#e3f2fd"),
        createStatCard("📅 Events Joined", stats("eventsJoined").toString, "#fff3e0"),
        createStatCard("⭐ Community Points", stats("communityPoints").toString, "#f3e5f5")
      )
    }
  }

  private def createStatCard(title: String, value: String, bgColor: String): VBox = {
    new VBox {
      spacing = 10
      alignment = Pos.Center
      padding = Insets(20)
      prefWidth = 180
      prefHeight = 120
      style = s"-fx-background-color: $bgColor; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;"
      children = Seq(
        new Label(value) {
          style = "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new Label(title) {
          style = "-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-text-alignment: center;"
          wrapText = true
        }
      )
    }
  }

  private def createCommunityHighlightsSection(): VBox = {
    new VBox {
      spacing = 15
      children = Seq(
        new Label("🌟 Community Highlights") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new ListView[String] {
          items = scalafx.collections.ObservableBuffer(
            "🎉 Mary shared 10kg of fresh vegetables with the community!",
            "📢 Upcoming: Community Garden Workshop this Saturday",
            "🍞 Bob's Bakery donated bread to 5 families in need",
            "🌱 New nutrition education program starting next week",
            "👥 15 new community members joined this month!"
          )
          prefHeight = 140
          style = "-fx-font-size: 13px;"
          cellFactory = { (_: ListView[String]) =>
            new ListCell[String]() {
              item.onChange { (_, _, newValue) =>
                if (newValue != null) {
                  text = newValue
                  style = "-fx-padding: 8px; -fx-background-color: #f8f9fa;"
                }
              }
            }
          }
        }
      )
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10;"
    }
  }
  
  private def createPersonalActivitySection(): VBox = {
    new VBox {
      spacing = 15
      children = Seq(
        new Label("📈 My Recent Activity") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new ListView[String] {
          items = scalafx.collections.ObservableBuffer(
            "✅ You shared vegetables with the Chen family",
            "📢 You created: 'Nutrition Workshop Announcement'",
            "👍 You liked Mary's food sharing post",
            "💬 You commented on the 'Sustainable Cooking' discussion",
            "📅 You RSVP'd to the Community Potluck event"
          )
          prefHeight = 140
          style = "-fx-font-size: 13px;"
          cellFactory = { (_: ListView[String]) =>
            new ListCell[String]() {
              item.onChange { (_, _, newValue) =>
                if (newValue != null) {
                  text = newValue
                  style = "-fx-padding: 8px; -fx-background-color: #f8f9fa;"
                }
              }
            }
          }
        }
      )
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10;"
    }
  }
  
  private def createCommunityInteractionSection(): VBox = {
    new VBox {
      spacing = 15
      children = Seq(
        new Label("💬 Community Chat") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        createChatArea(),
        createChatInput()
      )
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 10;"
    }
  }
  
  private def createChatArea(): ScrollPane = {
    val chatMessages = new VBox {
      spacing = 10
      padding = Insets(15)
      children = Seq(
        createChatMessage("Alice Johnson", "Thanks everyone for the amazing vegetables yesterday!", "2 min ago", false),
        createChatMessage("You", "Glad to help! More tomatoes coming this weekend 🍅", "1 min ago", true),
        createChatMessage("Bob Smith", "Looking forward to the nutrition workshop!", "30 sec ago", false)
      )
    }
    
    new ScrollPane {
      content = chatMessages
      fitToWidth = true
      prefHeight = 150
      hbarPolicy = ScrollPane.ScrollBarPolicy.Never
      vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
      style = "-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;"
    }
  }
  
  private def createChatInput(): HBox = {
    val messageField = new TextField {
      promptText = "Type your message here..."
      prefWidth = 400
      style = "-fx-font-size: 14px; -fx-border-radius: 20; -fx-background-radius: 20;"
    }
    
    val sendButton = new Button("Send") {
      style = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-border-radius: 20; -fx-background-radius: 20;"
      onAction = (_: ActionEvent) => {
        if (messageField.text.value.trim.nonEmpty) {
          GuiUtils.showInfo("Message Sent", s"Your message '${messageField.text.value}' has been sent!")
          messageField.text = ""
        }
      }
    }
    
    new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(messageField, sendButton)
    }
  }
  
  private def createChatMessage(username: String, message: String, time: String, isCurrentUser: Boolean): HBox = {
    new HBox {
      alignment = if (isCurrentUser) Pos.CenterRight else Pos.CenterLeft
      children = {
        val messageBox = new VBox {
          spacing = 2
          alignment = if (isCurrentUser) Pos.CenterRight else Pos.CenterLeft
          maxWidth = 300
          children = Seq(
            new Label(username) {
              style = s"-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: ${if (isCurrentUser) "#007bff" else "#6c757d"};"
            },
            new Label(message) {
              style = "-fx-font-size: 13px; -fx-text-fill: #333; -fx-wrap-text: true;"
              wrapText = true
            },
            new Label(time) {
              style = "-fx-font-size: 10px; -fx-text-fill: #aaa;"
            }
          )
          style = s"-fx-background-color: ${if (isCurrentUser) "#007bff" else "#e9ecef"}; -fx-background-radius: 15; -fx-padding: 10px 15px;"
        }
        
        if (isCurrentUser) Seq(messageBox) else Seq(messageBox)
      }
      padding = Insets(5, 0, 5, 0)
    }
  }
}
