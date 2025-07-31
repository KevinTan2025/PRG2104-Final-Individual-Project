package manager

import model._
import java.time.LocalDateTime

/**
 * Manager class for handling food sharing operations
 */
class FoodPostManager extends Manager[FoodPost] {
  
  /**
   * Create a new food post
   * @param foodPost the food post to create
   */
  def createFoodPost(foodPost: FoodPost): Unit = {
    add(foodPost.postId, foodPost)
  }
  
  /**
   * Get food posts by type (REQUEST or OFFER)
   * @param postType the type to filter by
   * @return list of food posts of the specified type
   */
  def getFoodPostsByType(postType: FoodPostType): List[FoodPost] = {
    items.values.filter(_.postType == postType).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get active food posts (pending or accepted)
   * @return list of active food posts
   */
  def getActiveFoodPosts: List[FoodPost] = {
    items.values.filter(_.isActive).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get food posts by status
   * @param status the status to filter by
   * @return list of food posts with the specified status
   */
  def getFoodPostsByStatus(status: FoodPostStatus): List[FoodPost] = {
    items.values.filter(_.status == status).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get food posts by author
   * @param authorId the author ID to filter by
   * @return list of food posts by the specified author
   */
  def getFoodPostsByAuthor(authorId: String): List[FoodPost] = {
    items.values.filter(_.authorId == authorId).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Search food posts by title or description
   * @param searchTerm the term to search for
   * @return list of matching food posts
   */
  def searchFoodPosts(searchTerm: String): List[FoodPost] = {
    val term = searchTerm.toLowerCase
    items.values.filter { post =>
      post.title.toLowerCase.contains(term) || 
      post.description.toLowerCase.contains(term) ||
      post.location.toLowerCase.contains(term)
    }.toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get food posts by location
   * @param location the location to filter by
   * @return list of food posts in the specified location
   */
  def getFoodPostsByLocation(location: String): List[FoodPost] = {
    items.values.filter(_.location.toLowerCase.contains(location.toLowerCase)).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get food posts that are expiring soon (within specified hours)
   * @param hours number of hours to look ahead
   * @return list of food posts expiring soon
   */
  def getExpiringSoon(hours: Int = 24): List[FoodPost] = {
    val cutoffTime = LocalDateTime.now().plusHours(hours)
    items.values.filter { post =>
      post.expiryDate.exists(_.isBefore(cutoffTime)) && post.isActive
    }.toList.sortBy(_.expiryDate)
  }
  
  /**
   * Accept a food post
   * @param postId the post ID
   * @param userId the user ID accepting the post
   * @return true if successful, false if post not found or already accepted
   */
  def acceptFoodPost(postId: String, userId: String): Boolean = {
    get(postId) match {
      case Some(post) if post.status == FoodPostStatus.PENDING =>
        post.accept(userId)
        true
      case _ => false
    }
  }
  
  /**
   * Complete a food post
   * @param postId the post ID
   * @return true if successful, false if post not found or not in accepted state
   */
  def completeFoodPost(postId: String): Boolean = {
    get(postId) match {
      case Some(post) if post.status == FoodPostStatus.ACCEPTED =>
        val updatedPost = post.complete()
        add(post.postId, updatedPost)
        true
      case _ => false
    }
  }
  
  /**
   * Cancel a food post
   * @param postId the post ID
   * @return true if successful, false if post not found
   */
  def cancelFoodPost(postId: String): Boolean = {
    get(postId) match {
      case Some(post) =>
        val updatedPost = post.cancel()
        add(post.postId, updatedPost)
        true
      case None => false
    }
  }
  
  /**
   * Add comment to food post
   * @param postId the post ID
   * @param comment the comment to add
   * @return true if successful, false if post not found
   */
  def addComment(postId: String, comment: Comment): Boolean = {
    get(postId) match {
      case Some(post) =>
        post.addComment(comment)
        true
      case None => false
    }
  }
  
  /**
   * Add like to food post
   * @param postId the post ID
   * @return true if successful, false if post not found
   */
  def addLike(postId: String): Boolean = {
    get(postId) match {
      case Some(post) =>
        post.addLike()
        true
      case None => false
    }
  }
  
  /**
   * Get statistics for food sharing
   * @return tuple of (total posts, active posts, completed posts, total helped)
   */
  def getStatistics: (Int, Int, Int, Int) = {
    val allPosts = items.values.toList
    val activePosts = allPosts.count(_.isActive)
    val completedPosts = allPosts.count(_.status == FoodPostStatus.COMPLETED)
    val totalHelped = allPosts.count(_.status == FoodPostStatus.COMPLETED)
    
    (allPosts.size, activePosts, completedPosts, totalHelped)
  }
}
