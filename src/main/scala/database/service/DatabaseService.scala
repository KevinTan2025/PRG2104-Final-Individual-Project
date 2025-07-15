package database.service

import database.dao._
import database.{DatabaseConnection, DatabaseSchema}
import model._
import java.time.LocalDateTime
import java.util.UUID

/**
 * Database service layer that provides high-level database operations
 * This acts as the middleware between the business logic and data access layer
 */
class DatabaseService {
  
  // DAO instances
  private val userDAO = new UserDAO()
  private val announcementDAO = new AnnouncementDAO()
  private val foodPostDAO = new FoodPostDAO()
  // We'll add more DAOs as needed
  
  // Initialize database on first use
  DatabaseSchema.initializeDatabase()
  
  /**
   * User Operations
   */
  
  def saveUser(user: User): Boolean = {
    userDAO.insert(user)
  }
  
  def findUserByUsername(username: String): Option[User] = {
    userDAO.findByUsername(username)
  }
  
  def findUserById(userId: String): Option[User] = {
    userDAO.findById(userId)
  }
  
  def getAllUsers: List[User] = {
    userDAO.findAll()
  }
  
  def updateUser(user: User): Boolean = {
    userDAO.update(user)
  }
  
  def deleteUser(userId: String): Boolean = {
    userDAO.delete(userId)
  }
  
  def getUserCount: Int = {
    userDAO.count()
  }
  
  def getAdminCount: Int = {
    userDAO.countAdmins()
  }
  
  def getCommunityMemberCount: Int = {
    getUserCount - getAdminCount
  }
  
  /**
   * Announcement Operations
   */
  
  def saveAnnouncement(announcement: Announcement): Boolean = {
    announcementDAO.insert(announcement)
  }
  
  def findAnnouncementById(announcementId: String): Option[Announcement] = {
    announcementDAO.findById(announcementId)
  }
  
  def getAllAnnouncements: List[Announcement] = {
    announcementDAO.findAll()
  }
  
  def searchAnnouncements(searchTerm: String): List[Announcement] = {
    announcementDAO.search(searchTerm)
  }
  
  def updateAnnouncementLikes(announcementId: String, likes: Int): Boolean = {
    announcementDAO.updateLikes(announcementId, likes)
  }
  
  def moderateAnnouncement(announcementId: String, moderatorId: String): Boolean = {
    announcementDAO.moderate(announcementId, moderatorId)
  }
  
  def deleteAnnouncement(announcementId: String): Boolean = {
    announcementDAO.delete(announcementId)
  }
  
  def getAnnouncementCount: Int = {
    announcementDAO.count()
  }
  
  /**
   * Food Post Operations
   */
  
  def saveFoodPost(foodPost: FoodPost): Boolean = {
    foodPostDAO.insert(foodPost)
  }
  
  def findFoodPostById(postId: String): Option[FoodPost] = {
    foodPostDAO.findById(postId)
  }
  
  def getAllFoodPosts: List[FoodPost] = {
    foodPostDAO.findAll()
  }
  
  def getFoodPostsByType(postType: FoodPostType): List[FoodPost] = {
    foodPostDAO.findByType(postType)
  }
  
  def getFoodPostsByStatus(status: FoodPostStatus): List[FoodPost] = {
    foodPostDAO.findByStatus(status)
  }
  
  def getActiveFoodPosts: List[FoodPost] = {
    getFoodPostsByStatus(FoodPostStatus.PENDING)
  }
  
  def searchFoodPosts(searchTerm: String): List[FoodPost] = {
    foodPostDAO.search(searchTerm)
  }
  
  def updateFoodPostStatus(postId: String, status: FoodPostStatus, acceptedBy: Option[String] = None): Boolean = {
    foodPostDAO.updateStatus(postId, status, acceptedBy)
  }
  
  def updateFoodPostLikes(postId: String, likes: Int): Boolean = {
    foodPostDAO.updateLikes(postId, likes)
  }
  
  def moderateFoodPost(postId: String, moderatorId: String): Boolean = {
    foodPostDAO.moderate(postId, moderatorId)
  }
  
  def deleteFoodPost(postId: String): Boolean = {
    foodPostDAO.delete(postId)
  }
  
  def getFoodPostStatistics: (Int, Int, Int) = {
    foodPostDAO.getStatistics
  }
  
  /**
   * Comment Operations (using direct SQL for now)
   */
  
  def saveComment(contentType: String, contentId: String, comment: Comment): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO comments 
           (comment_id, content_type, content_id, author_id, content, created_at) 
           VALUES (?, ?, ?, ?, ?, ?)""",
        comment.commentId, contentType, contentId, comment.authorId, comment.content,
        DatabaseConnection.formatDateTime(comment.timestamp)
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error saving comment: ${e.getMessage}")
        false
    }
  }
  
  def getComments(contentType: String, contentId: String): List[Comment] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        """SELECT * FROM comments 
           WHERE content_type = ? AND content_id = ? 
           ORDER BY created_at""",
        contentType, contentId
      )
      
      val comments = scala.collection.mutable.ListBuffer[Comment]()
      while (rs.next()) {
        val commentId = rs.getString("comment_id")
        val authorId = rs.getString("author_id")
        val content = rs.getString("content")
        val createdAt = DatabaseConnection.parseDateTime(rs.getString("created_at"))
        
        comments += new Comment(commentId, authorId, content) {
          override val timestamp: LocalDateTime = createdAt
        }
      }
      
      rs.close()
      comments.toList
    } catch {
      case e: Exception =>
        println(s"Error getting comments: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * Notification Operations (using direct SQL for now)
   */
  
  def saveNotification(notification: Notification): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO notifications 
           (notification_id, recipient_id, sender_id, type, title, message, related_id, created_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
        notification.notificationId, notification.recipientId, notification.senderId.orNull,
        notification.notificationType.toString, notification.title, notification.message,
        notification.relatedItemId.orNull, DatabaseConnection.formatDateTime(notification.timestamp)
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error saving notification: ${e.getMessage}")
        false
    }
  }
  
  def getNotificationsForUser(userId: String): List[Notification] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        """SELECT * FROM notifications 
           WHERE recipient_id = ? 
           ORDER BY created_at DESC""",
        userId
      )
      
      val notifications = scala.collection.mutable.ListBuffer[Notification]()
      while (rs.next()) {
        val notificationId = rs.getString("notification_id")
        val recipientId = rs.getString("recipient_id")
        val senderId = Option(rs.getString("sender_id"))
        val notificationType = NotificationType.valueOf(rs.getString("type"))
        val title = rs.getString("title")
        val message = rs.getString("message")
        val relatedId = Option(rs.getString("related_id"))
        val isRead = rs.getBoolean("is_read")
        val createdAt = DatabaseConnection.parseDateTime(rs.getString("created_at"))
        
        val notification = Notification(notificationId, recipientId, senderId, title, message, notificationType, relatedId, createdAt)
        notification.isRead = isRead
        
        notifications += notification
      }
      
      rs.close()
      notifications.toList
    } catch {
      case e: Exception =>
        println(s"Error getting notifications for user: ${e.getMessage}")
        List.empty
    }
  }
  
  def markNotificationAsRead(notificationId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "UPDATE notifications SET is_read = 1 WHERE notification_id = ?",
        notificationId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error marking notification as read: ${e.getMessage}")
        false
    }
  }
  
  def getUnreadNotificationCount(userId: String): Int = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT COUNT(*) FROM notifications WHERE recipient_id = ? AND is_read = 0",
        userId
      )
      val count = if (rs.next()) rs.getInt(1) else 0
      rs.close()
      count
    } catch {
      case e: Exception =>
        println(s"Error getting unread notification count: ${e.getMessage}")
        0
    }
  }
  
  def markAllNotificationsAsRead(userId: String): Int = {
    try {
      DatabaseConnection.executeUpdate(
        "UPDATE notifications SET is_read = 1 WHERE recipient_id = ? AND is_read = 0",
        userId
      )
    } catch {
      case e: Exception =>
        println(s"Error marking all notifications as read: ${e.getMessage}")
        0
    }
  }
  
  def deleteNotification(notificationId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM notifications WHERE notification_id = ?",
        notificationId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error deleting notification: ${e.getMessage}")
        false
    }
  }
  
  /**
   * Database Management Operations
   */
  
  def closeConnection(): Unit = {
    DatabaseConnection.close()
  }
  
  def resetDatabase(): Unit = {
    DatabaseSchema.dropAllTables()
    DatabaseSchema.initializeDatabase()
  }
  
  /**
   * Health check
   */
  def isHealthy: Boolean = {
    try {
      val rs = DatabaseConnection.executeQuery("SELECT 1")
      val healthy = rs.next()
      rs.close()
      healthy
    } catch {
      case _: Exception => false
    }
  }
}

/**
 * Singleton object for DatabaseService
 */
object DatabaseService {
  private var instance: Option[DatabaseService] = None
  
  def getInstance: DatabaseService = {
    instance match {
      case Some(service) => service
      case None =>
        val service = new DatabaseService()
        instance = Some(service)
        service
    }
  }
}
