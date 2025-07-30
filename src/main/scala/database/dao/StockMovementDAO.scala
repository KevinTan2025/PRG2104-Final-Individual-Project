package database.dao

import database.DatabaseConnection
import model._
import java.time.LocalDateTime
import java.sql.ResultSet
import scala.util.{Try, Success, Failure, Using}
import scala.util.control.NonFatal

/**
 * Data Access Object for StockMovement operations
 */
class StockMovementDAO {
  
  def insert(movement: StockMovement): Boolean = {
    insertSafe(movement).getOrElse(false)
  }
  
  def insertSafe(movement: StockMovement): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO stock_movements 
           (movement_id, stock_id, action_type, quantity, previous_quantity, 
            new_quantity, user_id, notes, timestamp) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        movement.movementId, movement.stockId, movement.actionType.toString,
        movement.quantity, movement.previousQuantity, movement.newQuantity,
        movement.userId, movement.notes, DatabaseConnection.formatDateTime(movement.timestamp)
      )
      rowsAffected > 0
    }.recover {
      case NonFatal(e) =>
        println(s"Error inserting stock movement: ${e.getMessage}")
        false
    }
  }
  
  def findById(movementId: String): Option[StockMovement] = {
    findByIdSafe(movementId).getOrElse(None)
  }
  
  def findByIdSafe(movementId: String): Try[Option[StockMovement]] = {
    Try {
      Using(DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE movement_id = ?", movementId
      )) { rs =>
        if (rs.next()) {
          Some(resultSetToStockMovement(rs))
        } else {
          None
        }
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding stock movement by ID: ${e.getMessage}")
        None
    }
  }
  
  def findByStockId(stockId: String): List[StockMovement] = {
    findByStockIdSafe(stockId).getOrElse(List.empty)
  }
  
  def findByStockIdSafe(stockId: String): Try[List[StockMovement]] = {
    Try {
      Using(DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE stock_id = ? ORDER BY timestamp DESC",
        stockId
      )) { rs =>
        Iterator.continually(rs)
          .takeWhile(_.next())
          .map(resultSetToStockMovement)
          .toList
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding stock movements by stock ID: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByUserId(userId: String): List[StockMovement] = {
    findByUserIdSafe(userId).getOrElse(List.empty)
  }
  
  def findByUserIdSafe(userId: String): Try[List[StockMovement]] = {
    Try {
      Using(DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE user_id = ? ORDER BY timestamp DESC",
        userId
      )) { rs =>
        Iterator.continually(rs)
          .takeWhile(_.next())
          .map(resultSetToStockMovement)
          .toList
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding stock movements by user ID: ${e.getMessage}")
        List.empty
    }
  }
  
  def findAll(): List[StockMovement] = {
    findAllSafe().getOrElse(List.empty)
  }
  
  def findAllSafe(): Try[List[StockMovement]] = {
    Try {
      Using(DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements ORDER BY timestamp DESC"
      )) { rs =>
        Iterator.continually(rs)
          .takeWhile(_.next())
          .map(resultSetToStockMovement)
          .toList
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding all stock movements: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List[StockMovement] = {
    findByDateRangeSafe(startDate, endDate).getOrElse(List.empty)
  }
  
  def findByDateRangeSafe(startDate: LocalDateTime, endDate: LocalDateTime): Try[List[StockMovement]] = {
    Try {
      val startDateStr = DatabaseConnection.formatDateTime(startDate)
      val endDateStr = DatabaseConnection.formatDateTime(endDate)
      Using(DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC",
        startDateStr, endDateStr
      )) { rs =>
        Iterator.continually(rs)
          .takeWhile(_.next())
          .map(resultSetToStockMovement)
          .toList
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding stock movements by date range: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByActionType(actionType: StockActionType): List[StockMovement] = {
    findByActionTypeSafe(actionType).getOrElse(List.empty)
  }
  
  def findByActionTypeSafe(actionType: StockActionType): Try[List[StockMovement]] = {
    Try {
      Using(DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE action_type = ? ORDER BY timestamp DESC",
        actionType.toString
      )) { rs =>
        Iterator.continually(rs)
          .takeWhile(_.next())
          .map(resultSetToStockMovement)
          .toList
      }.get
    }.recover {
      case NonFatal(e) =>
        println(s"Error finding stock movements by action type: ${e.getMessage}")
        List.empty
    }
  }
  
  def delete(movementId: String): Boolean = {
    deleteSafe(movementId).getOrElse(false)
  }
  
  def deleteSafe(movementId: String): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM stock_movements WHERE movement_id = ?", movementId
      )
      rowsAffected > 0
    }.recover {
      case NonFatal(e) =>
        println(s"Error deleting stock movement: ${e.getMessage}")
        false
    }
  }
  
  def deleteByStockId(stockId: String): Boolean = {
    deleteByStockIdSafe(stockId).getOrElse(false)
  }
  
  def deleteByStockIdSafe(stockId: String): Try[Boolean] = {
    Try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM stock_movements WHERE stock_id = ?", stockId
      )
      rowsAffected >= 0 // Allow 0 if no movements exist
    }.recover {
      case NonFatal(e) =>
        println(s"Error deleting stock movements by stock ID: ${e.getMessage}")
        false
    }
  }
  
  def getStatistics: Map[String, Int] = {
    getStatisticsSafe().getOrElse(Map.empty)
  }
  
  def getStatisticsSafe(): Try[Map[String, Int]] = {
    Try {
      // Total movements
      val totalMovements = Using(DatabaseConnection.executeQuery("SELECT COUNT(*) FROM stock_movements")) { rs =>
        if (rs.next()) rs.getInt(1) else 0
      }.get
      
      // Movements by type
      val typeStats = StockActionType.values.map { actionType =>
        val count = Using(DatabaseConnection.executeQuery(
          "SELECT COUNT(*) FROM stock_movements WHERE action_type = ?",
          actionType.toString
        )) { rs =>
          if (rs.next()) rs.getInt(1) else 0
        }.get
        actionType.toString -> count
      }.toMap
      
      Map("totalMovements" -> totalMovements) ++ typeStats
    }.recover {
      case NonFatal(e) =>
        println(s"Error getting stock movement statistics: ${e.getMessage}")
        Map.empty
    }
  }
  
  private def resultSetToStockMovement(rs: ResultSet): StockMovement = {
    val movementId = rs.getString("movement_id")
    val stockId = rs.getString("stock_id")
    val actionTypeStr = rs.getString("action_type")
    val actionType = try {
      StockActionType.valueOf(actionTypeStr)
    } catch {
      case _: IllegalArgumentException =>
        println(s"Unknown stock action type: $actionTypeStr, defaulting to ADJUSTMENT")
        StockActionType.ADJUSTMENT
    }
    val quantity = rs.getDouble("quantity")
    val previousQuantity = rs.getDouble("previous_quantity")
    val newQuantity = rs.getDouble("new_quantity")
    val userId = rs.getString("user_id")
    val notes = rs.getString("notes")
    val timestamp = DatabaseConnection.parseDateTime(rs.getString("timestamp"))
    
    StockMovement(
      movementId, stockId, actionType, quantity, previousQuantity,
      newQuantity, userId, notes, timestamp
    )
  }
}
