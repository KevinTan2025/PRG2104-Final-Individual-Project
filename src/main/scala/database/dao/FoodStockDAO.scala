package database.dao

import database.DatabaseConnection
import model._
import java.time.LocalDateTime
import java.sql.ResultSet

/**
 * Data Access Object for FoodStock operations
 */
class FoodStockDAO {
  
  def insert(foodStock: FoodStock): Boolean = {
    try {
      val expiryDateStr = foodStock.expiryDate.map(DatabaseConnection.formatDateTime)
      val lastModifiedByStr = foodStock.lastModifiedBy.orNull
      
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO food_stocks 
           (stock_id, food_name, category, current_quantity, unit, minimum_threshold, 
            expiry_date, is_packaged, location, last_modified_by, last_modified_date, created_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        foodStock.stockId, foodStock.foodName, foodStock.category.toString,
        foodStock.currentQuantity, foodStock.unit, foodStock.minimumThreshold,
        expiryDateStr.orNull, foodStock.isPackaged, foodStock.location,
        lastModifiedByStr, DatabaseConnection.formatDateTime(foodStock.lastModifiedDate),
        DatabaseConnection.formatDateTime(foodStock.createdAt)
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error inserting food stock: ${e.getMessage}")
        false
    }
  }
  
  def findById(stockId: String): Option[FoodStock] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM food_stocks WHERE stock_id = ?", stockId
      )
      
      if (rs.next()) {
        val foodStock = resultSetToFoodStock(rs)
        rs.close()
        Some(foodStock)
      } else {
        rs.close()
        None
      }
    } catch {
      case e: Exception =>
        println(s"Error finding food stock by ID: ${e.getMessage}")
        None
    }
  }
  
  def findAll(): List[FoodStock] = {
    try {
      val rs = DatabaseConnection.executeQuery("SELECT * FROM food_stocks ORDER BY food_name ASC")
      val foodStocks = scala.collection.mutable.ListBuffer[FoodStock]()
      
      while (rs.next()) {
        foodStocks += resultSetToFoodStock(rs)
      }
      
      rs.close()
      foodStocks.toList
    } catch {
      case e: Exception =>
        println(s"Error finding all food stocks: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByCategory(category: FoodCategory): List[FoodStock] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM food_stocks WHERE category = ? ORDER BY food_name ASC",
        category.toString
      )
      
      val foodStocks = scala.collection.mutable.ListBuffer[FoodStock]()
      while (rs.next()) {
        foodStocks += resultSetToFoodStock(rs)
      }
      
      rs.close()
      foodStocks.toList
    } catch {
      case e: Exception =>
        println(s"Error finding food stocks by category: ${e.getMessage}")
        List.empty
    }
  }
  
  def findByLocation(location: String): List[FoodStock] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM food_stocks WHERE location LIKE ? ORDER BY food_name ASC",
        s"%$location%"
      )
      
      val foodStocks = scala.collection.mutable.ListBuffer[FoodStock]()
      while (rs.next()) {
        foodStocks += resultSetToFoodStock(rs)
      }
      
      rs.close()
      foodStocks.toList
    } catch {
      case e: Exception =>
        println(s"Error finding food stocks by location: ${e.getMessage}")
        List.empty
    }
  }
  
  def search(searchTerm: String): List[FoodStock] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        """SELECT * FROM food_stocks 
           WHERE food_name LIKE ? OR category LIKE ? OR location LIKE ?
           ORDER BY food_name ASC""",
        s"%$searchTerm%", s"%$searchTerm%", s"%$searchTerm%"
      )
      
      val foodStocks = scala.collection.mutable.ListBuffer[FoodStock]()
      while (rs.next()) {
        foodStocks += resultSetToFoodStock(rs)
      }
      
      rs.close()
      foodStocks.toList
    } catch {
      case e: Exception =>
        println(s"Error searching food stocks: ${e.getMessage}")
        List.empty
    }
  }
  
  def update(foodStock: FoodStock): Boolean = {
    try {
      val expiryDateStr = foodStock.expiryDate.map(DatabaseConnection.formatDateTime)
      val lastModifiedByStr = foodStock.lastModifiedBy.orNull
      
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE food_stocks 
           SET food_name = ?, category = ?, current_quantity = ?, unit = ?, 
               minimum_threshold = ?, expiry_date = ?, is_packaged = ?, 
               location = ?, last_modified_by = ?, last_modified_date = ?
           WHERE stock_id = ?""",
        foodStock.foodName, foodStock.category.toString, foodStock.currentQuantity,
        foodStock.unit, foodStock.minimumThreshold, expiryDateStr.orNull,
        foodStock.isPackaged, foodStock.location, lastModifiedByStr,
        DatabaseConnection.formatDateTime(foodStock.lastModifiedDate), foodStock.stockId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error updating food stock: ${e.getMessage}")
        false
    }
  }
  
  def delete(stockId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM food_stocks WHERE stock_id = ?", stockId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error deleting food stock: ${e.getMessage}")
        false
    }
  }
  
  def findLowStock(): List[FoodStock] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM food_stocks WHERE current_quantity <= minimum_threshold ORDER BY food_name ASC"
      )
      
      val foodStocks = scala.collection.mutable.ListBuffer[FoodStock]()
      while (rs.next()) {
        foodStocks += resultSetToFoodStock(rs)
      }
      
      rs.close()
      foodStocks.toList
    } catch {
      case e: Exception =>
        println(s"Error finding low stock items: ${e.getMessage}")
        List.empty
    }
  }
  
  def findExpired(): List[FoodStock] = {
    try {
      val now = DatabaseConnection.formatDateTime(LocalDateTime.now())
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM food_stocks WHERE expiry_date IS NOT NULL AND expiry_date < ? ORDER BY expiry_date ASC",
        now
      )
      
      val foodStocks = scala.collection.mutable.ListBuffer[FoodStock]()
      while (rs.next()) {
        foodStocks += resultSetToFoodStock(rs)
      }
      
      rs.close()
      foodStocks.toList
    } catch {
      case e: Exception =>
        println(s"Error finding expired items: ${e.getMessage}")
        List.empty
    }
  }
  
  def findExpiringSoon(days: Int = 7): List[FoodStock] = {
    try {
      val futureDate = DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(days))
      val now = DatabaseConnection.formatDateTime(LocalDateTime.now())
      val rs = DatabaseConnection.executeQuery(
        """SELECT * FROM food_stocks 
           WHERE expiry_date IS NOT NULL AND expiry_date > ? AND expiry_date <= ? 
           ORDER BY expiry_date ASC""",
        now, futureDate
      )
      
      val foodStocks = scala.collection.mutable.ListBuffer[FoodStock]()
      while (rs.next()) {
        foodStocks += resultSetToFoodStock(rs)
      }
      
      rs.close()
      foodStocks.toList
    } catch {
      case e: Exception =>
        println(s"Error finding expiring soon items: ${e.getMessage}")
        List.empty
    }
  }
  
  def getStatistics: (Int, Int, Int, Int) = {
    try {
      // Total items
      val totalRs = DatabaseConnection.executeQuery("SELECT COUNT(*) FROM food_stocks")
      val total = if (totalRs.next()) totalRs.getInt(1) else 0
      totalRs.close()
      
      // Low stock items
      val lowStockRs = DatabaseConnection.executeQuery(
        "SELECT COUNT(*) FROM food_stocks WHERE current_quantity <= minimum_threshold"
      )
      val lowStock = if (lowStockRs.next()) lowStockRs.getInt(1) else 0
      lowStockRs.close()
      
      // Out of stock items
      val outOfStockRs = DatabaseConnection.executeQuery(
        "SELECT COUNT(*) FROM food_stocks WHERE current_quantity <= 0"
      )
      val outOfStock = if (outOfStockRs.next()) outOfStockRs.getInt(1) else 0
      outOfStockRs.close()
      
      // Expired items
      val now = DatabaseConnection.formatDateTime(LocalDateTime.now())
      val expiredRs = DatabaseConnection.executeQuery(
        "SELECT COUNT(*) FROM food_stocks WHERE expiry_date IS NOT NULL AND expiry_date < ?",
        now
      )
      val expired = if (expiredRs.next()) expiredRs.getInt(1) else 0
      expiredRs.close()
      
      (total, lowStock, outOfStock, expired)
    } catch {
      case e: Exception =>
        println(s"Error getting food stock statistics: ${e.getMessage}")
        (0, 0, 0, 0)
    }
  }
  
  private def resultSetToFoodStock(rs: ResultSet): FoodStock = {
    val stockId = rs.getString("stock_id")
    val foodName = rs.getString("food_name")
    val category = FoodCategory.valueOf(rs.getString("category"))
    val currentQuantity = rs.getDouble("current_quantity")
    val unit = rs.getString("unit")
    val minimumThreshold = rs.getDouble("minimum_threshold")
    val expiryDateStr = rs.getString("expiry_date")
    val expiryDate = Option(expiryDateStr).filter(_ != null).map(DatabaseConnection.parseDateTime)
    val isPackaged = rs.getBoolean("is_packaged")
    val location = rs.getString("location")
    val lastModifiedByStr = rs.getString("last_modified_by")
    val lastModifiedBy = Option(lastModifiedByStr).filter(_ != null)
    val lastModifiedDate = DatabaseConnection.parseDateTime(rs.getString("last_modified_date"))
    val createdAt = DatabaseConnection.parseDateTime(rs.getString("created_at"))
    
    val foodStock = FoodStock(
      stockId, foodName, category, currentQuantity, unit, minimumThreshold,
      expiryDate, isPackaged, location, lastModifiedBy, lastModifiedDate, createdAt
    )
    
    foodStock
  }
}
