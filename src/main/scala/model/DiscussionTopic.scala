package model

import java.time.LocalDateTime

/**
 * Immutable case class representing a discussion topic
 * @param topicId unique identifier for the topic
 * @param authorId ID of the topic creator
 * @param title topic title
 * @param description topic description
 * @param category topic category
 * @param timestamp creation timestamp
 * @param isActive whether the topic is active
 * @param isPinned whether the topic is pinned
 * @param replies list of replies
 * @param likes number of likes
 * @param comments list of comments
 * @param isModerated moderation status
 * @param moderatedBy moderator ID
 * @param moderationDate moderation timestamp
 */
case class DiscussionTopic(
  topicId: String,
  authorId: String,
  title: String,
  description: String,
  category: DiscussionCategory,
  timestamp: LocalDateTime = LocalDateTime.now(),
  isActive: Boolean = true,
  isPinned: Boolean = false,
  replies: List[Reply] = List.empty,
  likes: Int = 0,
  comments: List[Comment] = List.empty,
  isModerated: Boolean = false,
  moderatedBy: Option[String] = None,
  moderationDate: Option[LocalDateTime] = None
) extends Likeable with Moderatable {
  
  // Likeable trait implementation
  def withLike: DiscussionTopic = copy(likes = likes + 1)
  def withoutLike: DiscussionTopic = copy(likes = if (likes > 0) likes - 1 else 0)
  def withComment(comment: Comment): DiscussionTopic = copy(comments = comment :: comments)
  
  // Moderatable trait implementation
  def withModeration(adminId: String): DiscussionTopic = copy(
    isModerated = true,
    moderatedBy = Some(adminId),
    moderationDate = Some(LocalDateTime.now())
  )
  
  /**
   * Add a reply to this topic
   * @param reply the reply to add
   * @return updated DiscussionTopic with new reply
   */
  def withReply(reply: Reply): DiscussionTopic = copy(replies = reply :: replies)
  
  /**
   * Pin the topic (admin function)
   * @return updated DiscussionTopic with pinned status
   */
  def pin: DiscussionTopic = copy(isPinned = true)
  
  /**
   * Unpin the topic (admin function)
   * @return updated DiscussionTopic with unpinned status
   */
  def unpin: DiscussionTopic = copy(isPinned = false)
  
  /**
   * Close the topic (admin function)
   * @return updated DiscussionTopic with inactive status
   */
  def close: DiscussionTopic = copy(isActive = false)
  
  /**
   * Open the topic (admin function)
   * @return updated DiscussionTopic with active status
   */
  def open: DiscussionTopic = copy(isActive = true)
  
  /**
   * Get the number of replies
   * @return number of replies
   */
  def replyCount: Int = replies.length
}

/**
 * Immutable case class representing a reply to a discussion topic
 * @param replyId unique identifier for the reply
 * @param topicId ID of the topic this reply belongs to
 * @param authorId ID of the reply author
 * @param content reply content
 * @param timestamp creation timestamp
 * @param likes number of likes
 * @param comments list of comments
 * @param isModerated moderation status
 * @param moderatedBy moderator ID
 * @param moderationDate moderation timestamp
 */
case class Reply(
  replyId: String,
  topicId: String,
  authorId: String,
  content: String,
  timestamp: LocalDateTime = LocalDateTime.now(),
  likes: Int = 0,
  comments: List[Comment] = List.empty,
  isModerated: Boolean = false,
  moderatedBy: Option[String] = None,
  moderationDate: Option[LocalDateTime] = None
) extends Likeable with Moderatable {
  
  def withLike: Reply = copy(likes = likes + 1)
  def withoutLike: Reply = copy(likes = if (likes > 0) likes - 1 else 0)
  def withComment(comment: Comment): Reply = copy(comments = comment :: comments)
  def withModeration(adminId: String): Reply = copy(
    isModerated = true,
    moderatedBy = Some(adminId),
    moderationDate = Some(LocalDateTime.now())
  )
}

/**
 * Enumeration for discussion categories
 */
enum DiscussionCategory:
  case NUTRITION, SUSTAINABLE_AGRICULTURE, FOOD_SECURITY, COMMUNITY_GARDEN, 
       COOKING_TIPS, GENERAL, EVENTS, ANNOUNCEMENTS, FOOD_PRESERVATION,
       COMMUNITY_PROJECTS, SUSTAINABILITY, RESOURCE_SHARING, EDUCATION
