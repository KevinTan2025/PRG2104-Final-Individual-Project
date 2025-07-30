package model

import java.time.LocalDateTime

/**
 * Enumeration for food post status
 */
enum FoodPostStatus:
  case PENDING, ACCEPTED, COMPLETED, CANCELLED

/**
 * Enumeration for food post types
 */
enum FoodPostType:
  case REQUEST, OFFER

/**
 * Case class representing a food sharing post
 * @param postId unique identifier for the post
 * @param authorId ID of the post author
 * @param title post title
 * @param description detailed description of the food/resource
 * @param postType whether this is a request or offer
 * @param quantity quantity of food/resource
 * @param location pickup/delivery location
 * @param expiryDate expiry date for the food (if applicable)
 */
case class FoodPost(
  postId: String,
  authorId: String,
  title: String,
  description: String,
  postType: FoodPostType,
  quantity: String,
  location: String,
  expiryDate: Option[LocalDateTime] = None,
  timestamp: LocalDateTime = LocalDateTime.now()
) extends Likeable with Moderatable {
  
  var status: FoodPostStatus = FoodPostStatus.PENDING
  var acceptedBy: Option[String] = None
  var acceptedDate: Option[LocalDateTime] = None
  var completedDate: Option[LocalDateTime] = None
  
  /**
   * Accept a food post (for requests) or respond to offer
   * @param userId ID of the user accepting/responding
   */
  def accept(userId: String): Unit = {
    if (status == FoodPostStatus.PENDING) {
      status = FoodPostStatus.ACCEPTED
      acceptedBy = Some(userId)
      acceptedDate = Some(LocalDateTime.now())
    }
  }
  
  /**
   * Mark the food post as completed
   */
  def complete(): Unit = {
    if (status == FoodPostStatus.ACCEPTED) {
      status = FoodPostStatus.COMPLETED
      completedDate = Some(LocalDateTime.now())
    }
  }
  
  /**
   * Cancel the food post
   */
  def cancel(): Unit = {
    status = FoodPostStatus.CANCELLED
  }
  
  /**
   * Check if the post is still active (pending or accepted)
   * @return true if active, false otherwise
   */
  def isActive: Boolean = {
    status == FoodPostStatus.PENDING || status == FoodPostStatus.ACCEPTED
  }
  
  /**
   * Check if the food has expired
   * @return true if expired, false otherwise
   */
  def isExpired: Boolean = {
    expiryDate.exists(_.isBefore(LocalDateTime.now()))
  }
}
