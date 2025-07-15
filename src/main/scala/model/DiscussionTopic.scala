package model

import java.time.LocalDateTime

/**
 * Case class representing a discussion topic
 * @param topicId unique identifier for the topic
 * @param authorId ID of the topic creator
 * @param title topic title
 * @param description topic description
 * @param category topic category (nutrition, sustainable_agriculture, etc.)
 */
case class DiscussionTopic(
  topicId: String,
  authorId: String,
  title: String,
  description: String,
  category: DiscussionCategory,
  timestamp: LocalDateTime = LocalDateTime.now()
) extends Likeable with Moderatable {
  
  var isActive: Boolean = true
  var isPinned: Boolean = false
  var replies: List[Reply] = List.empty
  
  /**
   * Add a reply to this topic
   * @param reply the reply to add
   */
  def addReply(reply: Reply): Unit = {
    replies = reply :: replies
  }
  
  /**
   * Pin the topic (admin function)
   */
  def pin(): Unit = {
    isPinned = true
  }
  
  /**
   * Unpin the topic (admin function)
   */
  def unpin(): Unit = {
    isPinned = false
  }
  
  /**
   * Close the topic (admin function)
   */
  def close(): Unit = {
    isActive = false
  }
  
  /**
   * Get the number of replies
   * @return number of replies
   */
  def getReplyCount: Int = replies.length
}

/**
 * Case class representing a reply to a discussion topic
 * @param replyId unique identifier for the reply
 * @param topicId ID of the topic this reply belongs to
 * @param authorId ID of the reply author
 * @param content reply content
 */
case class Reply(
  replyId: String,
  topicId: String,
  authorId: String,
  content: String,
  timestamp: LocalDateTime = LocalDateTime.now()
) extends Likeable with Moderatable

/**
 * Enumeration for discussion categories
 */
enum DiscussionCategory:
  case NUTRITION, SUSTAINABLE_AGRICULTURE, FOOD_SECURITY, COMMUNITY_GARDEN, 
       COOKING_TIPS, GENERAL, EVENTS, ANNOUNCEMENTS
