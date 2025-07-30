package manager

import model._
import java.time.LocalDateTime
import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal

/**
 * Immutable state for NotificationManager
 * @param notifications map of notification ID to Notification
 */
case class NotificationManagerState(notifications: Map[String, Notification] = Map.empty)

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
   * Send a notification (functional version with Try)
   * @param state current state
   * @param notification the notification to send
   * @return Try containing new state
   */
  def sendNotification(state: NotificationManagerState, notification: Notification): Try[NotificationManagerState] = {
    Try {
      state.copy(notifications = state.notifications + (notification.notificationId -> notification))
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to send notification: ${e.getMessage}", e)
    }
  }
  
  /**
   * Send a notification (safe functional version)
   * @param state current state
   * @param notification the notification to send
   * @return new state or original state if failed
   */
  def sendNotificationSafe(state: NotificationManagerState, notification: Notification): NotificationManagerState = {
    sendNotification(state, notification).getOrElse(state)
  }
  
  /**
   * Get notifications for a user
   * @param userId the user ID
   * @return list of notifications for the user
   */
  def getNotificationsForUser(userId: String): List[Notification] = {
    items.values.filter(_.recipientId == userId).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get notifications for a user (functional version with Try)
   * @param state current state
   * @param userId the user ID
   * @return Try containing list of notifications for the user
   */
  def getNotificationsForUser(state: NotificationManagerState, userId: String): Try[List[Notification]] = {
    Try {
      state.notifications.values.filter(_.recipientId == userId).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get notifications for user $userId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get notifications for a user (safe functional version)
   * @param state current state
   * @param userId the user ID
   * @return list of notifications for the user or empty list if failed
   */
  def getNotificationsForUserSafe(state: NotificationManagerState, userId: String): List[Notification] = {
    getNotificationsForUser(state, userId).getOrElse(List.empty)
  }
  
  /**
   * Get unread notifications for a user
   * @param userId the user ID
   * @return list of unread notifications for the user
   */
  def getUnreadNotifications(userId: String): List[Notification] = {
    items.values.filter(n => n.recipientId == userId && !n.isRead).toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get unread notifications for a user (functional version with Try)
   * @param state current state
   * @param userId the user ID
   * @return Try containing list of unread notifications for the user
   */
  def getUnreadNotifications(state: NotificationManagerState, userId: String): Try[List[Notification]] = {
    Try {
      state.notifications.values.filter(n => n.recipientId == userId && !n.isRead).toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get unread notifications for user $userId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get unread notifications for a user (safe functional version)
   * @param state current state
   * @param userId the user ID
   * @return list of unread notifications for the user or empty list if failed
   */
  def getUnreadNotificationsSafe(state: NotificationManagerState, userId: String): List[Notification] = {
    getUnreadNotifications(state, userId).getOrElse(List.empty)
  }
  
  /**
   * Get notifications by type for a user
   * @param userId the user ID
   * @param notificationType the notification type
   * @return list of notifications of the specified type for the user
   */
  def getNotificationsByType(userId: String, notificationType: NotificationType): List[Notification] = {
    items.values.filter(n => n.recipientId == userId && n.notificationType == notificationType)
      .toList.sortBy(_.timestamp).reverse
  }
  
  /**
   * Get notifications by type for a user (functional version with Try)
   * @param state current state
   * @param userId the user ID
   * @param notificationType the notification type
   * @return Try containing list of notifications of the specified type for the user
   */
  def getNotificationsByType(state: NotificationManagerState, userId: String, notificationType: NotificationType): Try[List[Notification]] = {
    Try {
      state.notifications.values.filter(n => n.recipientId == userId && n.notificationType == notificationType)
        .toList.sortBy(_.timestamp).reverse
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get notifications by type for user $userId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get notifications by type for a user (safe functional version)
   * @param state current state
   * @param userId the user ID
   * @param notificationType the notification type
   * @return list of notifications of the specified type for the user or empty list if failed
   */
  def getNotificationsByTypeSafe(state: NotificationManagerState, userId: String, notificationType: NotificationType): List[Notification] = {
    getNotificationsByType(state, userId, notificationType).getOrElse(List.empty)
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
   * Mark notification as read (functional version with Try)
   * @param state current state
   * @param notificationId the notification ID
   * @return Try containing (new state, success flag)
   */
  def markAsRead(state: NotificationManagerState, notificationId: String): Try[(NotificationManagerState, Boolean)] = {
    Try {
      state.notifications.get(notificationId) match {
        case Some(notification) =>
          val updatedNotification = notification.copy()
          updatedNotification.markAsRead()
          val newState = state.copy(notifications = state.notifications + (notificationId -> updatedNotification))
          (newState, true)
        case None => (state, false)
      }
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to mark notification as read: ${e.getMessage}", e)
    }
  }
  
  /**
   * Mark notification as read (safe functional version)
   * @param state current state
   * @param notificationId the notification ID
   * @return (new state, success flag)
   */
  def markAsReadSafe(state: NotificationManagerState, notificationId: String): (NotificationManagerState, Boolean) = {
    markAsRead(state, notificationId).getOrElse((state, false))
  }
  
  /**
   * Mark all notifications as read for a user
   * @param userId the user ID
   * @return number of notifications marked as read
   */
  def markAllAsRead(userId: String): Int = {
    val userNotifications = items.values.filter(_.recipientId == userId)
    val unreadCount = userNotifications.count(!_.isRead)
    userNotifications.foreach(_.markAsRead())
    unreadCount
  }
  
  /**
   * Mark all notifications as read for a user (functional version with Try)
   * @param state current state
   * @param userId the user ID
   * @return Try containing (new state, number of notifications marked as read)
   */
  def markAllAsRead(state: NotificationManagerState, userId: String): Try[(NotificationManagerState, Int)] = {
    Try {
      val userNotifications = state.notifications.values.filter(_.recipientId == userId)
      val unreadCount = userNotifications.count(!_.isRead)
      val updatedNotifications = state.notifications.map {
        case (id, notification) if notification.recipientId == userId && !notification.isRead =>
          val updatedNotification = notification.copy()
          updatedNotification.markAsRead()
          id -> updatedNotification
        case (id, notification) => id -> notification
      }
      val newState = state.copy(notifications = updatedNotifications)
      (newState, unreadCount)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to mark all notifications as read for user $userId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Mark all notifications as read for a user (safe functional version)
   * @param state current state
   * @param userId the user ID
   * @return (new state, number of notifications marked as read)
   */
  def markAllAsReadSafe(state: NotificationManagerState, userId: String): (NotificationManagerState, Int) = {
    markAllAsRead(state, userId).getOrElse((state, 0))
  }
  
  /**
   * Get count of unread notifications for a user
   * @param userId the user ID
   * @return number of unread notifications
   */
  def getUnreadCount(userId: String): Int = {
    items.values.count(n => n.recipientId == userId && !n.isRead)
  }
  
  /**
   * Get count of unread notifications for a user (functional version with Try)
   * @param state current state
   * @param userId the user ID
   * @return Try containing number of unread notifications
   */
  def getUnreadCount(state: NotificationManagerState, userId: String): Try[Int] = {
    Try {
      state.notifications.values.count(n => n.recipientId == userId && !n.isRead)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to get unread count for user $userId: ${e.getMessage}", e)
    }
  }
  
  /**
   * Get count of unread notifications for a user (safe functional version)
   * @param state current state
   * @param userId the user ID
   * @return number of unread notifications or 0 if failed
   */
  def getUnreadCountSafe(state: NotificationManagerState, userId: String): Int = {
    getUnreadCount(state, userId).getOrElse(0)
  }
  
  /**
   * Delete old notifications (older than specified days)
   * @param days number of days to keep notifications
   * @return number of notifications deleted
   */
  def cleanupOldNotifications(days: Int = 30): Int = {
    val cutoffDate = LocalDateTime.now().minusDays(days)
    val oldNotifications = items.filter(_._2.timestamp.isBefore(cutoffDate))
    oldNotifications.foreach { case (id, _) => remove(id) }
    oldNotifications.size
  }
  
  /**
   * Delete old notifications (functional version with Try)
   * @param state current state
   * @param days number of days to keep notifications
   * @return Try containing (new state, number of notifications deleted)
   */
  def cleanupOldNotifications(state: NotificationManagerState, days: Int): Try[(NotificationManagerState, Int)] = {
    Try {
      val cutoffDate = LocalDateTime.now().minusDays(days)
      val (oldNotifications, keepNotifications) = state.notifications.partition(_._2.timestamp.isBefore(cutoffDate))
      val newState = state.copy(notifications = keepNotifications)
      (newState, oldNotifications.size)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to cleanup old notifications: ${e.getMessage}", e)
    }
  }
  
  /**
   * Delete old notifications (safe functional version)
   * @param state current state
   * @param days number of days to keep notifications
   * @return (new state, number of notifications deleted)
   */
  def cleanupOldNotificationsSafe(state: NotificationManagerState, days: Int): (NotificationManagerState, Int) = {
    cleanupOldNotifications(state, days).getOrElse((state, 0))
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
   * Create and send a new message notification (functional version with Try)
   * @param state current state
   * @param recipientId recipient user ID
   * @param senderId sender user ID
   * @param title notification title
   * @param message notification message
   * @param relatedItemId optional related item ID
   * @return Try containing (new state, created notification)
   */
  def createMessageNotification(
    state: NotificationManagerState,
    recipientId: String, 
    senderId: String, 
    title: String, 
    message: String, 
    relatedItemId: Option[String]
  ): Try[(NotificationManagerState, Notification)] = {
    Try {
      val notification = Notification(
        notificationId = java.util.UUID.randomUUID().toString,
        recipientId = recipientId,
        senderId = Some(senderId),
        title = title,
        message = message,
        notificationType = NotificationType.MESSAGE,
        relatedItemId = relatedItemId
      )
      val newState = state.copy(notifications = state.notifications + (notification.notificationId -> notification))
      (newState, notification)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to create message notification: ${e.getMessage}", e)
    }
  }
  
  /**
   * Create and send a new message notification (safe functional version)
   * @param state current state
   * @param recipientId recipient user ID
   * @param senderId sender user ID
   * @param title notification title
   * @param message notification message
   * @param relatedItemId optional related item ID
   * @return (new state, created notification option)
   */
  def createMessageNotificationSafe(
    state: NotificationManagerState,
    recipientId: String, 
    senderId: String, 
    title: String, 
    message: String, 
    relatedItemId: Option[String]
  ): (NotificationManagerState, Option[Notification]) = {
    createMessageNotification(state, recipientId, senderId, title, message, relatedItemId)
      .map { case (newState, notification) => (newState, Some(notification)) }
      .getOrElse((state, None))
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
   * Create and send an event reminder notification (functional version with Try)
   * @param state current state
   * @param recipientId recipient user ID
   * @param eventId event ID
   * @param eventTitle event title
   * @param reminderTime time before event to send reminder
   * @return Try containing (new state, created notification)
   */
  def createEventReminderNotification(
    state: NotificationManagerState,
    recipientId: String, 
    eventId: String, 
    eventTitle: String, 
    reminderTime: String
  ): Try[(NotificationManagerState, Notification)] = {
    Try {
      val notification = Notification(
        notificationId = java.util.UUID.randomUUID().toString,
        recipientId = recipientId,
        senderId = None,
        title = "Event Reminder",
        message = s"Reminder: Event '$eventTitle' starts $reminderTime",
        notificationType = NotificationType.EVENT_REMINDER,
        relatedItemId = Some(eventId)
      )
      val newState = state.copy(notifications = state.notifications + (notification.notificationId -> notification))
      (newState, notification)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to create event reminder notification: ${e.getMessage}", e)
    }
  }
  
  /**
   * Create and send an event reminder notification (safe functional version)
   * @param state current state
   * @param recipientId recipient user ID
   * @param eventId event ID
   * @param eventTitle event title
   * @param reminderTime time before event to send reminder
   * @return (new state, created notification option)
   */
  def createEventReminderNotificationSafe(
    state: NotificationManagerState,
    recipientId: String, 
    eventId: String, 
    eventTitle: String, 
    reminderTime: String
  ): (NotificationManagerState, Option[Notification]) = {
    createEventReminderNotification(state, recipientId, eventId, eventTitle, reminderTime)
      .map { case (newState, notification) => (newState, Some(notification)) }
      .getOrElse((state, None))
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
   * Create and send an announcement notification (functional version with Try)
   * @param state current state
   * @param recipientId recipient user ID
   * @param announcementId announcement ID
   * @param announcementTitle announcement title
   * @return Try containing (new state, created notification)
   */
  def createAnnouncementNotification(
    state: NotificationManagerState,
    recipientId: String, 
    announcementId: String, 
    announcementTitle: String
  ): Try[(NotificationManagerState, Notification)] = {
    Try {
      val notification = Notification(
        notificationId = java.util.UUID.randomUUID().toString,
        recipientId = recipientId,
        senderId = None,
        title = "New Announcement",
        message = s"New announcement: $announcementTitle",
        notificationType = NotificationType.ANNOUNCEMENT,
        relatedItemId = Some(announcementId)
      )
      val newState = state.copy(notifications = state.notifications + (notification.notificationId -> notification))
      (newState, notification)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to create announcement notification: ${e.getMessage}", e)
    }
  }
  
  /**
   * Create and send an announcement notification (safe functional version)
   * @param state current state
   * @param recipientId recipient user ID
   * @param announcementId announcement ID
   * @param announcementTitle announcement title
   * @return (new state, created notification option)
   */
  def createAnnouncementNotificationSafe(
    state: NotificationManagerState,
    recipientId: String, 
    announcementId: String, 
    announcementTitle: String
  ): (NotificationManagerState, Option[Notification]) = {
    createAnnouncementNotification(state, recipientId, announcementId, announcementTitle)
      .map { case (newState, notification) => (newState, Some(notification)) }
      .getOrElse((state, None))
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
  
  /**
   * Create and send a food request/offer notification (functional version with Try)
   * @param state current state
   * @param recipientId recipient user ID
   * @param senderId sender user ID
   * @param foodPostId food post ID
   * @param postTitle post title
   * @param isOffer true if offer, false if request
   * @return Try containing (new state, created notification)
   */
  def createFoodNotification(
    state: NotificationManagerState,
    recipientId: String, 
    senderId: String, 
    foodPostId: String, 
    postTitle: String, 
    isOffer: Boolean
  ): Try[(NotificationManagerState, Notification)] = {
    Try {
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
      val newState = state.copy(notifications = state.notifications + (notification.notificationId -> notification))
      (newState, notification)
    }.recover {
      case NonFatal(e) => throw new RuntimeException(s"Failed to create food notification: ${e.getMessage}", e)
    }
  }
  
  /**
   * Create and send a food request/offer notification (safe functional version)
   * @param state current state
   * @param recipientId recipient user ID
   * @param senderId sender user ID
   * @param foodPostId food post ID
   * @param postTitle post title
   * @param isOffer true if offer, false if request
   * @return (new state, created notification option)
   */
  def createFoodNotificationSafe(
    state: NotificationManagerState,
    recipientId: String, 
    senderId: String, 
    foodPostId: String, 
    postTitle: String, 
    isOffer: Boolean
  ): (NotificationManagerState, Option[Notification]) = {
    createFoodNotification(state, recipientId, senderId, foodPostId, postTitle, isOffer)
      .map { case (newState, notification) => (newState, Some(notification)) }
      .getOrElse((state, None))
  }
}
