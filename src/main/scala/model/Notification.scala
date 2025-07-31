package model

import java.time.LocalDateTime

/**
 * Enumeration for notification types
 */
enum NotificationType:
  case MESSAGE, EVENT_REMINDER, ANNOUNCEMENT, FOOD_REQUEST, FOOD_OFFER, 
       COMMENT, LIKE, RSVP_CONFIRMATION, SYSTEM

/**
 * Immutable case class representing a notification
 * @param notificationId unique identifier for the notification
 * @param recipientId ID of the user receiving the notification
 * @param senderId ID of the user sending the notification (optional for system notifications)
 * @param title notification title
 * @param message notification message
 * @param notificationType type of notification
 * @param relatedItemId ID of the related item (post, event, etc.)
 * @param timestamp when the notification was created
 * @param isRead whether the notification has been read
 * @param readAt when the notification was read
 */
case class Notification(
  notificationId: String,
  recipientId: String,
  senderId: Option[String],
  title: String,
  message: String,
  notificationType: NotificationType,
  relatedItemId: Option[String] = None,
  timestamp: LocalDateTime = LocalDateTime.now(),
  isRead: Boolean = false,
  readAt: Option[LocalDateTime] = None
) {
  
  /**
   * Mark the notification as read
   */
  def markAsRead: Notification = {
    this.copy(
      isRead = true,
      readAt = Some(LocalDateTime.now())
    )
  }
  
  /**
   * Mark the notification as unread
   */
  def markAsUnread: Notification = {
    this.copy(
      isRead = false,
      readAt = None
    )
  }
  
  /**
   * Check if the notification is recent (within last 24 hours)
   * @return true if recent, false otherwise
   */
  def isRecent: Boolean = {
    timestamp.isAfter(LocalDateTime.now().minusDays(1))
  }
}
