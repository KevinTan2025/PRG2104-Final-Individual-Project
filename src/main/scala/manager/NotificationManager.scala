package manager

import model._
import java.time.LocalDateTime
import scala.jdk.CollectionConverters._

/**
 * Manager class for handling notification operations
 */
class NotificationManager extends Manager[Notification] {
  
  /**
   * Send a notification
   * @param notification the notification to send
   */
  def sendNotification(notification: Notification): Unit = {
    add(notification.notificationId, notification)
  }
  
  /**
   * Get notifications for a user
   * @param userId the user ID
   * @return list of notifications for the user
   */
  def getNotificationsForUser(userId: String): List[Notification] = {
    items.values().asScala.filter(_.recipientId == userId).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get unread notifications for a user
   * @param userId the user ID
   * @return list of unread notifications for the user
   */
  def getUnreadNotifications(userId: String): List[Notification] = {
    items.values().asScala.filter(n => n.recipientId == userId && !n.isRead).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get notifications by type for a user
   * @param userId the user ID
   * @param notificationType the notification type
   * @return list of notifications of the specified type for the user
   */
  def getNotificationsByType(userId: String, notificationType: NotificationType): List[Notification] = {
    items.values().asScala.filter(n => n.recipientId == userId && n.notificationType == notificationType)
      .toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Mark notification as read
   * @param notificationId the notification ID
   * @return true if successful, false if notification not found
   */
  def markAsRead(notificationId: String): Boolean = {
    get(notificationId) match {
      case Some(notification) =>
        notification.markAsRead()
        true
      case None => false
    }
  }
  
  /**
   * Mark all notifications as read for a user
   * @param userId the user ID
   * @return number of notifications marked as read
   */
  def markAllAsRead(userId: String): Int = {
    val userNotifications = items.values().asScala.filter(_.recipientId == userId)
    val unreadCount = userNotifications.count(!_.isRead)
    userNotifications.foreach(_.markAsRead())
    unreadCount
  }
  
  /**
   * Get count of unread notifications for a user
   * @param userId the user ID
   * @return number of unread notifications
   */
  def getUnreadCount(userId: String): Int = {
    items.values().asScala.count(n => n.recipientId == userId && !n.isRead)
  }
  
  /**
   * Delete old notifications (older than specified days)
   * @param days number of days to keep notifications
   * @return number of notifications deleted
   */
  def cleanupOldNotifications(days: Int = 30): Int = {
    val cutoffDate = LocalDateTime.now().minusDays(days)
    val oldNotifications = items.asScala.filter(_._2.timestamp.isBefore(cutoffDate))
    oldNotifications.foreach { case (id, _) => remove(id) }
    oldNotifications.size
  }
  
  /**
   * Create and send a new message notification
   * @param recipientId recipient user ID
   * @param senderId sender user ID
   * @param title notification title
   * @param message notification message
   * @param relatedItemId optional related item ID
   * @return the created notification
   */
  def createMessageNotification(
    recipientId: String, 
    senderId: String, 
    title: String, 
    message: String, 
    relatedItemId: Option[String] = None
  ): Notification = {
    val notification = Notification(
      notificationId = java.util.UUID.randomUUID().toString,
      recipientId = recipientId,
      senderId = Some(senderId),
      title = title,
      message = message,
      notificationType = NotificationType.MESSAGE,
      relatedItemId = relatedItemId
    )
    sendNotification(notification)
    notification
  }
  
  /**
   * Create and send an event reminder notification
   * @param recipientId recipient user ID
   * @param eventId event ID
   * @param eventTitle event title
   * @param reminderTime time before event to send reminder
   * @return the created notification
   */
  def createEventReminderNotification(
    recipientId: String, 
    eventId: String, 
    eventTitle: String, 
    reminderTime: String
  ): Notification = {
    val notification = Notification(
      notificationId = java.util.UUID.randomUUID().toString,
      recipientId = recipientId,
      senderId = None,
      title = "Event Reminder",
      message = s"Reminder: Event '$eventTitle' starts $reminderTime",
      notificationType = NotificationType.EVENT_REMINDER,
      relatedItemId = Some(eventId)
    )
    sendNotification(notification)
    notification
  }
  
  /**
   * Create and send an announcement notification
   * @param recipientId recipient user ID
   * @param announcementId announcement ID
   * @param announcementTitle announcement title
   * @return the created notification
   */
  def createAnnouncementNotification(
    recipientId: String, 
    announcementId: String, 
    announcementTitle: String
  ): Notification = {
    val notification = Notification(
      notificationId = java.util.UUID.randomUUID().toString,
      recipientId = recipientId,
      senderId = None,
      title = "New Announcement",
      message = s"New announcement: $announcementTitle",
      notificationType = NotificationType.ANNOUNCEMENT,
      relatedItemId = Some(announcementId)
    )
    sendNotification(notification)
    notification
  }
  
  /**
   * Create and send a food request/offer notification
   * @param recipientId recipient user ID
   * @param senderId sender user ID
   * @param foodPostId food post ID
   * @param postTitle post title
   * @param isOffer true if offer, false if request
   * @return the created notification
   */
  def createFoodNotification(
    recipientId: String, 
    senderId: String, 
    foodPostId: String, 
    postTitle: String, 
    isOffer: Boolean
  ): Notification = {
    val notificationType = if (isOffer) NotificationType.FOOD_OFFER else NotificationType.FOOD_REQUEST
    val title = if (isOffer) "New Food Offer" else "New Food Request"
    val message = s"$title: $postTitle"
    
    val notification = Notification(
      notificationId = java.util.UUID.randomUUID().toString,
      recipientId = recipientId,
      senderId = Some(senderId),
      title = title,
      message = message,
      notificationType = notificationType,
      relatedItemId = Some(foodPostId)
    )
    sendNotification(notification)
    notification
  }
}
