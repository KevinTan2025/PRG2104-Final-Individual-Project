package manager

import model._
import java.time.LocalDateTime
import scala.jdk.CollectionConverters._

/**
 * Manager class for handling food stock operations
 */
class FoodStockManager extends Manager[FoodStock] {
  
  /**
   * Create a new food stock item
   * @param foodStock the food stock item to create
   */
  def createFoodStock(foodStock: FoodStock): Unit = {
    add(foodStock.stockId, foodStock)
  }
  
  /**
   * Get all food stock items
   * @return list of all food stock items
   */
  def getAllFoodStocks: List[FoodStock] = {
    getAll.sortBy(_.foodName)
  }
  
  /**
   * Get food stock items by category
   * @param category the category to filter by
   * @return list of food stock items in the specified category
   */
  def getFoodStocksByCategory(category: FoodCategory): List[FoodStock] = {
    items.values().asScala.filter(_.category == category).toList.sortBy(_.foodName)
  }
  
  /**
   * Get food stock items by status
   * @param status the status to filter by
   * @return list of food stock items with the specified status
   */
  def getFoodStocksByStatus(status: StockStatus): List[FoodStock] = {
    items.values().asScala.filter(_.getStockStatus == status).toList.sortBy(_.foodName)
  }
  
  /**
   * Get low stock items
   * @return list of food stock items that are low in stock
   */
  def getLowStockItems: List[FoodStock] = {
    getFoodStocksByStatus(StockStatus.LOW_STOCK)
  }
  
  /**
   * Get out of stock items
   * @return list of food stock items that are out of stock
   */
  def getOutOfStockItems: List[FoodStock] = {
    getFoodStocksByStatus(StockStatus.OUT_OF_STOCK)
  }
  
  /**
   * Get expired items
   * @return list of food stock items that are expired
   */
  def getExpiredItems: List[FoodStock] = {
    items.values().asScala.filter(_.isExpired).toList.sortBy(_.expiryDate)
  }
  
  /**
   * Get items expiring soon
   * @param days number of days to look ahead
   * @return list of food stock items expiring within specified days
   */
  def getExpiringSoonItems(days: Int = 7): List[FoodStock] = {
    items.values().asScala.filter(_.isExpiringSoon(days)).toList.sortBy(_.expiryDate)
  }
  
  /**
   * Search food stock items by name
   * @param searchTerm the term to search for
   * @return list of matching food stock items
   */
  def searchFoodStocks(searchTerm: String): List[FoodStock] = {
    val term = searchTerm.toLowerCase
    items.values().asScala.filter { stock =>
      stock.foodName.toLowerCase.contains(term) || 
      stock.category.toString.toLowerCase.contains(term) ||
      stock.location.toLowerCase.contains(term)
    }.toList.sortBy(_.foodName)
  }
  
  /**
   * Get food stock items by location
   * @param location the location to filter by
   * @return list of food stock items in the specified location
   */
  def getFoodStocksByLocation(location: String): List[FoodStock] = {
    items.values().asScala.filter(_.location.toLowerCase.contains(location.toLowerCase)).toList.sortBy(_.foodName)
  }
  
  /**
   * Add stock to an existing item
   * @param stockId the stock ID
   * @param quantity quantity to add
   * @param userId user performing the action
   * @param notes additional notes
   * @return true if successful, false if stock not found
   */
  def addStock(stockId: String, quantity: Double, userId: String, notes: String = ""): Boolean = {
    get(stockId) match {
      case Some(stock) =>
        stock.addStock(quantity, userId, notes)
        true
      case None => false
    }
  }
  
  /**
   * Remove stock from an existing item
   * @param stockId the stock ID
   * @param quantity quantity to remove
   * @param userId user performing the action
   * @param notes additional notes
   * @return true if successful, false if stock not found or insufficient quantity
   */
  def removeStock(stockId: String, quantity: Double, userId: String, notes: String = ""): Boolean = {
    get(stockId) match {
      case Some(stock) => 
        stock.removeStock(quantity, userId, notes) match {
          case Right(updatedStock) => 
            add(stockId, updatedStock)
            true
          case Left(_) => false
        }
      case None => false
    }
  }
  
  /**
   * Adjust stock quantity
   * @param stockId the stock ID
   * @param newQuantity new quantity to set
   * @param userId user performing the action
   * @param notes additional notes
   * @return true if successful, false if stock not found
   */
  def adjustStock(stockId: String, newQuantity: Double, userId: String, notes: String = ""): Boolean = {
    get(stockId) match {
      case Some(stock) =>
        stock.adjustStock(newQuantity, userId, notes)
        true
      case None => false
    }
  }
  
  /**
   * Get all stock movements for a specific stock item
   * @param stockId the stock ID
   * @return list of stock movements
   */
  def getStockMovements(stockId: String): List[StockMovement] = {
    get(stockId) match {
      case Some(stock) => stock.stockHistory.sortBy(_.timestamp).reverse
      case None => List.empty
    }
  }
  
  /**
   * Get all stock movements across all items
   * @return list of all stock movements
   */
  def getAllStockMovements: List[StockMovement] = {
    items.values().asScala.flatMap(_.stockHistory).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get stock movements by user
   * @param userId the user ID
   * @return list of stock movements performed by the user
   */
  def getStockMovementsByUser(userId: String): List[StockMovement] = {
    getAllStockMovements.filter(_.userId == userId)
  }
  
  /**
   * Get stock movements within a date range
   * @param startDate start date
   * @param endDate end date
   * @return list of stock movements within the date range
   */
  def getStockMovementsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List[StockMovement] = {
    getAllStockMovements.filter { movement =>
      movement.timestamp.isAfter(startDate) && movement.timestamp.isBefore(endDate)
    }
  }
  
  /**
   * Get stock statistics
   * @return tuple of (total items, low stock items, out of stock items, expired items)
   */
  def getStockStatistics: (Int, Int, Int, Int) = {
    val allStocks = items.values().asScala.toList
    val lowStockCount = allStocks.count(_.getStockStatus == StockStatus.LOW_STOCK)
    val outOfStockCount = allStocks.count(_.getStockStatus == StockStatus.OUT_OF_STOCK)
    val expiredCount = allStocks.count(_.isExpired)
    
    (allStocks.size, lowStockCount, outOfStockCount, expiredCount)
  }
  
  /**
   * Get stock value summary (if costs were tracked)
   * For now, just return counts by category
   */
  def getStockSummaryByCategory: Map[FoodCategory, Int] = {
    items.values().asScala.groupBy(_.category).map { case (category, stocks) =>
      category -> stocks.size
    }.toMap
  }
  
  /**
   * Remove expired items (mark them as expired removal)
   * @param userId user performing the cleanup
   * @return number of items removed
   */
  def removeExpiredItems(userId: String): Int = {
    val expiredItems = getExpiredItems
    expiredItems.foreach { stock =>
      stock.removeStock(stock.currentQuantity, userId, "Expired item removal")
    }
    expiredItems.size
  }
  
  /**
   * Generate stock alerts
   * @return list of alert messages
   */
  def generateStockAlerts: List[String] = {
    val lowStockItems = getLowStockItems
    val outOfStockItems = getOutOfStockItems
    val expiringSoonItems = getExpiringSoonItems()
    val expiredItems = getExpiredItems
    
    val lowStockAlerts = if (lowStockItems.nonEmpty) {
      s"⚠️ ${lowStockItems.size} items are low in stock" ::
      lowStockItems.take(3).map { stock =>
        s"  • ${stock.foodName}: ${stock.currentQuantity} ${stock.unit} (min: ${stock.minimumThreshold})"
      }.toList
    } else List.empty
    
    val outOfStockAlerts = if (outOfStockItems.nonEmpty) {
      s"🚨 ${outOfStockItems.size} items are out of stock" ::
      outOfStockItems.take(3).map { stock =>
        s"  • ${stock.foodName}"
      }.toList
    } else List.empty
    
    val expiringSoonAlerts = if (expiringSoonItems.nonEmpty) {
      s"⏰ ${expiringSoonItems.size} items are expiring soon" ::
      expiringSoonItems.take(3).map { stock =>
        val daysLeft = stock.getDaysUntilExpiry.getOrElse(0)
        s"  • ${stock.foodName}: ${daysLeft} days left"
      }.toList
    } else List.empty
    
    val expiredAlerts = if (expiredItems.nonEmpty) {
      s"💀 ${expiredItems.size} items have expired" ::
      expiredItems.take(3).map { stock =>
        s"  • ${stock.foodName}: expired"
      }.toList
    } else List.empty
    
    lowStockAlerts ++ outOfStockAlerts ++ expiringSoonAlerts ++ expiredAlerts
  }
}
