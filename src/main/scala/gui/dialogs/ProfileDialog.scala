package gui.dialogs

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import service.CommunityEngagementService

/**
 * Profile management dialog for user profile editing
 */
class ProfileDialog {
  
  private val service = CommunityEngagementService.getInstance
  
  def showAndWait(): Unit = {
    service.getCurrentUser match {
      case Some(user) =>
        val dialog = new Dialog[Unit]()
        dialog.title = "User Profile"
        dialog.headerText = s"Edit profile for ${user.username}"
        
        val nameField = new TextField {
          text = user.name
          promptText = "Full Name"
        }
        
        val contactField = new TextField {
          text = user.contactInfo
          promptText = "Contact Info"
        }
        
        val emailField = new TextField {
          text = user.email
          promptText = "Email"
          editable = false  // Email usually shouldn't be changed
        }
        
        val roleLabel = new Label(user.getUserRole) {
          style = "-fx-font-weight: bold;"
        }
        
        val regDateLabel = new Label(user.registrationDate.toLocalDate.toString)
        
        // Password change section
        val currentPasswordField = new PasswordField {
          promptText = "Current Password"
        }
        
        val newPasswordField = new PasswordField {
          promptText = "New Password (8+ chars, letter, digit, special char)"
        }
        
        val confirmPasswordField = new PasswordField {
          promptText = "Confirm New Password"
        }
        
        val changePasswordButton = new Button("Change Password") {
          onAction = _ => {
            val currentPass = currentPasswordField.text.value
            val newPass = newPasswordField.text.value
            val confirmPass = confirmPasswordField.text.value
            
            if (currentPass.isEmpty || newPass.isEmpty || confirmPass.isEmpty) {
              GuiUtils.showWarning("Missing Fields", "Please fill in all password fields.")
            } else if (newPass != confirmPass) {
              GuiUtils.showError("Password Mismatch", "New password and confirmation do not match.")
            } else if (!util.PasswordHasher.isPasswordValid(newPass)) {
              GuiUtils.showError("Invalid Password", util.PasswordHasher.getPasswordRequirements)
            } else {
              if (service.resetPassword(currentPass, newPass)) {
                GuiUtils.showInfo("Success", "Password changed successfully!")
                currentPasswordField.text = ""
                newPasswordField.text = ""
                confirmPasswordField.text = ""
              } else {
                GuiUtils.showError("Failed", "Current password is incorrect.")
              }
            }
          }
        }
        
        val grid = new GridPane {
          hgap = 10
          vgap = 10
          padding = Insets(20)
          
          // Profile information
          add(new Label("Username:"), 0, 0)
          add(new Label(user.username) { style = "-fx-font-weight: bold;" }, 1, 0)
          add(new Label("Email:"), 0, 1)
          add(emailField, 1, 1)
          add(new Label("Full Name:"), 0, 2)
          add(nameField, 1, 2)
          add(new Label("Contact Info:"), 0, 3)
          add(contactField, 1, 3)
          add(new Label("Role:"), 0, 4)
          add(roleLabel, 1, 4)
          add(new Label("Registration Date:"), 0, 5)
          add(regDateLabel, 1, 5)
          
          // Password change section
          add(new Separator(), 0, 6, 2, 1)
          add(new Label("Change Password") { style = "-fx-font-weight: bold; -fx-font-size: 14px;" }, 0, 7, 2, 1)
          add(new Label("Current Password:"), 0, 8)
          add(currentPasswordField, 1, 8)
          add(new Label("New Password:"), 0, 9)
          add(newPasswordField, 1, 9)
          add(new Label("Confirm Password:"), 0, 10)
          add(confirmPasswordField, 1, 10)
          add(changePasswordButton, 1, 11)
        }
        
        dialog.dialogPane().content = grid
        dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
        
        dialog.resultConverter = dialogButton => {
          if (dialogButton == ButtonType.OK) {
            if (service.updateUserProfile(nameField.text.value, contactField.text.value)) {
              GuiUtils.showInfo("Success", "Profile updated successfully!")
            } else {
              GuiUtils.showError("Failed", "Could not update profile.")
            }
          }
        }
        
        dialog.showAndWait()
        
      case None =>
        GuiUtils.showWarning("Not Logged In", "Please log in to view your profile.")
    }
  }
}
