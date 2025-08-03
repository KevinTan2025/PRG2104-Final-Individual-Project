package model

import java.time.LocalDateTime

/**
 * Trait for content that can be liked and commented on
 */
trait Likeable {
  val likes: Int
  val comments: List[Comment]
  
  def addLike(): Likeable
  def removeLike(): Likeable
  def addComment(comment: Comment): Likeable
}

/**
 * Trait for content that can be moderated
 */
trait Moderatable {
  val isModerated: Boolean
  val moderatedBy: Option[String]
  val moderationDate: Option[LocalDateTime]
  
  def moderate(adminId: String): Moderatable
}

/**
 * Case class representing a comment
 * @param commentId unique identifier for the comment
 * @param authorId ID of the comment author
 * @param content comment content
 * @param timestamp when the comment was created
 * @param likes number of likes
 * @param comments nested comments (replies)
 * @param isModerated whether the comment is moderated
 * @param moderatedBy ID of the moderator
 * @param moderationDate when the comment was moderated
 */
case class Comment(
  commentId: String,
  authorId: String,
  content: String,
  timestamp: LocalDateTime = LocalDateTime.now(),
  likes: Int = 0,
  comments: List[Comment] = List.empty,
  isModerated: Boolean = false,
  moderatedBy: Option[String] = None,
  moderationDate: Option[LocalDateTime] = None
) extends Likeable with Moderatable {
  
  def addLike(): Comment = {
    this.copy(likes = likes + 1)
  }
  
  def removeLike(): Comment = {
    this.copy(likes = if (likes > 0) likes - 1 else 0)
  }
  
  def addComment(comment: Comment): Comment = {
    this.copy(comments = comment :: comments)
  }
  
  def moderate(adminId: String): Comment = {
    this.copy(
      isModerated = true,
      moderatedBy = Some(adminId),
      moderationDate = Some(LocalDateTime.now())
    )
  }
}

/**
 * Announcement class for community announcements
 * @param announcementId unique identifier
 * @param authorId ID of the announcement author
 * @param title announcement title
 * @param content announcement content
 * @param announcementType type of announcement (food_distribution, events, tips)
 * @param timestamp when the announcement was created
 * @param isActive whether the announcement is active
 * @param priority priority level of the announcement
 * @param likes number of likes
 * @param comments list of comments
 * @param isModerated whether the announcement is moderated
 * @param moderatedBy ID of the moderator
 * @param moderationDate when the announcement was moderated
 */
case class Announcement(
  announcementId: String,
  authorId: String,
  title: String,
  content: String,
  announcementType: AnnouncementType,
  timestamp: LocalDateTime = LocalDateTime.now(),
  isActive: Boolean = true,
  priority: Priority = Priority.NORMAL,
  likes: Int = 0,
  comments: List[Comment] = List.empty,
  isModerated: Boolean = false,
  moderatedBy: Option[String] = None,
  moderationDate: Option[LocalDateTime] = None
) extends Likeable with Moderatable {
  
  def deactivate(): Announcement = {
    this.copy(isActive = false)
  }
  
  def setPriority(newPriority: Priority): Announcement = {
    this.copy(priority = newPriority)
  }
  
  def addLike(): Announcement = {
    this.copy(likes = likes + 1)
  }
  
  def removeLike(): Announcement = {
    this.copy(likes = if (likes > 0) likes - 1 else 0)
  }
  
  def addComment(comment: Comment): Announcement = {
    this.copy(comments = comment :: comments)
  }
  
  def moderate(adminId: String): Announcement = {
    this.copy(
      isModerated = true,
      moderatedBy = Some(adminId),
      moderationDate = Some(LocalDateTime.now())
    )
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
