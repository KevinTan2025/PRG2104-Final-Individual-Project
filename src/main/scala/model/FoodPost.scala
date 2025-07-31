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
 * Immutable case class representing a food sharing post
 * @param postId unique identifier for the post
 * @param authorId ID of the post author
 * @param title post title
 * @param description detailed description of the food/resource
 * @param postType whether this is a request or offer
 * @param quantity quantity of food/resource
 * @param location pickup/delivery location
 * @param expiryDate expiry date for the food (if applicable)
 * @param timestamp creation timestamp
 * @param status current status of the post
 * @param acceptedBy user who accepted the post
 * @param acceptedDate when the post was accepted
 * @param completedDate when the post was completed
 * @param likes number of likes
 * @param comments list of comments
 * @param isModerated moderation status
 * @param moderatedBy moderator ID
 * @param moderationDate moderation timestamp
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
  timestamp: LocalDateTime = LocalDateTime.now(),
  status: FoodPostStatus = FoodPostStatus.PENDING,
  acceptedBy: Option[String] = None,
  acceptedDate: Option[LocalDateTime] = None,
  completedDate: Option[LocalDateTime] = None,
  likes: Int = 0,
  comments: List[Comment] = List.empty,
  isModerated: Boolean = false,
  moderatedBy: Option[String] = None,
  moderationDate: Option[LocalDateTime] = None
) extends Likeable with Moderatable {
  
  // Likeable trait implementation
  def withLike: FoodPost = copy(likes = likes + 1)
  def withoutLike: FoodPost = copy(likes = if (likes > 0) likes - 1 else 0)
  def withComment(comment: Comment): FoodPost = copy(comments = comment :: comments)
  
  // Moderatable trait implementation
  def withModeration(adminId: String): FoodPost = copy(
    isModerated = true,
    moderatedBy = Some(adminId),
    moderationDate = Some(LocalDateTime.now())
  )
  
  /**
   * Accept a food post (for requests) or respond to offer
   * @param userId ID of the user accepting/responding
   * @return new FoodPost with accepted status if valid, otherwise unchanged
   */
  def accept(userId: String): FoodPost = {
    if (status == FoodPostStatus.PENDING) {
      copy(
        status = FoodPostStatus.ACCEPTED,
        acceptedBy = Some(userId),
        acceptedDate = Some(LocalDateTime.now())
      )
    } else this
  }
  
  /**
   * Mark the food post as completed
   * @return new FoodPost with completed status if valid, otherwise unchanged
   */
  def complete: FoodPost = {
    if (status == FoodPostStatus.ACCEPTED) {
      copy(
        status = FoodPostStatus.COMPLETED,
        completedDate = Some(LocalDateTime.now())
      )
    } else this
  }
  
  /**
   * Cancel the food post
   * @return new FoodPost with cancelled status
   */
  def cancel: FoodPost = copy(status = FoodPostStatus.CANCELLED)
  
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
