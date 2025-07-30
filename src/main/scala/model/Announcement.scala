package model

import java.time.LocalDateTime

/**
 * Trait for content that can be liked and commented on
 */
trait Likeable {
  var likes: Int = 0
  var comments: List[Comment] = List.empty
  
  def addLike(): Unit = {
    likes += 1
  }
  
  def removeLike(): Unit = {
    if (likes > 0) likes -= 1
  }
  
  def addComment(comment: Comment): Unit = {
    comments = comment :: comments
  }
}

/**
 * Trait for content that can be moderated
 */
trait Moderatable {
  var isModerated: Boolean = false
  var moderatedBy: Option[String] = None
  var moderationDate: Option[LocalDateTime] = None
  
  def moderate(adminId: String): Unit = {
    isModerated = true
    moderatedBy = Some(adminId)
    moderationDate = Some(LocalDateTime.now())
  }
}

/**
 * Case class representing a comment
 * @param commentId unique identifier for the comment
 * @param authorId ID of the comment author
 * @param content comment content
 * @param timestamp when the comment was created
 */
case class Comment(
  commentId: String,
  authorId: String,
  content: String,
  timestamp: LocalDateTime = LocalDateTime.now()
) extends Likeable with Moderatable

/**
 * Announcement class for community announcements
 * @param announcementId unique identifier
 * @param authorId ID of the announcement author
 * @param title announcement title
 * @param content announcement content
 * @param announcementType type of announcement (food_distribution, events, tips)
 */
case class Announcement(
  announcementId: String,
  authorId: String,
  title: String,
  content: String,
  announcementType: AnnouncementType,
  timestamp: LocalDateTime = LocalDateTime.now()
) extends Likeable with Moderatable {
  
  var isActive: Boolean = true
  var priority: Priority = Priority.NORMAL
  
  def deactivate(): Unit = {
    isActive = false
  }
  
  def setPriority(newPriority: Priority): Unit = {
    priority = newPriority
  }
}

/**
 * Enumeration for announcement types
 */
enum AnnouncementType:
  case FOOD_DISTRIBUTION, EVENTS, TIPS, GENERAL, EMERGENCY, SKILL_SHARING, COMMUNITY_EVENT

/**
 * Enumeration for priority levels
 */
enum Priority:
  case LOW, NORMAL, HIGH, URGENT
