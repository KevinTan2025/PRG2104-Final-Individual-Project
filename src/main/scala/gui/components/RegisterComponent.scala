package gui.components

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils

/**
 * Registration form component
 */
class RegisterComponent(
  onRegistrationSuccess: () => Unit,
  onBackClick: () => Unit
) extends BaseComponent {
  
  private val usernameField = new TextField { promptText = "Username" }
  private val emailField = new TextField { promptText = "Email" }
  private val nameField = new TextField { promptText = "Full Name" }
  private val contactField = new TextField { promptText = "Contact Info" }
  private val passwordField = new PasswordField { promptText = "Password (8+ chars, letter, digit, special char)" }
  private val confirmPasswordField = new PasswordField { promptText = "Confirm Password" }
  private val adminCheckBox = new CheckBox("Register as Administrator")
  
  override def build(): Region = {
    val registerButton = new Button("Register") {
      onAction = (_: ActionEvent) => handleRegistration()
    }
    
    val backButton = new Button("Back to Login") {
      onAction = (_: ActionEvent) => onBackClick()
    }
    
    val formBox = new VBox {
      spacing = 15
      alignment = Pos.Center
      children = Seq(
        new Label("Register New Account") {
          style = "-fx-font-size: 20px; -fx-font-weight: bold;"
        },
        usernameField,
        emailField,
        nameField,
        contactField,
        passwordField,
        confirmPasswordField,
        adminCheckBox,
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(registerButton, backButton)
        }
      )
      padding = Insets(50)
    }
    
    new BorderPane {
      center = formBox
      style = "-fx-background-color: #f5f5f5;"
    }
  }
  
  private def handleRegistration(): Unit = {
    val username = usernameField.text.value
    val email = emailField.text.value
    val name = nameField.text.value
    val contact = contactField.text.value
    val password = passwordField.text.value
    val confirmPassword = confirmPasswordField.text.value
    val isAdmin = adminCheckBox.selected.value
    
    if (username.nonEmpty && email.nonEmpty && name.nonEmpty && password.nonEmpty) {
      if (password != confirmPassword) {
        GuiUtils.showError("Password Mismatch", "Password and confirmation do not match.")
      } else if (!util.PasswordHasher.isPasswordValid(password)) {
        GuiUtils.showError("Invalid Password", util.PasswordHasher.getPasswordRequirements)
      } else if (service.registerUser(username, email, name, contact, password, isAdmin)) {
        GuiUtils.showInfo("Registration Successful", "Account created successfully!")
        onRegistrationSuccess()
      } else {
        GuiUtils.showError("Registration Failed", "Username or email already exists.")
      }
    } else {
      GuiUtils.showWarning("Incomplete Information", "Please fill in all required fields.")
    }
  }
}
