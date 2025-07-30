package manager

import model._
import java.time.LocalDateTime
import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal

/**
 * 食品库存管理状态
 * @param stocks 库存映射表
 */
case class FoodStockManagerState(
  stocks: Map[String, FoodStock] = Map.empty
)

/**
 * Manager class for handling food stock operations
 * 函数式食品库存管理器
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
   * 函数式创建食品库存项目
   * @param state 当前状态
   * @param foodStock 要创建的食品库存项目
   * @return 新状态
   */
  def createFoodStock(state: FoodStockManagerState, foodStock: FoodStock): Try[FoodStockManagerState] = {
    Try {
      state.copy(stocks = state.stocks + (foodStock.stockId -> foodStock))
    }.recover {
      case NonFatal(e) =>
        println(s"创建食品库存失败: ${e.getMessage}")
        state
    }
  }
  
  /**
   * 函数式创建食品库存项目 - 安全版本
   */
  def createFoodStockSafe(state: FoodStockManagerState, foodStock: FoodStock): FoodStockManagerState = {
    createFoodStock(state, foodStock).getOrElse(state)
  }
  
  /**
   * Get all food stock items
   * @return list of all food stock items
   */
  def getAllFoodStocks: List[FoodStock] = {
    getAll.sortBy(_.foodName)
  }
  
  /**
   * 函数式获取所有食品库存项目
   * @param state 当前状态
   * @return 排序后的食品库存列表
   */
  def getAllFoodStocks(state: FoodStockManagerState): Try[List[FoodStock]] = {
    Try {
      state.stocks.values.toList.sortBy(_.foodName)
    }.recover {
      case NonFatal(e) =>
        println(s"获取所有食品库存失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式获取所有食品库存项目 - 安全版本
   */
  def getAllFoodStocksSafe(state: FoodStockManagerState): List[FoodStock] = {
    getAllFoodStocks(state).getOrElse(List.empty)
  }
  
  /**
   * Get food stock items by category
   * @param category the category to filter by
   * @return list of food stock items in the specified category
   */
  def getFoodStocksByCategory(category: FoodCategory): List[FoodStock] = {
    items.values.filter(_.category == category).toList.sortBy(_.foodName)
  }
  
  /**
   * 函数式按类别获取食品库存项目
   * @param state 当前状态
   * @param category 要过滤的类别
   * @return 指定类别的食品库存列表
   */
  def getFoodStocksByCategory(state: FoodStockManagerState, category: FoodCategory): Try[List[FoodStock]] = {
    Try {
      state.stocks.values.filter(_.category == category).toList.sortBy(_.foodName)
    }.recover {
      case NonFatal(e) =>
        println(s"按类别获取食品库存失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按类别获取食品库存项目 - 安全版本
   */
  def getFoodStocksByCategorySafe(state: FoodStockManagerState, category: FoodCategory): List[FoodStock] = {
    getFoodStocksByCategory(state, category).getOrElse(List.empty)
  }
  
  /**
   * Get food stock items by status
   * @param status the status to filter by
   * @return list of food stock items with the specified status
   */
  def getFoodStocksByStatus(status: StockStatus): List[FoodStock] = {
    items.values.filter(_.getStockStatus == status).toList.sortBy(_.foodName)
  }
  
  /**
   * 函数式按状态获取食品库存项目
   * @param state 当前状态
   * @param status 要过滤的状态
   * @return 指定状态的食品库存列表
   */
  def getFoodStocksByStatus(state: FoodStockManagerState, status: StockStatus): Try[List[FoodStock]] = {
    Try {
      state.stocks.values.filter(_.getStockStatus == status).toList.sortBy(_.foodName)
    }.recover {
      case NonFatal(e) =>
        println(s"按状态获取食品库存失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按状态获取食品库存项目 - 安全版本
   */
  def getFoodStocksByStatusSafe(state: FoodStockManagerState, status: StockStatus): List[FoodStock] = {
    getFoodStocksByStatus(state, status).getOrElse(List.empty)
  }
  
  /**
   * Get low stock items
   * @return list of food stock items that are low in stock
   */
  def getLowStockItems: List[FoodStock] = {
    getFoodStocksByStatus(StockStatus.LOW_STOCK)
  }
  
  /**
   * 函数式获取低库存项目
   * @param state 当前状态
   * @return 低库存的食品库存列表
   */
  def getLowStockItems(state: FoodStockManagerState): Try[List[FoodStock]] = {
    getFoodStocksByStatus(state, StockStatus.LOW_STOCK)
  }
  
  /**
   * 函数式获取低库存项目 - 安全版本
   */
  def getLowStockItemsSafe(state: FoodStockManagerState): List[FoodStock] = {
    getLowStockItems(state).getOrElse(List.empty)
  }
  
  /**
   * Get out of stock items
   * @return list of food stock items that are out of stock
   */
  def getOutOfStockItems: List[FoodStock] = {
    getFoodStocksByStatus(StockStatus.OUT_OF_STOCK)
  }
  
  /**
   * 函数式获取缺货项目
   * @param state 当前状态
   * @return 缺货的食品库存列表
   */
  def getOutOfStockItems(state: FoodStockManagerState): Try[List[FoodStock]] = {
    getFoodStocksByStatus(state, StockStatus.OUT_OF_STOCK)
  }
  
  /**
   * 函数式获取缺货项目 - 安全版本
   */
  def getOutOfStockItemsSafe(state: FoodStockManagerState): List[FoodStock] = {
    getOutOfStockItems(state).getOrElse(List.empty)
  }
  
  /**
   * Get expired items
   * @return list of food stock items that are expired
   */
  def getExpiredItems: List[FoodStock] = {
    items.values.filter(_.isExpired).toList.sortBy(_.expiryDate)
  }
  
  /**
   * 函数式获取过期项目
   * @param state 当前状态
   * @return 过期的食品库存列表
   */
  def getExpiredItems(state: FoodStockManagerState): Try[List[FoodStock]] = {
    Try {
      state.stocks.values.filter(_.isExpired).toList.sortBy(_.expiryDate)
    }.recover {
      case NonFatal(e) =>
        println(s"获取过期项目失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式获取过期项目 - 安全版本
   */
  def getExpiredItemsSafe(state: FoodStockManagerState): List[FoodStock] = {
    getExpiredItems(state).getOrElse(List.empty)
  }
  
  /**
   * Get items expiring soon
   * @param days number of days to look ahead
   * @return list of food stock items expiring within specified days
   */
  def getExpiringSoonItems(days: Int = 7): List[FoodStock] = {
    items.values.filter(_.isExpiringSoon(days)).toList.sortBy(_.expiryDate)
  }
  
  /**
   * 函数式获取即将过期的项目
   * @param state 当前状态
   * @param days 天数阈值
   * @return 即将过期的库存列表
   */
  def getExpiringSoonItems(state: FoodStockManagerState, days: Int): Try[List[FoodStock]] = {
    Try {
      state.stocks.values.filter(_.isExpiringSoon(days)).toList.sortBy(_.expiryDate)
    }.recover {
      case NonFatal(e) =>
        println(s"获取即将过期项目失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式获取即将过期的项目 - 安全版本
   */
  def getExpiringSoonItemsSafe(state: FoodStockManagerState, days: Int): List[FoodStock] = {
    getExpiringSoonItems(state, days).getOrElse(List.empty)
  }
  
  /**
   * Search food stock items by name
   * @param searchTerm the term to search for
   * @return list of matching food stock items
   */
  def searchFoodStocks(searchTerm: String): List[FoodStock] = {
    val term = searchTerm.toLowerCase
    items.values.filter { stock =>
      stock.foodName.toLowerCase.contains(term) || 
      stock.category.toString.toLowerCase.contains(term) ||
      stock.location.toLowerCase.contains(term)
    }.toList.sortBy(_.foodName)
  }
  
  /**
   * 函数式搜索食品库存项目
   * @param state 当前状态
   * @param searchTerm 搜索词
   * @return 匹配的食品库存列表
   */
  def searchFoodStocks(state: FoodStockManagerState, searchTerm: String): Try[List[FoodStock]] = {
    Try {
      val term = searchTerm.toLowerCase
      state.stocks.values.filter { stock =>
        stock.foodName.toLowerCase.contains(term) || 
        stock.category.toString.toLowerCase.contains(term) ||
        stock.location.toLowerCase.contains(term)
      }.toList.sortBy(_.foodName)
    }.recover {
      case NonFatal(e) =>
        println(s"搜索食品库存失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式搜索食品库存项目 - 安全版本
   */
  def searchFoodStocksSafe(state: FoodStockManagerState, searchTerm: String): List[FoodStock] = {
    searchFoodStocks(state, searchTerm).getOrElse(List.empty)
  }
  
  /**
   * Get food stock items by location
   * @param location the location to filter by
   * @return list of food stock items in the specified location
   */
  def getFoodStocksByLocation(location: String): List[FoodStock] = {
    items.values.filter(_.location.toLowerCase.contains(location.toLowerCase)).toList.sortBy(_.foodName)
  }
  
  /**
   * 函数式按位置获取食品库存项目
   * @param state 当前状态
   * @param location 位置
   * @return 指定位置的食品库存列表
   */
  def getFoodStocksByLocation(state: FoodStockManagerState, location: String): Try[List[FoodStock]] = {
    Try {
      state.stocks.values.filter(_.location.toLowerCase.contains(location.toLowerCase)).toList.sortBy(_.foodName)
    }.recover {
      case NonFatal(e) =>
        println(s"按位置获取食品库存失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按位置获取食品库存项目 - 安全版本
   */
  def getFoodStocksByLocationSafe(state: FoodStockManagerState, location: String): List[FoodStock] = {
    getFoodStocksByLocation(state, location).getOrElse(List.empty)
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
   * 函数式添加库存
   * @param state 当前状态
   * @param stockId 库存ID
   * @param quantity 添加数量
   * @param userId 操作用户ID
   * @param notes 备注
   * @return (新状态, 操作是否成功)
   */
  def addStock(state: FoodStockManagerState, stockId: String, quantity: Double, userId: String, notes: String): Try[(FoodStockManagerState, Boolean)] = {
    Try {
      state.stocks.get(stockId) match {
        case Some(stock) =>
          // 创建新的库存对象，添加库存
          val updatedStock = stock.copy()
          updatedStock.addStock(quantity, userId, notes)
          val newState = state.copy(stocks = state.stocks + (stockId -> updatedStock))
          (newState, true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"添加库存失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式添加库存 - 安全版本
   */
  def addStockSafe(state: FoodStockManagerState, stockId: String, quantity: Double, userId: String, notes: String): (FoodStockManagerState, Boolean) = {
    addStock(state, stockId, quantity, userId, notes).getOrElse((state, false))
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
      case Some(stock) => stock.removeStock(quantity, userId, notes)
      case None => false
    }
  }
  
  /**
   * 函数式移除库存
   * @param state 当前状态
   * @param stockId 库存ID
   * @param quantity 移除数量
   * @param userId 操作用户ID
   * @param notes 备注
   * @return (新状态, 操作是否成功)
   */
  def removeStock(state: FoodStockManagerState, stockId: String, quantity: Double, userId: String, notes: String): Try[(FoodStockManagerState, Boolean)] = {
    Try {
      state.stocks.get(stockId) match {
        case Some(stock) =>
          // 创建新的库存对象，移除库存
          val updatedStock = stock.copy()
          val success = updatedStock.removeStock(quantity, userId, notes)
          val newState = state.copy(stocks = state.stocks + (stockId -> updatedStock))
          (newState, success)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"移除库存失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式移除库存 - 安全版本
   */
  def removeStockSafe(state: FoodStockManagerState, stockId: String, quantity: Double, userId: String, notes: String): (FoodStockManagerState, Boolean) = {
    removeStock(state, stockId, quantity, userId, notes).getOrElse((state, false))
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
   * 函数式调整库存数量
   * @param state 当前状态
   * @param stockId 库存ID
   * @param newQuantity 新数量
   * @param userId 操作用户ID
   * @param notes 备注
   * @return (新状态, 操作是否成功)
   */
  def adjustStock(state: FoodStockManagerState, stockId: String, newQuantity: Double, userId: String, notes: String): Try[(FoodStockManagerState, Boolean)] = {
    Try {
      state.stocks.get(stockId) match {
        case Some(stock) =>
          // 创建新的库存对象，调整库存
          val updatedStock = stock.copy()
          updatedStock.adjustStock(newQuantity, userId, notes)
          val newState = state.copy(stocks = state.stocks + (stockId -> updatedStock))
          (newState, true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"调整库存失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式调整库存数量 - 安全版本
   */
  def adjustStockSafe(state: FoodStockManagerState, stockId: String, newQuantity: Double, userId: String, notes: String): (FoodStockManagerState, Boolean) = {
    adjustStock(state, stockId, newQuantity, userId, notes).getOrElse((state, false))
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
   * 函数式获取特定库存项目的所有库存移动
   * @param state 当前状态
   * @param stockId 库存ID
   * @return 库存移动列表
   */
  def getStockMovements(state: FoodStockManagerState, stockId: String): Try[List[StockMovement]] = {
    Try {
      state.stocks.get(stockId) match {
        case Some(stock) => stock.stockHistory.sortBy(_.timestamp).reverse
        case None => List.empty
      }
    }.recover {
      case NonFatal(e) =>
        println(s"获取库存移动失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式获取特定库存项目的所有库存移动 - 安全版本
   */
  def getStockMovementsSafe(state: FoodStockManagerState, stockId: String): List[StockMovement] = {
    getStockMovements(state, stockId).getOrElse(List.empty)
  }
  
  /**
   * Get all stock movements across all items
   * @return list of all stock movements
   */
  def getAllStockMovements: List[StockMovement] = {
    items.values.flatMap(_.stockHistory).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * 函数式获取所有库存移动
   * @param state 当前状态
   * @return 所有库存移动列表
   */
  def getAllStockMovements(state: FoodStockManagerState): Try[List[StockMovement]] = {
    Try {
      state.stocks.values.flatMap(_.stockHistory).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) =>
        println(s"获取所有库存移动失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式获取所有库存移动 - 安全版本
   */
  def getAllStockMovementsSafe(state: FoodStockManagerState): List[StockMovement] = {
    getAllStockMovements(state).getOrElse(List.empty)
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
   * 函数式按用户获取库存移动
   * @param state 当前状态
   * @param userId 用户ID
   * @return 用户执行的库存移动列表
   */
  def getStockMovementsByUser(state: FoodStockManagerState, userId: String): Try[List[StockMovement]] = {
    getAllStockMovements(state).map(_.filter(_.userId == userId))
  }
  
  /**
   * 函数式按用户获取库存移动 - 安全版本
   */
  def getStockMovementsByUserSafe(state: FoodStockManagerState, userId: String): List[StockMovement] = {
    getStockMovementsByUser(state, userId).getOrElse(List.empty)
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
   * 函数式按日期范围获取库存移动
   * @param state 当前状态
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @return 日期范围内的库存移动列表
   */
  def getStockMovementsByDateRange(state: FoodStockManagerState, startDate: LocalDateTime, endDate: LocalDateTime): Try[List[StockMovement]] = {
    getAllStockMovements(state).map { movements =>
      movements.filter { movement =>
        movement.timestamp.isAfter(startDate) && movement.timestamp.isBefore(endDate)
      }
    }
  }
  
  /**
   * 函数式按日期范围获取库存移动 - 安全版本
   */
  def getStockMovementsByDateRangeSafe(state: FoodStockManagerState, startDate: LocalDateTime, endDate: LocalDateTime): List[StockMovement] = {
    getStockMovementsByDateRange(state, startDate, endDate).getOrElse(List.empty)
  }
  
  /**
   * Get stock statistics
   * @return tuple of (total items, low stock items, out of stock items, expired items)
   */
  def getStockStatistics: (Int, Int, Int, Int) = {
    val allStocks = items.values.toList
    val lowStockCount = allStocks.count(_.getStockStatus == StockStatus.LOW_STOCK)
    val outOfStockCount = allStocks.count(_.getStockStatus == StockStatus.OUT_OF_STOCK)
    val expiredCount = allStocks.count(_.isExpired)
    
    (allStocks.size, lowStockCount, outOfStockCount, expiredCount)
  }
  
  /**
   * 函数式获取库存统计
   * @param state 当前状态
   * @return (总项目数, 低库存项目数, 缺货项目数, 过期项目数)
   */
  def getStockStatistics(state: FoodStockManagerState): Try[(Int, Int, Int, Int)] = {
    Try {
      val allStocks = state.stocks.values.toList
      val lowStockCount = allStocks.count(_.getStockStatus == StockStatus.LOW_STOCK)
      val outOfStockCount = allStocks.count(_.getStockStatus == StockStatus.OUT_OF_STOCK)
      val expiredCount = allStocks.count(_.isExpired)
      
      (allStocks.size, lowStockCount, outOfStockCount, expiredCount)
    }.recover {
      case NonFatal(e) =>
        println(s"获取库存统计失败: ${e.getMessage}")
        (0, 0, 0, 0)
    }
  }
  
  /**
   * 函数式获取库存统计 - 安全版本
   */
  def getStockStatisticsSafe(state: FoodStockManagerState): (Int, Int, Int, Int) = {
    getStockStatistics(state).getOrElse((0, 0, 0, 0))
  }
  
  /**
   * Get stock value summary (if costs were tracked)
   * For now, just return counts by category
   */
  def getStockSummaryByCategory: Map[FoodCategory, Int] = {
    items.values.groupBy(_.category).map { case (category, stocks) =>
      category -> stocks.size
    }.toMap
  }
  
  /**
   * 函数式按类别获取库存摘要
   * @param state 当前状态
   * @return 按类别分组的库存数量映射
   */
  def getStockSummaryByCategory(state: FoodStockManagerState): Try[Map[FoodCategory, Int]] = {
    Try {
      state.stocks.values.groupBy(_.category).map { case (category, stocks) =>
        category -> stocks.size
      }.toMap
    }.recover {
      case NonFatal(e) =>
        println(s"获取库存摘要失败: ${e.getMessage}")
        Map.empty
    }
  }
  
  /**
   * 函数式按类别获取库存摘要 - 安全版本
   */
  def getStockSummaryByCategorySafe(state: FoodStockManagerState): Map[FoodCategory, Int] = {
    getStockSummaryByCategory(state).getOrElse(Map.empty)
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
   * 函数式移除过期项目
   * @param state 当前状态
   * @param userId 执行清理的用户
   * @return (新状态, 移除的项目数)
   */
  def removeExpiredItems(state: FoodStockManagerState, userId: String): Try[(FoodStockManagerState, Int)] = {
    Try {
      val expiredItems = getExpiredItemsSafe(state)
      val updatedStocks = state.stocks.map { case (stockId, stock) =>
        if (expiredItems.contains(stock)) {
          val updatedStock = stock.copy()
          updatedStock.removeStock(updatedStock.currentQuantity, userId, "Expired item removal")
          stockId -> updatedStock
        } else {
          stockId -> stock
        }
      }
      val newState = state.copy(stocks = updatedStocks)
      (newState, expiredItems.size)
    }.recover {
      case NonFatal(e) =>
        println(s"移除过期项目失败: ${e.getMessage}")
        (state, 0)
    }
  }
  
  /**
   * 函数式移除过期项目 - 安全版本
   */
  def removeExpiredItemsSafe(state: FoodStockManagerState, userId: String): (FoodStockManagerState, Int) = {
    removeExpiredItems(state, userId).getOrElse((state, 0))
  }
  
  /**
   * Generate stock alerts
   * @return list of alert messages
   */
  def generateStockAlerts: List[String] = {
    val alerts = scala.collection.mutable.ListBuffer[String]()
    
    // Low stock alerts
    val lowStockItems = getLowStockItems
    if (lowStockItems.nonEmpty) {
      alerts += s"⚠️ ${lowStockItems.size} items are low in stock"
      lowStockItems.take(3).foreach { stock =>
        alerts += s"  • ${stock.foodName}: ${stock.currentQuantity} ${stock.unit} (min: ${stock.minimumThreshold})"
      }
    }
    
    // Out of stock alerts
    val outOfStockItems = getOutOfStockItems
    if (outOfStockItems.nonEmpty) {
      alerts += s"🚨 ${outOfStockItems.size} items are out of stock"
      outOfStockItems.take(3).foreach { stock =>
        alerts += s"  • ${stock.foodName}"
      }
    }
    
    // Expiring soon alerts
    val expiringSoonItems = getExpiringSoonItems()
    if (expiringSoonItems.nonEmpty) {
      alerts += s"⏰ ${expiringSoonItems.size} items are expiring soon"
      expiringSoonItems.take(3).foreach { stock =>
        val daysLeft = stock.getDaysUntilExpiry.getOrElse(0)
        alerts += s"  • ${stock.foodName}: ${daysLeft} days left"
      }
    }
    
    // Expired items alerts
    val expiredItems = getExpiredItems
    if (expiredItems.nonEmpty) {
      alerts += s"💀 ${expiredItems.size} items have expired"
      expiredItems.take(3).foreach { stock =>
        alerts += s"  • ${stock.foodName}: expired"
      }
    }
    
    alerts.toList
  }
  
  /**
   * 函数式生成库存警报
   * @param state 当前状态
   * @return 警报消息列表
   */
  def generateStockAlerts(state: FoodStockManagerState): Try[List[String]] = {
    Try {
      val alerts = scala.collection.mutable.ListBuffer[String]()
      
      // Low stock alerts
      val lowStockItems = getLowStockItemsSafe(state)
      if (lowStockItems.nonEmpty) {
        alerts += s"⚠️ ${lowStockItems.size} items are low in stock"
        lowStockItems.take(3).foreach { stock =>
          alerts += s"  • ${stock.foodName}: ${stock.currentQuantity} ${stock.unit} (min: ${stock.minimumThreshold})"
        }
      }
      
      // Out of stock alerts
      val outOfStockItems = getOutOfStockItemsSafe(state)
      if (outOfStockItems.nonEmpty) {
        alerts += s"🚨 ${outOfStockItems.size} items are out of stock"
        outOfStockItems.take(3).foreach { stock =>
          alerts += s"  • ${stock.foodName}"
        }
      }
      
      // Expiring soon alerts
      val expiringSoonItems = getExpiringSoonItemsSafe(state, 7)
      if (expiringSoonItems.nonEmpty) {
        alerts += s"⏰ ${expiringSoonItems.size} items are expiring soon"
        expiringSoonItems.take(3).foreach { stock =>
          val daysLeft = stock.getDaysUntilExpiry.getOrElse(0)
          alerts += s"  • ${stock.foodName}: ${daysLeft} days left"
        }
      }
      
      // Expired items alerts
      val expiredItems = getExpiredItemsSafe(state)
      if (expiredItems.nonEmpty) {
        alerts += s"💀 ${expiredItems.size} items have expired"
        expiredItems.take(3).foreach { stock =>
          alerts += s"  • ${stock.foodName}: expired"
        }
      }
      
      alerts.toList
    }.recover {
      case NonFatal(e) =>
        println(s"生成库存警报失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式生成库存警报 - 安全版本
   */
  def generateStockAlertsSafe(state: FoodStockManagerState): List[String] = {
    generateStockAlerts(state).getOrElse(List.empty)
  }
}
