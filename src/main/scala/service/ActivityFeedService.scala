package service

import model._
import database.service.DatabaseService
import java.time.LocalDateTime
import java.util.UUID

/**
 * Service for managing the unified activity feed
 * Combines announcements, food posts, events, and discussions into a single feed
 */
class ActivityFeedService {
  
  private val dbService = DatabaseService.getInstance
  
  /**
   * Get the unified activity feed with all content types
   * @param limit maximum number of items to return
   * @param userId optional user ID for personalized content (likes, etc.)
   * @return List of activity feed items sorted by timestamp (newest first)
   */
  def getActivityFeed(limit: Int = 50, userId: Option[String] = None): List[ActivityFeedItem] = {
    try {
      // Get announcements
      val announcementItems = dbService.getAllAnnouncements.filter(_.isActive).map { announcement =>
        val authorName = dbService.findUserById(announcement.authorId)
          .map(_.name)
          .getOrElse("Unknown User")
        ActivityFeedItem.fromAnnouncement(announcement, authorName)
      }
      
      // Get food posts
      val foodPostItems = dbService.allFoodPosts.filter(_.status == FoodPostStatus.PENDING).map { foodPost =>
        val authorName = dbService.findUserById(foodPost.authorId)
          .map(_.name)
          .getOrElse("Unknown User")
        ActivityFeedItem.fromFoodPost(foodPost, authorName)
      }
      
      // Get events (upcoming events only)
      val eventItems = getUpcomingEvents().map { event =>
        val organizerName = dbService.findUserById(event.organizerId)
          .map(_.name)
          .getOrElse("Unknown User")
        ActivityFeedItem.fromEvent(event, organizerName)
      }
      
      // Get discussion topics
      val discussionItems = getActiveDiscussionTopics().map { topic =>
        val authorName = dbService.findUserById(topic.authorId)
          .map(_.name)
          .getOrElse("Unknown User")
        ActivityFeedItem.fromDiscussionTopic(topic, authorName)
      }
      
      // Combine all items and sort by timestamp (newest first) and take limit
      (announcementItems ++ foodPostItems ++ eventItems ++ discussionItems)
        .sortBy(_.timestamp)
        .reverse
        .take(limit)
        
    } catch {
      case e: Exception =>
        println(s"Error loading activity feed: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * Get activity feed filtered by type
   */
  def getActivityFeedByType(feedType: ActivityFeedType, limit: Int = 20, userId: Option[String] = None): List[ActivityFeedItem] = {
    getActivityFeed(limit = 100, userId)
      .filter(_.feedType == feedType)
      .take(limit)
  }
  
  /**
   * Like an activity feed item
   */
  def likeItem(itemId: String, userId: String, feedType: ActivityFeedType): Boolean = {
    try {
      feedType match {
        case ActivityFeedType.ANNOUNCEMENT =>
          dbService.findAnnouncementById(itemId) match {
            case Some(announcement) =>
              announcement.addLike()
              dbService.updateAnnouncementLikes(itemId, announcement.likes)
            case None => false
          }
          
        case ActivityFeedType.FOOD_SHARING =>
          dbService.findFoodPostById(itemId) match {
            case Some(foodPost) =>
              foodPost.addLike()
              dbService.updateFoodPostLikes(itemId, foodPost.likes)
            case None => false
          }
          
        case ActivityFeedType.EVENT =>
          // TODO: Implement event likes when event DAO is available
          true
          
        case ActivityFeedType.DISCUSSION =>
          // TODO: Implement discussion likes when discussion DAO is available
          true
          
        case _ => false
      }
    } catch {
      case e: Exception =>
        println(s"Error liking item: ${e.getMessage}")
        false
    }
  }
  
  /**
   * Unlike an activity feed item
   */
  def unlikeItem(itemId: String, userId: String, feedType: ActivityFeedType): Boolean = {
    try {
      feedType match {
        case ActivityFeedType.ANNOUNCEMENT =>
          dbService.findAnnouncementById(itemId) match {
            case Some(announcement) =>
              announcement.removeLike()
              dbService.updateAnnouncementLikes(itemId, announcement.likes)
            case None => false
          }
          
        case ActivityFeedType.FOOD_SHARING =>
          dbService.findFoodPostById(itemId) match {
            case Some(foodPost) =>
              foodPost.removeLike()
              dbService.updateFoodPostLikes(itemId, foodPost.likes)
            case None => false
          }
          
        case _ => true // For event and discussion, return true for now
      }
    } catch {
      case e: Exception =>
        println(s"Error unliking item: ${e.getMessage}")
        false
    }
  }
  
  /**
   * Add comment to an activity feed item
   */
  def addComment(itemId: String, userId: String, content: String, feedType: ActivityFeedType): Boolean = {
    try {
      val commentId = UUID.randomUUID().toString
      val comment = Comment(commentId, userId, content)
      
      val contentTypeString = feedType match {
        case ActivityFeedType.ANNOUNCEMENT => "announcement"
        case ActivityFeedType.FOOD_SHARING => "food_post"
        case ActivityFeedType.EVENT => "event"
        case ActivityFeedType.DISCUSSION => "discussion"
        case _ => "unknown"
      }
      
      dbService.saveComment(contentTypeString, itemId, comment)
    } catch {
      case e: Exception =>
        println(s"Error adding comment: ${e.getMessage}")
        false
    }
  }
  
  /**
   * Get comments for an item
   */
  def getComments(itemId: String, feedType: ActivityFeedType): List[Comment] = {
    try {
      val contentTypeString = feedType match {
        case ActivityFeedType.ANNOUNCEMENT => "announcement"
        case ActivityFeedType.FOOD_SHARING => "food_post"
        case ActivityFeedType.EVENT => "event"
        case ActivityFeedType.DISCUSSION => "discussion"
        case _ => "unknown"
      }
      
      dbService.getComments(contentTypeString, itemId)
    } catch {
      case e: Exception =>
        println(s"Error getting comments: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * Search activity feed
   */
  def searchActivityFeed(searchTerm: String, feedTypes: List[ActivityFeedType] = List.empty): List[ActivityFeedItem] = {
    val allItems = getActivityFeed(limit = 200)
    
    val filteredByType = if (feedTypes.nonEmpty) {
      allItems.filter(item => feedTypes.contains(item.feedType))
    } else {
      allItems
    }
    
    filteredByType.filter { item =>
      item.title.toLowerCase.contains(searchTerm.toLowerCase) ||
      item.content.toLowerCase.contains(searchTerm.toLowerCase) ||
      item.authorName.toLowerCase.contains(searchTerm.toLowerCase)
    }
  }
  
  /**
   * Get trending items (most liked in the last 24 hours)
   */
  def getTrendingItems(limit: Int = 10): List[ActivityFeedItem] = {
    val oneDayAgo = LocalDateTime.now().minusDays(1)
    
    getActivityFeed(limit = 100)
      .filter(_.timestamp.isAfter(oneDayAgo))
      .sortBy(_.likes)
      .reverse
      .take(limit)
  }
  
  /**
   * Get recent activity for a specific user
   */
  def getUserActivity(userId: String, limit: Int = 20): List[ActivityFeedItem] = {
    getActivityFeed(limit = 200)
      .filter(_.authorId == userId)
      .take(limit)
  }
  
  // Helper methods for getting data not yet in DatabaseService
  
  private def getUpcomingEvents(): List[Event] = {
    dbService.upcomingEvents
  }
  
  private def getActiveDiscussionTopics(): List[DiscussionTopic] = {
    // TODO: This should be replaced with proper database call when DiscussionDAO is implemented
    // For now, return empty list or mock data
    List.empty
  }
  
  /**
   * Get activity feed statistics
   */
  def getActivityFeedStats(): Map[String, Int] = {
    try {
      val announcements = dbService.getAllAnnouncements.size
      val foodPosts = dbService.allFoodPosts.size
      val events = getUpcomingEvents().size
      val discussions = getActiveDiscussionTopics().size
      
      Map(
        "announcements" -> announcements,
        "foodPosts" -> foodPosts,
        "events" -> events,
        "discussions" -> discussions,
        "total" -> (announcements + foodPosts + events + discussions)
      )
    } catch {
      case e: Exception =>
        println(s"Error getting stats: ${e.getMessage}")
        Map.empty
    }
  }
}
