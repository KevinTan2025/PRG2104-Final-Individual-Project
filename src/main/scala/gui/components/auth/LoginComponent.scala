package gui.components.auth

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.BaseComponent

/**
 * Login form component
 * 安全级别: PUBLIC - 登录功能对所有人开放
 */
class LoginComponent(
  onLoginSuccess: () => Unit,
  onRegisterClick: () => Unit,
  onBrowseAnonymouslyClick: Option[() => Unit] = None
) extends BaseComponent {
  
  private val usernameField = new TextField {
    promptText = "Username"
    prefWidth = 250
  }
  
  private val passwordField = new PasswordField {
    promptText = "Password"
    prefWidth = 250
  }
  
  override def build(): Region = {
    val loginButton = new Button("Login") {
      prefWidth = 100
      onAction = (_: ActionEvent) => handleLogin()
    }
    
    val registerButton = new Button("Register") {
      prefWidth = 100
      onAction = (_: ActionEvent) => onRegisterClick()
    }
    
    val browseButton = onBrowseAnonymouslyClick.map { callback =>
      new Button("Browse Anonymously") {
        prefWidth = 150
        style = "-fx-background-color: #6c757d; -fx-text-fill: white;"
        onAction = (_: ActionEvent) => callback()
      }
    }
    
    val buttonBox = browseButton match {
      case Some(btn) =>
        new VBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(
            new HBox {
              spacing = 10
              alignment = Pos.Center
              children = Seq(loginButton, registerButton)
            },
            new Label("OR") {
              style = "-fx-font-size: 12px; -fx-text-fill: gray;"
            },
            btn
          )
        }
      case None =>
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(loginButton, registerButton)
        }
    }
    
    val loginBox = new VBox {
      spacing = 15
      alignment = Pos.Center
      children = Seq(
        new Label("Community Engagement Platform") {
          style = "-fx-font-size: 24px; -fx-font-weight: bold;"
        },
        new Label("Facilitating community collaboration for food security") {
          style = "-fx-font-size: 14px; -fx-text-fill: gray;"
        },
        new Separator(),
        usernameField,
        passwordField,
        buttonBox,
        new Label("Demo credentials: admin/Admin123*, john_doe/Password123!") {
          style = "-fx-font-size: 12px; -fx-text-fill: gray;"
        }
      )
      padding = Insets(50)
    }
    
    new BorderPane {
      center = loginBox
      style = "-fx-background-color: #f5f5f5;"
    }
  }
  
  private def handleLogin(): Unit = {
    val username = usernameField.text.value
    val password = passwordField.text.value
    
    service.login(username, password) match {
      case Some(user) =>
        GuiUtils.showInfo("Login Successful", s"Welcome, ${user.name}!")
        onLoginSuccess()
      case None =>
        GuiUtils.showError("Login Failed", "Invalid username or password.")
        passwordField.text = ""
    }
  }
}
