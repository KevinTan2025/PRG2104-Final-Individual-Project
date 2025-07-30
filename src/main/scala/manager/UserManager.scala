package manager

import model._
import scala.collection.mutable

/**
 * Generic trait for managing collections of items
 * @tparam T the type of items being managed
 */
trait Manager[T] {
  protected val items: mutable.Map[String, T] = mutable.Map.empty
  
  def add(id: String, item: T): Unit = {
    items(id) = item
  }
  
  def remove(id: String): Option[T] = {
    items.remove(id)
  }
  
  def get(id: String): Option[T] = {
    items.get(id)
  }
  
  def getAll: List[T] = {
    items.values.toList
  }
  
  def exists(id: String): Boolean = {
    items.contains(id)
  }
  
  def size: Int = items.size
  
  def clear(): Unit = {
    items.clear()
  }
}

/**
 * Manager class for handling user operations
 */
class UserManager extends Manager[User] {
  
  private val usernames: mutable.Set[String] = mutable.Set.empty
  private val emails: mutable.Set[String] = mutable.Set.empty
  
  /**
   * Register a new user
   * @param user the user to register
   * @return true if successful, false if username or email already exists
   */
  def registerUser(user: User): Boolean = {
    if (usernames.contains(user.username) || emails.contains(user.email)) {
      false
    } else {
      add(user.userId, user)
      usernames.add(user.username)
      emails.add(user.email)
      true
    }
  }
  
  /**
   * Authenticate a user by username and password
   * @param username the username
   * @param password the password (simplified for demo)
   * @return the user if authentication successful, None otherwise
   */
  def authenticate(username: String, password: String): Option[User] = {
    // Simplified authentication - in real app, password would be hashed
    items.values.find(_.username == username)
  }
  
  /**
   * Get user by username
   * @param username the username to search for
   * @return the user if found, None otherwise
   */
  def getUserByUsername(username: String): Option[User] = {
    items.values.find(_.username == username)
  }
  
  /**
   * Get user by email
   * @param email the email to search for
   * @return the user if found, None otherwise
   */
  def getUserByEmail(email: String): Option[User] = {
    items.values.find(_.email == email)
  }
  
  /**
   * Get all admin users
   * @return list of admin users
   */
  def getAdminUsers: List[AdminUser] = {
    items.values.collect {
      case admin: AdminUser => admin
    }.toList
  }
  
  /**
   * Get all community members
   * @return list of community members
   */
  def getCommunityMembers: List[CommunityMember] = {
    items.values.collect {
      case member: CommunityMember => member
    }.toList
  }
  
  /**
   * Check if username is available
   * @param username the username to check
   * @return true if available, false otherwise
   */
  def isUsernameAvailable(username: String): Boolean = {
    !usernames.contains(username)
  }
  
  /**
   * Check if email is available
   * @param email the email to check
   * @return true if available, false otherwise
   */
  def isEmailAvailable(email: String): Boolean = {
    !emails.contains(email)
  }
  
  override def remove(id: String): Option[User] = {
    val user = super.remove(id)
    user.foreach { u =>
      usernames.remove(u.username)
      emails.remove(u.email)
    }
    user
  }
}
