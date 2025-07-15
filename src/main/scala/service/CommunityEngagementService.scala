package service

import manager._
import model._
import java.time.LocalDateTime
import java.util.UUID

/**
 * Main service class that coordinates all managers and provides high-level operations
 * This class implements the Singleton pattern to ensure single instance
 */
class CommunityEngagementService {
  
  // Manager instances
  val userManager = new UserManager()
  val announcementBoard = new AnnouncementBoard()
  val foodPostManager = new FoodPostManager()
  val discussionForumManager = new DiscussionForumManager()
  val eventManager = new EventManager()
  val notificationManager = new NotificationManager()
  
  // Current logged-in user
  private var currentUser: Option[User] = None
  
  /**
   * Initialize the service with some sample data
   */
  def initializeWithSampleData(): Unit = {
    // Create sample admin user
    val admin = new AdminUser(
      userId = "admin1",
      username = "admin",
      email = "admin@community.org",
      name = "System Administrator",
      contactInfo = "admin@community.org"
    )
    userManager.registerUser(admin)
    
    // Create sample community members
    val member1 = new CommunityMember(
      userId = "user1",
      username = "john_doe",
      email = "john@example.com",
      name = "John Doe",
      contactInfo = "john@example.com"
    )
    
    val member2 = new CommunityMember(
      userId = "user2",
      username = "jane_smith",
      email = "jane@example.com",
      name = "Jane Smith",
      contactInfo = "jane@example.com"
    )
    
    userManager.registerUser(member1)
    userManager.registerUser(member2)
    
    // Create sample announcements
    val announcement1 = Announcement(
      announcementId = UUID.randomUUID().toString,
      authorId = admin.userId,
      title = "Welcome to Community Engagement Platform",
      content = "Welcome everyone! This platform helps our community share resources and collaborate for food security.",
      announcementType = AnnouncementType.GENERAL
    )
    
    val announcement2 = Announcement(
      announcementId = UUID.randomUUID().toString,
      authorId = admin.userId,
      title = "Food Distribution Event This Weekend",
      content = "Join us this Saturday for our monthly food distribution event at the community center.",
      announcementType = AnnouncementType.FOOD_DISTRIBUTION
    )
    
    announcementBoard.postAnnouncement(announcement1)
    announcementBoard.postAnnouncement(announcement2)
    
    // Create sample food posts
    val foodPost1 = FoodPost(
      postId = UUID.randomUUID().toString,
      authorId = member1.userId,
      title = "Fresh vegetables available",
      description = "I have excess vegetables from my garden. Free to good home!",
      postType = FoodPostType.OFFER,
      quantity = "5 bags",
      location = "Downtown Community Center",
      expiryDate = Some(LocalDateTime.now().plusDays(2))
    )
    
    val foodPost2 = FoodPost(
      postId = UUID.randomUUID().toString,
      authorId = member2.userId,
      title = "Looking for canned goods",
      description = "Family in need of non-perishable food items.",
      postType = FoodPostType.REQUEST,
      quantity = "Any amount",
      location = "North Side"
    )
    
    foodPostManager.createFoodPost(foodPost1)
    foodPostManager.createFoodPost(foodPost2)
    
    // Create sample discussion topic
    val topic1 = DiscussionTopic(
      topicId = UUID.randomUUID().toString,
      authorId = member1.userId,
      title = "Tips for Urban Gardening",
      description = "Let's share tips and experiences about growing food in urban environments.",
      category = DiscussionCategory.SUSTAINABLE_AGRICULTURE
    )
    
    discussionForumManager.createTopic(topic1)
    
    // Create sample event
    val event1 = Event(
      eventId = UUID.randomUUID().toString,
      organizerId = admin.userId,
      title = "Community Garden Workshop",
      description = "Learn how to start and maintain a community garden. All skill levels welcome!",
      location = "Community Center Room A",
      startDateTime = LocalDateTime.now().plusDays(7),
      endDateTime = LocalDateTime.now().plusDays(7).plusHours(3),
      maxParticipants = Some(20)
    )
    
    eventManager.createEvent(event1)
  }
  
  /**
   * User Authentication
   */
  
  def login(username: String, password: String): Option[User] = {
    val user = userManager.authenticate(username, password)
    currentUser = user
    user
  }
  
  def logout(): Unit = {
    currentUser = None
  }
  
  def getCurrentUser: Option[User] = currentUser
  
  def isLoggedIn: Boolean = currentUser.isDefined
  
  def registerUser(username: String, email: String, name: String, contactInfo: String, isAdmin: Boolean = false): Boolean = {
    val userId = UUID.randomUUID().toString
    val user = if (isAdmin) {
      new AdminUser(userId, username, email, name, contactInfo)
    } else {
      new CommunityMember(userId, username, email, name, contactInfo)
    }
    userManager.registerUser(user)
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
      announcementBoard.postAnnouncement(announcement)
      
      // Notify all users about new announcement (simplified)
      userManager.getAll.foreach { recipient =>
        if (recipient.userId != user.userId) {
          notificationManager.createAnnouncementNotification(
            recipient.userId,
            announcement.announcementId,
            title
          )
        }
      }
      
      announcement
    }
  }
  
  def getAnnouncements: List[Announcement] = {
    announcementBoard.getActiveAnnouncements
  }
  
  def searchAnnouncements(searchTerm: String): List[Announcement] = {
    announcementBoard.searchAnnouncements(searchTerm)
  }
  
  def addCommentToAnnouncement(announcementId: String, content: String): Boolean = {
    currentUser.exists { user =>
      val comment = Comment(
        commentId = UUID.randomUUID().toString,
        authorId = user.userId,
        content = content
      )
      announcementBoard.addComment(announcementId, comment)
    }
  }
  
  def likeAnnouncement(announcementId: String): Boolean = {
    announcementBoard.addLike(announcementId)
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
      foodPostManager.createFoodPost(post)
      
      // Notify relevant users
      userManager.getAll.foreach { recipient =>
        if (recipient.userId != user.userId) {
          notificationManager.createFoodNotification(
            recipient.userId,
            user.userId,
            post.postId,
            title,
            postType == FoodPostType.OFFER
          )
        }
      }
      
      post
    }
  }
  
  def getFoodPosts: List[FoodPost] = {
    foodPostManager.getActiveFoodPosts
  }
  
  def getFoodPostsByType(postType: FoodPostType): List[FoodPost] = {
    foodPostManager.getFoodPostsByType(postType)
  }
  
  def searchFoodPosts(searchTerm: String): List[FoodPost] = {
    foodPostManager.searchFoodPosts(searchTerm)
  }
  
  def acceptFoodPost(postId: String): Boolean = {
    currentUser.exists { user =>
      foodPostManager.acceptFoodPost(postId, user.userId)
    }
  }
  
  /**
   * Discussion Forum Operations
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
      discussionForumManager.createTopic(topic)
      topic
    }
  }
  
  def getDiscussionTopics: List[DiscussionTopic] = {
    discussionForumManager.getActiveTopics
  }
  
  def getTopicsByCategory(category: DiscussionCategory): List[DiscussionTopic] = {
    discussionForumManager.getTopicsByCategory(category)
  }
  
  def addReplyToTopic(topicId: String, content: String): Boolean = {
    currentUser.exists { user =>
      val reply = Reply(
        replyId = UUID.randomUUID().toString,
        topicId = topicId,
        authorId = user.userId,
        content = content
      )
      discussionForumManager.addReply(topicId, reply)
    }
  }
  
  def searchTopics(searchTerm: String): List[DiscussionTopic] = {
    discussionForumManager.searchTopics(searchTerm)
  }
  
  def likeTopic(topicId: String): Boolean = {
    discussionForumManager.addLike(topicId)
  }
  
  /**
   * Event Management Operations
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
      eventManager.createEvent(event)
      event
    }
  }
  
  def getUpcomingEvents: List[Event] = {
    eventManager.getUpcomingEvents
  }
  
  def rsvpToEvent(eventId: String): Boolean = {
    currentUser.exists { user =>
      val success = eventManager.rsvpToEvent(eventId, user.userId)
      if (success) {
        eventManager.get(eventId).foreach { event =>
          notificationManager.createMessageNotification(
            user.userId,
            "system",
            "RSVP Confirmation",
            s"You have successfully RSVP'd to '${event.title}'",
            Some(eventId)
          )
        }
      }
      success
    }
  }
  
  def cancelRsvp(eventId: String): Boolean = {
    currentUser.exists { user =>
      eventManager.cancelRsvp(eventId, user.userId)
    }
  }
  
  def getMyEvents(userId: String): List[Event] = {
    eventManager.getEventsByParticipant(userId)
  }
  
  def searchEvents(searchTerm: String): List[Event] = {
    eventManager.searchEvents(searchTerm)
  }
  
  /**
   * Notification Operations
   */
  
  def getNotifications: List[Notification] = {
    currentUser.map { user =>
      notificationManager.getNotificationsForUser(user.userId)
    }.getOrElse(List.empty)
  }
  
  def getUnreadNotifications: List[Notification] = {
    currentUser.map { user =>
      notificationManager.getUnreadNotifications(user.userId)
    }.getOrElse(List.empty)
  }
  
  def markNotificationAsRead(notificationId: String): Boolean = {
    notificationManager.markAsRead(notificationId)
  }
  
  def getUnreadNotificationCount: Int = {
    currentUser.map { user =>
      notificationManager.getUnreadCount(user.userId)
    }.getOrElse(0)
  }
  
  def markAllNotificationsAsRead: Int = {
    currentUser.map { user =>
      notificationManager.markAllAsRead(user.userId)
    }.getOrElse(0)
  }
  
  def deleteNotification(notificationId: String): Boolean = {
    notificationManager.remove(notificationId).isDefined
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
          case "announcement" => announcementBoard.moderateAnnouncement(contentId, admin.userId)
          case "foodpost" => foodPostManager.get(contentId).exists { post =>
            post.moderate(admin.userId)
            true
          }
          case "topic" => discussionForumManager.moderateTopic(contentId, admin.userId)
          case "event" => eventManager.get(contentId).exists { event =>
            event.moderate(admin.userId)
            true
          }
          case _ => false
        }
      }
    } else {
      false
    }
  }
  
  def getAllUsers: List[User] = {
    userManager.getAll
  }
  
  def getDetailedStatistics: Map[String, Any] = {
    val allNotifications = notificationManager.getAll.size
    val allAnnouncements = announcementBoard.getAll
    val allFoodPosts = foodPostManager.getAll
    val allTopics = discussionForumManager.getAll
    val allEvents = eventManager.getAll
    
    val totalComments = allAnnouncements.map(_.comments.size).sum + 
                       allFoodPosts.map(_.comments.size).sum + 
                       allTopics.map(_.comments.size).sum + 
                       allEvents.map(_.comments.size).sum
    
    val totalLikes = allAnnouncements.map(_.likes).sum + 
                    allFoodPosts.map(_.likes).sum + 
                    allTopics.map(_.likes).sum + 
                    allEvents.map(_.likes).sum
    
    Map(
      "totalNotifications" -> allNotifications,
      "totalComments" -> totalComments,
      "totalLikes" -> totalLikes
    )
  }
  
  def getContentForModeration: List[(String, String, String)] = {
    val announcements = announcementBoard.getAll.filter(!_.isModerated)
      .map(a => (a.announcementId, "announcement", a.title))
    val foodPosts = foodPostManager.getAll.filter(!_.isModerated)
      .map(p => (p.postId, "foodpost", p.title))
    val topics = discussionForumManager.getAll.filter(!_.isModerated)
      .map(t => (t.topicId, "topic", t.title))
    val events = eventManager.getAll.filter(!_.isModerated)
      .map(e => (e.eventId, "event", e.title))
    
    announcements ++ foodPosts ++ topics ++ events
  }
  
  /**
   * Statistics and Analytics
   */
  
  def getDashboardStatistics: Map[String, Any] = {
    val userStats = (userManager.size, userManager.getAdminUsers.size, userManager.getCommunityMembers.size)
    val announcementStats = announcementBoard.getActiveAnnouncements.size
    val foodStats = foodPostManager.getStatistics
    val eventStats = eventManager.getStatistics
    val notificationStats = currentUser.map(u => notificationManager.getUnreadCount(u.userId)).getOrElse(0)
    
    Map(
      "totalUsers" -> userStats._1,
      "adminUsers" -> userStats._2,
      "communityMembers" -> userStats._3,
      "activeAnnouncements" -> announcementStats,
      "totalFoodPosts" -> foodStats._1,
      "activeFoodPosts" -> foodStats._2,
      "completedFoodPosts" -> foodStats._3,
      "totalEvents" -> eventStats._1,
      "upcomingEvents" -> eventStats._2,
      "completedEvents" -> eventStats._3,
      "unreadNotifications" -> notificationStats
    )
  }
  
  /**
   * User Profile Operations
   */
  
  def updateUserProfile(newName: String, newContactInfo: String): Boolean = {
    currentUser.exists { user =>
      user.updateProfile(newName, newContactInfo)
      true
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
        service.initializeWithSampleData()
        instance = Some(service)
        service
    }
  }
}
