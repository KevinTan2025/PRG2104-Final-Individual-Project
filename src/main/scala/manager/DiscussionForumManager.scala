package manager

import model._
import java.time.LocalDateTime
import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal

/**
 * Immutable state for DiscussionForumManager
 * @param topics map of topic ID to DiscussionTopic
 */
case class DiscussionForumManagerState(topics: Map[String, DiscussionTopic] = Map.empty)

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
   * Create a new discussion topic (functional version with Try)
   * @param state current state
   * @param topic the topic to create
   * @return Try containing new state
   */
  def createTopic(state: DiscussionForumManagerState, topic: DiscussionTopic): Try[DiscussionForumManagerState] = {
    Try {
      state.copy(topics = state.topics + (topic.topicId -> topic))
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to create topic: ${e.getMessage}", e)
    }
  }
  
  /**
   * Create a new discussion topic (safe functional version)
   * @param state current state
   * @param topic the topic to create
   * @return new state or original state if failed
   */
  def createTopicSafe(state: DiscussionForumManagerState, topic: DiscussionTopic): DiscussionForumManagerState = {
    createTopic(state, topic).getOrElse(state)
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
   * Get topics by category (functional version with Try)
   * @param state current state
   * @param category the category to filter by
   * @return Try containing list of topics in the specified category
   */
  def getTopicsByCategory(state: DiscussionForumManagerState, category: DiscussionCategory): Try[List[DiscussionTopic]] = {
    Try {
      state.topics.values.filter(_.category == category).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get topics by category: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get topics by category (safe functional version)
   * @param state current state
   * @param category the category to filter by
   * @return list of topics in the specified category or empty list if failed
   */
  def getTopicsByCategorySafe(state: DiscussionForumManagerState, category: DiscussionCategory): List[DiscussionTopic] = {
    getTopicsByCategory(state, category).getOrElse(List.empty)
  }
  
  /**
   * Get active topics (not closed)
   * @return list of active topics
   */
  def getActiveTopics: List[DiscussionTopic] = {
    items.values.filter(_.isActive).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get active topics (functional version with Try)
   * @param state current state
   * @return Try containing list of active topics
   */
  def getActiveTopics(state: DiscussionForumManagerState): Try[List[DiscussionTopic]] = {
    Try {
      state.topics.values.filter(_.isActive).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get active topics: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get active topics (safe functional version)
   * @param state current state
   * @return list of active topics or empty list if failed
   */
  def getActiveTopicsSafe(state: DiscussionForumManagerState): List[DiscussionTopic] = {
    getActiveTopics(state).getOrElse(List.empty)
  }
  
  /**
   * Get pinned topics
   * @return list of pinned topics
   */
  def getPinnedTopics: List[DiscussionTopic] = {
    items.values.filter(_.isPinned).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get pinned topics (functional version with Try)
   * @param state current state
   * @return Try containing list of pinned topics
   */
  def getPinnedTopics(state: DiscussionForumManagerState): Try[List[DiscussionTopic]] = {
    Try {
      state.topics.values.filter(_.isPinned).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get pinned topics: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get pinned topics (safe functional version)
   * @param state current state
   * @return list of pinned topics or empty list if failed
   */
  def getPinnedTopicsSafe(state: DiscussionForumManagerState): List[DiscussionTopic] = {
    getPinnedTopics(state).getOrElse(List.empty)
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
   * Get topics by author (functional version with Try)
   * @param state current state
   * @param authorId the author ID to filter by
   * @return Try containing list of topics by the specified author
   */
  def getTopicsByAuthor(state: DiscussionForumManagerState, authorId: String): Try[List[DiscussionTopic]] = {
    Try {
      state.topics.values.filter(_.authorId == authorId).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get topics by author $authorId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get topics by author (safe functional version)
   * @param state current state
   * @param authorId the author ID to filter by
   * @return list of topics by the specified author or empty list if failed
   */
  def getTopicsByAuthorSafe(state: DiscussionForumManagerState, authorId: String): List[DiscussionTopic] = {
    getTopicsByAuthor(state, authorId).getOrElse(List.empty)
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
   * Search topics by title or description (functional version with Try)
   * @param state current state
   * @param searchTerm the term to search for
   * @return Try containing list of matching topics
   */
  def searchTopics(state: DiscussionForumManagerState, searchTerm: String): Try[List[DiscussionTopic]] = {
    Try {
      val term = searchTerm.toLowerCase
      state.topics.values.filter { topic =>
        topic.title.toLowerCase.contains(term) || 
        topic.description.toLowerCase.contains(term)
      }.toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to search topics: ${e.getMessage}", e)
    }
  }
  
  /**
   * Search topics by title or description (safe functional version)
   * @param state current state
   * @param searchTerm the term to search for
   * @return list of matching topics or empty list if failed
   */
  def searchTopicsSafe(state: DiscussionForumManagerState, searchTerm: String): List[DiscussionTopic] = {
    searchTopics(state, searchTerm).getOrElse(List.empty)
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
   * Get most popular topics (functional version with Try)
   * @param state current state
   * @param limit maximum number of topics to return
   * @return Try containing list of popular topics
   */
  def getPopularTopics(state: DiscussionForumManagerState, limit: Int): Try[List[DiscussionTopic]] = {
    Try {
      state.topics.values.toList
        .sortBy(topic => topic.likes + topic.getReplyCount)
        .reverse
        .take(limit)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get popular topics: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get most popular topics (safe functional version)
   * @param state current state
   * @param limit maximum number of topics to return
   * @return list of popular topics or empty list if failed
   */
  def getPopularTopicsSafe(state: DiscussionForumManagerState, limit: Int): List[DiscussionTopic] = {
    getPopularTopics(state, limit).getOrElse(List.empty)
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
   * Get recent topics (functional version with Try)
   * @param state current state
   * @param days number of days to look back
   * @return Try containing list of recent topics
   */
  def getRecentTopics(state: DiscussionForumManagerState, days: Int): Try[List[DiscussionTopic]] = {
    Try {
      val cutoffDate = LocalDateTime.now().minusDays(days)
      state.topics.values.filter(_.timestamp.isAfter(cutoffDate)).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get recent topics: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get recent topics (safe functional version)
   * @param state current state
   * @param days number of days to look back
   * @return list of recent topics or empty list if failed
   */
  def getRecentTopicsSafe(state: DiscussionForumManagerState, days: Int): List[DiscussionTopic] = {
    getRecentTopics(state, days).getOrElse(List.empty)
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
   * Add reply to topic (functional version with Try)
   * @param state current state
   * @param topicId the topic ID
   * @param reply the reply to add
   * @return Try containing updated state and success flag
   */
  def addReply(state: DiscussionForumManagerState, topicId: String, reply: Reply): Try[(DiscussionForumManagerState, Boolean)] = {
    Try {
      state.topics.get(topicId) match {
        case Some(topic) if topic.isActive =>
          val updatedTopic = topic.copy()
          updatedTopic.addReply(reply)
          val updatedTopics = state.topics + (topicId -> updatedTopic)
          (DiscussionForumManagerState(updatedTopics), true)
        case _ => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to add reply to topic $topicId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Add reply to topic (safe functional version)
   * @param state current state
   * @param topicId the topic ID
   * @param reply the reply to add
   * @return updated state and success flag
   */
  def addReplySafe(state: DiscussionForumManagerState, topicId: String, reply: Reply): (DiscussionForumManagerState, Boolean) = {
    addReply(state, topicId, reply).getOrElse((state, false))
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
   * Add comment to topic (functional version with Try)
   * @param state current state
   * @param topicId the topic ID
   * @param comment the comment to add
   * @return Try containing updated state and success flag
   */
  def addComment(state: DiscussionForumManagerState, topicId: String, comment: Comment): Try[(DiscussionForumManagerState, Boolean)] = {
    Try {
      state.topics.get(topicId) match {
        case Some(topic) =>
          val updatedTopic = topic.copy()
          updatedTopic.addComment(comment)
          val updatedTopics = state.topics + (topicId -> updatedTopic)
          (DiscussionForumManagerState(updatedTopics), true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to add comment to topic $topicId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Add comment to topic (safe functional version)
   * @param state current state
   * @param topicId the topic ID
   * @param comment the comment to add
   * @return updated state and success flag
   */
  def addCommentSafe(state: DiscussionForumManagerState, topicId: String, comment: Comment): (DiscussionForumManagerState, Boolean) = {
    addComment(state, topicId, comment).getOrElse((state, false))
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
   * Add like to topic (functional version with Try)
   * @param state current state
   * @param topicId the topic ID
   * @return Try containing updated state and success flag
   */
  def addLike(state: DiscussionForumManagerState, topicId: String): Try[(DiscussionForumManagerState, Boolean)] = {
    Try {
      state.topics.get(topicId) match {
        case Some(topic) =>
          val updatedTopic = topic.copy()
          updatedTopic.addLike()
          val updatedTopics = state.topics + (topicId -> updatedTopic)
          (DiscussionForumManagerState(updatedTopics), true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to add like to topic $topicId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Add like to topic (safe functional version)
   * @param state current state
   * @param topicId the topic ID
   * @return updated state and success flag
   */
  def addLikeSafe(state: DiscussionForumManagerState, topicId: String): (DiscussionForumManagerState, Boolean) = {
    addLike(state, topicId).getOrElse((state, false))
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
   * Pin topic (functional version with Try)
   * @param state current state
   * @param topicId the topic ID
   * @return Try containing updated state and success flag
   */
  def pinTopic(state: DiscussionForumManagerState, topicId: String): Try[(DiscussionForumManagerState, Boolean)] = {
    Try {
      state.topics.get(topicId) match {
        case Some(topic) =>
          val updatedTopic = topic.copy()
          updatedTopic.pin()
          val updatedTopics = state.topics + (topicId -> updatedTopic)
          (DiscussionForumManagerState(updatedTopics), true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to pin topic $topicId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Pin topic (safe functional version)
   * @param state current state
   * @param topicId the topic ID
   * @return updated state and success flag
   */
  def pinTopicSafe(state: DiscussionForumManagerState, topicId: String): (DiscussionForumManagerState, Boolean) = {
    pinTopic(state, topicId).getOrElse((state, false))
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
   * Unpin topic (functional version with Try)
   * @param state current state
   * @param topicId the topic ID
   * @return Try containing updated state and success flag
   */
  def unpinTopic(state: DiscussionForumManagerState, topicId: String): Try[(DiscussionForumManagerState, Boolean)] = {
    Try {
      state.topics.get(topicId) match {
        case Some(topic) =>
          val updatedTopic = topic.copy()
          updatedTopic.unpin()
          val updatedTopics = state.topics + (topicId -> updatedTopic)
          (DiscussionForumManagerState(updatedTopics), true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to unpin topic $topicId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Unpin topic (safe functional version)
   * @param state current state
   * @param topicId the topic ID
   * @return updated state and success flag
   */
  def unpinTopicSafe(state: DiscussionForumManagerState, topicId: String): (DiscussionForumManagerState, Boolean) = {
    unpinTopic(state, topicId).getOrElse((state, false))
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
   * Close topic (functional version with Try)
   * @param state current state
   * @param topicId the topic ID
   * @return Try containing updated state and success flag
   */
  def closeTopic(state: DiscussionForumManagerState, topicId: String): Try[(DiscussionForumManagerState, Boolean)] = {
    Try {
      state.topics.get(topicId) match {
        case Some(topic) =>
          val updatedTopic = topic.copy()
          updatedTopic.close()
          val updatedTopics = state.topics + (topicId -> updatedTopic)
          (DiscussionForumManagerState(updatedTopics), true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to close topic $topicId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Close topic (safe functional version)
   * @param state current state
   * @param topicId the topic ID
   * @return updated state and success flag
   */
  def closeTopicSafe(state: DiscussionForumManagerState, topicId: String): (DiscussionForumManagerState, Boolean) = {
    closeTopic(state, topicId).getOrElse((state, false))
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
  
  /**
   * Moderate topic (functional version with Try)
   * @param state current state
   * @param topicId the topic ID
   * @param adminId the admin user ID
   * @return Try containing updated state and success flag
   */
  def moderateTopic(state: DiscussionForumManagerState, topicId: String, adminId: String): Try[(DiscussionForumManagerState, Boolean)] = {
    Try {
      state.topics.get(topicId) match {
        case Some(topic) =>
          val updatedTopic = topic.copy()
          updatedTopic.moderate(adminId)
          val updatedTopics = state.topics + (topicId -> updatedTopic)
          (DiscussionForumManagerState(updatedTopics), true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to moderate topic $topicId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Moderate topic (safe functional version)
   * @param state current state
   * @param topicId the topic ID
   * @param adminId the admin user ID
   * @return updated state and success flag
   */
  def moderateTopicSafe(state: DiscussionForumManagerState, topicId: String, adminId: String): (DiscussionForumManagerState, Boolean) = {
    moderateTopic(state, topicId, adminId).getOrElse((state, false))
  }
}
