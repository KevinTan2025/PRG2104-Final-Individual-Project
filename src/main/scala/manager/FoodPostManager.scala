package manager

import model._
import java.time.LocalDateTime
import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal

/**
 * 不可变的食物分享管理器状态
 * @param posts 食物帖子映射
 */
case class FoodPostManagerState(posts: Map[String, FoodPost] = Map.empty)

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
   * 函数式创建食物帖子
   * @param state 当前状态
   * @param foodPost 食物帖子
   * @return 新状态
   */
  def createFoodPost(state: FoodPostManagerState, foodPost: FoodPost): Try[FoodPostManagerState] = {
    Try {
      val updatedPosts = state.posts + (foodPost.postId -> foodPost)
      state.copy(posts = updatedPosts)
    }.recover {
      case NonFatal(e) =>
        println(s"创建食物帖子失败: ${e.getMessage}")
        state
    }
  }
  
  /**
   * 函数式创建食物帖子 - 安全版本
   */
  def createFoodPostSafe(state: FoodPostManagerState, foodPost: FoodPost): FoodPostManagerState = {
    createFoodPost(state, foodPost).getOrElse(state)
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
   * 函数式按类型获取食物帖子
   * @param state 当前状态
   * @param postType 帖子类型
   * @return 指定类型的食物帖子列表
   */
  def getFoodPostsByType(state: FoodPostManagerState, postType: FoodPostType): Try[List[FoodPost]] = {
    Try {
      state.posts.values.filter(_.postType == postType).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) =>
        println(s"按类型获取食物帖子失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按类型获取食物帖子 - 安全版本
   */
  def getFoodPostsByTypeSafe(state: FoodPostManagerState, postType: FoodPostType): List[FoodPost] = {
    getFoodPostsByType(state, postType).getOrElse(List.empty)
  }
  
  /**
   * Get active food posts (pending or accepted)
   * @return list of active food posts
   */
  def getActiveFoodPosts: List[FoodPost] = {
    items.values.filter(_.isActive).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * 函数式获取活跃食物帖子
   * @param state 当前状态
   * @return 活跃食物帖子列表
   */
  def getActiveFoodPosts(state: FoodPostManagerState): Try[List[FoodPost]] = {
    Try {
      state.posts.values.filter(_.isActive).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) =>
        println(s"获取活跃食物帖子失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式获取活跃食物帖子 - 安全版本
   */
  def getActiveFoodPostsSafe(state: FoodPostManagerState): List[FoodPost] = {
    getActiveFoodPosts(state).getOrElse(List.empty)
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
   * 函数式按状态获取食物帖子
   * @param state 当前状态
   * @param status 帖子状态
   * @return 指定状态的食物帖子列表
   */
  def getFoodPostsByStatus(state: FoodPostManagerState, status: FoodPostStatus): Try[List[FoodPost]] = {
    Try {
      state.posts.values.filter(_.status == status).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) =>
        println(s"按状态获取食物帖子失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按状态获取食物帖子 - 安全版本
   */
  def getFoodPostsByStatusSafe(state: FoodPostManagerState, status: FoodPostStatus): List[FoodPost] = {
    getFoodPostsByStatus(state, status).getOrElse(List.empty)
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
   * 函数式按作者获取食物帖子
   * @param state 当前状态
   * @param authorId 作者ID
   * @return 指定作者的食物帖子列表
   */
  def getFoodPostsByAuthor(state: FoodPostManagerState, authorId: String): Try[List[FoodPost]] = {
    Try {
      state.posts.values.filter(_.authorId == authorId).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) =>
        println(s"按作者获取食物帖子失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按作者获取食物帖子 - 安全版本
   */
  def getFoodPostsByAuthorSafe(state: FoodPostManagerState, authorId: String): List[FoodPost] = {
    getFoodPostsByAuthor(state, authorId).getOrElse(List.empty)
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
   * 函数式搜索食物帖子
   * @param state 当前状态
   * @param searchTerm 搜索词
   * @return 匹配的食物帖子列表
   */
  def searchFoodPosts(state: FoodPostManagerState, searchTerm: String): Try[List[FoodPost]] = {
    Try {
      val term = searchTerm.toLowerCase
      state.posts.values.filter { post =>
        post.title.toLowerCase.contains(term) || 
        post.description.toLowerCase.contains(term) ||
        post.location.toLowerCase.contains(term)
      }.toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) =>
        println(s"搜索食物帖子失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式搜索食物帖子 - 安全版本
   */
  def searchFoodPostsSafe(state: FoodPostManagerState, searchTerm: String): List[FoodPost] = {
    searchFoodPosts(state, searchTerm).getOrElse(List.empty)
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
   * 函数式按位置获取食物帖子
   * @param state 当前状态
   * @param location 位置
   * @return 指定位置的食物帖子列表
   */
  def getFoodPostsByLocation(state: FoodPostManagerState, location: String): Try[List[FoodPost]] = {
    Try {
      state.posts.values.filter(_.location.toLowerCase.contains(location.toLowerCase)).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) =>
        println(s"按位置获取食物帖子失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按位置获取食物帖子 - 安全版本
   */
  def getFoodPostsByLocationSafe(state: FoodPostManagerState, location: String): List[FoodPost] = {
    getFoodPostsByLocation(state, location).getOrElse(List.empty)
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
   * 函数式获取即将过期的食物帖子
   * @param state 当前状态
   * @param hours 小时数
   * @return 即将过期的食物帖子列表
   */
  def getExpiringSoon(state: FoodPostManagerState, hours: Int): Try[List[FoodPost]] = {
    Try {
      val cutoffTime = LocalDateTime.now().plusHours(hours)
      state.posts.values.filter { post =>
        post.expiryDate.exists(_.isBefore(cutoffTime)) && post.isActive
      }.toList.sortBy(_.expiryDate)
    }.recover {
      case NonFatal(e) =>
        println(s"获取即将过期的食物帖子失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式获取即将过期的食物帖子 - 安全版本
   */
  def getExpiringSoonSafe(state: FoodPostManagerState, hours: Int = 24): List[FoodPost] = {
    getExpiringSoon(state, hours).getOrElse(List.empty)
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
   * 函数式接受食物帖子
   * @param state 当前状态
   * @param postId 帖子ID
   * @param userId 用户ID
   * @return (新状态, 是否成功)
   */
  def acceptFoodPost(state: FoodPostManagerState, postId: String, userId: String): Try[(FoodPostManagerState, Boolean)] = {
    Try {
      state.posts.get(postId) match {
        case Some(post) if post.status == FoodPostStatus.PENDING =>
          post.accept(userId)
          val updatedPosts = state.posts + (postId -> post)
          (state.copy(posts = updatedPosts), true)
        case _ =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"接受食物帖子失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式接受食物帖子 - 安全版本
   */
  def acceptFoodPostSafe(state: FoodPostManagerState, postId: String, userId: String): (FoodPostManagerState, Boolean) = {
    acceptFoodPost(state, postId, userId).getOrElse((state, false))
  }
  
  /**
   * Complete a food post
   * @param postId the post ID
   * @return true if successful, false if post not found or not in accepted state
   */
  def completeFoodPost(postId: String): Boolean = {
    get(postId) match {
      case Some(post) if post.status == FoodPostStatus.ACCEPTED =>
        post.complete()
        true
      case _ => false
    }
  }
  
  /**
   * 函数式完成食物帖子
   * @param state 当前状态
   * @param postId 帖子ID
   * @return (新状态, 是否成功)
   */
  def completeFoodPost(state: FoodPostManagerState, postId: String): Try[(FoodPostManagerState, Boolean)] = {
    Try {
      state.posts.get(postId) match {
        case Some(post) if post.status == FoodPostStatus.ACCEPTED =>
          post.complete()
          val updatedPosts = state.posts + (postId -> post)
          (state.copy(posts = updatedPosts), true)
        case _ =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"完成食物帖子失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式完成食物帖子 - 安全版本
   */
  def completeFoodPostSafe(state: FoodPostManagerState, postId: String): (FoodPostManagerState, Boolean) = {
    completeFoodPost(state, postId).getOrElse((state, false))
  }
  
  /**
   * Cancel a food post
   * @param postId the post ID
   * @return true if successful, false if post not found
   */
  def cancelFoodPost(postId: String): Boolean = {
    get(postId) match {
      case Some(post) =>
        post.cancel()
        true
      case None => false
    }
  }
  
  /**
   * 函数式取消食物帖子
   * @param state 当前状态
   * @param postId 帖子ID
   * @return (新状态, 是否成功)
   */
  def cancelFoodPost(state: FoodPostManagerState, postId: String): Try[(FoodPostManagerState, Boolean)] = {
    Try {
      state.posts.get(postId) match {
        case Some(post) =>
          post.cancel()
          val updatedPosts = state.posts + (postId -> post)
          (state.copy(posts = updatedPosts), true)
        case None =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"取消食物帖子失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式取消食物帖子 - 安全版本
   */
  def cancelFoodPostSafe(state: FoodPostManagerState, postId: String): (FoodPostManagerState, Boolean) = {
    cancelFoodPost(state, postId).getOrElse((state, false))
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
   * 函数式添加评论
   * @param state 当前状态
   * @param postId 帖子ID
   * @param comment 评论
   * @return (新状态, 是否成功)
   */
  def addComment(state: FoodPostManagerState, postId: String, comment: Comment): Try[(FoodPostManagerState, Boolean)] = {
    Try {
      state.posts.get(postId) match {
        case Some(post) =>
          post.addComment(comment)
          val updatedPosts = state.posts + (postId -> post)
          (state.copy(posts = updatedPosts), true)
        case None =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"添加评论失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式添加评论 - 安全版本
   */
  def addCommentSafe(state: FoodPostManagerState, postId: String, comment: Comment): (FoodPostManagerState, Boolean) = {
    addComment(state, postId, comment).getOrElse((state, false))
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
   * 函数式添加点赞
   * @param state 当前状态
   * @param postId 帖子ID
   * @return (新状态, 是否成功)
   */
  def addLike(state: FoodPostManagerState, postId: String): Try[(FoodPostManagerState, Boolean)] = {
    Try {
      state.posts.get(postId) match {
        case Some(post) =>
          post.addLike()
          val updatedPosts = state.posts + (postId -> post)
          (state.copy(posts = updatedPosts), true)
        case None =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"添加点赞失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式添加点赞 - 安全版本
   */
  def addLikeSafe(state: FoodPostManagerState, postId: String): (FoodPostManagerState, Boolean) = {
    addLike(state, postId).getOrElse((state, false))
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
  
  /**
   * 函数式获取食物分享统计
   * @param state 当前状态
   * @return (总帖子数, 活跃帖子数, 已完成帖子数, 总帮助数)
   */
  def getStatistics(state: FoodPostManagerState): Try[(Int, Int, Int, Int)] = {
    Try {
      val allPosts = state.posts.values.toList
      val activePosts = allPosts.count(_.isActive)
      val completedPosts = allPosts.count(_.status == FoodPostStatus.COMPLETED)
      val totalHelped = allPosts.count(_.status == FoodPostStatus.COMPLETED)
      
      (allPosts.size, activePosts, completedPosts, totalHelped)
    }.recover {
      case NonFatal(e) =>
        println(s"获取食物分享统计失败: ${e.getMessage}")
        (0, 0, 0, 0)
    }
  }
  
  /**
   * 函数式获取食物分享统计 - 安全版本
   */
  def getStatisticsSafe(state: FoodPostManagerState): (Int, Int, Int, Int) = {
    getStatistics(state).getOrElse((0, 0, 0, 0))
  }
}
