package gui.components.auth

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.BaseComponent

/**
 * Welcome screen component that allows users to choose between login/register or browse anonymously
 * 安全级别: PUBLIC - 欢迎界面对所有人开放
 */
class WelcomeComponent(
  onLoginClick: () => Unit,
  onRegisterClick: () => Unit,
  onBrowseAnonymouslyClick: () => Unit
) extends BaseComponent {
  
  override def build(): Region = {
    val loginButton = new Button("🔐 Login") {
      prefWidth = 180
      prefHeight = 50
      style = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => onLoginClick()
    }
    
    val registerButton = new Button("📝 Register") {
      prefWidth = 180
      prefHeight = 50
      style = "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => onRegisterClick()
    }
    
    val browseButton = new Button("👀 Browse Anonymously") {
      prefWidth = 180
      prefHeight = 50
      style = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => onBrowseAnonymouslyClick()
    }
    
    val welcomeBox = new VBox {
      spacing = 25
      alignment = Pos.Center
      children = Seq(
        new VBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(
            new Label("🏠 Community Engagement Platform") {
              style = "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #333333;"
            },
            new Label("Facilitating community collaboration for food security") {
              style = "-fx-font-size: 16px; -fx-text-fill: #666666;"
            }
          )
        },
        new Separator() {
          style = "-fx-background-color: #e0e0e0;"
        },
        new VBox {
          spacing = 15
          alignment = Pos.Center
          children = Seq(
            new Label("Welcome! How would you like to proceed?") {
              style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #444444;"
            },
            new VBox {
              spacing = 15
              alignment = Pos.Center
              children = Seq(
                new HBox {
                  spacing = 15
                  alignment = Pos.Center
                  children = Seq(loginButton, registerButton)
                },
                new VBox {
                  spacing = 8
                  alignment = Pos.Center
                  children = Seq(
                    new Label("OR") {
                      style = "-fx-font-size: 14px; -fx-text-fill: #888888; -fx-font-weight: bold;"
                    },
                    browseButton,
                    new Label("(Limited features - login for full access)") {
                      style = "-fx-font-size: 12px; -fx-text-fill: #999999; -fx-font-style: italic;"
                    }
                  )
                }
              )
            }
          )
        },
        new Separator() {
          style = "-fx-background-color: #e0e0e0;"
        },
        new VBox {
          spacing = 8
          alignment = Pos.Center
          children = Seq(
            new Label("✨ Preview what you can explore anonymously:") {
              style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #5a5a5a;"
            },
            new Label("• View community announcements") {
              style = "-fx-font-size: 12px; -fx-text-fill: #777777;"
            },
            new Label("• Browse food sharing posts") {
              style = "-fx-font-size: 12px; -fx-text-fill: #777777;"
            },
            new Label("• Read community discussions") {
              style = "-fx-font-size: 12px; -fx-text-fill: #777777;"
            },
            new Label("• Check upcoming events") {
              style = "-fx-font-size: 12px; -fx-text-fill: #777777;"
            },
            new Label("• Learn about the platform") {
              style = "-fx-font-size: 12px; -fx-text-fill: #777777;"
            }
          )
        }
      )
      padding = Insets(40)
    }
    
    new BorderPane {
      center = welcomeBox
      style = "-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);"
    }
  }
}
