package model

import java.time.LocalDateTime

/**
 * Enumeration for activity feed item types (like Facebook categories)
 */
enum ActivityFeedType:
  case ANNOUNCEMENT, FOOD_SHARING, DISCUSSION, EVENT, USER_ACTIVITY

/**
 * Immutable case class representing a unified activity feed item
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
  likes: Int = 0,
  comments: List[Comment] = List.empty,
  // Optional fields for specific content types
  category: Option[String] = None,
  location: Option[String] = None,
  eventDateTime: Option[LocalDateTime] = None,
  expiryDate: Option[LocalDateTime] = None,
  // Interaction tracking
  likedByUsers: Set[String] = Set.empty,
  isActive: Boolean = true
) {
  
  /**
   * Add a like from a specific user
   */
  def withLike(userId: String): Option[ActivityFeedItem] = {
    if (!likedByUsers.contains(userId)) {
      Some(this.copy(
        likes = likes + 1,
        likedByUsers = likedByUsers + userId
      ))
    } else {
      None
    }
  }
  
  /**
   * Remove a like from a specific user
   */
  def withoutLike(userId: String): Option[ActivityFeedItem] = {
    if (likedByUsers.contains(userId)) {
      Some(this.copy(
        likes = likes - 1,
        likedByUsers = likedByUsers - userId
      ))
    } else {
      None
    }
  }
  
  /**
   * Toggle like for a user
   */
  def toggleLike(userId: String): (ActivityFeedItem, Boolean) = {
    if (likedByUsers.contains(userId)) {
      (this.copy(
        likes = likes - 1,
        likedByUsers = likedByUsers - userId
      ), false) // unliked
    } else {
      (this.copy(
        likes = likes + 1,
        likedByUsers = likedByUsers + userId
      ), true) // liked
    }
  }
  
  /**
   * Add a comment
   */
  def withComment(comment: Comment): ActivityFeedItem = {
    this.copy(comments = comment :: comments)
  }
  
  /**
   * Deactivate this feed item
   */
  def deactivate: ActivityFeedItem = {
    this.copy(isActive = false)
  }
  
  /**
   * Activate this feed item
   */
  def activate: ActivityFeedItem = {
    this.copy(isActive = true)
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
  def timeAgo: String = {
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
  def categoryIcon: String = feedType match {
    case ActivityFeedType.ANNOUNCEMENT => "📢"
    case ActivityFeedType.FOOD_SHARING => "🍕"
    case ActivityFeedType.DISCUSSION => "💬"
    case ActivityFeedType.EVENT => "📅"
    case ActivityFeedType.USER_ACTIVITY => "👤"
  }
  
  /**
   * Get category display name
   */
  def categoryName: String = feedType match {
    case ActivityFeedType.ANNOUNCEMENT => "Announcement"
    case ActivityFeedType.FOOD_SHARING => "Food Sharing"
    case ActivityFeedType.DISCUSSION => "Discussion"
    case ActivityFeedType.EVENT => "Event"
    case ActivityFeedType.USER_ACTIVITY => "User Activity"
  }
  
  /**
   * Get category color for UI
   */
  def categoryColor: String = feedType match {
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
