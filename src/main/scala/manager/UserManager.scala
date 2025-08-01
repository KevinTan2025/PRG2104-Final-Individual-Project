package manager

import model._
import scala.jdk.CollectionConverters._
// Removed mutable import - using ConcurrentHashMap instead

/**
 * Generic trait for managing collections of items
 * @tparam T the type of items being managed
 */
trait Manager[T] {
  protected val items: java.util.concurrent.ConcurrentHashMap[String, T] = new java.util.concurrent.ConcurrentHashMap[String, T]()
  
  def add(id: String, item: T): Unit = {
    items.put(id, item)
  }
  
  def remove(id: String): Option[T] = {
    Option(items.remove(id))
  }
  
  def get(id: String): Option[T] = {
    Option(items.get(id))
  }
  
  def all: List[T] = {
    items.values().asScala.toList
  }
  
  def exists(id: String): Boolean = {
    items.containsKey(id)
  }
  
  def size: Int = items.size()
  
  def clear(): Unit = {
    items.clear()
  }
}

/**
 * Manager class for handling user operations
 */
class UserManager extends Manager[User] {
  
  private val usernames: java.util.concurrent.ConcurrentHashMap[String, Boolean] = new java.util.concurrent.ConcurrentHashMap[String, Boolean]()
  private val emails: java.util.concurrent.ConcurrentHashMap[String, Boolean] = new java.util.concurrent.ConcurrentHashMap[String, Boolean]()
  
  /**
   * Register a new user
   * @param user the user to register
   * @return true if successful, false if username or email already exists
   */
  def registerUser(user: User): Boolean = {
    if (usernames.containsKey(user.username) || emails.containsKey(user.email)) {
      false
    } else {
      add(user.userId, user)
      usernames.put(user.username, true)
      emails.put(user.email, true)
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
    items.values().asScala.find(_.username == username)
  }
  
  /**
   * Get user by username
   * @param username the username to search for
   * @return the user if found, None otherwise
   */
  def userByUsername(username: String): Option[User] = {
    items.values().asScala.find(_.username == username)
  }
  
  /**
   * Get user by email
   * @param email the email to search for
   * @return the user if found, None otherwise
   */
  def userByEmail(email: String): Option[User] = {
    items.values().asScala.find(_.email == email)
  }
  
  /**
   * Get all admin users
   * @return list of admin users
   */
  def adminUsers: List[AdminUser] = {
    items.values().asScala.collect {
      case admin: AdminUser => admin
    }.toList
  }
  
  /**
   * Get all community members
   * @return list of community members
   */
  def communityMembers: List[CommunityMember] = {
    items.values().asScala.collect {
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
