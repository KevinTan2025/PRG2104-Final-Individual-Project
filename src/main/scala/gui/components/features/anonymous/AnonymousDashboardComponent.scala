package gui.components.features.anonymous

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.BaseTabComponent

/**
 * Dashboard component for anonymous users
 * 安全级别: PUBLIC - 匿名用户的仪表板
 */
class AnonymousDashboardComponent(
  onLoginPrompt: () => Unit
) extends BaseTabComponent {
  
  def build(): Tab = {
    val welcomeCard = createWelcomeCard()
    val featuresCard = createFeaturesCard()
    val statsCard = createStatsCard()
    val joinCard = createJoinCard()
    
    val scrollContent = new ScrollPane {
      fitToWidth = true
      content = new VBox {
        spacing = 20
        padding = Insets(20)
        children = Seq(
          new HBox {
            spacing = 20
            children = Seq(welcomeCard, joinCard)
          },
          new HBox {
            spacing = 20
            children = Seq(featuresCard, statsCard)
          }
        )
      }
    }
    
    new Tab {
      text = "🏠 Welcome"
      content = scrollContent
      closable = false
    }
  }
  
  override def refresh(): Unit = {
    // Nothing to refresh for anonymous dashboard
  }
  
  override def initialize(): Unit = {
    // Nothing to initialize for anonymous dashboard
  }
  
  private def createWelcomeCard(): Region = {
    new VBox {
      spacing = 15
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
      prefWidth = 400
      children = Seq(
        new Label("👋 Welcome to Our Community!") {
          style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new Label("You are currently browsing in anonymous mode.") {
          style = "-fx-font-size: 14px; -fx-text-fill: #7f8c8d;"
        },
        new Separator(),
        new Label("🔍 What you can explore:") {
          style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
        },
        new VBox {
          spacing = 8
          children = Seq(
            new Label("• Browse community announcements") {
              style = "-fx-font-size: 13px; -fx-text-fill: #555555;"
            },
            new Label("• View food sharing opportunities") {
              style = "-fx-font-size: 13px; -fx-text-fill: #555555;"
            },
            new Label("• Read community discussions") {
              style = "-fx-font-size: 13px; -fx-text-fill: #555555;"
            },
            new Label("• Check upcoming events") {
              style = "-fx-font-size: 13px; -fx-text-fill: #555555;"
            },
            new Label("• Learn about our platform") {
              style = "-fx-font-size: 13px; -fx-text-fill: #555555;"
            }
          )
        }
      )
    }
  }
  
  private def createFeaturesCard(): Region = {
    new VBox {
      spacing = 15
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
      prefWidth = 400
      children = Seq(
        new Label("✨ Platform Features") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new GridPane {
          hgap = 15
          vgap = 10
          add(new Label("🍕") { style = "-fx-font-size: 20px;" }, 0, 0)
          add(new Label("Food Sharing") { style = "-fx-font-weight: bold;" }, 1, 0)
          add(new Label("Share surplus food with neighbors") { style = "-fx-font-size: 12px; -fx-text-fill: #666;" }, 1, 1)
          
          add(new Label("💬") { style = "-fx-font-size: 20px;" }, 0, 2)
          add(new Label("Community Chat") { style = "-fx-font-weight: bold;" }, 1, 2)
          add(new Label("Connect and discuss with community") { style = "-fx-font-size: 12px; -fx-text-fill: #666;" }, 1, 3)
          
          add(new Label("📅") { style = "-fx-font-size: 20px;" }, 0, 4)
          add(new Label("Events") { style = "-fx-font-weight: bold;" }, 1, 4)
          add(new Label("Organize and join community events") { style = "-fx-font-size: 12px; -fx-text-fill: #666;" }, 1, 5)
        }
      )
    }
  }
  
  private def createStatsCard(): Region = {
    // In a real app, these would be actual statistics
    new VBox {
      spacing = 15
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
      prefWidth = 400
      children = Seq(
        new Label("📊 Community Stats") {
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        },
        new GridPane {
          hgap = 20
          vgap = 15
          add(new Label("👥") { style = "-fx-font-size: 24px;" }, 0, 0)
          add(new Label("Active Members") { style = "-fx-font-weight: bold;" }, 1, 0)
          add(new Label("1,234+") { style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3498db;" }, 2, 0)
          
          add(new Label("🍽️") { style = "-fx-font-size: 24px;" }, 0, 1)
          add(new Label("Meals Shared") { style = "-fx-font-weight: bold;" }, 1, 1)
          add(new Label("5,678+") { style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;" }, 2, 1)
          
          add(new Label("🌟") { style = "-fx-font-size: 24px;" }, 0, 2)
          add(new Label("Events Hosted") { style = "-fx-font-weight: bold;" }, 1, 2)
          add(new Label("890+") { style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f39c12;" }, 2, 2)
        }
      )
    }
  }
  
  private def createJoinCard(): Region = {
    new VBox {
      spacing = 15
      padding = Insets(20)
      style = "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);"
      prefWidth = 400
      children = Seq(
        new Label("🚀 Ready to Join?") {
          style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"
        },
        new Label("Create your account to unlock all features!") {
          style = "-fx-font-size: 14px; -fx-text-fill: white; -fx-opacity: 0.9;"
        },
        new Separator() {
          style = "-fx-background-color: rgba(255,255,255,0.3);"
        },
        new VBox {
          spacing = 10
          children = Seq(
            new Label("✅ Post food items") { style = "-fx-text-fill: white; -fx-font-size: 13px;" },
            new Label("✅ Join discussions") { style = "-fx-text-fill: white; -fx-font-size: 13px;" },
            new Label("✅ Create events") { style = "-fx-text-fill: white; -fx-font-size: 13px;" },
            new Label("✅ Get notifications") { style = "-fx-text-fill: white; -fx-font-size: 13px;" },
            new Label("✅ Build your profile") { style = "-fx-text-fill: white; -fx-font-size: 13px;" }
          )
        },
        new Button("Create Account Now! 🎉") {
          prefWidth = Double.MaxValue
          style = "-fx-background-color: #fff; -fx-text-fill: #667eea; -fx-font-weight: bold; -fx-background-radius: 5;"
          onAction = (_: ActionEvent) => onLoginPrompt()
        }
      )
    }
  }
}
