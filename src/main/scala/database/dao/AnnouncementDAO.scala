package database.dao

import database.DatabaseConnection
import model._
import java.time.LocalDateTime
import java.sql.ResultSet
import java.util.UUID

/**
 * Data Access Object for Announcement operations
 */
class AnnouncementDAO {
  
  def insert(announcement: Announcement): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO announcements 
           (announcement_id, author_id, title, content, announcement_type, likes, created_at, updated_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
        announcement.announcementId, announcement.authorId, announcement.title, 
        announcement.content, announcement.announcementType.toString, announcement.likes,
        DatabaseConnection.formatDateTime(announcement.timestamp),
        DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error inserting announcement: ${e.getMessage}")
        false
    }
  }
  
  def findById(announcementId: String): Option[Announcement] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM announcements WHERE announcement_id = ?", announcementId
      )
      
      if (rs.next()) {
        val announcement = resultSetToAnnouncement(rs)
        rs.close()
        Some(announcement)
      } else {
        rs.close()
        None
      }
    } catch {
      case e: Exception =>
        println(s"Error finding announcement by ID: ${e.getMessage}")
        None
    }
  }
  
  def findAll(): List[Announcement] = {
    try {
      val rs = DatabaseConnection.executeQuery("SELECT * FROM announcements ORDER BY created_at DESC")
      val announcements = scala.collection.mutable.ListBuffer[Announcement]()
      
      while (rs.next()) {
        announcements += resultSetToAnnouncement(rs)
      }
      
      rs.close()
      announcements.toList
    } catch {
      case e: Exception =>
        println(s"Error finding all announcements: ${e.getMessage}")
        List.empty
    }
  }
  
  def search(searchTerm: String): List[Announcement] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        """SELECT * FROM announcements 
           WHERE title LIKE ? OR content LIKE ? 
           ORDER BY created_at DESC""",
        s"%$searchTerm%", s"%$searchTerm%"
      )
      
      val announcements = scala.collection.mutable.ListBuffer[Announcement]()
      while (rs.next()) {
        announcements += resultSetToAnnouncement(rs)
      }
      
      rs.close()
      announcements.toList
    } catch {
      case e: Exception =>
        println(s"Error searching announcements: ${e.getMessage}")
        List.empty
    }
  }
  
  def updateLikes(announcementId: String, likes: Int): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "UPDATE announcements SET likes = ?, updated_at = ? WHERE announcement_id = ?",
        likes, DatabaseConnection.formatDateTime(LocalDateTime.now()), announcementId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error updating announcement likes: ${e.getMessage}")
        false
    }
  }
  
  def moderate(announcementId: String, moderatorId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE announcements 
           SET is_moderated = 1, moderator_id = ?, updated_at = ? 
           WHERE announcement_id = ?""",
        moderatorId, DatabaseConnection.formatDateTime(LocalDateTime.now()), announcementId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error moderating announcement: ${e.getMessage}")
        false
    }
  }
  
  def delete(announcementId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM announcements WHERE announcement_id = ?", announcementId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error deleting announcement: ${e.getMessage}")
        false
    }
  }
  
  def count(): Int = {
    try {
      val rs = DatabaseConnection.executeQuery("SELECT COUNT(*) FROM announcements")
      val count = if (rs.next()) rs.getInt(1) else 0
      rs.close()
      count
    } catch {
      case e: Exception =>
        println(s"Error counting announcements: ${e.getMessage}")
        0
    }
  }
  
  private def resultSetToAnnouncement(rs: ResultSet): Announcement = {
    val announcementId = rs.getString("announcement_id")
    val authorId = rs.getString("author_id")
    val title = rs.getString("title")
    val content = rs.getString("content")
    val announcementTypeStr = rs.getString("announcement_type")
    val announcementType = try {
      AnnouncementType.valueOf(announcementTypeStr)
    } catch {
      case _: IllegalArgumentException =>
        println(s"Unknown announcement type: $announcementTypeStr, defaulting to GENERAL")
        AnnouncementType.GENERAL
    }
    val isModerated = rs.getBoolean("is_moderated")
    val moderatorId = Option(rs.getString("moderator_id"))
    val likes = rs.getInt("likes")
    val createdAt = DatabaseConnection.parseDateTime(rs.getString("created_at"))
    
    // Create announcement with database values
    val moderatorIdStr = rs.getString("moderator_id")
    val moderatedBy = if (moderatorIdStr != null) Some(moderatorIdStr) else None
    
    Announcement(
      announcementId = announcementId,
      authorId = authorId,
      title = title,
      content = content,
      announcementType = announcementType,
      timestamp = createdAt,
      likes = rs.getInt("likes"),
      isModerated = rs.getBoolean("is_moderated"),
      moderatedBy = moderatedBy
    )
  }
}
