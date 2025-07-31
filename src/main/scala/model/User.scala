package model

import java.time.LocalDateTime
import util.PasswordHasher
import scala.util.{Try, Success, Failure}

/**
 * Sealed trait representing a user in the community engagement platform
 * Uses functional programming principles with immutable data structures
 */
sealed trait User {
  def userId: String
  def username: String
  def email: String
  def name: String
  def contactInfo: String
  def passwordHash: String
  def registrationDate: LocalDateTime
  def isActive: Boolean
  def getUserRole: String
  
  /**
   * Update user profile information functionally
   * @param newName new display name
   * @param newContactInfo new contact information
   * @return Try containing updated user instance
   */
  def updateProfile(newName: String, newContactInfo: String): Try[User]
  
  /**
   * Set a new password functionally (hashes the password before storing)
   * @param newPassword the new plain text password
   * @return Try containing updated user instance with new password
   */
  def setPassword(newPassword: String): Try[User]
  
  /**
   * Set the password hash directly (for loading from database)
   * @param hash the hashed password from database
   * @return updated user instance with new password hash
   */
  def withPasswordHash(hash: String): User
  
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
   * Reset password with validation functionally
   * @param currentPassword the current password for verification
   * @param newPassword the new password to set
   * @return Try containing updated user instance with new password
   */
  def resetPassword(currentPassword: String, newPassword: String): Try[User] = {
    if (verifyPassword(currentPassword) && PasswordHasher.isPasswordValid(newPassword)) {
      setPassword(newPassword)
    } else {
      Failure(new IllegalArgumentException("Invalid current password or new password does not meet requirements"))
    }
  }
  
  /**
   * Check if user has admin privileges
   * @return true if user is admin, false otherwise
   */
  def hasAdminPrivileges: Boolean = false
  
  /**
   * Toggle user active status functionally
   * @return updated user instance with toggled active status
   */
  def toggleActiveStatus: User
}

/**
 * Companion object for User trait with utility functions
 */
object User {
  /**
   * Create a string representation of a user
   * @param user the user instance
   * @return formatted string representation
   */
  def userToString(user: User): String = 
    s"User(${user.userId}, ${user.username}, ${user.name}, ${user.getUserRole})"
}

/**
 * Community Member user case class - immutable data structure
 * @param userId unique identifier for the user
 * @param username user's username
 * @param email user's email address
 * @param name user's display name
 * @param contactInfo user's contact information
 * @param passwordHash hashed password for authentication
 * @param registrationDate when the user registered
 * @param isActive whether the user account is active
 */
case class CommunityMember(
  userId: String,
  username: String,
  email: String,
  name: String,
  contactInfo: String,
  passwordHash: String = "",
  registrationDate: LocalDateTime = LocalDateTime.now(),
  isActive: Boolean = true
) extends User {
  
  override def getUserRole: String = "Community Member"
  
  override def updateProfile(newName: String, newContactInfo: String): Try[User] = {
    Try(this.copy(name = newName, contactInfo = newContactInfo))
  }
  
  override def setPassword(newPassword: String): Try[User] = {
    if (PasswordHasher.isPasswordValid(newPassword)) {
      Try(this.copy(passwordHash = PasswordHasher.hashPassword(newPassword)))
    } else {
      Failure(new IllegalArgumentException("Password does not meet security requirements"))
    }
  }
  
  override def withPasswordHash(hash: String): User = {
    this.copy(passwordHash = hash)
  }
  
  override def toggleActiveStatus: User = {
    this.copy(isActive = !isActive)
  }
  
  /**
   * Member-specific functionality - pure functions that return actions/results
   */
  def createFoodRequest(): String = {
    s"Food request created by user $userId"
  }
  
  def createFoodOffer(): String = {
    s"Food offer created by user $userId"
  }
}

/**
 * Administrator user case class with additional privileges - immutable data structure
 * @param userId unique identifier for the user
 * @param username user's username
 * @param email user's email address
 * @param name user's display name
 * @param contactInfo user's contact information
 * @param passwordHash hashed password for authentication
 * @param registrationDate when the user registered
 * @param isActive whether the user account is active
 */
case class AdminUser(
  userId: String,
  username: String,
  email: String,
  name: String,
  contactInfo: String,
  passwordHash: String = "",
  registrationDate: LocalDateTime = LocalDateTime.now(),
  isActive: Boolean = true
) extends User {
  
  override def getUserRole: String = "Administrator"
  
  override def hasAdminPrivileges: Boolean = true
  
  override def updateProfile(newName: String, newContactInfo: String): Try[User] = {
    Try(this.copy(name = newName, contactInfo = newContactInfo))
  }
  
  override def setPassword(newPassword: String): Try[User] = {
    if (PasswordHasher.isPasswordValid(newPassword)) {
      Try(this.copy(passwordHash = PasswordHasher.hashPassword(newPassword)))
    } else {
      Failure(new IllegalArgumentException("Password does not meet security requirements"))
    }
  }
  
  override def withPasswordHash(hash: String): User = {
    this.copy(passwordHash = hash)
  }
  
  override def toggleActiveStatus: User = {
    this.copy(isActive = !isActive)
  }
  
  /**
   * Admin-specific functionality - pure functions that return action descriptions
   */
  def createModerationAction(contentId: String): String = {
    s"Content moderation action created for content $contentId by admin $userId"
  }
  
  def createDeletionAction(contentId: String): String = {
    s"Content deletion action created for content $contentId by admin $userId"
  }
  
  def createUserManagementAction(targetUserId: String, action: String): String = {
    s"User management action '$action' created for user $targetUserId by admin $userId"
  }
}
