package model

import java.time.LocalDateTime

/**
 * Case class representing a discussion topic
 * @param topicId unique identifier for the topic
 * @param authorId ID of the topic creator
 * @param title topic title
 * @param description topic description
 * @param category topic category (nutrition, sustainable_agriculture, etc.)
 * @param timestamp when the topic was created
 * @param isActive whether the topic is active
 * @param isPinned whether the topic is pinned
 * @param replies list of replies
 * @param likes number of likes
 * @param comments list of comments
 * @param isModerated whether the topic is moderated
 * @param moderatedBy ID of the moderator
 * @param moderationDate when the topic was moderated
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
  
  /**
   * Add a reply to this topic
   * @param reply the reply to add
   */
  def addReply(reply: Reply): DiscussionTopic = {
    this.copy(replies = reply :: replies)
  }
  
  /**
   * Pin the topic (admin function)
   */
  def pin(): DiscussionTopic = {
    this.copy(isPinned = true)
  }
  
  /**
   * Unpin the topic (admin function)
   */
  def unpin(): DiscussionTopic = {
    this.copy(isPinned = false)
  }
  
  /**
   * Close the topic (admin function)
   */
  def close(): DiscussionTopic = {
    this.copy(isActive = false)
  }
  
  /**
   * Get the number of replies
   * @return number of replies
   */
  def replyCount: Int = replies.length
  
  /**
   * Implement Likeable trait methods
   */
  def addLike(): DiscussionTopic = {
    this.copy(likes = likes + 1)
  }
  
  def removeLike(): DiscussionTopic = {
    this.copy(likes = if (likes > 0) likes - 1 else 0)
  }
  
  def addComment(comment: Comment): DiscussionTopic = {
    this.copy(comments = comment :: comments)
  }
  
  /**
   * Implement Moderatable trait method
   */
  def moderate(adminId: String): DiscussionTopic = {
    this.copy(
      isModerated = true,
      moderatedBy = Some(adminId),
      moderationDate = Some(LocalDateTime.now())
    )
  }
}

/**
 * Case class representing a reply to a discussion topic
 * @param replyId unique identifier for the reply
 * @param topicId ID of the topic this reply belongs to
 * @param authorId ID of the reply author
 * @param content reply content
 * @param timestamp when the reply was created
 * @param likes number of likes
 * @param comments list of comments (nested replies)
 * @param isModerated whether the reply is moderated
 * @param moderatedBy ID of the moderator
 * @param moderationDate when the reply was moderated
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
  
  /**
   * Implement Likeable trait methods
   */
  def addLike(): Reply = {
    this.copy(likes = likes + 1)
  }
  
  def removeLike(): Reply = {
    this.copy(likes = if (likes > 0) likes - 1 else 0)
  }
  
  def addComment(comment: Comment): Reply = {
    this.copy(comments = comment :: comments)
  }
  
  /**
   * Implement Moderatable trait method
   */
  def moderate(adminId: String): Reply = {
    this.copy(
      isModerated = true,
      moderatedBy = Some(adminId),
      moderationDate = Some(LocalDateTime.now())
    )
  }
}

/**
 * Enumeration for discussion categories
 */
enum DiscussionCategory:
  case NUTRITION, SUSTAINABLE_AGRICULTURE, FOOD_SECURITY, COMMUNITY_GARDEN, 
       COOKING_TIPS, GENERAL, EVENTS, ANNOUNCEMENTS, FOOD_PRESERVATION,
       COMMUNITY_PROJECTS, SUSTAINABILITY, RESOURCE_SHARING, EDUCATION
