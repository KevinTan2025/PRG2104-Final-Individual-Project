package database.dao

import database.DatabaseConnection
import model._
import java.time.LocalDateTime
import java.sql.ResultSet
import java.util.UUID
import scala.util.{Try, Success, Failure}
import scala.util.Using

/**
 * Data Access Object for User operations
 */
class UserDAO {
  
  def insert(user: User): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO users 
           (user_id, username, email, name, contact_info, is_admin, password_hash, created_at, updated_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        user.userId, user.username, user.email, user.name, user.contactInfo, 
        user.hasAdminPrivileges, user.passwordHash,
        DatabaseConnection.formatDateTime(user.registrationDate),
        DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error inserting user: ${e.getMessage}")
        false
    }
  }
  
  def findByUsername(username: String): Option[User] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM users WHERE LOWER(username) = LOWER(?)", username
      )
      
      if (rs.next()) {
        val user = resultSetToUser(rs)
        rs.close()
        Some(user)
      } else {
        rs.close()
        None
      }
    } catch {
      case e: Exception =>
        println(s"Error finding user by username: ${e.getMessage}")
        None
    }
  }
  
  def findByEmail(email: String): Option[User] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM users WHERE email = ?", email
      )
      
      if (rs.next()) {
        val user = resultSetToUser(rs)
        rs.close()
        Some(user)
      } else {
        rs.close()
        None
      }
    } catch {
      case e: Exception =>
        println(s"Error finding user by email: ${e.getMessage}")
        None
    }
  }
  
  def findById(userId: String): Option[User] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM users WHERE user_id = ?", userId
      )
      
      if (rs.next()) {
        val user = resultSetToUser(rs)
        rs.close()
        Some(user)
      } else {
        rs.close()
        None
      }
    } catch {
      case e: Exception =>
        println(s"Error finding user by ID: ${e.getMessage}")
        None
    }
  }
  
  def findAll(): List[User] = {
    try {
      val rs = DatabaseConnection.executeQuery("SELECT * FROM users ORDER BY created_at")
      val users = scala.collection.mutable.ListBuffer[User]()
      
      while (rs.next()) {
        users += resultSetToUser(rs)
      }
      
      rs.close()
      users.toList
    } catch {
      case e: Exception =>
        println(s"Error finding all users: ${e.getMessage}")
        List.empty
    }
  }
  
  def update(user: User): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE users 
           SET name = ?, contact_info = ?, password_hash = ?, updated_at = ? 
           WHERE user_id = ?""",
        user.name, user.contactInfo, user.passwordHash,
        DatabaseConnection.formatDateTime(LocalDateTime.now()),
        user.userId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error updating user: ${e.getMessage}")
        false
    }
  }
  
  def updatePassword(userId: String, newPasswordHash: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE users SET password_hash = ?, updated_at = ? WHERE user_id = ?""",
        newPasswordHash, DatabaseConnection.formatDateTime(LocalDateTime.now()), userId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error updating password: ${e.getMessage}")
        false
    }
  }
  
  def delete(userId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM users WHERE user_id = ?", userId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error deleting user: ${e.getMessage}")
        false
    }
  }
  
  def count(): Int = {
    try {
      val rs = DatabaseConnection.executeQuery("SELECT COUNT(*) FROM users")
      val count = if (rs.next()) rs.getInt(1) else 0
      rs.close()
      count
    } catch {
      case e: Exception =>
        println(s"Error counting users: ${e.getMessage}")
        0
    }
  }
  
  def countAdmins(): Int = {
    try {
      val rs = DatabaseConnection.executeQuery("SELECT COUNT(*) FROM users WHERE is_admin = 1")
      val count = if (rs.next()) rs.getInt(1) else 0
      rs.close()
      count
    } catch {
      case e: Exception =>
        println(s"Error counting admin users: ${e.getMessage}")
        0
    }
  }
  
  // 安全方法版本 - 使用 Try 类型进行错误处理
  
  /**
   * 安全插入用户
   */
  def insertSafe(user: User): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO users 
           (user_id, username, email, name, contact_info, is_admin, password_hash, created_at, updated_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        user.userId, user.username, user.email, user.name, user.contactInfo, 
        user.hasAdminPrivileges, user.passwordHash,
        DatabaseConnection.formatDateTime(user.registrationDate),
        DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
      rowsAffected > 0
    }
  }
  
  /**
   * 安全根据用户名查找用户
   */
  def findByUsernameSafe(username: String): Try[Option[User]] = {
    Try {
      Using.resource(DatabaseConnection.executeQuery(
        "SELECT * FROM users WHERE LOWER(username) = LOWER(?)", username
      )) { rs =>
        if (rs.next()) {
          Some(resultSetToUser(rs))
        } else {
          None
        }
      }
    }
  }
  
  /**
   * 安全根据邮箱查找用户
   */
  def findByEmailSafe(email: String): Try[Option[User]] = {
    Try {
      Using.resource(DatabaseConnection.executeQuery(
        "SELECT * FROM users WHERE email = ?", email
      )) { rs =>
        if (rs.next()) {
          Some(resultSetToUser(rs))
        } else {
          None
        }
      }
    }
  }
  
  /**
   * 安全根据ID查找用户
   */
  def findByIdSafe(userId: String): Try[Option[User]] = {
    Try {
      Using.resource(DatabaseConnection.executeQuery(
        "SELECT * FROM users WHERE user_id = ?", userId
      )) { rs =>
        if (rs.next()) {
          Some(resultSetToUser(rs))
        } else {
          None
        }
      }
    }
  }
  
  /**
   * 安全查找所有用户
   */
  def findAllSafe(): Try[List[User]] = {
    Try {
      Using.resource(DatabaseConnection.executeQuery("SELECT * FROM users ORDER BY created_at")) { rs =>
        val users = scala.collection.mutable.ListBuffer[User]()
        while (rs.next()) {
          users += resultSetToUser(rs)
        }
        users.toList
      }
    }
  }
  
  /**
   * 安全更新用户
   */
  def updateSafe(user: User): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE users 
           SET name = ?, contact_info = ?, password_hash = ?, updated_at = ? 
           WHERE user_id = ?""",
        user.name, user.contactInfo, user.passwordHash,
        DatabaseConnection.formatDateTime(LocalDateTime.now()),
        user.userId
      )
      rowsAffected > 0
    }
  }
  
  /**
   * 安全更新密码
   */
  def updatePasswordSafe(userId: String, newPasswordHash: String): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE users SET password_hash = ?, updated_at = ? WHERE user_id = ?""",
        newPasswordHash, DatabaseConnection.formatDateTime(LocalDateTime.now()), userId
      )
      rowsAffected > 0
    }
  }
  
  /**
   * 安全删除用户
   */
  def deleteSafe(userId: String): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM users WHERE user_id = ?", userId
      )
      rowsAffected > 0
    }
  }
  
  /**
   * 安全统计用户数量
   */
  def countSafe(): Try[Int] = {
    Try {
      Using.resource(DatabaseConnection.executeQuery("SELECT COUNT(*) FROM users")) { rs =>
        if (rs.next()) rs.getInt(1) else 0
      }
    }
  }
  
  /**
   * 安全统计管理员数量
   */
  def countAdminsSafe(): Try[Int] = {
    Try {
      Using.resource(DatabaseConnection.executeQuery("SELECT COUNT(*) FROM users WHERE is_admin = 1")) { rs =>
        if (rs.next()) rs.getInt(1) else 0
      }
    }
  }

  private def resultSetToUser(rs: ResultSet): User = {
    val userId = rs.getString("user_id")
    val username = rs.getString("username")
    val email = rs.getString("email")
    val name = rs.getString("name")
    val contactInfo = rs.getString("contact_info")
    val isAdmin = rs.getBoolean("is_admin")
    val passwordHash = rs.getString("password_hash")
    val createdAt = DatabaseConnection.parseDateTime(rs.getString("created_at"))
    
    val user = if (isAdmin) {
      new AdminUser(userId, username, email, name, contactInfo, passwordHash) {
        override val registrationDate: LocalDateTime = createdAt
      }
    } else {
      new CommunityMember(userId, username, email, name, contactInfo, passwordHash) {
        override val registrationDate: LocalDateTime = createdAt
      }
    }
    
    user
  }
}
