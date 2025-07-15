package manager

import model._
import java.time.LocalDateTime

/**
 * Manager class for handling discussion forum operations
 */
class DiscussionForumManager extends Manager[DiscussionTopic] {
  
  /**
   * Create a new discussion topic
   * @param topic the topic to create
   */
  def createTopic(topic: DiscussionTopic): Unit = {
    add(topic.topicId, topic)
  }
  
  /**
   * Get topics by category
   * @param category the category to filter by
   * @return list of topics in the specified category
   */
  def getTopicsByCategory(category: DiscussionCategory): List[DiscussionTopic] = {
    items.values.filter(_.category == category).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get active topics (not closed)
   * @return list of active topics
   */
  def getActiveTopics: List[DiscussionTopic] = {
    items.values.filter(_.isActive).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get pinned topics
   * @return list of pinned topics
   */
  def getPinnedTopics: List[DiscussionTopic] = {
    items.values.filter(_.isPinned).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get topics by author
   * @param authorId the author ID to filter by
   * @return list of topics by the specified author
   */
  def getTopicsByAuthor(authorId: String): List[DiscussionTopic] = {
    items.values.filter(_.authorId == authorId).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Search topics by title or description
   * @param searchTerm the term to search for
   * @return list of matching topics
   */
  def searchTopics(searchTerm: String): List[DiscussionTopic] = {
    val term = searchTerm.toLowerCase
    items.values.filter { topic =>
      topic.title.toLowerCase.contains(term) || 
      topic.description.toLowerCase.contains(term)
    }.toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get most popular topics (by likes and replies)
   * @param limit maximum number of topics to return
   * @return list of popular topics
   */
  def getPopularTopics(limit: Int = 10): List[DiscussionTopic] = {
    items.values.toList
      .sortBy(topic => topic.likes + topic.getReplyCount)
      .reverse
      .take(limit)
  }
  
  /**
   * Get recent topics (within specified days)
   * @param days number of days to look back
   * @return list of recent topics
   */
  def getRecentTopics(days: Int = 7): List[DiscussionTopic] = {
    val cutoffDate = LocalDateTime.now().minusDays(days)
    items.values.filter(_.timestamp.isAfter(cutoffDate)).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Add reply to topic
   * @param topicId the topic ID
   * @param reply the reply to add
   * @return true if successful, false if topic not found
   */
  def addReply(topicId: String, reply: Reply): Boolean = {
    get(topicId) match {
      case Some(topic) if topic.isActive =>
        topic.addReply(reply)
        true
      case _ => false
    }
  }
  
  /**
   * Add comment to topic
   * @param topicId the topic ID
   * @param comment the comment to add
   * @return true if successful, false if topic not found
   */
  def addComment(topicId: String, comment: Comment): Boolean = {
    get(topicId) match {
      case Some(topic) =>
        topic.addComment(comment)
        true
      case None => false
    }
  }
  
  /**
   * Add like to topic
   * @param topicId the topic ID
   * @return true if successful, false if topic not found
   */
  def addLike(topicId: String): Boolean = {
    get(topicId) match {
      case Some(topic) =>
        topic.addLike()
        true
      case None => false
    }
  }
  
  /**
   * Pin topic (admin function)
   * @param topicId the topic ID
   * @return true if successful, false if topic not found
   */
  def pinTopic(topicId: String): Boolean = {
    get(topicId) match {
      case Some(topic) =>
        topic.pin()
        true
      case None => false
    }
  }
  
  /**
   * Unpin topic (admin function)
   * @param topicId the topic ID
   * @return true if successful, false if topic not found
   */
  def unpinTopic(topicId: String): Boolean = {
    get(topicId) match {
      case Some(topic) =>
        topic.unpin()
        true
      case None => false
    }
  }
  
  /**
   * Close topic (admin function)
   * @param topicId the topic ID
   * @return true if successful, false if topic not found
   */
  def closeTopic(topicId: String): Boolean = {
    get(topicId) match {
      case Some(topic) =>
        topic.close()
        true
      case None => false
    }
  }
  
  /**
   * Moderate topic (admin function)
   * @param topicId the topic ID
   * @param adminId the admin user ID
   * @return true if successful, false if topic not found
   */
  def moderateTopic(topicId: String, adminId: String): Boolean = {
    get(topicId) match {
      case Some(topic) =>
        topic.moderate(adminId)
        true
      case None => false
    }
  }
}
