package model

import java.time.LocalDateTime

/**
 * Enumeration for activity feed item types (like Facebook categories)
 */
enum ActivityFeedType:
  case ANNOUNCEMENT, FOOD_SHARING, DISCUSSION, EVENT, USER_ACTIVITY

/**
 * Case class representing a unified activity feed item
 * This provides a common interface for all content types in the activity feed
 */
case class ActivityFeedItem(
  id: String,
  feedType: ActivityFeedType,
  title: String,
  content: String,
  authorId: String,
  authorName: String,
  timestamp: LocalDateTime,
  var likes: Int = 0,
  var comments: List[Comment] = List.empty,
  // Optional fields for specific content types
  category: Option[String] = None,
  location: Option[String] = None,
  eventDateTime: Option[LocalDateTime] = None,
  expiryDate: Option[LocalDateTime] = None,
  // Interaction tracking
  var likedByUsers: Set[String] = Set.empty,
  var isActive: Boolean = true
) {
  
  /**
   * Add a like from a specific user
   */
  def addLike(userId: String): Boolean = {
    if (!likedByUsers.contains(userId)) {
      likes += 1
      likedByUsers += userId
      true
    } else {
      false
    }
  }
  
  /**
   * Remove a like from a specific user
   */
  def removeLike(userId: String): Boolean = {
    if (likedByUsers.contains(userId)) {
      likes -= 1
      likedByUsers -= userId
      true
    } else {
      false
    }
  }
  
  /**
   * Toggle like for a user
   */
  def toggleLike(userId: String): Boolean = {
    if (likedByUsers.contains(userId)) {
      removeLike(userId)
      false // unliked
    } else {
      addLike(userId)
      true // liked
    }
  }
  
  /**
   * Add a comment
   */
  def addComment(comment: Comment): Unit = {
    comments = comment :: comments
  }
  
  /**
   * Check if a user has liked this item
   */
  def isLikedBy(userId: String): Boolean = {
    likedByUsers.contains(userId)
  }
  
  /**
   * Get formatted time string
   */
  def getTimeAgo: String = {
    val now = LocalDateTime.now()
    val duration = java.time.Duration.between(timestamp, now)
    
    if (duration.toDays() > 0) {
      s"${duration.toDays()} days ago"
    } else if (duration.toHours() > 0) {
      s"${duration.toHours()} hours ago"
    } else if (duration.toMinutes() > 0) {
      s"${duration.toMinutes()} minutes ago"
    } else {
      "Just now"
    }
  }
  
  /**
   * Get category icon based on feed type
   */
  def getCategoryIcon: String = feedType match {
    case ActivityFeedType.ANNOUNCEMENT => "📢"
    case ActivityFeedType.FOOD_SHARING => "🍕"
    case ActivityFeedType.DISCUSSION => "💬"
    case ActivityFeedType.EVENT => "📅"
    case ActivityFeedType.USER_ACTIVITY => "👤"
  }
  
  /**
   * Get category display name
   */
  def getCategoryName: String = feedType match {
    case ActivityFeedType.ANNOUNCEMENT => "Announcement"
    case ActivityFeedType.FOOD_SHARING => "Food Sharing"
    case ActivityFeedType.DISCUSSION => "Discussion"
    case ActivityFeedType.EVENT => "Event"
    case ActivityFeedType.USER_ACTIVITY => "User Activity"
  }
  
  /**
   * Get category color for UI
   */
  def getCategoryColor: String = feedType match {
    case ActivityFeedType.ANNOUNCEMENT => "#1877f2"
    case ActivityFeedType.FOOD_SHARING => "#42b883"
    case ActivityFeedType.DISCUSSION => "#9b59b6"
    case ActivityFeedType.EVENT => "#e74c3c"
    case ActivityFeedType.USER_ACTIVITY => "#34495e"
  }
}

/**
 * Object for creating ActivityFeedItem instances from different models
 */
object ActivityFeedItem {
  
  def fromAnnouncement(announcement: Announcement, authorName: String): ActivityFeedItem = {
    ActivityFeedItem(
      id = announcement.announcementId,
      feedType = ActivityFeedType.ANNOUNCEMENT,
      title = announcement.title,
      content = announcement.content,
      authorId = announcement.authorId,
      authorName = authorName,
      timestamp = announcement.timestamp,
      likes = announcement.likes,
      comments = announcement.comments,
      category = Some(announcement.announcementType.toString),
      likedByUsers = Set.empty // TODO: Load from database
    )
  }
  
  def fromFoodPost(foodPost: FoodPost, authorName: String): ActivityFeedItem = {
    ActivityFeedItem(
      id = foodPost.postId,
      feedType = ActivityFeedType.FOOD_SHARING,
      title = foodPost.title,
      content = foodPost.description,
      authorId = foodPost.authorId,
      authorName = authorName,
      timestamp = foodPost.timestamp,
      likes = foodPost.likes,
      comments = foodPost.comments,
      category = Some(foodPost.postType.toString),
      location = Some(foodPost.location),
      expiryDate = foodPost.expiryDate,
      likedByUsers = Set.empty // TODO: Load from database
    )
  }
  
  def fromEvent(event: Event, organizerName: String): ActivityFeedItem = {
    ActivityFeedItem(
      id = event.eventId,
      feedType = ActivityFeedType.EVENT,
      title = event.title,
      content = event.description,
      authorId = event.organizerId,
      authorName = organizerName,
      timestamp = event.createdAt,
      likes = event.likes,
      comments = event.comments,
      location = Some(event.location),
      eventDateTime = Some(event.startDateTime),
      likedByUsers = Set.empty // TODO: Load from database
    )
  }
  
  def fromDiscussionTopic(topic: DiscussionTopic, authorName: String): ActivityFeedItem = {
    ActivityFeedItem(
      id = topic.topicId,
      feedType = ActivityFeedType.DISCUSSION,
      title = topic.title,
      content = topic.description,
      authorId = topic.authorId,
      authorName = authorName,
      timestamp = topic.timestamp,
      likes = topic.likes,
      comments = topic.comments,
      category = Some(topic.category.toString),
      likedByUsers = Set.empty // TODO: Load from database
    )
  }
}
