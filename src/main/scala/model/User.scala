package model

import java.time.LocalDateTime
import util.PasswordHasher

/**
 * Abstract base class representing a user in the community engagement platform
 * @param userId unique identifier for the user
 * @param username user's username
 * @param email user's email address
 * @param name user's display name
 * @param contactInfo user's contact information
 * @param passwordHash hashed password for authentication
 */
abstract class User(
  val userId: String,
  var username: String,
  var email: String,
  var name: String,
  var contactInfo: String,
  private var passwordHash: String = ""
) {
  
  val registrationDate: LocalDateTime = LocalDateTime.now()
  var isActive: Boolean = true
  
  /**
   * Abstract method to get user role
   * @return string representation of user role
   */
  def getUserRole: String
  
  /**
   * Update user profile information
   * @param newName new display name
   * @param newContactInfo new contact information
   */
  def updateProfile(newName: String, newContactInfo: String): Unit = {
    this.name = newName
    this.contactInfo = newContactInfo
  }
  
  /**
   * Set a new password (hashes the password before storing)
   * @param newPassword the new plain text password
   * @return true if password was set successfully, false if invalid
   */
  def setPassword(newPassword: String): Boolean = {
    if (PasswordHasher.isPasswordValid(newPassword)) {
      this.passwordHash = PasswordHasher.hashPassword(newPassword)
      true
    } else {
      false
    }
  }
  
  /**
   * Verify a password against the stored hash
   * @param password the plain text password to verify
   * @return true if password matches, false otherwise
   */
  def verifyPassword(password: String): Boolean = {
    if (passwordHash.isEmpty) false
    else PasswordHasher.verifyPassword(password, passwordHash)
  }
  
  /**
   * Get the stored password hash (for database storage)
   * @return the hashed password
   */
  def getPasswordHash: String = passwordHash
  
  /**
   * Set the password hash directly (for loading from database)
   * @param hash the hashed password from database
   */
  def setPasswordHash(hash: String): Unit = {
    this.passwordHash = hash
  }
  
  /**
   * Reset password with validation
   * @param currentPassword the current password for verification
   * @param newPassword the new password to set
   * @return true if password was reset successfully, false otherwise
   */
  def resetPassword(currentPassword: String, newPassword: String): Boolean = {
    if (verifyPassword(currentPassword) && PasswordHasher.isPasswordValid(newPassword)) {
      setPassword(newPassword)
    } else {
      false
    }
  }
  
  /**
   * Check if user has admin privileges
   * @return true if user is admin, false otherwise
   */
  def hasAdminPrivileges: Boolean = false
  
  override def toString: String = s"User($userId, $username, $name, ${getUserRole})"
}

/**
 * Community Member user class
 */
class CommunityMember(
  userId: String,
  username: String,
  email: String,
  name: String,
  contactInfo: String,
  passwordHash: String = ""
) extends User(userId, username, email, name, contactInfo, passwordHash) {
  
  override def getUserRole: String = "Community Member"
  
  /**
   * Member-specific functionality can be added here
   */
  def requestFood(): Unit = {
    // Implementation for food requests
  }
  
  def offerFood(): Unit = {
    // Implementation for food offers
  }
}

/**
 * Administrator user class with additional privileges
 */
class AdminUser(
  userId: String,
  username: String,
  email: String,
  name: String,
  contactInfo: String,
  passwordHash: String = ""
) extends User(userId, username, email, name, contactInfo, passwordHash) {
  
  override def getUserRole: String = "Administrator"
  
  override def hasAdminPrivileges: Boolean = true
  
  /**
   * Admin-specific functionality
   */
  def moderateContent(contentId: String): Unit = {
    // Implementation for content moderation
  }
  
  def deleteInappropriateContent(contentId: String): Unit = {
    // Implementation for content deletion
  }
  
  def manageUsers(): Unit = {
    // Implementation for user management
  }
}
