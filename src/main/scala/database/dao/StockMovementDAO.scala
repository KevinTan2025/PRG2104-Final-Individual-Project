package database.dao

import database.DatabaseConnection
import model._
import java.time.LocalDateTime
import java.sql.ResultSet

/**
 * Data Access Object for StockMovement operations
 */
class StockMovementDAO {
  
  def insert(movement: StockMovement): Boolean = {
    try {
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
    } catch {
      case e: Exception =>
        println(s"Error inserting stock movement: ${e.getMessage}")
        false
    }
  }
  
  def findById(movementId: String): Option[StockMovement] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE movement_id = ?", movementId
      )
      
      if (rs.next()) {
        val movement = resultSetToStockMovement(rs)
        rs.close()
        Some(movement)
      } else {
        rs.close()
        None
      }
    } catch {
      case e: Exception =>
        println(s"Error finding stock movement by ID: ${e.getMessage}")
        None
    }
  }
  
  def findByStockId(stockId: String): List[StockMovement] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE stock_id = ? ORDER BY timestamp DESC",
        stockId
      )
      
      val movements = scala.collection.mutable.ListBuffer[StockMovement]()
      while (rs.next()) {
        movements += resultSetToStockMovement(rs)
      }
      
      rs.close()
      movements.toList
    } catch {
      case e: Exception =>
        println(s"Error finding stock movements by stock ID: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByUserId(userId: String): List[StockMovement] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE user_id = ? ORDER BY timestamp DESC",
        userId
      )
      
      val movements = scala.collection.mutable.ListBuffer[StockMovement]()
      while (rs.next()) {
        movements += resultSetToStockMovement(rs)
      }
      
      rs.close()
      movements.toList
    } catch {
      case e: Exception =>
        println(s"Error finding stock movements by user ID: ${e.getMessage}")
        List.empty
    }
  }
  
  def findAll(): List[StockMovement] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements ORDER BY timestamp DESC"
      )
      
      val movements = scala.collection.mutable.ListBuffer[StockMovement]()
      while (rs.next()) {
        movements += resultSetToStockMovement(rs)
      }
      
      rs.close()
      movements.toList
    } catch {
      case e: Exception =>
        println(s"Error finding all stock movements: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List[StockMovement] = {
    try {
      val startDateStr = DatabaseConnection.formatDateTime(startDate)
      val endDateStr = DatabaseConnection.formatDateTime(endDate)
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC",
        startDateStr, endDateStr
      )
      
      val movements = scala.collection.mutable.ListBuffer[StockMovement]()
      while (rs.next()) {
        movements += resultSetToStockMovement(rs)
      }
      
      rs.close()
      movements.toList
    } catch {
      case e: Exception =>
        println(s"Error finding stock movements by date range: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByActionType(actionType: StockActionType): List[StockMovement] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM stock_movements WHERE action_type = ? ORDER BY timestamp DESC",
        actionType.toString
      )
      
      val movements = scala.collection.mutable.ListBuffer[StockMovement]()
      while (rs.next()) {
        movements += resultSetToStockMovement(rs)
      }
      
      rs.close()
      movements.toList
    } catch {
      case e: Exception =>
        println(s"Error finding stock movements by action type: ${e.getMessage}")
        List.empty
    }
  }
  
  def delete(movementId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM stock_movements WHERE movement_id = ?", movementId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error deleting stock movement: ${e.getMessage}")
        false
    }
  }
  
  def deleteByStockId(stockId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM stock_movements WHERE stock_id = ?", stockId
      )
      rowsAffected >= 0 // Allow 0 if no movements exist
    } catch {
      case e: Exception =>
        println(s"Error deleting stock movements by stock ID: ${e.getMessage}")
        false
    }
  }
  
  def getStatistics: Map[String, Int] = {
    try {
      val stats = scala.collection.mutable.Map[String, Int]()
      
      // Total movements
      val totalRs = DatabaseConnection.executeQuery("SELECT COUNT(*) FROM stock_movements")
      stats("totalMovements") = if (totalRs.next()) totalRs.getInt(1) else 0
      totalRs.close()
      
      // Movements by type
      StockActionType.values.foreach { actionType =>
        val typeRs = DatabaseConnection.executeQuery(
          "SELECT COUNT(*) FROM stock_movements WHERE action_type = ?",
          actionType.toString
        )
        stats(actionType.toString) = if (typeRs.next()) typeRs.getInt(1) else 0
        typeRs.close()
      }
      
      stats.toMap
    } catch {
      case e: Exception =>
        println(s"Error getting stock movement statistics: ${e.getMessage}")
        Map.empty
    }
  }
  
  private def resultSetToStockMovement(rs: ResultSet): StockMovement = {
    val movementId = rs.getString("movement_id")
    val stockId = rs.getString("stock_id")
    val actionType = StockActionType.valueOf(rs.getString("action_type"))
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
