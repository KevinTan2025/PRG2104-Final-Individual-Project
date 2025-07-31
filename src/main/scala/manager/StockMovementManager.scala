package manager

import model._
import java.time.LocalDateTime
import scala.jdk.CollectionConverters._

/**
 * Manager class for handling stock movement operations
 */
class StockMovementManager extends Manager[StockMovement] {
  
  /**
   * Create a new stock movement
   * @param movement the stock movement to create
   */
  def createStockMovement(movement: StockMovement): Unit = {
    add(movement.movementId, movement)
  }
  
  /**
   * Get all stock movements
   * @return list of all stock movements
   */
  def getAllStockMovements: List[StockMovement] = {
    getAll.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
  }
  
  /**
   * Get stock movements by stock ID
   * @param stockId the stock ID to filter by
   * @return list of stock movements for the specified stock
   */
  def getMovementsByStockId(stockId: String): List[StockMovement] = {
    getAll.filter(_.stockId == stockId).sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
  }
  
  /**
   * Get stock movements by user ID
   * @param userId the user ID to filter by
   * @return list of stock movements by the specified user
   */
  def getMovementsByUserId(userId: String): List[StockMovement] = {
    getAll.filter(_.userId == userId).sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
  }
  
  /**
   * Get stock movements within a date range
   * @param startDate the start date
   * @param endDate the end date
   * @return list of stock movements within the date range
   */
  def getMovementsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List[StockMovement] = {
    getAll.filter { movement =>
      movement.timestamp.isAfter(startDate.minusSeconds(1)) && 
      movement.timestamp.isBefore(endDate.plusSeconds(1))
    }.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
  }
  
  /**
   * Get stock movements by action type
   * @param actionType the action type to filter by
   * @return list of stock movements with the specified action type
   */
  def getMovementsByActionType(actionType: StockActionType): List[StockMovement] = {
    getAll.filter(_.actionType == actionType).sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
  }
  
  /**
   * Get recent stock movements (last N days)
   * @param days number of days to look back
   * @return list of recent stock movements
   */
  def getRecentMovements(days: Int = 7): List[StockMovement] = {
    val cutoffDate = LocalDateTime.now().minusDays(days)
    getAll.filter(_.timestamp.isAfter(cutoffDate)).sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
  }
  
  /**
   * Get movement statistics
   * @return tuple of (total movements, additions, removals, adjustments)
   */
  def getMovementStatistics: (Int, Int, Int, Int) = {
    val movements = getAll
    val total = movements.length
    val additions = movements.count(_.actionType == StockActionType.STOCK_IN)
    val removals = movements.count(_.actionType == StockActionType.STOCK_OUT)
    val adjustments = movements.count(_.actionType == StockActionType.ADJUSTMENT)
    (total, additions, removals, adjustments)
  }
  
  /**
   * Get top users by activity (most movements)
   * @param limit number of top users to return
   * @return list of (userId, movement count) pairs
   */
  def getTopUsersByActivity(limit: Int = 10): List[(String, Int)] = {
    getAll
      .groupBy(_.userId)
      .map { case (userId, movements) => (userId, movements.length) }
      .toList
      .sortBy(-_._2)
      .take(limit)
  }
  
  /**
   * Get movement summary for a specific stock
   * @param stockId the stock ID
   * @return summary string
   */
  def getMovementSummary(stockId: String): String = {
    val movements = getMovementsByStockId(stockId)
    if (movements.isEmpty) {
      "No movement history available."
    } else {
      val totalMovements = movements.length
      val additions = movements.count(_.actionType == StockActionType.STOCK_IN)
      val removals = movements.count(_.actionType == StockActionType.STOCK_OUT)
      val adjustments = movements.count(_.actionType == StockActionType.ADJUSTMENT)
      val lastMovement = movements.head
      
      s"Total movements: $totalMovements (Add: $additions, Remove: $removals, Adjust: $adjustments)\n" +
      s"Last activity: ${lastMovement.getDescription} by ${lastMovement.userId} on ${lastMovement.timestamp.toLocalDate}"
    }
  }
  
  /**
   * Delete movements older than specified days
   * @param days number of days to keep
   * @return number of movements deleted
   */
  def cleanupOldMovements(days: Int = 365): Int = {
    val cutoffDate = LocalDateTime.now().minusDays(days)
    val oldMovements = getAll.filter(_.timestamp.isBefore(cutoffDate))
    oldMovements.foreach(movement => remove(movement.movementId))
    oldMovements.length
  }
  
  /**
   * Get movements that resulted in negative stock
   * @return list of movements that caused stock to go negative
   */
  def getProblematicMovements: List[StockMovement] = {
    // This would require cross-referencing with stock quantities
    // For now, return removals with high quantities that might be problematic
    getAll.filter { movement =>
      movement.actionType == StockActionType.STOCK_OUT && movement.quantity > 100
    }.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
  }
  
  override def toString: String = {
    s"StockMovementManager(${getAll.length} movements)"
  }
}
