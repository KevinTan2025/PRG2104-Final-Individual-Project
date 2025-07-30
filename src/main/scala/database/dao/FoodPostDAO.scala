package database.dao

import database.DatabaseConnection
import model._
import java.time.LocalDateTime
import java.sql.ResultSet
import scala.util.{Try, Success, Failure, Using}
import scala.util.control.NonFatal

/**
 * Data Access Object for FoodPost operations
 */
class FoodPostDAO {
  
  def insert(foodPost: FoodPost): Boolean = {
    insertSafe(foodPost).getOrElse(false)
  }
  
  def insertSafe(foodPost: FoodPost): Try[Boolean] = {
    Try {
      val expiryDateStr = foodPost.expiryDate.map(DatabaseConnection.formatDateTime)
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO food_posts 
           (post_id, author_id, title, description, post_type, quantity, location, expiry_date, status, likes, created_at, updated_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        foodPost.postId, foodPost.authorId, foodPost.title, foodPost.description,
        foodPost.postType.toString, foodPost.quantity, foodPost.location,
        expiryDateStr.orNull, foodPost.status.toString, foodPost.likes,
        DatabaseConnection.formatDateTime(foodPost.timestamp),
        DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
      rowsAffected > 0
    }.recover {
      case NonFatal(e) =>
        println(s"Error inserting food post: ${e.getMessage}")
        false
    }
  }
  
  def findById(postId: String): Option[FoodPost] = {
    findByIdSafe(postId).getOrElse(None)
  }
  
  def findByIdSafe(postId: String): Try[Option[FoodPost]] = {
    Try {
      Using(DatabaseConnection.executeQuery(
        "SELECT * FROM food_posts WHERE post_id = ?", postId
      )) { rs =>
        if (rs.next()) {
          Some(resultSetToFoodPost(rs))
        } else {
          None
        }
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding food post by ID: ${e.getMessage}")
        None
    }
  }
  
  def findAll(): List[FoodPost] = {
    findAllSafe().getOrElse(List.empty)
  }
  
  def findAllSafe(): Try[List[FoodPost]] = {
    Try {
      Using(DatabaseConnection.executeQuery("SELECT * FROM food_posts ORDER BY created_at DESC")) { rs =>
        Iterator.continually(rs)
          .takeWhile(_.next())
          .map(resultSetToFoodPost)
          .toList
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding all food posts: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByType(postType: FoodPostType): List[FoodPost] = {
    findByTypeSafe(postType).getOrElse(List.empty)
  }
  
  def findByTypeSafe(postType: FoodPostType): Try[List[FoodPost]] = {
    Try {
      Using(DatabaseConnection.executeQuery(
        "SELECT * FROM food_posts WHERE post_type = ? ORDER BY created_at DESC",
        postType.toString
      )) { rs =>
        Iterator.continually(rs)
          .takeWhile(_.next())
          .map(resultSetToFoodPost)
          .toList
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding food posts by type: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByStatus(status: FoodPostStatus): List[FoodPost] = {
    findByStatusSafe(status).getOrElse(List.empty)
  }
  
  def findByStatusSafe(status: FoodPostStatus): Try[List[FoodPost]] = {
    Try {
      Using(DatabaseConnection.executeQuery(
        "SELECT * FROM food_posts WHERE status = ? ORDER BY created_at DESC",
        status.toString
      )) { rs =>
        Iterator.continually(rs)
          .takeWhile(_.next())
          .map(resultSetToFoodPost)
          .toList
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding food posts by status: ${e.getMessage}")
        List.empty
    }
  }
  
  def search(searchTerm: String): List[FoodPost] = {
    searchSafe(searchTerm).getOrElse(List.empty)
  }
  
  def searchSafe(searchTerm: String): Try[List[FoodPost]] = {
    Try {
      Using(DatabaseConnection.executeQuery(
        """SELECT * FROM food_posts 
           WHERE title LIKE ? OR description LIKE ? OR location LIKE ?
           ORDER BY created_at DESC""",
        s"%$searchTerm%", s"%$searchTerm%", s"%$searchTerm%"
      )) { rs =>
        Iterator.continually(rs)
          .takeWhile(_.next())
          .map(resultSetToFoodPost)
          .toList
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error searching food posts: ${e.getMessage}")
        List.empty
    }
  }
  
  def updateStatus(postId: String, status: FoodPostStatus, acceptedBy: Option[String] = None): Boolean = {
    updateStatusSafe(postId, status, acceptedBy).getOrElse(false)
  }
  
  def updateStatusSafe(postId: String, status: FoodPostStatus, acceptedBy: Option[String] = None): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE food_posts 
           SET status = ?, accepted_by = ?, updated_at = ? 
           WHERE post_id = ?""",
        status.toString, acceptedBy.orNull,
        DatabaseConnection.formatDateTime(LocalDateTime.now()), postId
      )
      rowsAffected > 0
    }.recover {
      case NonFatal(e) =>
        println(s"Error updating food post status: ${e.getMessage}")
        false
    }
  }
  
  def updateLikes(postId: String, likes: Int): Boolean = {
    updateLikesSafe(postId, likes).getOrElse(false)
  }
  
  def updateLikesSafe(postId: String, likes: Int): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "UPDATE food_posts SET likes = ?, updated_at = ? WHERE post_id = ?",
        likes, DatabaseConnection.formatDateTime(LocalDateTime.now()), postId
      )
      rowsAffected > 0
    }.recover {
      case NonFatal(e) =>
        println(s"Error updating food post likes: ${e.getMessage}")
        false
    }
  }
  
  def moderate(postId: String, moderatorId: String): Boolean = {
    moderateSafe(postId, moderatorId).getOrElse(false)
  }
  
  def moderateSafe(postId: String, moderatorId: String): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE food_posts 
           SET is_moderated = 1, moderator_id = ?, updated_at = ? 
           WHERE post_id = ?""",
        moderatorId, DatabaseConnection.formatDateTime(LocalDateTime.now()), postId
      )
      rowsAffected > 0
    }.recover {
      case NonFatal(e) =>
        println(s"Error moderating food post: ${e.getMessage}")
        false
    }
  }
  
  def delete(postId: String): Boolean = {
    deleteSafe(postId).getOrElse(false)
  }
  
  def deleteSafe(postId: String): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM food_posts WHERE post_id = ?", postId
      )
      rowsAffected > 0
    }.recover {
      case NonFatal(e) =>
        println(s"Error deleting food post: ${e.getMessage}")
        false
    }
  }
  
  def getStatistics: (Int, Int, Int) = {
    getStatisticsSafe().getOrElse((0, 0, 0))
  }
  
  def getStatisticsSafe(): Try[(Int, Int, Int)] = {
    Try {
      val total = Using(DatabaseConnection.executeQuery("SELECT COUNT(*) FROM food_posts")) { rs =>
        if (rs.next()) rs.getInt(1) else 0
      }.get
      
      val active = Using(DatabaseConnection.executeQuery("SELECT COUNT(*) FROM food_posts WHERE status = 'PENDING'")) { rs =>
        if (rs.next()) rs.getInt(1) else 0
      }.get
      
      val completed = Using(DatabaseConnection.executeQuery("SELECT COUNT(*) FROM food_posts WHERE status = 'COMPLETED'")) { rs =>
        if (rs.next()) rs.getInt(1) else 0
      }.get
      
      (total, active, completed)
    }.recover {
      case NonFatal(e) =>
        println(s"Error getting food post statistics: ${e.getMessage}")
        (0, 0, 0)
    }
  }
  
  private def resultSetToFoodPost(rs: ResultSet): FoodPost = {
    val postId = rs.getString("post_id")
    val authorId = rs.getString("author_id")
    val title = rs.getString("title")
    val description = rs.getString("description")
    val postType = FoodPostType.valueOf(rs.getString("post_type"))
    val quantity = rs.getString("quantity")
    val location = rs.getString("location")
    val expiryDateStr = rs.getString("expiry_date")
    val expiryDate = Option(expiryDateStr).filter(_ != null).map(DatabaseConnection.parseDateTime)
    val status = FoodPostStatus.valueOf(rs.getString("status"))
    val acceptedBy = Option(rs.getString("accepted_by"))
    val isModerated = rs.getBoolean("is_moderated")
    val moderatorIdStr = rs.getString("moderator_id")
    val likes = rs.getInt("likes")
    val createdAt = DatabaseConnection.parseDateTime(rs.getString("created_at"))
    
    val foodPost = FoodPost(postId, authorId, title, description, postType, quantity, location, expiryDate, createdAt)
    
    // Set additional properties from database
    foodPost.status = status
    foodPost.acceptedBy = acceptedBy
    foodPost.likes = likes
    foodPost.isModerated = isModerated
    if (moderatorIdStr != null) {
      foodPost.moderatedBy = Some(moderatorIdStr)
    }
    
    foodPost
  }
}
