package service

import database.service.DatabaseService
import model._
import java.time.LocalDateTime
import java.util.UUID

/**
 * Main service class that coordinates all operations and provides high-level business logic
 * Now uses DatabaseService as the middleware for data persistence
 */
class CommunityEngagementService {
  
  // Database service instance for data persistence
  private val dbService = DatabaseService.getInstance
  
  // Current logged-in user
  private var currentUser: Option[User] = None
  
  /**
   * User Authentication
   */
  
  def login(username: String, password: String): Option[User] = {
    val user = dbService.authenticateUser(username, password)
    currentUser = user
    user
  }
  
  def logout(): Unit = {
    currentUser = None
  }
  
  def getCurrentUser: Option[User] = currentUser
  
  def isLoggedIn: Boolean = currentUser.isDefined
  
  def registerUser(username: String, email: String, name: String, contactInfo: String, password: String, isAdmin: Boolean = false): Boolean = {
    val userId = UUID.randomUUID().toString
    val user = if (isAdmin) {
      new AdminUser(userId, username, email, name, contactInfo)
    } else {
      new CommunityMember(userId, username, email, name, contactInfo)
    }
    
    if (user.setPassword(password)) {
      dbService.saveUser(user)
    } else {
      false
    }
  }
  
  /**
   * Announcement Operations
   */
  
  def createAnnouncement(title: String, content: String, announcementType: AnnouncementType): Option[Announcement] = {
    currentUser.map { user =>
      val announcement = Announcement(
        announcementId = UUID.randomUUID().toString,
        authorId = user.userId,
        title = title,
        content = content,
        announcementType = announcementType
      )
      
      if (dbService.saveAnnouncement(announcement)) {
        // Notify all users about new announcement
        dbService.getAllUsers.foreach { recipient =>
          if (recipient.userId != user.userId) {
            val notification = Notification(
              notificationId = UUID.randomUUID().toString,
              recipientId = recipient.userId,
              senderId = Some(user.userId),
              title = "New Announcement",
              message = s"New announcement: $title",
              notificationType = NotificationType.ANNOUNCEMENT,
              relatedItemId = Some(announcement.announcementId)
            )
            dbService.saveNotification(notification)
          }
        }
        announcement
      } else {
        throw new RuntimeException("Failed to save announcement")
      }
    }
  }
  
  def getAnnouncements: List[Announcement] = {
    val announcements = dbService.getAllAnnouncements
    // Load comments for each announcement
    announcements.map { announcement =>
      val comments = dbService.getComments("announcement", announcement.announcementId)
      announcement.comments = comments
      announcement
    }
  }
  
  def searchAnnouncements(searchTerm: String): List[Announcement] = {
    dbService.searchAnnouncements(searchTerm)
  }
  
  def addCommentToAnnouncement(announcementId: String, content: String): Boolean = {
    currentUser.exists { user =>
      val comment = Comment(
        commentId = UUID.randomUUID().toString,
        authorId = user.userId,
        content = content
      )
      dbService.saveComment("announcement", announcementId, comment)
    }
  }
  
  def likeAnnouncement(announcementId: String): Boolean = {
    dbService.findAnnouncementById(announcementId).exists { announcement =>
      val newLikes = announcement.likes + 1
      dbService.updateAnnouncementLikes(announcementId, newLikes)
    }
  }
  
  /**
   * Food Sharing Operations
   */
  
  def createFoodPost(title: String, description: String, postType: FoodPostType, 
                    quantity: String, location: String, expiryDate: Option[LocalDateTime] = None): Option[FoodPost] = {
    currentUser.map { user =>
      val post = FoodPost(
        postId = UUID.randomUUID().toString,
        authorId = user.userId,
        title = title,
        description = description,
        postType = postType,
        quantity = quantity,
        location = location,
        expiryDate = expiryDate
      )
      
      if (dbService.saveFoodPost(post)) {
        // Notify relevant users
        dbService.getAllUsers.foreach { recipient =>
          if (recipient.userId != user.userId) {
            val notification = Notification(
              notificationId = UUID.randomUUID().toString,
              recipientId = recipient.userId,
              senderId = Some(user.userId),
              title = if (postType == FoodPostType.OFFER) "New Food Offer" else "New Food Request",
              message = s"${if (postType == FoodPostType.OFFER) "Food offered" else "Food requested"}: $title",
              notificationType = NotificationType.FOOD_OFFER,
              relatedItemId = Some(post.postId)
            )
            dbService.saveNotification(notification)
          }
        }
        post
      } else {
        throw new RuntimeException("Failed to save food post")
      }
    }
  }
  
  def getFoodPosts: List[FoodPost] = {
    val foodPosts = dbService.getActiveFoodPosts
    // Load comments for each food post
    foodPosts.map { post =>
      val comments = dbService.getComments("foodpost", post.postId)
      post.comments = comments
      post
    }
  }
  
  def getFoodPostsByType(postType: FoodPostType): List[FoodPost] = {
    dbService.getFoodPostsByType(postType)
  }
  
  def searchFoodPosts(searchTerm: String): List[FoodPost] = {
    dbService.searchFoodPosts(searchTerm)
  }
  
  def acceptFoodPost(postId: String): Boolean = {
    currentUser.exists { user =>
      dbService.updateFoodPostStatus(postId, FoodPostStatus.COMPLETED, Some(user.userId))
    }
  }
  
  /**
   * Discussion Forum Operations (Simplified - using direct database calls)
   */
  
  def createDiscussionTopic(title: String, description: String, category: DiscussionCategory): Option[DiscussionTopic] = {
    currentUser.map { user =>
      val topic = DiscussionTopic(
        topicId = UUID.randomUUID().toString,
        authorId = user.userId,
        title = title,
        description = description,
        category = category
      )
      // For now, return the topic (implement database storage later)
      topic
    }
  }
  
  def getDiscussionTopics: List[DiscussionTopic] = {
    // Simplified implementation - return empty list for now
    List.empty
  }
  
  def getTopicsByCategory(category: DiscussionCategory): List[DiscussionTopic] = {
    List.empty
  }
  
  def addReplyToTopic(topicId: String, content: String): Boolean = {
    currentUser.exists { user =>
      val reply = Reply(
        replyId = UUID.randomUUID().toString,
        topicId = topicId,
        authorId = user.userId,
        content = content
      )
      // For now, return true (implement database storage later)
      true
    }
  }
  
  def searchTopics(searchTerm: String): List[DiscussionTopic] = {
    List.empty
  }
  
  def likeTopic(topicId: String): Boolean = {
    true
  }
  
  /**
   * Event Management Operations (Simplified)
   */
  
  def createEvent(title: String, description: String, location: String, 
                 startDateTime: LocalDateTime, endDateTime: LocalDateTime, 
                 maxParticipants: Option[Int] = None): Option[Event] = {
    currentUser.map { user =>
      val event = Event(
        eventId = UUID.randomUUID().toString,
        organizerId = user.userId,
        title = title,
        description = description,
        location = location,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        maxParticipants = maxParticipants
      )
      // For now, return the event (implement database storage later)
      event
    }
  }
  
  def getUpcomingEvents: List[Event] = {
    List.empty
  }
  
  def rsvpToEvent(eventId: String): Boolean = {
    currentUser.exists { user =>
      // For now, return true (implement database storage later)
      true
    }
  }
  
  def cancelRsvp(eventId: String): Boolean = {
    currentUser.exists { user =>
      true
    }
  }
  
  def getMyEvents(userId: String): List[Event] = {
    List.empty
  }
  
  def searchEvents(searchTerm: String): List[Event] = {
    List.empty
  }
  
  /**
   * Notification Operations
   */
  
  def getNotifications: List[Notification] = {
    currentUser.map { user =>
      dbService.getNotificationsForUser(user.userId)
    }.getOrElse(List.empty)
  }
  
  def getUnreadNotifications: List[Notification] = {
    currentUser.map { user =>
      dbService.getNotificationsForUser(user.userId).filter(!_.isRead)
    }.getOrElse(List.empty)
  }
  
  def markNotificationAsRead(notificationId: String): Boolean = {
    dbService.markNotificationAsRead(notificationId)
  }
  
  def getUnreadNotificationCount: Int = {
    currentUser.map { user =>
      dbService.getUnreadNotificationCount(user.userId)
    }.getOrElse(0)
  }
  
  def markAllNotificationsAsRead: Int = {
    currentUser.map { user =>
      dbService.markAllNotificationsAsRead(user.userId)
    }.getOrElse(0)
  }
  
  def deleteNotification(notificationId: String): Boolean = {
    dbService.deleteNotification(notificationId)
  }
  
  /**
   * Admin Operations
   */
  
  def isCurrentUserAdmin: Boolean = {
    currentUser.exists(_.hasAdminPrivileges)
  }
  
  def moderateContent(contentId: String, contentType: String): Boolean = {
    if (isCurrentUserAdmin) {
      currentUser.exists { admin =>
        contentType.toLowerCase match {
          case "announcement" => dbService.moderateAnnouncement(contentId, admin.userId)
          case "foodpost" => dbService.moderateFoodPost(contentId, admin.userId)
          case _ => false
        }
      }
    } else {
      false
    }
  }
  
  def getAllUsers: List[User] = {
    dbService.getAllUsers
  }
  
  def getDetailedStatistics: Map[String, Any] = {
    val totalNotifications = getNotifications.size
    val announcements = dbService.getAllAnnouncements
    val foodPosts = dbService.getAllFoodPosts
    
    val totalComments = announcements.flatMap(_.comments).size + foodPosts.flatMap(_.comments).size
    val totalLikes = announcements.map(_.likes).sum + foodPosts.map(_.likes).sum
    
    Map(
      "totalNotifications" -> totalNotifications,
      "totalComments" -> totalComments,
      "totalLikes" -> totalLikes
    )
  }
  
  def getContentForModeration: List[(String, String, String)] = {
    val announcements = dbService.getAllAnnouncements.filter(!_.isModerated)
      .map(a => (a.announcementId, "announcement", a.title))
    val foodPosts = dbService.getAllFoodPosts.filter(!_.isModerated)
      .map(p => (p.postId, "foodpost", p.title))
    
    announcements ++ foodPosts
  }
  
  /**
   * Statistics and Analytics
   */
  
  def getDashboardStatistics: Map[String, Any] = {
    val totalUsers = dbService.getUserCount
    val adminUsers = dbService.getAdminCount
    val communityMembers = dbService.getCommunityMemberCount
    val activeAnnouncements = dbService.getAllAnnouncements.size
    val foodStats = dbService.getFoodPostStatistics
    val notificationStats = currentUser.map(u => dbService.getUnreadNotificationCount(u.userId)).getOrElse(0)
    
    Map(
      "totalUsers" -> totalUsers,
      "adminUsers" -> adminUsers,
      "communityMembers" -> communityMembers,
      "activeAnnouncements" -> activeAnnouncements,
      "totalFoodPosts" -> foodStats._1,
      "activeFoodPosts" -> foodStats._2,
      "completedFoodPosts" -> foodStats._3,
      "totalEvents" -> 0, // Simplified for now
      "upcomingEvents" -> 0,
      "completedEvents" -> 0,
      "unreadNotifications" -> notificationStats
    )
  }
  
  /**
   * User Profile Operations
   */
  
  def updateUserProfile(newName: String, newContactInfo: String): Boolean = {
    currentUser.exists { user =>
      user.updateProfile(newName, newContactInfo)
      dbService.updateUser(user)
    }
  }
  
  def resetPassword(currentPassword: String, newPassword: String): Boolean = {
    currentUser.exists { user =>
      dbService.resetUserPassword(user.userId, currentPassword, newPassword)
    }
  }
}

/**
 * Singleton object for the CommunityEngagementService
 */
object CommunityEngagementService {
  private var instance: Option[CommunityEngagementService] = None
  
  def getInstance: CommunityEngagementService = {
    instance match {
      case Some(service) => service
      case None =>
        val service = new CommunityEngagementService()
        instance = Some(service)
        service
    }
  }
}
