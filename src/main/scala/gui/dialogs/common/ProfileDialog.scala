package gui.dialogs.common

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.components.common.public.EnhancedTextField
import service.CommunityEngagementService

/**
 * Profile management dialog for user profile editing
 */
class ProfileDialog {
  
  private val service = CommunityEngagementService.getInstance
  
  def showAndWait(): Unit = {
    service.currentUserInfo match {
      case Some(user) =>
        val dialog = new Dialog[Unit]()
        dialog.title = "User Profile"
        dialog.headerText = s"Edit profile for ${user.username}"
        dialog.resizable = true
        
        val nameField = new EnhancedTextField("Full Name") {
          text = user.name
        }
        
        val contactField = new EnhancedTextField("Contact Info") {
          text = user.contactInfo
        }
        
        val emailField = new EnhancedTextField("Email") {
          text = user.email
          editable = false  // Email usually shouldn't be changed
        }
        
        val roleLabel = new Label(user.userRole) {
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
              GuiUtils.showError("Invalid Password", util.PasswordHasher.passwordRequirements)
            } else {
              if (service.resetPassword(currentPass, newPass)) {
                GuiUtils.showInfo("Success", "Password changed successfully!")
                currentPasswordField.text = ""
                newPasswordField.text = ""
                confirmPasswordField.text = ""
              } else {
                GuiUtils.showError("Password Change Failed", "Current password is incorrect or password change failed.")
              }
            }
          }
        }
        
        // Admin-only database reset section
        val adminSection = if (user.hasAdminPrivileges) {
          val warningLabel = new Label("⚠️ DANGER ZONE ⚠️") {
            style = "-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: red;"
          }
          
          val resetWarningLabel = new Label("WARNING!!! Only click if you know what you are doing!!!") {
            style = "-fx-font-weight: bold; -fx-text-fill: red; -fx-font-size: 12px;"
            wrapText = true
          }
          
          val resetDescriptionLabel = new Label("This will completely reset the database, removing ALL data and recreating sample data. The application will exit after reset.") {
            style = "-fx-text-fill: darkred; -fx-font-size: 11px;"
            wrapText = true
            maxWidth = 400
          }
          
          val resetButton = new Button("🗑️ RESET DATABASE") {
            style = "-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;"
            onAction = _ => {
              val confirmDialog = new Alert(Alert.AlertType.Confirmation) {
                title = "⚠️ CONFIRM DATABASE RESET"
                headerText = "CRITICAL ACTION: Reset Database"
                contentText = "Are you absolutely sure you want to reset the database?\n\n" +
                             "This action will:\n" +
                             "• Delete ALL user data\n" +
                             "• Delete ALL food stock records\n" +
                             "• Delete ALL announcements and posts\n" +
                             "• Reset to sample data only\n" +
                             "• Close the application\n\n" +
                             "This action CANNOT be undone!"
              }
              
              confirmDialog.showAndWait() match {
                case Some(ButtonType.OK) =>
                  val secondConfirmDialog = new Alert(Alert.AlertType.Warning) {
                    title = "⚠️ FINAL CONFIRMATION"
                    headerText = "LAST CHANCE TO CANCEL"
                    contentText = "Type 'RESET' in the text field below to confirm database reset:"
                  }
                  
                  val confirmField = new TextField()
                  secondConfirmDialog.dialogPane().content = new VBox(10) {
                    children = List(
                      new Label("Type 'RESET' to confirm:"),
                      confirmField
                    )
                  }
                  
                  secondConfirmDialog.showAndWait() match {
                    case Some(ButtonType.OK) if confirmField.text.value == "RESET" =>
                      if (service.resetDatabaseForAdmin()) {
                        GuiUtils.showInfo("Database Reset", "Database has been reset successfully!\nThe application will now exit.")
                        // Close dialog first
                        dialog.close()
                        // Exit application
                        scalafx.application.Platform.exit()
                        System.exit(0)
                      } else {
                        GuiUtils.showError("Reset Failed", "Database reset failed. Please check the logs.")
                      }
                    case Some(ButtonType.OK) =>
                      GuiUtils.showWarning("Reset Cancelled", "Incorrect confirmation text. Reset cancelled.")
                    case _ => // User cancelled
                  }
                case _ => // User cancelled
              }
            }
          }
          
          Some((warningLabel, resetWarningLabel, resetDescriptionLabel, resetButton))
        } else {
          None
        }
        
        val grid = new GridPane {
          padding = Insets(20)
          hgap = 10
          vgap = 10
          
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
          
          // Add admin section if user is admin
          adminSection match {
            case Some((warningLabel, resetWarningLabel, resetDescriptionLabel, resetButton)) =>
              add(new Separator(), 0, 12, 2, 1)
              add(warningLabel, 0, 13, 2, 1)
              add(resetWarningLabel, 0, 14, 2, 1)
              add(resetDescriptionLabel, 0, 15, 2, 1)
              add(resetButton, 0, 16, 2, 1)
            case None => // No admin section for regular users
          }
        }
        
        val saveButton = new ButtonType("Save Changes", ButtonBar.ButtonData.OKDone)
        val cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CancelClose)
        
        dialog.dialogPane().buttonTypes = List(saveButton, cancelButton)
        dialog.dialogPane().content = grid
        
        val result = dialog.showAndWait()
        
        result match {
          case Some(`saveButton`) =>
            // Update user info
            if (service.updateUserProfile(nameField.text.value.trim, contactField.text.value.trim)) {
              GuiUtils.showInfo("Success", "Profile updated successfully!")
            } else {
              GuiUtils.showError("Update Failed", "Failed to update profile. Please try again.")
            }
          case _ => // Cancel or close
        }
        
      case None =>
        GuiUtils.showError("No User", "No user is currently logged in.")
    }
  }
}
