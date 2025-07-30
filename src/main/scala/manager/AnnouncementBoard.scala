package manager

import model._
import java.time.LocalDateTime
import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal
import java.time.LocalDateTime

/**
 * 公告板管理器状态 - 不可变数据结构
 * @param announcements 公告映射
 */
case class AnnouncementBoardState(
  announcements: Map[String, Announcement] = Map.empty
)

/**
 * Manager class for handling announcement operations
 */
class AnnouncementBoard extends Manager[Announcement] {
  
  /**
   * Post a new announcement
   * @param announcement the announcement to post
   */
  def postAnnouncement(announcement: Announcement): Unit = {
    add(announcement.announcementId, announcement)
  }
  
  /**
   * Post a new announcement (functional version with Try)
   * @param state current state
   * @param announcement the announcement to post
   * @return Try containing updated state
   */
  def postAnnouncement(state: AnnouncementBoardState, announcement: Announcement): Try[AnnouncementBoardState] = {
    Try {
      val updatedAnnouncements = state.announcements + (announcement.announcementId -> announcement)
      state.copy(announcements = updatedAnnouncements)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to post announcement: ${e.getMessage}", e)
    }
  }
  
  /**
   * Post a new announcement (safe functional version)
   * @param state current state
   * @param announcement the announcement to post
   * @return updated state
   */
  def postAnnouncementSafe(state: AnnouncementBoardState, announcement: Announcement): AnnouncementBoardState = {
    postAnnouncement(state, announcement).getOrElse(state)
  }
  
  /**
   * Get announcements by type
   * @param announcementType the type to filter by
   * @return list of announcements of the specified type
   */
  def getAnnouncementsByType(announcementType: AnnouncementType): List[Announcement] = {
    items.values.filter(_.announcementType == announcementType).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get announcements by type (functional version with Try)
   * @param state current state
   * @param announcementType the type to filter by
   * @return Try containing list of announcements of the specified type
   */
  def getAnnouncementsByType(state: AnnouncementBoardState, announcementType: AnnouncementType): Try[List[Announcement]] = {
    Try {
      state.announcements.values.filter(_.announcementType == announcementType).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get announcements by type: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get announcements by type (safe functional version)
   * @param state current state
   * @param announcementType the type to filter by
   * @return list of announcements of the specified type or empty list if failed
   */
  def getAnnouncementsByTypeSafe(state: AnnouncementBoardState, announcementType: AnnouncementType): List[Announcement] = {
    getAnnouncementsByType(state, announcementType).getOrElse(List.empty)
  }
  
  /**
   * Get active announcements (not deactivated)
   * @return list of active announcements
   */
  def getActiveAnnouncements: List[Announcement] = {
    items.values.filter(_.isActive).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get active announcements (functional version with Try)
   * @param state current state
   * @return Try containing list of active announcements
   */
  def getActiveAnnouncements(state: AnnouncementBoardState): Try[List[Announcement]] = {
    Try {
      state.announcements.values.filter(_.isActive).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get active announcements: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get active announcements (safe functional version)
   * @param state current state
   * @return list of active announcements or empty list if failed
   */
  def getActiveAnnouncementsSafe(state: AnnouncementBoardState): List[Announcement] = {
    getActiveAnnouncements(state).getOrElse(List.empty)
  }
  
  /**
   * Get announcements by priority
   * @param priority the priority level to filter by
   * @return list of announcements with the specified priority
   */
  def getAnnouncementsByPriority(priority: Priority): List[Announcement] = {
    items.values.filter(_.priority == priority).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get announcements by priority (functional version with Try)
   * @param state current state
   * @param priority the priority level to filter by
   * @return Try containing list of announcements with the specified priority
   */
  def getAnnouncementsByPriority(state: AnnouncementBoardState, priority: Priority): Try[List[Announcement]] = {
    Try {
      state.announcements.values.filter(_.priority == priority).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get announcements by priority: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get announcements by priority (safe functional version)
   * @param state current state
   * @param priority the priority level to filter by
   * @return list of announcements with the specified priority or empty list if failed
   */
  def getAnnouncementsByPrioritySafe(state: AnnouncementBoardState, priority: Priority): List[Announcement] = {
    getAnnouncementsByPriority(state, priority).getOrElse(List.empty)
  }
  
  /**
   * Search announcements by title or content
   * @param searchTerm the term to search for
   * @return list of matching announcements
   */
  def searchAnnouncements(searchTerm: String): List[Announcement] = {
    val term = searchTerm.toLowerCase
    items.values.filter { announcement =>
      announcement.title.toLowerCase.contains(term) || 
      announcement.content.toLowerCase.contains(term)
    }.toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Search announcements by title or content (functional version with Try)
   * @param state current state
   * @param searchTerm the term to search for
   * @return Try containing list of matching announcements
   */
  def searchAnnouncements(state: AnnouncementBoardState, searchTerm: String): Try[List[Announcement]] = {
    Try {
      val term = searchTerm.toLowerCase
      state.announcements.values.filter { announcement =>
        announcement.title.toLowerCase.contains(term) || 
        announcement.content.toLowerCase.contains(term)
      }.toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to search announcements: ${e.getMessage}", e)
    }
  }
  
  /**
   * Search announcements by title or content (safe functional version)
   * @param state current state
   * @param searchTerm the term to search for
   * @return list of matching announcements or empty list if failed
   */
  def searchAnnouncementsSafe(state: AnnouncementBoardState, searchTerm: String): List[Announcement] = {
    searchAnnouncements(state, searchTerm).getOrElse(List.empty)
  }
  
  /**
   * Get announcements by author
   * @param authorId the author ID to filter by
   * @return list of announcements by the specified author
   */
  def getAnnouncementsByAuthor(authorId: String): List[Announcement] = {
    items.values.filter(_.authorId == authorId).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get announcements by author (functional version with Try)
   * @param state current state
   * @param authorId the author ID to filter by
   * @return Try containing list of announcements by the specified author
   */
  def getAnnouncementsByAuthor(state: AnnouncementBoardState, authorId: String): Try[List[Announcement]] = {
    Try {
      state.announcements.values.filter(_.authorId == authorId).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get announcements by author: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get announcements by author (safe functional version)
   * @param state current state
   * @param authorId the author ID to filter by
   * @return list of announcements by the specified author or empty list if failed
   */
  def getAnnouncementsByAuthorSafe(state: AnnouncementBoardState, authorId: String): List[Announcement] = {
    getAnnouncementsByAuthor(state, authorId).getOrElse(List.empty)
  }
  
  /**
   * Get recent announcements (within specified days)
   * @param days number of days to look back
   * @return list of recent announcements
   */
  def getRecentAnnouncements(days: Int = 7): List[Announcement] = {
    val cutoffDate = LocalDateTime.now().minusDays(days)
    items.values.filter(_.timestamp.isAfter(cutoffDate)).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get recent announcements (functional version with Try)
   * @param state current state
   * @param days number of days to look back
   * @return Try containing list of recent announcements
   */
  def getRecentAnnouncements(state: AnnouncementBoardState, days: Int): Try[List[Announcement]] = {
    Try {
      val cutoffDate = LocalDateTime.now().minusDays(days)
      state.announcements.values.filter(_.timestamp.isAfter(cutoffDate)).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get recent announcements: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get recent announcements (safe functional version)
   * @param state current state
   * @param days number of days to look back
   * @return list of recent announcements or empty list if failed
   */
  def getRecentAnnouncementsSafe(state: AnnouncementBoardState, days: Int): List[Announcement] = {
    getRecentAnnouncements(state, days).getOrElse(List.empty)
  }
  
  /**
   * Add comment to announcement
   * @param announcementId the announcement ID
   * @param comment the comment to add
   * @return true if successful, false if announcement not found
   */
  def addComment(announcementId: String, comment: Comment): Boolean = {
    get(announcementId) match {
      case Some(announcement) =>
        announcement.addComment(comment)
        true
      case None => false
    }
  }
  
  /**
   * Add comment to announcement (functional version with Try)
   * @param state current state
   * @param announcementId the announcement ID
   * @param comment the comment to add
   * @return Try containing updated state and success flag
   */
  def addComment(state: AnnouncementBoardState, announcementId: String, comment: Comment): Try[(AnnouncementBoardState, Boolean)] = {
    Try {
      state.announcements.get(announcementId) match {
        case Some(announcement) =>
          val updatedAnnouncement = announcement.copy()
          updatedAnnouncement.comments = announcement.comments :+ comment
          updatedAnnouncement.likes = announcement.likes
          updatedAnnouncement.isModerated = announcement.isModerated
          updatedAnnouncement.moderatedBy = announcement.moderatedBy
          updatedAnnouncement.moderationDate = announcement.moderationDate
          updatedAnnouncement.isActive = announcement.isActive
          updatedAnnouncement.priority = announcement.priority
          val updatedState = state.copy(announcements = state.announcements + (announcementId -> updatedAnnouncement))
          (updatedState, true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to add comment: ${e.getMessage}", e)
    }
  }
  
  /**
   * Add comment to announcement (safe functional version)
   * @param state current state
   * @param announcementId the announcement ID
   * @param comment the comment to add
   * @return updated state and success flag
   */
  def addCommentSafe(state: AnnouncementBoardState, announcementId: String, comment: Comment): (AnnouncementBoardState, Boolean) = {
    addComment(state, announcementId, comment).getOrElse((state, false))
  }
  
  /**
   * Add like to announcement
   * @param announcementId the announcement ID
   * @return true if successful, false if announcement not found
   */
  def addLike(announcementId: String): Boolean = {
    get(announcementId) match {
      case Some(announcement) =>
        announcement.addLike()
        true
      case None => false
    }
  }
  
  /**
   * Add like to announcement (functional version with Try)
   * @param state current state
   * @param announcementId the announcement ID
   * @return Try containing updated state and success flag
   */
  def addLike(state: AnnouncementBoardState, announcementId: String): Try[(AnnouncementBoardState, Boolean)] = {
    Try {
      state.announcements.get(announcementId) match {
        case Some(announcement) =>
          val updatedAnnouncement = announcement.copy()
           updatedAnnouncement.likes = announcement.likes + 1
           updatedAnnouncement.comments = announcement.comments
           updatedAnnouncement.isModerated = announcement.isModerated
           updatedAnnouncement.moderatedBy = announcement.moderatedBy
           updatedAnnouncement.moderationDate = announcement.moderationDate
           updatedAnnouncement.isActive = announcement.isActive
           updatedAnnouncement.priority = announcement.priority
          val updatedState = state.copy(announcements = state.announcements + (announcementId -> updatedAnnouncement))
          (updatedState, true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to add like: ${e.getMessage}", e)
    }
  }
  
  /**
   * Add like to announcement (safe functional version)
   * @param state current state
   * @param announcementId the announcement ID
   * @return updated state and success flag
   */
  def addLikeSafe(state: AnnouncementBoardState, announcementId: String): (AnnouncementBoardState, Boolean) = {
    addLike(state, announcementId).getOrElse((state, false))
  }
  
  /**
   * Moderate announcement (admin function)
   * @param announcementId the announcement ID
   * @param adminId the admin user ID
   * @return true if successful, false if announcement not found
   */
  def moderateAnnouncement(announcementId: String, adminId: String): Boolean = {
    get(announcementId) match {
      case Some(announcement) =>
        announcement.moderate(adminId)
        true
      case None => false
    }
  }
  
  /**
   * Moderate announcement (functional version with Try)
   * @param state current state
   * @param announcementId the announcement ID
   * @param adminId the admin user ID
   * @return Try containing updated state and success flag
   */
  def moderateAnnouncement(state: AnnouncementBoardState, announcementId: String, adminId: String): Try[(AnnouncementBoardState, Boolean)] = {
    Try {
      state.announcements.get(announcementId) match {
        case Some(announcement) =>
          val updatedAnnouncement = announcement.copy()
           updatedAnnouncement.likes = announcement.likes
           updatedAnnouncement.comments = announcement.comments
           updatedAnnouncement.isModerated = true
           updatedAnnouncement.moderatedBy = Some(adminId)
           updatedAnnouncement.moderationDate = Some(LocalDateTime.now())
           updatedAnnouncement.isActive = announcement.isActive
           updatedAnnouncement.priority = announcement.priority
          val updatedState = state.copy(announcements = state.announcements + (announcementId -> updatedAnnouncement))
          (updatedState, true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to moderate announcement: ${e.getMessage}", e)
    }
  }
  
  /**
   * Moderate announcement (safe functional version)
   * @param state current state
   * @param announcementId the announcement ID
   * @param adminId the admin user ID
   * @return updated state and success flag
   */
  def moderateAnnouncementSafe(state: AnnouncementBoardState, announcementId: String, adminId: String): (AnnouncementBoardState, Boolean) = {
    moderateAnnouncement(state, announcementId, adminId).getOrElse((state, false))
  }
  
  /**
   * Deactivate announcement
   * @param announcementId the announcement ID
   * @return true if successful, false if announcement not found
   */
  def deactivateAnnouncement(announcementId: String): Boolean = {
    get(announcementId) match {
      case Some(announcement) =>
        announcement.deactivate()
        true
      case None => false
    }
  }
  
  /**
   * Deactivate announcement (functional version with Try)
   * @param state current state
   * @param announcementId the announcement ID
   * @return Try containing updated state and success flag
   */
  def deactivateAnnouncement(state: AnnouncementBoardState, announcementId: String): Try[(AnnouncementBoardState, Boolean)] = {
    Try {
      state.announcements.get(announcementId) match {
        case Some(announcement) =>
          val updatedAnnouncement = announcement.copy()
           updatedAnnouncement.likes = announcement.likes
           updatedAnnouncement.comments = announcement.comments
           updatedAnnouncement.isModerated = announcement.isModerated
           updatedAnnouncement.moderatedBy = announcement.moderatedBy
           updatedAnnouncement.moderationDate = announcement.moderationDate
           updatedAnnouncement.isActive = false
           updatedAnnouncement.priority = announcement.priority
          val updatedState = state.copy(announcements = state.announcements + (announcementId -> updatedAnnouncement))
          (updatedState, true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to deactivate announcement: ${e.getMessage}", e)
    }
  }
  
  /**
   * Deactivate announcement (safe functional version)
   * @param state current state
   * @param announcementId the announcement ID
   * @return updated state and success flag
   */
  def deactivateAnnouncementSafe(state: AnnouncementBoardState, announcementId: String): (AnnouncementBoardState, Boolean) = {
    deactivateAnnouncement(state, announcementId).getOrElse((state, false))
  }
}
