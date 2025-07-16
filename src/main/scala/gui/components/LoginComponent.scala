package gui.components

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils

/**
 * Login form component
 */
class LoginComponent(
  onLoginSuccess: () => Unit,
  onRegisterClick: () => Unit
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
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(loginButton, registerButton)
        },
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
