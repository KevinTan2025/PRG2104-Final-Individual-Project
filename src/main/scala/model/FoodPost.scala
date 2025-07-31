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
 * @param timestamp when the post was created
 * @param status current status of the post
 * @param acceptedBy ID of user who accepted the post
 * @param acceptedDate when the post was accepted
 * @param completedDate when the post was completed
 * @param likes number of likes
 * @param comments list of comments
 * @param isModerated whether the post is moderated
 * @param moderatedBy ID of the moderator
 * @param moderationDate when the post was moderated
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
  
  /**
   * Accept a food post (for requests) or respond to offer
   * @param userId ID of the user accepting/responding
   */
  def accept(userId: String): FoodPost = {
    if (status == FoodPostStatus.PENDING) {
      this.copy(
        status = FoodPostStatus.ACCEPTED,
        acceptedBy = Some(userId),
        acceptedDate = Some(LocalDateTime.now())
      )
    } else {
      this
    }
  }
  
  /**
   * Mark the food post as completed
   */
  def complete(): FoodPost = {
    if (status == FoodPostStatus.ACCEPTED) {
      this.copy(
        status = FoodPostStatus.COMPLETED,
        completedDate = Some(LocalDateTime.now())
      )
    } else {
      this
    }
  }
  
  /**
   * Cancel the food post
   */
  def cancel(): FoodPost = {
    this.copy(status = FoodPostStatus.CANCELLED)
  }
  
  /**
   * Implement Likeable trait methods
   */
  def addLike(): FoodPost = {
    this.copy(likes = likes + 1)
  }
  
  def removeLike(): FoodPost = {
    this.copy(likes = if (likes > 0) likes - 1 else 0)
  }
  
  def addComment(comment: Comment): FoodPost = {
    this.copy(comments = comment :: comments)
  }
  
  /**
   * Implement Moderatable trait method
   */
  def moderate(adminId: String): FoodPost = {
    this.copy(
      isModerated = true,
      moderatedBy = Some(adminId),
      moderationDate = Some(LocalDateTime.now())
    )
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
