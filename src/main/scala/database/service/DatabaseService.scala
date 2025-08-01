package database.service

import database.dao._
import database.{DatabaseConnection, DatabaseSchema}
import manager.{FoodStockManager, StockMovementManager}
import model._
import java.time.LocalDateTime
import java.util.UUID
import scala.util.{Try, Success, Failure}

/**
 * Database service layer that provides high-level database operations
 * This acts as the middleware between the business logic and data access layer
 */
class DatabaseService {
  
  // DAO instances
  private val userDAO = new UserDAO()
  private val announcementDAO = new AnnouncementDAO()
  private val foodPostDAO = new FoodPostDAO()
  private val foodStockDAO = new FoodStockDAO()
  private val stockMovementDAO = new StockMovementDAO()
  private val discussionTopicDAO = new DiscussionTopicDAO()
  private val discussionReplyDAO = new DiscussionReplyDAO()
  private val eventDAO = new EventDAO()
  
  // Manager instances for food stock
  val foodStockManager = new FoodStockManager()
  val stockMovementManager = new StockMovementManager()
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
  
  def findUserByEmail(email: String): Option[User] = {
    userDAO.findByEmail(email)
  }
  
  def findUserById(userId: String): Option[User] = {
    userDAO.findById(userId)
  }
  
  def allUsers: List[User] = {
    userDAO.findAll()
  }
  
  def updateUser(user: User): Boolean = {
    userDAO.update(user)
  }
  
  def deleteUser(userId: String): Boolean = {
    userDAO.delete(userId)
  }
  
  def resetUserPassword(userId: String, currentPassword: String, newPassword: String): Boolean = {
    findUserById(userId) match {
      case Some(user) =>
        user.resetPassword(currentPassword, newPassword) match {
          case Success(updatedUser) => 
            userDAO.updatePassword(userId, updatedUser.passwordHash)
          case Failure(_) => false
        }
      case None => false
    }
  }
  
  def authenticateUser(username: String, password: String): Option[User] = {
    findUserByUsername(username) match {
      case Some(user) =>
        if (user.verifyPassword(password)) Some(user) else None
      case None => None
    }
  }
  
  def userCount: Int = {
    userDAO.count()
  }
  
  def adminCount: Int = {
    userDAO.countAdmins()
  }
  
  def communityMemberCount: Int = {
    userCount - adminCount
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
  
  def allAnnouncements: List[Announcement] = {
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
  
  def announcementCount: Int = {
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
  
  def allFoodPosts: List[FoodPost] = {
    foodPostDAO.findAll()
  }
  
  def foodPostsByType(postType: FoodPostType): List[FoodPost] = {
    foodPostDAO.findByType(postType)
  }
  
  def foodPostsByStatus(status: FoodPostStatus): List[FoodPost] = {
    foodPostDAO.findByStatus(status)
  }
  
  def activeFoodPosts: List[FoodPost] = {
    // Get both PENDING and ACCEPTED food posts
    val pendingPosts = foodPostsByStatus(FoodPostStatus.PENDING)
    val acceptedPosts = foodPostsByStatus(FoodPostStatus.ACCEPTED)
    (pendingPosts ++ acceptedPosts).sortBy(_.timestamp).reverse
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
  
  def foodPostStatistics: (Int, Int, Int) = {
    foodPostDAO.statistics
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
  
  def comments(contentType: String, contentId: String): List[Comment] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        """SELECT * FROM comments 
           WHERE content_type = ? AND content_id = ? 
           ORDER BY created_at""",
        contentType, contentId
      )
      
      val comments = Iterator.continually(rs)
        .takeWhile(_.next())
        .map { rs =>
          val commentId = rs.getString("comment_id")
          val authorId = rs.getString("author_id")
          val content = rs.getString("content")
          val createdAt = DatabaseConnection.parseDateTime(rs.getString("created_at"))
          
          new Comment(commentId, authorId, content) {
            override val timestamp: LocalDateTime = createdAt
          }
        }
        .toList
      
      rs.close()
      comments
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
  
  def notificationsForUser(userId: String): List[Notification] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        """SELECT * FROM notifications 
           WHERE recipient_id = ? 
           ORDER BY created_at DESC""",
        userId
      )
      
      val notifications = Iterator.continually(rs)
        .takeWhile(_.next())
        .map { rs =>
          val notificationId = rs.getString("notification_id")
          val recipientId = rs.getString("recipient_id")
          val senderId = Option(rs.getString("sender_id"))
          val notificationType = NotificationType.valueOf(rs.getString("type"))
          val title = rs.getString("title")
          val message = rs.getString("message")
          val relatedId = Option(rs.getString("related_id"))
          val isRead = rs.getBoolean("is_read")
          val createdAt = DatabaseConnection.parseDateTime(rs.getString("created_at"))
          
          Notification(notificationId, recipientId, senderId, title, message, notificationType, relatedId, createdAt, isRead)
        }
        .toList
      
      rs.close()
      notifications
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
  
  def unreadNotificationCount(userId: String): Int = {
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
    DatabaseSchema.resetDatabase()
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
  
  /**
   * Discussion Forum Operations
   */
  
  def saveDiscussionTopic(topic: DiscussionTopic): Boolean = {
    discussionTopicDAO.insert(topic)
  }
  
  def updateDiscussionTopic(topic: DiscussionTopic): Boolean = {
    discussionTopicDAO.update(topic)
  }
  
  def findDiscussionTopicById(topicId: String): Option[DiscussionTopic] = {
    discussionTopicDAO.findById(topicId)
  }
  
  def allDiscussionTopics: List[DiscussionTopic] = {
    discussionTopicDAO.findAll()
  }
  
  def discussionTopicsByCategory(category: DiscussionCategory): List[DiscussionTopic] = {
    discussionTopicDAO.findByCategory(category)
  }
  
  def discussionTopicsByAuthor(authorId: String): List[DiscussionTopic] = {
    discussionTopicDAO.findByAuthor(authorId)
  }
  
  def searchDiscussionTopics(searchTerm: String): List[DiscussionTopic] = {
    discussionTopicDAO.searchByTitleOrDescription(searchTerm)
  }
  
  def likeDiscussionTopic(topicId: String): Boolean = {
    discussionTopicDAO.incrementLikes(topicId)
  }
  
  def deleteDiscussionTopic(topicId: String): Boolean = {
    // First delete all replies
    val replies = discussionReplyDAO.findByTopicId(topicId)
    replies.foreach(reply => discussionReplyDAO.delete(reply.replyId))
    
    // Then delete the topic
    discussionTopicDAO.delete(topicId)
  }
  
  // Reply operations
  def saveDiscussionReply(reply: Reply): Boolean = {
    discussionReplyDAO.insert(reply)
  }
  
  def updateDiscussionReply(reply: Reply): Boolean = {
    discussionReplyDAO.update(reply)
  }
  
  def findDiscussionReplyById(replyId: String): Option[Reply] = {
    discussionReplyDAO.findById(replyId)
  }
  
  def repliesForTopic(topicId: String): List[Reply] = {
    discussionReplyDAO.findByTopicId(topicId)
  }
  
  def repliesByAuthor(authorId: String): List[Reply] = {
    discussionReplyDAO.findByAuthor(authorId)
  }
  
  def likeDiscussionReply(replyId: String): Boolean = {
    discussionReplyDAO.incrementLikes(replyId)
  }
  
  def deleteDiscussionReply(replyId: String): Boolean = {
    discussionReplyDAO.delete(replyId)
  }
  
  def replyCountForTopic(topicId: String): Int = {
    discussionReplyDAO.replyCountForTopic(topicId)
  }

  /**
   * Event Operations
   */
  
  def saveEvent(event: Event): Boolean = {
    eventDAO.insert(event)
  }
  
  def updateEvent(event: Event): Boolean = {
    eventDAO.update(event)
  }
  
  def deleteEvent(eventId: String): Boolean = {
    eventDAO.delete(eventId)
  }
  
  def eventById(eventId: String): Option[Event] = {
    eventDAO.findById(eventId)
  }
  
  def allEvents: List[Event] = {
    eventDAO.findAll()
  }
  
  def upcomingEvents: List[Event] = {
    eventDAO.findUpcomingEvents()
  }
  
  def eventsByOrganizer(organizerId: String): List[Event] = {
    eventDAO.findEventsByOrganizer(organizerId)
  }
  
  def searchEvents(searchTerm: String): List[Event] = {
    eventDAO.searchEvents(searchTerm)
  }
  
  def rsvpToEvent(eventId: String, userId: String): Boolean = {
    eventDAO.rsvpToEvent(eventId, userId)
  }
  
  def cancelEventRsvp(eventId: String, userId: String): Boolean = {
    eventDAO.cancelRsvp(eventId, userId)
  }
  
  def eventRsvpCount(eventId: String): Int = {
    eventDAO.rsvpCount(eventId)
  }

  def userEvents(userId: String): List[Event] = {
    eventDAO.userEvents(userId)
  }
  
  /**
   * Food Stock Operations
   */
  
  def saveFoodStock(foodStock: FoodStock): Boolean = {
    foodStockDAO.insert(foodStock)
  }
  
  def findFoodStockById(stockId: String): Option[FoodStock] = {
    foodStockDAO.findById(stockId)
  }
  
  def allFoodStocks: List[FoodStock] = {
    foodStockDAO.findAll()
  }
  
  def foodStocksByCategory(category: FoodCategory): List[FoodStock] = {
    foodStockDAO.findByCategory(category)
  }
  
  def foodStocksByLocation(location: String): List[FoodStock] = {
    foodStockDAO.findByLocation(location)
  }
  
  def searchFoodStocks(searchTerm: String): List[FoodStock] = {
    foodStockDAO.search(searchTerm)
  }
  
  def updateFoodStock(foodStock: FoodStock): Boolean = {
    foodStockDAO.update(foodStock)
  }
  
  def deleteFoodStock(stockId: String): Boolean = {
    foodStockDAO.delete(stockId)
  }
  
  def lowStockItems: List[FoodStock] = {
    foodStockDAO.findLowStock()
  }
  
  def expiredItems: List[FoodStock] = {
    foodStockDAO.findExpired()
  }
  
  def expiringSoonItems(days: Int = 7): List[FoodStock] = {
    foodStockDAO.findExpiringSoon(days)
  }
  
  def foodStockStatistics: (Int, Int, Int, Int) = {
    foodStockDAO.statistics
  }
  
  /**
   * Stock Movement Operations
   */
  
  def saveStockMovement(movement: StockMovement): Boolean = {
    stockMovementDAO.insert(movement)
  }
  
  def findStockMovementById(movementId: String): Option[StockMovement] = {
    stockMovementDAO.findById(movementId)
  }
  
  def allStockMovements: List[StockMovement] = {
    stockMovementDAO.findAll()
  }
  
  def stockMovementsByStockId(stockId: String): List[StockMovement] = {
    stockMovementDAO.findByStockId(stockId)
  }
  
  def stockMovementsByUser(userId: String): List[StockMovement] = {
    stockMovementDAO.findByUserId(userId)
  }
  
  def stockMovementsByActionType(actionType: StockActionType): List[StockMovement] = {
    stockMovementDAO.findByActionType(actionType)
  }
  
  def stockMovementsByDateRange(startDate: java.time.LocalDateTime, endDate: java.time.LocalDateTime): List[StockMovement] = {
    stockMovementDAO.findByDateRange(startDate, endDate)
  }
}

/**
 * Singleton object for DatabaseService
 */
object DatabaseService {
  lazy val getInstance: DatabaseService = new DatabaseService()
}
