package database.dao

import database.DatabaseConnection
import model.Reply
import java.sql.{PreparedStatement, ResultSet}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
// Removed mutable ListBuffer import - using functional approach

/**
 * Data Access Object for Reply operations
 */
class DiscussionReplyDAO {
  
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  
  def insert(reply: Reply): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO discussion_replies 
           (reply_id, topic_id, author_id, content, likes, created_at) 
           VALUES (?, ?, ?, ?, ?, ?)""",
        reply.replyId, reply.topicId, reply.authorId, reply.content, 
        reply.likes, DatabaseConnection.formatDateTime(reply.timestamp)
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error inserting discussion reply: ${e.getMessage}")
        false
    }
  }
  
  def update(reply: Reply): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE discussion_replies 
           SET content = ?, likes = ?
           WHERE reply_id = ?""",
        reply.content, reply.likes, reply.replyId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error updating discussion reply: ${e.getMessage}")
        false
    }
  }
  
  def delete(id: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM discussion_replies WHERE reply_id = ?", id
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error deleting discussion reply: ${e.getMessage}")
        false
    }
  }
  
  def findById(id: String): Option[Reply] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM discussion_replies WHERE reply_id = ?", id
      )
      
      if (rs.next()) {
        val reply = mapResultSetToReply(rs)
        rs.close()
        Some(reply)
      } else {
        rs.close()
        None
      }
    } catch {
      case e: Exception =>
        println(s"Error finding discussion reply by id: ${e.getMessage}")
        None
    }
  }
  
  def findAll(): List[Reply] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM discussion_replies ORDER BY created_at ASC"
      )
      // Use functional approach to build list
      val replies = Iterator.continually(rs)
        .takeWhile(_.next())
        .map(mapResultSetToReply)
        .toList
      
      rs.close()
      replies
    } catch {
      case e: Exception =>
        println(s"Error finding all discussion replies: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByTopicId(topicId: String): List[Reply] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM discussion_replies WHERE topic_id = ? ORDER BY created_at ASC",
        topicId
      )
      // Use functional approach to build list
      val replies = Iterator.continually(rs)
        .takeWhile(_.next())
        .map(mapResultSetToReply)
        .toList
      
      rs.close()
      replies
    } catch {
      case e: Exception =>
        println(s"Error finding discussion replies by topic: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByAuthor(authorId: String): List[Reply] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM discussion_replies WHERE author_id = ? ORDER BY created_at DESC",
        authorId
      )
      // Use functional approach to build list
      val replies = Iterator.continually(rs)
        .takeWhile(_.next())
        .map(mapResultSetToReply)
        .toList
      
      rs.close()
      replies
    } catch {
      case e: Exception =>
        println(s"Error finding discussion replies by author: ${e.getMessage}")
        List.empty
    }
  }
  
  def updateLikes(replyId: String, likes: Int): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "UPDATE discussion_replies SET likes = ? WHERE reply_id = ?",
        likes, replyId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error updating reply likes: ${e.getMessage}")
        false
    }
  }
  
  def incrementLikes(replyId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "UPDATE discussion_replies SET likes = likes + 1 WHERE reply_id = ?",
        replyId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error incrementing reply likes: ${e.getMessage}")
        false
    }
  }
  
  def replyCountForTopic(topicId: String): Int = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT COUNT(*) as count FROM discussion_replies WHERE topic_id = ?",
        topicId
      )
      
      if (rs.next()) {
        val count = rs.getInt("count")
        rs.close()
        count
      } else {
        rs.close()
        0
      }
    } catch {
      case e: Exception =>
        println(s"Error getting reply count: ${e.getMessage}")
        0
    }
  }
  
  private def mapResultSetToReply(rs: ResultSet): Reply = {
    Reply(
      replyId = rs.getString("reply_id"),
      topicId = rs.getString("topic_id"),
      authorId = rs.getString("author_id"),
      content = rs.getString("content"),
      timestamp = DatabaseConnection.parseDateTime(rs.getString("created_at")),
      likes = rs.getInt("likes")
    )
  }
}
