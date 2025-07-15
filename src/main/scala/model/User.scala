package model

import java.time.LocalDateTime

/**
 * Abstract base class representing a user in the community engagement platform
 * @param userId unique identifier for the user
 * @param username user's username
 * @param email user's email address
 * @param name user's display name
 * @param contactInfo user's contact information
 */
abstract class User(
  val userId: String,
  var username: String,
  var email: String,
  var name: String,
  var contactInfo: String
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
  contactInfo: String
) extends User(userId, username, email, name, contactInfo) {
  
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
  contactInfo: String
) extends User(userId, username, email, name, contactInfo) {
  
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
