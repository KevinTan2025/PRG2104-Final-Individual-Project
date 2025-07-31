package manager

import model._
import java.time.LocalDateTime
import scala.jdk.CollectionConverters._

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
   * Get announcements by type
   * @param announcementType the type to filter by
   * @return list of announcements of the specified type
   */
  def getAnnouncementsByType(announcementType: AnnouncementType): List[Announcement] = {
    items.values().asScala.filter(_.announcementType == announcementType).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get active announcements (not deactivated)
   * @return list of active announcements
   */
  def getActiveAnnouncements: List[Announcement] = {
    items.values().asScala.filter(_.isActive).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get announcements by priority
   * @param priority the priority level to filter by
   * @return list of announcements with the specified priority
   */
  def getAnnouncementsByPriority(priority: Priority): List[Announcement] = {
    items.values().asScala.filter(_.priority == priority).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Search announcements by title or content
   * @param searchTerm the term to search for
   * @return list of matching announcements
   */
  def searchAnnouncements(searchTerm: String): List[Announcement] = {
    val term = searchTerm.toLowerCase
    items.values().asScala.filter { announcement =>
      announcement.title.toLowerCase.contains(term) || 
      announcement.content.toLowerCase.contains(term)
    }.toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get announcements by author
   * @param authorId the author ID to filter by
   * @return list of announcements by the specified author
   */
  def getAnnouncementsByAuthor(authorId: String): List[Announcement] = {
    items.values().asScala.filter(_.authorId == authorId).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get recent announcements (within specified days)
   * @param days number of days to look back
   * @return list of recent announcements
   */
  def getRecentAnnouncements(days: Int = 7): List[Announcement] = {
    val cutoffDate = LocalDateTime.now().minusDays(days)
    items.values().asScala.filter(_.timestamp.isAfter(cutoffDate)).toList.sortBy(_.timestamp).reverse
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
}
