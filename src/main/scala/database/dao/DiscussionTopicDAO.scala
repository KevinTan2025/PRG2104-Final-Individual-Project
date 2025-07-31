package database.dao

import database.DatabaseConnection
import model.{DiscussionTopic, DiscussionCategory, Reply}
import java.sql.{PreparedStatement, ResultSet}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
// Using functional approach instead of mutable collections

/**
 * Data Access Object for DiscussionTopic operations
 */
class DiscussionTopicDAO {
  
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  
  def insert(topic: DiscussionTopic): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO discussion_topics 
           (topic_id, author_id, title, description, category, likes, created_at, updated_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
        topic.topicId, topic.authorId, topic.title, topic.description, 
        topic.category.toString, topic.likes,
        DatabaseConnection.formatDateTime(topic.timestamp),
        DatabaseConnection.formatDateTime(topic.timestamp)
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error inserting discussion topic: ${e.getMessage}")
        false
    }
  }
  
  def update(topic: DiscussionTopic): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE discussion_topics 
           SET title = ?, description = ?, category = ?, likes = ?, updated_at = ?
           WHERE topic_id = ?""",
        topic.title, topic.description, topic.category.toString, topic.likes,
        DatabaseConnection.formatDateTime(LocalDateTime.now()), topic.topicId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error updating discussion topic: ${e.getMessage}")
        false
    }
  }
  
  def delete(id: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM discussion_topics WHERE topic_id = ?", id
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error deleting discussion topic: ${e.getMessage}")
        false
    }
  }
  
  def findById(id: String): Option[DiscussionTopic] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM discussion_topics WHERE topic_id = ?", id
      )
      
      if (rs.next()) {
        val topic = mapResultSetToTopic(rs)
        rs.close()
        Some(topic)
      } else {
        rs.close()
        None
      }
    } catch {
      case e: Exception =>
        println(s"Error finding discussion topic by id: ${e.getMessage}")
        None
    }
  }
  
  def findAll(): List[DiscussionTopic] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM discussion_topics ORDER BY created_at DESC"
      )
      val topics = Iterator.continually(rs)
        .takeWhile(_.next())
        .map(mapResultSetToTopic)
        .toList
      
      rs.close()
      topics
    } catch {
      case e: Exception =>
        println(s"Error finding all discussion topics: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByCategory(category: DiscussionCategory): List[DiscussionTopic] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM discussion_topics WHERE category = ? ORDER BY created_at DESC",
        category.toString
      )
      val topics = Iterator.continually(rs)
        .takeWhile(_.next())
        .map(mapResultSetToTopic)
        .toList
      
      rs.close()
      topics
    } catch {
      case e: Exception =>
        println(s"Error finding discussion topics by category: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByAuthor(authorId: String): List[DiscussionTopic] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM discussion_topics WHERE author_id = ? ORDER BY created_at DESC",
        authorId
      )
      val topics = Iterator.continually(rs)
        .takeWhile(_.next())
        .map(mapResultSetToTopic)
        .toList
      
      rs.close()
      topics
    } catch {
      case e: Exception =>
        println(s"Error finding discussion topics by author: ${e.getMessage}")
        List.empty
    }
  }
  
  def searchByTitleOrDescription(searchTerm: String): List[DiscussionTopic] = {
    try {
      val searchPattern = s"%$searchTerm%"
      val rs = DatabaseConnection.executeQuery(
        """SELECT * FROM discussion_topics 
           WHERE title LIKE ? OR description LIKE ? 
           ORDER BY created_at DESC""",
        searchPattern, searchPattern
      )
      val topics = Iterator.continually(rs)
        .takeWhile(_.next())
        .map(mapResultSetToTopic)
        .toList
      
      rs.close()
      topics
    } catch {
      case e: Exception =>
        println(s"Error searching discussion topics: ${e.getMessage}")
        List.empty
    }
  }
  
  def updateLikes(topicId: String, likes: Int): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "UPDATE discussion_topics SET likes = ?, updated_at = ? WHERE topic_id = ?",
        likes, DatabaseConnection.formatDateTime(LocalDateTime.now()), topicId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error updating topic likes: ${e.getMessage}")
        false
    }
  }
  
  def incrementLikes(topicId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE discussion_topics 
           SET likes = likes + 1, updated_at = ? 
           WHERE topic_id = ?""",
        DatabaseConnection.formatDateTime(LocalDateTime.now()), topicId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error incrementing topic likes: ${e.getMessage}")
        false
    }
  }
  
  private def mapResultSetToTopic(rs: ResultSet): DiscussionTopic = {
    val categoryStr = rs.getString("category")
    val category = try {
      DiscussionCategory.valueOf(categoryStr)
    } catch {
      case _: IllegalArgumentException =>
        println(s"Unknown discussion category: $categoryStr, defaulting to GENERAL")
        DiscussionCategory.GENERAL
    }
    
    // Load replies for this topic
    val replies = loadRepliesForTopic(rs.getString("topic_id"))
    val likes = rs.getInt("likes")
    
    // Create topic with all properties from database
    DiscussionTopic(
      topicId = rs.getString("topic_id"),
      authorId = rs.getString("author_id"),
      title = rs.getString("title"),
      description = rs.getString("description"),
      category = category,
      timestamp = DatabaseConnection.parseDateTime(rs.getString("created_at")),
      replies = replies,
      likes = likes
    )
  }
  
  private def loadRepliesForTopic(topicId: String): List[Reply] = {
    val replyDAO = new DiscussionReplyDAO()
    replyDAO.findByTopicId(topicId)
  }
}
