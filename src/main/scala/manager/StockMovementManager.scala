package manager

import model._
import java.time.LocalDateTime
import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal

/**
 * Immutable state for StockMovementManager
 * @param movements map of movement ID to StockMovement
 */
case class StockMovementManagerState(movements: Map[String, StockMovement] = Map.empty)

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
   * Functional version: Create a new stock movement
   * @param state current state
   * @param movement the stock movement to create
   * @return Try containing new state
   */
  def createStockMovement(state: StockMovementManagerState, movement: StockMovement): Try[StockMovementManagerState] = {
    Try {
      state.copy(movements = state.movements + (movement.movementId -> movement))
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to create stock movement: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Create a new stock movement
   * @param state current state
   * @param movement the stock movement to create
   * @return new state or original state if failed
   */
  def createStockMovementSafe(state: StockMovementManagerState, movement: StockMovement): StockMovementManagerState = {
    createStockMovement(state, movement).getOrElse(state)
  }
  
  /**
   * Get all stock movements
   * @return list of all stock movements
   */
  def getAllStockMovements: List[StockMovement] = {
    getAll.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
  }
  
  /**
   * Functional version: Get all stock movements
   * @param state current state
   * @return Try containing sorted list of all stock movements
   */
  def getAllStockMovements(state: StockMovementManagerState): Try[List[StockMovement]] = {
    Try {
      state.movements.values.toList.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get all stock movements: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get all stock movements
   * @param state current state
   * @return sorted list of all stock movements or empty list if failed
   */
  def getAllStockMovementsSafe(state: StockMovementManagerState): List[StockMovement] = {
    getAllStockMovements(state).getOrElse(List.empty)
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
   * Functional version: Get stock movements by stock ID
   * @param state current state
   * @param stockId the stock ID to filter by
   * @return Try containing list of stock movements for the specified stock
   */
  def getMovementsByStockId(state: StockMovementManagerState, stockId: String): Try[List[StockMovement]] = {
    Try {
      state.movements.values.filter(_.stockId == stockId).toList.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get movements by stock ID: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get stock movements by stock ID
   * @param state current state
   * @param stockId the stock ID to filter by
   * @return list of stock movements for the specified stock or empty list if failed
   */
  def getMovementsByStockIdSafe(state: StockMovementManagerState, stockId: String): List[StockMovement] = {
    getMovementsByStockId(state, stockId).getOrElse(List.empty)
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
   * Functional version: Get stock movements by user ID
   * @param state current state
   * @param userId the user ID to filter by
   * @return Try containing list of stock movements by the specified user
   */
  def getMovementsByUserId(state: StockMovementManagerState, userId: String): Try[List[StockMovement]] = {
    Try {
      state.movements.values.filter(_.userId == userId).toList.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get movements by user ID: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get stock movements by user ID
   * @param state current state
   * @param userId the user ID to filter by
   * @return list of stock movements by the specified user or empty list if failed
   */
  def getMovementsByUserIdSafe(state: StockMovementManagerState, userId: String): List[StockMovement] = {
    getMovementsByUserId(state, userId).getOrElse(List.empty)
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
   * Functional version: Get stock movements within a date range
   * @param state current state
   * @param startDate the start date
   * @param endDate the end date
   * @return Try containing list of stock movements within the date range
   */
  def getMovementsByDateRange(state: StockMovementManagerState, startDate: LocalDateTime, endDate: LocalDateTime): Try[List[StockMovement]] = {
    Try {
      state.movements.values.filter { movement =>
        movement.timestamp.isAfter(startDate.minusSeconds(1)) && 
        movement.timestamp.isBefore(endDate.plusSeconds(1))
      }.toList.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get movements by date range: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get stock movements within a date range
   * @param state current state
   * @param startDate the start date
   * @param endDate the end date
   * @return list of stock movements within the date range or empty list if failed
   */
  def getMovementsByDateRangeSafe(state: StockMovementManagerState, startDate: LocalDateTime, endDate: LocalDateTime): List[StockMovement] = {
    getMovementsByDateRange(state, startDate, endDate).getOrElse(List.empty)
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
   * Functional version: Get stock movements by action type
   * @param state current state
   * @param actionType the action type to filter by
   * @return Try containing list of stock movements with the specified action type
   */
  def getMovementsByActionType(state: StockMovementManagerState, actionType: StockActionType): Try[List[StockMovement]] = {
    Try {
      state.movements.values.filter(_.actionType == actionType).toList.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get movements by action type: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get stock movements by action type
   * @param state current state
   * @param actionType the action type to filter by
   * @return list of stock movements with the specified action type or empty list if failed
   */
  def getMovementsByActionTypeSafe(state: StockMovementManagerState, actionType: StockActionType): List[StockMovement] = {
    getMovementsByActionType(state, actionType).getOrElse(List.empty)
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
   * Functional version: Get recent stock movements (last N days)
   * @param state current state
   * @param days number of days to look back
   * @return Try containing list of recent stock movements
   */
  def getRecentMovements(state: StockMovementManagerState, days: Int): Try[List[StockMovement]] = {
    Try {
      val cutoffDate = LocalDateTime.now().minusDays(days)
      state.movements.values.filter(_.timestamp.isAfter(cutoffDate)).toList.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get recent movements: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get recent stock movements (last N days)
   * @param state current state
   * @param days number of days to look back
   * @return list of recent stock movements or empty list if failed
   */
  def getRecentMovementsSafe(state: StockMovementManagerState, days: Int): List[StockMovement] = {
    getRecentMovements(state, days).getOrElse(List.empty)
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
   * Functional version: Get movement statistics
   * @param state current state
   * @return Try containing tuple of (total movements, additions, removals, adjustments)
   */
  def getMovementStatistics(state: StockMovementManagerState): Try[(Int, Int, Int, Int)] = {
    Try {
      val movements = state.movements.values.toList
      val total = movements.length
      val additions = movements.count(_.actionType == StockActionType.STOCK_IN)
      val removals = movements.count(_.actionType == StockActionType.STOCK_OUT)
      val adjustments = movements.count(_.actionType == StockActionType.ADJUSTMENT)
      (total, additions, removals, adjustments)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get movement statistics: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get movement statistics
   * @param state current state
   * @return tuple of (total movements, additions, removals, adjustments) or (0,0,0,0) if failed
   */
  def getMovementStatisticsSafe(state: StockMovementManagerState): (Int, Int, Int, Int) = {
    getMovementStatistics(state).getOrElse((0, 0, 0, 0))
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
   * Functional version: Get top users by activity (most movements)
   * @param state current state
   * @param limit number of top users to return
   * @return Try containing list of (userId, movement count) pairs
   */
  def getTopUsersByActivity(state: StockMovementManagerState, limit: Int): Try[List[(String, Int)]] = {
    Try {
      state.movements.values
        .groupBy(_.userId)
        .map { case (userId, movements) => (userId, movements.size) }
        .toList
        .sortBy(-_._2)
        .take(limit)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get top users by activity: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get top users by activity (most movements)
   * @param state current state
   * @param limit number of top users to return
   * @return list of (userId, movement count) pairs or empty list if failed
   */
  def getTopUsersByActivitySafe(state: StockMovementManagerState, limit: Int): List[(String, Int)] = {
    getTopUsersByActivity(state, limit).getOrElse(List.empty)
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
   * Functional version: Get movement summary for a specific stock
   * @param state current state
   * @param stockId the stock ID
   * @return Try containing summary string
   */
  def getMovementSummary(state: StockMovementManagerState, stockId: String): Try[String] = {
    Try {
      val movements = state.movements.values.filter(_.stockId == stockId).toList.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
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
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get movement summary: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get movement summary for a specific stock
   * @param state current state
   * @param stockId the stock ID
   * @return summary string or error message if failed
   */
  def getMovementSummarySafe(state: StockMovementManagerState, stockId: String): String = {
    getMovementSummary(state, stockId).getOrElse("Error generating movement summary.")
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
   * Functional version: Delete movements older than specified days
   * @param state current state
   * @param days number of days to keep
   * @return Try containing tuple of (new state, number of movements deleted)
   */
  def cleanupOldMovements(state: StockMovementManagerState, days: Int): Try[(StockMovementManagerState, Int)] = {
    Try {
      val cutoffDate = LocalDateTime.now().minusDays(days)
      val (oldMovements, recentMovements) = state.movements.values.partition(_.timestamp.isBefore(cutoffDate))
      val newState = state.copy(movements = recentMovements.map(m => m.movementId -> m).toMap)
      (newState, oldMovements.size)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to cleanup old movements: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Delete movements older than specified days
   * @param state current state
   * @param days number of days to keep
   * @return tuple of (new state, number of movements deleted) or (original state, 0) if failed
   */
  def cleanupOldMovementsSafe(state: StockMovementManagerState, days: Int): (StockMovementManagerState, Int) = {
    cleanupOldMovements(state, days).getOrElse((state, 0))
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
  
  /**
   * Functional version: Get movements that resulted in negative stock
   * @param state current state
   * @return Try containing list of movements that caused stock to go negative
   */
  def getProblematicMovements(state: StockMovementManagerState): Try[List[StockMovement]] = {
    Try {
      // This would require cross-referencing with stock quantities
      // For now, return removals with high quantities that might be problematic
      state.movements.values.filter { movement =>
        movement.actionType == StockActionType.STOCK_OUT && movement.quantity > 100
      }.toList.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get problematic movements: ${e.getMessage}", e)
    }
  }
  
  /**
   * Safe version: Get movements that resulted in negative stock
   * @param state current state
   * @return list of movements that caused stock to go negative or empty list if failed
   */
  def getProblematicMovementsSafe(state: StockMovementManagerState): List[StockMovement] = {
    getProblematicMovements(state).getOrElse(List.empty)
  }
  
  override def toString: String = {
    s"StockMovementManager(${getAll.length} movements)"
  }
}
