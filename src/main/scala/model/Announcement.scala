package model

import java.time.LocalDateTime

/**
 * Immutable data structure for content that can be liked and commented on
 */
trait Likeable {
  def likes: Int
  def comments: List[Comment]
  
  def withLike: Likeable
  def withoutLike: Likeable
  def withComment(comment: Comment): Likeable
}

/**
 * Immutable data structure for content that can be moderated
 */
trait Moderatable {
  def isModerated: Boolean
  def moderatedBy: Option[String]
  def moderationDate: Option[LocalDateTime]
  
  def withModeration(adminId: String): Moderatable
}

/**
 * Immutable case class representing a comment
 * @param commentId unique identifier for the comment
 * @param authorId ID of the comment author
 * @param content comment content
 * @param timestamp when the comment was created
 * @param likes number of likes
 * @param comments nested comments
 * @param isModerated moderation status
 * @param moderatedBy moderator ID
 * @param moderationDate moderation timestamp
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
  
  def withLike: Comment = copy(likes = likes + 1)
  def withoutLike: Comment = copy(likes = if (likes > 0) likes - 1 else 0)
  def withComment(comment: Comment): Comment = copy(comments = comment :: comments)
  def withModeration(adminId: String): Comment = copy(
    isModerated = true,
    moderatedBy = Some(adminId),
    moderationDate = Some(LocalDateTime.now())
  )
}

/**
 * Immutable Announcement case class for community announcements
 * @param announcementId unique identifier
 * @param authorId ID of the announcement author
 * @param title announcement title
 * @param content announcement content
 * @param announcementType type of announcement
 * @param timestamp creation timestamp
 * @param isActive whether the announcement is active
 * @param priority announcement priority
 * @param likes number of likes
 * @param comments list of comments
 * @param isModerated moderation status
 * @param moderatedBy moderator ID
 * @param moderationDate moderation timestamp
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
  
  def withLike: Announcement = copy(likes = likes + 1)
  def withoutLike: Announcement = copy(likes = if (likes > 0) likes - 1 else 0)
  def withComment(comment: Comment): Announcement = copy(comments = comment :: comments)
  def withModeration(adminId: String): Announcement = copy(
    isModerated = true,
    moderatedBy = Some(adminId),
    moderationDate = Some(LocalDateTime.now())
  )
  
  def deactivate: Announcement = copy(isActive = false)
  def activate: Announcement = copy(isActive = true)
  def withPriority(newPriority: Priority): Announcement = copy(priority = newPriority)
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
