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
  
  // Anonymous mode flag
  private var isAnonymousMode: Boolean = false
  
  /**
   * User Authentication
   */
  
  def login(username: String, password: String): Option[User] = {
    val user = dbService.authenticateUser(username, password)
    currentUser = user
    if (user.isDefined) {
      isAnonymousMode = false
    }
    user
  }
  
  def logout(): Unit = {
    currentUser = None
    isAnonymousMode = false
  }
  
  def enableAnonymousMode(): Unit = {
    isAnonymousMode = true
    currentUser = None
  }
  
  def disableAnonymousMode(): Unit = {
    isAnonymousMode = false
  }
  
  def getCurrentUser: Option[User] = currentUser
  
  def isLoggedIn: Boolean = currentUser.isDefined
  
  def isInAnonymousMode: Boolean = isAnonymousMode
  
  def canPerformAction(requiresLogin: Boolean): Boolean = {
    if (!requiresLogin) true
    else isLoggedIn
  }
  
  def isUsernameAvailable(username: String): Boolean = {
    dbService.findUserByUsername(username).isEmpty
  }
  
  def isEmailAvailable(email: String): Boolean = {
    dbService.findUserByEmail(email).isEmpty
  }
  
  def registerUser(username: String, email: String, name: String, contactInfo: String, password: String, isAdmin: Boolean = false): Boolean = {
    // Check for existing username and email (case-insensitive for username)
    if (!isUsernameAvailable(username)) {
      return false
    }
    
    if (!isEmailAvailable(email)) {
      return false
    }
    
    val userId = UUID.randomUUID().toString
    // Store username with original case but validation is case-insensitive
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
  }  /**
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
      dbService.updateFoodPostStatus(postId, FoodPostStatus.ACCEPTED, Some(user.userId))
    }
  }
  
  def completeFoodPost(postId: String): Boolean = {
    currentUser.exists { user =>
      dbService.updateFoodPostStatus(postId, FoodPostStatus.COMPLETED, Some(user.userId))
    }
  }
  
  /**
   * Discussion Forum Operations - Using Database Persistence
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
      
      if (dbService.saveDiscussionTopic(topic)) {
        Some(topic)
      } else {
        None
      }
    }.flatten
  }

  def getDiscussionTopics: List[DiscussionTopic] = {
    dbService.getAllDiscussionTopics
  }

  def getTopicsByCategory(category: DiscussionCategory): List[DiscussionTopic] = {
    dbService.getDiscussionTopicsByCategory(category)
  }

  def addReplyToTopic(topicId: String, content: String): Boolean = {
    currentUser.exists { user =>
      val reply = Reply(
        replyId = UUID.randomUUID().toString,
        topicId = topicId,
        authorId = user.userId,
        content = content
      )
      dbService.saveDiscussionReply(reply)
    }
  }

  def searchTopics(searchTerm: String): List[DiscussionTopic] = {
    dbService.searchDiscussionTopics(searchTerm)
  }

  def likeTopic(topicId: String): Boolean = {
    dbService.likeDiscussionTopic(topicId)
  }

  /**
   * Initialize sample discussion data for testing
   */
  def initializeSampleDiscussionData(): Unit = {
    // Create sample topics
    val topic1 = DiscussionTopic(
      topicId = "topic-1",
      authorId = "admin",
      title = "Healthy eating tips for families",
      description = "Let's share some practical tips for maintaining healthy eating habits for families with busy schedules. What works best in your household?",
      category = DiscussionCategory.NUTRITION,
      timestamp = LocalDateTime.now().minusDays(2)
    )
    topic1.addLike() // Add some likes
    topic1.addLike()
    topic1.addLike()
    topic1.addLike()
    topic1.addLike()
    
    val topic2 = DiscussionTopic(
      topicId = "topic-2", 
      authorId = "user1",
      title = "Community garden project update",
      description = "Our community garden project is making great progress! We've planted tomatoes, lettuce, and herbs. Looking for volunteers to help with watering schedule.",
      category = DiscussionCategory.COMMUNITY_GARDEN,
      timestamp = LocalDateTime.now().minusDays(1)
    )
    topic2.addLike()
    topic2.addLike()
    topic2.addLike()
    
    val topic3 = DiscussionTopic(
      topicId = "topic-3",
      authorId = "chef_mary",
      title = "Easy recipes for busy schedules",
      description = "Share your favorite quick and easy recipes that are perfect for weeknight dinners. Bonus points for one-pot meals!",
      category = DiscussionCategory.COOKING_TIPS,
      timestamp = LocalDateTime.now().minusHours(12)
    )
    topic3.addLike()
    topic3.addLike()
    
    val topic4 = DiscussionTopic(
      topicId = "topic-4",
      authorId = "green_thumb",
      title = "Urban farming techniques",
      description = "Exploring different methods for growing food in small urban spaces. Let's discuss vertical gardening, container growing, and hydroponic systems.",
      category = DiscussionCategory.SUSTAINABLE_AGRICULTURE,
      timestamp = LocalDateTime.now().minusHours(6)
    )
    topic4.addLike()
    
    val topic5 = DiscussionTopic(
      topicId = "topic-5",
      authorId = "community_leader",
      title = "Welcome to our discussion forum",
      description = "Welcome everyone to our new community discussion forum! This is a place to share ideas, ask questions, and connect with neighbors. Please read our community guidelines and introduce yourself!",
      category = DiscussionCategory.GENERAL,
      timestamp = LocalDateTime.now().minusDays(7)
    )
    
    val topic6 = DiscussionTopic(
      topicId = "topic-6",
      authorId = "organizer",
      title = "Spring planting schedule",
      description = "Planning our community garden for spring! Let's coordinate what everyone wants to plant and create a schedule for planting different crops.",
      category = DiscussionCategory.COMMUNITY_GARDEN,
      timestamp = LocalDateTime.now().minusHours(3)
    )
    
    // Add sample replies
    val reply1 = Reply(
      replyId = "reply-1",
      topicId = "topic-1",
      authorId = "mom_of_three",
      content = "Great topic! I've found that meal prepping on Sundays really helps. I cook grains and proteins in bulk, then just add fresh veggies during the week.",
      timestamp = LocalDateTime.now().minusDays(1)
    )
    reply1.addLike()
    reply1.addLike()
    
    val reply2 = Reply(
      replyId = "reply-2", 
      topicId = "topic-1",
      authorId = "nutritionist_jane",
      content = "Excellent advice! I'd also recommend involving kids in meal planning and preparation. It helps them develop healthy relationships with food.",
      timestamp = LocalDateTime.now().minusHours(18)
    )
    reply2.addLike()
    
    val reply3 = Reply(
      replyId = "reply-3",
      topicId = "topic-2",
      authorId = "volunteer_sam",
      content = "I can help with the watering schedule! I'm available Tuesday and Thursday mornings. The plants are looking amazing!",
      timestamp = LocalDateTime.now().minusHours(8)
    )
    
    val reply4 = Reply(
      replyId = "reply-4",
      topicId = "topic-3",
      authorId = "busy_parent",
      content = "One-pot pasta with vegetables is my go-to! Just throw everything in one pot with some broth and pasta. Kids love it too.",
      timestamp = LocalDateTime.now().minusHours(6)
    )
    reply4.addLike()
    
    // Save topics to database
    dbService.saveDiscussionTopic(topic1)
    dbService.saveDiscussionTopic(topic2)
    dbService.saveDiscussionTopic(topic3)
    dbService.saveDiscussionTopic(topic4)
    dbService.saveDiscussionTopic(topic5)
    dbService.saveDiscussionTopic(topic6)
    
    // Save replies to database
    dbService.saveDiscussionReply(reply1)
    dbService.saveDiscussionReply(reply2)
    dbService.saveDiscussionReply(reply3)
    dbService.saveDiscussionReply(reply4)
  }

  /**
   * Event Management Operations (Database-backed)
   */
  
  def createEvent(title: String, description: String, location: String, 
                 startDateTime: LocalDateTime, endDateTime: LocalDateTime, 
                 maxParticipants: Option[Int] = None): Option[Event] = {
    currentUser.flatMap { user =>
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
      
      if (dbService.saveEvent(event)) {
        Some(event)
      } else {
        None
      }
    }
  }
  
  def getUpcomingEvents: List[Event] = {
    dbService.getUpcomingEvents()
  }
  
  def getAllEvents: List[Event] = {
    dbService.getAllEvents()
  }
  
  def rsvpToEvent(eventId: String): Boolean = {
    currentUser.exists { user =>
      dbService.rsvpToEvent(eventId, user.userId)
    }
  }
  
  def cancelRsvp(eventId: String): Boolean = {
    currentUser.exists { user =>
      dbService.cancelEventRsvp(eventId, user.userId)
    }
  }
  
  def getMyEvents(userId: String): List[Event] = {
    dbService.getUserEvents(userId)
  }
  
  def searchEvents(searchTerm: String): List[Event] = {
    dbService.searchEvents(searchTerm)
  }
  
  def getEventsByOrganizer(organizerId: String): List[Event] = {
    dbService.getEventsByOrganizer(organizerId)
  }
  
  def getEventById(eventId: String): Option[Event] = {
    dbService.getEventById(eventId)
  }
  
  def updateEvent(event: Event): Boolean = {
    dbService.updateEvent(event)
  }
  
  def deleteEvent(eventId: String): Boolean = {
    currentUser.exists { user =>
      // Check if user is the organizer or an admin
      val event = dbService.getEventById(eventId)
      event.exists(e => e.organizerId == user.userId || user.hasAdminPrivileges) &&
      dbService.deleteEvent(eventId)
    }
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
  
  /**
   * Food Stock Management
   */
  
  def createFoodStock(foodStock: FoodStock): Boolean = {
    dbService.foodStockManager.createFoodStock(foodStock)
    true
  }
  
  def getAllFoodStocks: List[FoodStock] = {
    dbService.foodStockManager.getAllFoodStocks
  }
  
  def getFoodStockById(stockId: String): Option[FoodStock] = {
    dbService.foodStockManager.get(stockId)
  }
  
  def updateFoodStock(foodStock: FoodStock): Boolean = {
    dbService.foodStockManager.add(foodStock.stockId, foodStock)
    true
  }
  
  def deleteFoodStock(stockId: String): Boolean = {
    dbService.foodStockManager.remove(stockId).isDefined
  }
  
  def searchFoodStocks(searchTerm: String): List[FoodStock] = {
    dbService.foodStockManager.searchFoodStocks(searchTerm)
  }
  
  def getFoodStocksByCategory(category: FoodCategory): List[FoodStock] = {
    dbService.foodStockManager.getFoodStocksByCategory(category)
  }
  
  def getFoodStocksByStatus(status: StockStatus): List[FoodStock] = {
    dbService.foodStockManager.getFoodStocksByStatus(status)
  }
  
  def getFoodStocksByLocation(location: String): List[FoodStock] = {
    dbService.foodStockManager.getFoodStocksByLocation(location)
  }
  
  def generateStockAlerts: List[String] = {
    dbService.foodStockManager.generateStockAlerts
  }
  
  def getStockStatistics: (Int, Int, Int, Int) = {
    dbService.foodStockManager.getStockStatistics
  }
  
  def addStock(stockId: String, quantity: Double, userId: String, notes: String = ""): Boolean = {
    dbService.foodStockManager.addStock(stockId, quantity, userId, notes)
  }
  
  def removeStock(stockId: String, quantity: Double, userId: String, notes: String = ""): Boolean = {
    dbService.foodStockManager.removeStock(stockId, quantity, userId, notes)
  }
  
  def adjustStock(stockId: String, newQuantity: Double, userId: String, notes: String = ""): Boolean = {
    dbService.foodStockManager.adjustStock(stockId, newQuantity, userId, notes)
  }
  
  /**
   * Stock Movement Management
   */
  
  def addStockMovement(movement: StockMovement): Boolean = {
    dbService.stockMovementManager.createStockMovement(movement)
    // Update the stock quantity based on the movement
    getFoodStockById(movement.stockId) match {
      case Some(stock) =>
        val updatedStock = movement.actionType match {
          case StockActionType.STOCK_IN =>
            stock.copy(currentQuantity = stock.currentQuantity + movement.quantity)
          case StockActionType.STOCK_OUT =>
            stock.copy(currentQuantity = math.max(0, stock.currentQuantity - movement.quantity))
          case StockActionType.ADJUSTMENT =>
            stock.copy(currentQuantity = movement.quantity) // Set to exact quantity
          case StockActionType.EXPIRED_REMOVAL =>
            stock.copy(currentQuantity = math.max(0, stock.currentQuantity - movement.quantity))
        }
        updateFoodStock(updatedStock)
      case None => false
    }
  }
  
  def getStockMovements(stockId: String): List[StockMovement] = {
    dbService.stockMovementManager.getMovementsByStockId(stockId)
  }
  
  def getAllStockMovements: List[StockMovement] = {
    dbService.stockMovementManager.getAllStockMovements
  }
  
  def getStockMovementsByUser(userId: String): List[StockMovement] = {
    dbService.stockMovementManager.getMovementsByUserId(userId)
  }
  
  def getStockMovementsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List[StockMovement] = {
    dbService.stockMovementManager.getMovementsByDateRange(startDate, endDate)
  }
  
  /**
   * Database Management Operations (Admin Only)
   */
  
  def resetDatabaseForAdmin(): Boolean = {
    if (isCurrentUserAdmin) {
      try {
        dbService.resetDatabase()
        // Clear current session since database is reset
        currentUser = None
        isAnonymousMode = false
        true
      } catch {
        case e: Exception =>
          println(s"Error resetting database: ${e.getMessage}")
          false
      }
    } else {
      false
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
        // Initialize sample data only if no topics exist
        if (service.getDiscussionTopics.isEmpty) {
          service.initializeSampleDiscussionData()
        }
        instance = Some(service)
        service
    }
  }
}
