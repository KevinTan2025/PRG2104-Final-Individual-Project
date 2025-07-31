package model

import java.time.LocalDateTime

/**
 * Enumeration for food category
 */
enum FoodCategory:
  case VEGETABLES, FRUITS, GRAINS, PROTEIN, DAIRY, BEVERAGES, SNACKS, PACKAGED_FOOD, FROZEN_FOOD, CANNED_FOOD, OTHER

/**
 * Enumeration for stock status
 */
enum StockStatus:
  case IN_STOCK, LOW_STOCK, OUT_OF_STOCK, EXPIRED

/**
 * Enumeration for stock action type
 */
enum StockActionType:
  case STOCK_IN, STOCK_OUT, EXPIRED_REMOVAL, ADJUSTMENT

/**
 * Immutable case class representing a food stock item
 * @param stockId unique identifier for the stock item
 * @param foodName name of the food item
 * @param category food category
 * @param currentQuantity current quantity in stock
 * @param unit unit of measurement (kg, pieces, bottles, etc.)
 * @param minimumThreshold minimum quantity before considered low stock
 * @param expiryDate expiry date of the food item
 * @param isPackaged whether the food is packaged
 * @param location storage location
 * @param lastModifiedBy ID of user who last modified this stock
 * @param lastModifiedDate when the stock was last modified
 * @param createdAt creation timestamp
 * @param stockHistory list of stock movements
 */
case class FoodStock(
  stockId: String,
  foodName: String,
  category: FoodCategory,
  currentQuantity: Double,
  unit: String,
  minimumThreshold: Double,
  expiryDate: Option[LocalDateTime] = None,
  isPackaged: Boolean = false,
  location: String = "Main Storage",
  lastModifiedBy: Option[String] = None,
  lastModifiedDate: LocalDateTime = LocalDateTime.now(),
  createdAt: LocalDateTime = LocalDateTime.now(),
  stockHistory: List[StockMovement] = List.empty
) {
  
  /**
   * Get current stock status
   */
  def stockStatus: StockStatus = {
    if (isExpired) StockStatus.EXPIRED
    else if (currentQuantity <= 0) StockStatus.OUT_OF_STOCK
    else if (currentQuantity <= minimumThreshold) StockStatus.LOW_STOCK
    else StockStatus.IN_STOCK
  }
  
  /**
   * Check if the food item is expired
   */
  def isExpired: Boolean = {
    expiryDate.exists(_.isBefore(LocalDateTime.now()))
  }
  
  /**
   * Check if the food item is expiring soon (within specified days)
   */
  def isExpiringSoon(days: Int = 7): Boolean = {
    expiryDate.exists(_.isBefore(LocalDateTime.now().plusDays(days)))
  }
  
  /**
   * Add stock quantity
   * @return updated FoodStock with increased quantity and movement record
   */
  def addStock(quantity: Double, userId: String, notes: String = ""): FoodStock = {
    val oldQuantity = currentQuantity
    val newQuantity = currentQuantity + quantity
    val now = LocalDateTime.now()
    
    // Record the movement
    val movement = StockMovement(
      movementId = java.util.UUID.randomUUID().toString,
      stockId = stockId,
      actionType = StockActionType.STOCK_IN,
      quantity = quantity,
      previousQuantity = oldQuantity,
      newQuantity = newQuantity,
      userId = userId,
      notes = notes
    )
    
    copy(
      currentQuantity = newQuantity,
      lastModifiedBy = Some(userId),
      lastModifiedDate = now,
      stockHistory = movement :: stockHistory
    )
  }
  
  /**
   * Remove stock quantity
   * @return (updated FoodStock, success flag) - None if insufficient stock
   */
  def removeStock(quantity: Double, userId: String, notes: String = ""): Option[FoodStock] = {
    if (currentQuantity >= quantity) {
      val oldQuantity = currentQuantity
      val newQuantity = currentQuantity - quantity
      val now = LocalDateTime.now()
      
      // Record the movement
      val movement = StockMovement(
        movementId = java.util.UUID.randomUUID().toString,
        stockId = stockId,
        actionType = StockActionType.STOCK_OUT,
        quantity = quantity,
        previousQuantity = oldQuantity,
        newQuantity = newQuantity,
        userId = userId,
        notes = notes
      )
      
      Some(copy(
        currentQuantity = newQuantity,
        lastModifiedBy = Some(userId),
        lastModifiedDate = now,
        stockHistory = movement :: stockHistory
      ))
    } else {
      None // Insufficient stock
    }
  }
  
  /**
   * Adjust stock quantity (can be positive or negative)
   * @return updated FoodStock with adjusted quantity and movement record
   */
  def adjustStock(newQuantity: Double, userId: String, notes: String = ""): FoodStock = {
    val oldQuantity = currentQuantity
    val adjustedQuantity = math.max(0, newQuantity)
    val now = LocalDateTime.now()
    
    // Record the movement
    val movement = StockMovement(
      movementId = java.util.UUID.randomUUID().toString,
      stockId = stockId,
      actionType = StockActionType.ADJUSTMENT,
      quantity = adjustedQuantity - oldQuantity,
      previousQuantity = oldQuantity,
      newQuantity = adjustedQuantity,
      userId = userId,
      notes = notes
    )
    
    copy(
      currentQuantity = adjustedQuantity,
      lastModifiedBy = Some(userId),
      lastModifiedDate = now,
      stockHistory = movement :: stockHistory
    )
  }
  
  /**
   * Get days until expiry
   */
  def daysUntilExpiry: Option[Int] = {
    expiryDate.map { expiry =>
      val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiry)
      days.toInt
    }
  }
}

/**
 * Case class representing a stock movement record
 * @param movementId unique identifier for the movement
 * @param stockId ID of the stock item
 * @param actionType type of action performed
 * @param quantity quantity moved (positive for in, negative for out)
 * @param previousQuantity quantity before the movement
 * @param newQuantity quantity after the movement
 * @param userId ID of user who performed the action
 * @param notes additional notes about the movement
 * @param timestamp when the movement occurred
 */
case class StockMovement(
  movementId: String,
  stockId: String,
  actionType: StockActionType,
  quantity: Double,
  previousQuantity: Double,
  newQuantity: Double,
  userId: String,
  notes: String = "",
  timestamp: LocalDateTime = LocalDateTime.now()
) {
  
  /**
   * Get formatted movement description
   */
  def description: String = {
    val action = actionType match {
      case StockActionType.STOCK_IN => "Added"
      case StockActionType.STOCK_OUT => "Removed"
      case StockActionType.EXPIRED_REMOVAL => "Expired Removal"
      case StockActionType.ADJUSTMENT => "Adjusted"
    }
    
    val quantityStr = if (quantity >= 0) s"+$quantity" else quantity.toString
    s"$action $quantityStr (${previousQuantity} → ${newQuantity})"
  }
}
