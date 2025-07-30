package manager

import model._
import java.time.LocalDateTime
import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal

/**
 * 事件管理器状态 - 不可变数据结构
 * @param events 事件映射
 */
case class EventManagerState(
  events: Map[String, Event] = Map.empty
)

/**
 * Manager class for handling event operations
 */
class EventManager extends Manager[Event] {
  
  /**
   * Create a new event
   * @param event the event to create
   */
  def createEvent(event: Event): Unit = {
    add(event.eventId, event)
  }
  
  /**
   * 函数式创建事件
   * @param state 当前状态
   * @param event 要创建的事件
   * @return 新状态
   */
  def createEvent(state: EventManagerState, event: Event): Try[EventManagerState] = {
    Try {
      val newEvents = state.events + (event.eventId -> event)
      state.copy(events = newEvents)
    }.recover {
      case NonFatal(e) =>
        println(s"创建事件失败: ${e.getMessage}")
        state
    }
  }
  
  /**
   * 函数式创建事件 - 安全版本
   */
  def createEventSafe(state: EventManagerState, event: Event): EventManagerState = {
    createEvent(state, event).getOrElse(state)
  }
  
  /**
   * Get upcoming events
   * @return list of upcoming events
   */
  def getUpcomingEvents: List[Event] = {
    val now = LocalDateTime.now()
    items.values.filter { event =>
      event.status == EventStatus.UPCOMING && event.startDateTime.isAfter(now)
    }.toList.sortBy(_.startDateTime)
  }
  
  /**
   * 函数式获取即将到来的事件
   * @param state 当前状态
   * @return 即将到来的事件列表
   */
  def getUpcomingEvents(state: EventManagerState): Try[List[Event]] = {
    Try {
      val now = LocalDateTime.now()
      state.events.values.filter { event =>
        event.status == EventStatus.UPCOMING && event.startDateTime.isAfter(now)
      }.toList.sortBy(_.startDateTime)
    }.recover {
      case NonFatal(e) =>
        println(s"获取即将到来的事件失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式获取即将到来的事件 - 安全版本
   */
  def getUpcomingEventsSafe(state: EventManagerState): List[Event] = {
    getUpcomingEvents(state).getOrElse(List.empty)
  }
  
  /**
   * Get ongoing events
   * @return list of ongoing events
   */
  def getOngoingEvents: List[Event] = {
    items.values.filter(_.status == EventStatus.ONGOING).toList.sortBy(_.startDateTime)
  }
  
  /**
   * 函数式获取正在进行的事件
   * @param state 当前状态
   * @return 正在进行的事件列表
   */
  def getOngoingEvents(state: EventManagerState): Try[List[Event]] = {
    Try {
      state.events.values.filter(_.status == EventStatus.ONGOING).toList.sortBy(_.startDateTime)
    }.recover {
      case NonFatal(e) =>
        println(s"获取正在进行的事件失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式获取正在进行的事件 - 安全版本
   */
  def getOngoingEventsSafe(state: EventManagerState): List[Event] = {
    getOngoingEvents(state).getOrElse(List.empty)
  }
  
  /**
   * Get events by organizer
   * @param organizerId the organizer ID to filter by
   * @return list of events organized by the specified user
   */
  def getEventsByOrganizer(organizerId: String): List[Event] = {
    items.values.filter(_.organizerId == organizerId).toList.sortBy(_.startDateTime).reverse
  }
  
  /**
   * 函数式按组织者获取事件
   * @param state 当前状态
   * @param organizerId 组织者ID
   * @return 该组织者的事件列表
   */
  def getEventsByOrganizer(state: EventManagerState, organizerId: String): Try[List[Event]] = {
    Try {
      state.events.values.filter(_.organizerId == organizerId).toList.sortBy(_.startDateTime).reverse
    }.recover {
      case NonFatal(e) =>
        println(s"按组织者获取事件失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按组织者获取事件 - 安全版本
   */
  def getEventsByOrganizerSafe(state: EventManagerState, organizerId: String): List[Event] = {
    getEventsByOrganizer(state, organizerId).getOrElse(List.empty)
  }
  
  /**
   * Get events that a user has RSVP'd to
   * @param userId the user ID
   * @return list of events the user is attending
   */
  def getEventsByParticipant(userId: String): List[Event] = {
    items.values.filter(_.participants.contains(userId)).toList.sortBy(_.startDateTime)
  }
  
  /**
   * 函数式按参与者获取事件
   * @param state 当前状态
   * @param userId 用户ID
   * @return 用户参与的事件列表
   */
  def getEventsByParticipant(state: EventManagerState, userId: String): Try[List[Event]] = {
    Try {
      state.events.values.filter(_.participants.contains(userId)).toList.sortBy(_.startDateTime)
    }.recover {
      case NonFatal(e) =>
        println(s"按参与者获取事件失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按参与者获取事件 - 安全版本
   */
  def getEventsByParticipantSafe(state: EventManagerState, userId: String): List[Event] = {
    getEventsByParticipant(state, userId).getOrElse(List.empty)
  }
  
  /**
   * Search events by title or description
   * @param searchTerm the term to search for
   * @return list of matching events
   */
  def searchEvents(searchTerm: String): List[Event] = {
    val term = searchTerm.toLowerCase
    items.values.filter { event =>
      event.title.toLowerCase.contains(term) || 
      event.description.toLowerCase.contains(term) ||
      event.location.toLowerCase.contains(term)
    }.toList.sortBy(_.startDateTime)
  }
  
  /**
   * 函数式搜索事件
   * @param state 当前状态
   * @param searchTerm 搜索词
   * @return 匹配的事件列表
   */
  def searchEvents(state: EventManagerState, searchTerm: String): Try[List[Event]] = {
    Try {
      val term = searchTerm.toLowerCase
      state.events.values.filter { event =>
        event.title.toLowerCase.contains(term) || 
        event.description.toLowerCase.contains(term) ||
        event.location.toLowerCase.contains(term)
      }.toList.sortBy(_.startDateTime)
    }.recover {
      case NonFatal(e) =>
        println(s"搜索事件失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式搜索事件 - 安全版本
   */
  def searchEventsSafe(state: EventManagerState, searchTerm: String): List[Event] = {
    searchEvents(state, searchTerm).getOrElse(List.empty)
  }
  
  /**
   * Get events by location
   * @param location the location to filter by
   * @return list of events in the specified location
   */
  def getEventsByLocation(location: String): List[Event] = {
    items.values.filter(_.location.toLowerCase.contains(location.toLowerCase)).toList.sortBy(_.startDateTime)
  }
  
  /**
   * 函数式按地点获取事件
   * @param state 当前状态
   * @param location 地点
   * @return 指定地点的事件列表
   */
  def getEventsByLocation(state: EventManagerState, location: String): Try[List[Event]] = {
    Try {
      state.events.values.filter(_.location.toLowerCase.contains(location.toLowerCase)).toList.sortBy(_.startDateTime)
    }.recover {
      case NonFatal(e) =>
        println(s"按地点获取事件失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按地点获取事件 - 安全版本
   */
  def getEventsByLocationSafe(state: EventManagerState, location: String): List[Event] = {
    getEventsByLocation(state, location).getOrElse(List.empty)
  }
  
  /**
   * Get events within a date range
   * @param startDate the start date
   * @param endDate the end date
   * @return list of events within the date range
   */
  def getEventsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List[Event] = {
    items.values.filter { event =>
      !event.startDateTime.isBefore(startDate) && !event.startDateTime.isAfter(endDate)
    }.toList.sortBy(_.startDateTime)
  }
  
  /**
   * 函数式按日期范围获取事件
   * @param state 当前状态
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @return 日期范围内的事件列表
   */
  def getEventsInDateRange(state: EventManagerState, startDate: LocalDateTime, endDate: LocalDateTime): Try[List[Event]] = {
    Try {
      state.events.values.filter { event =>
        !event.startDateTime.isBefore(startDate) && !event.startDateTime.isAfter(endDate)
      }.toList.sortBy(_.startDateTime)
    }.recover {
      case NonFatal(e) =>
        println(s"按日期范围获取事件失败: ${e.getMessage}")
        List.empty
    }
  }
  
  /**
   * 函数式按日期范围获取事件 - 安全版本
   */
  def getEventsInDateRangeSafe(state: EventManagerState, startDate: LocalDateTime, endDate: LocalDateTime): List[Event] = {
    getEventsInDateRange(state, startDate, endDate).getOrElse(List.empty)
  }
  
  /**
   * RSVP to an event
   * @param eventId the event ID
   * @param userId the user ID
   * @return true if successfully RSVP'd, false if event is full or not found
   */
  def rsvpToEvent(eventId: String, userId: String): Boolean = {
    get(eventId) match {
      case Some(event) => event.rsvp(userId)
      case None => false
    }
  }
  
  /**
   * 函数式RSVP事件
   * @param state 当前状态
   * @param eventId 事件ID
   * @param userId 用户ID
   * @return (新状态, 是否成功)
   */
  def rsvpToEvent(state: EventManagerState, eventId: String, userId: String): Try[(EventManagerState, Boolean)] = {
    Try {
      state.events.get(eventId) match {
        case Some(event) =>
          val success = event.rsvp(userId)
          val updatedEvents = state.events + (eventId -> event)
          (state.copy(events = updatedEvents), success)
        case None =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"RSVP事件失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式RSVP事件 - 安全版本
   */
  def rsvpToEventSafe(state: EventManagerState, eventId: String, userId: String): (EventManagerState, Boolean) = {
    rsvpToEvent(state, eventId, userId).getOrElse((state, false))
  }
  
  /**
   * Cancel RSVP to an event
   * @param eventId the event ID
   * @param userId the user ID
   * @return true if successful, false if event not found or user not registered
   */
  def cancelRsvp(eventId: String, userId: String): Boolean = {
    get(eventId) match {
      case Some(event) if event.participants.contains(userId) =>
        event.cancelRsvp(userId)
        true
      case _ => false
    }
  }
  
  /**
   * 函数式取消RSVP
   * @param state 当前状态
   * @param eventId 事件ID
   * @param userId 用户ID
   * @return (新状态, 是否成功)
   */
  def cancelRsvp(state: EventManagerState, eventId: String, userId: String): Try[(EventManagerState, Boolean)] = {
    Try {
      state.events.get(eventId) match {
        case Some(event) if event.participants.contains(userId) =>
          event.cancelRsvp(userId)
          val updatedEvents = state.events + (eventId -> event)
          (state.copy(events = updatedEvents), true)
        case _ =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"取消RSVP失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式取消RSVP - 安全版本
   */
  def cancelRsvpSafe(state: EventManagerState, eventId: String, userId: String): (EventManagerState, Boolean) = {
    cancelRsvp(state, eventId, userId).getOrElse((state, false))
  }
  
  /**
   * Start an event
   * @param eventId the event ID
   * @return true if successful, false if event not found or not upcoming
   */
  def startEvent(eventId: String): Boolean = {
    get(eventId) match {
      case Some(event) if event.status == EventStatus.UPCOMING =>
        event.start()
        true
      case _ => false
    }
  }
  
  /**
   * 函数式开始事件
   * @param state 当前状态
   * @param eventId 事件ID
   * @return (新状态, 是否成功)
   */
  def startEvent(state: EventManagerState, eventId: String): Try[(EventManagerState, Boolean)] = {
    Try {
      state.events.get(eventId) match {
        case Some(event) if event.status == EventStatus.UPCOMING =>
          event.start()
          val updatedEvents = state.events + (eventId -> event)
          (state.copy(events = updatedEvents), true)
        case _ =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"开始事件失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式开始事件 - 安全版本
   */
  def startEventSafe(state: EventManagerState, eventId: String): (EventManagerState, Boolean) = {
    startEvent(state, eventId).getOrElse((state, false))
  }
  
  /**
   * Complete an event
   * @param eventId the event ID
   * @return true if successful, false if event not found or not ongoing
   */
  def completeEvent(eventId: String): Boolean = {
    get(eventId) match {
      case Some(event) if event.status == EventStatus.ONGOING =>
        event.complete()
        true
      case _ => false
    }
  }
  
  /**
   * 函数式完成事件
   * @param state 当前状态
   * @param eventId 事件ID
   * @return (新状态, 是否成功)
   */
  def completeEvent(state: EventManagerState, eventId: String): Try[(EventManagerState, Boolean)] = {
    Try {
      state.events.get(eventId) match {
        case Some(event) if event.status == EventStatus.ONGOING =>
          event.complete()
          val updatedEvents = state.events + (eventId -> event)
          (state.copy(events = updatedEvents), true)
        case _ =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"完成事件失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式完成事件 - 安全版本
   */
  def completeEventSafe(state: EventManagerState, eventId: String): (EventManagerState, Boolean) = {
    completeEvent(state, eventId).getOrElse((state, false))
  }
  
  /**
   * Cancel an event
   * @param eventId the event ID
   * @return true if successful, false if event not found
   */
  def cancelEvent(eventId: String): Boolean = {
    get(eventId) match {
      case Some(event) =>
        event.cancel()
        true
      case None => false
    }
  }
  
  /**
   * 函数式取消事件
   * @param state 当前状态
   * @param eventId 事件ID
   * @return (新状态, 是否成功)
   */
  def cancelEvent(state: EventManagerState, eventId: String): Try[(EventManagerState, Boolean)] = {
    Try {
      state.events.get(eventId) match {
        case Some(event) =>
          event.cancel()
          val updatedEvents = state.events + (eventId -> event)
          (state.copy(events = updatedEvents), true)
        case None =>
          (state, false)
      }
    }.recover {
      case NonFatal(e) =>
        println(s"取消事件失败: ${e.getMessage}")
        (state, false)
    }
  }
  
  /**
   * 函数式取消事件 - 安全版本
   */
  def cancelEventSafe(state: EventManagerState, eventId: String): (EventManagerState, Boolean) = {
    cancelEvent(state, eventId).getOrElse((state, false))
  }
  
  /**
   * Add comment to event
   * @param eventId the event ID
   * @param comment the comment to add
   * @return true if successful, false if event not found
   */
  def addComment(eventId: String, comment: Comment): Boolean = {
    get(eventId) match {
      case Some(event) =>
        event.addComment(comment)
        true
      case None => false
    }
  }
  
  /**
   * 函数式添加评论
   * @param state 当前状态
   * @param eventId 事件ID
   * @param comment 评论
   * @return (新状态, 是否成功)
   */
  def addComment(state: EventManagerState, eventId: String, comment: Comment): Try[(EventManagerState, Boolean)] = {
    Try {
      state.events.get(eventId) match {
        case Some(event) =>
          event.addComment(comment)
          val updatedEvents = state.events + (eventId -> event)
          (state.copy(events = updatedEvents), true)
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
  def addCommentSafe(state: EventManagerState, eventId: String, comment: Comment): (EventManagerState, Boolean) = {
    addComment(state, eventId, comment).getOrElse((state, false))
  }
  
  /**
   * Add like to event
   * @param eventId the event ID
   * @return true if successful, false if event not found
   */
  def addLike(eventId: String): Boolean = {
    get(eventId) match {
      case Some(event) =>
        event.addLike()
        true
      case None => false
    }
  }
  
  /**
   * 函数式添加点赞
   * @param state 当前状态
   * @param eventId 事件ID
   * @return (新状态, 是否成功)
   */
  def addLike(state: EventManagerState, eventId: String): Try[(EventManagerState, Boolean)] = {
    Try {
      state.events.get(eventId) match {
        case Some(event) =>
          event.addLike()
          val updatedEvents = state.events + (eventId -> event)
          (state.copy(events = updatedEvents), true)
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
  def addLikeSafe(state: EventManagerState, eventId: String): (EventManagerState, Boolean) = {
    addLike(state, eventId).getOrElse((state, false))
  }
  
  /**
   * Get event statistics
   * @return tuple of (total events, upcoming events, completed events, total participants)
   */
  def getStatistics: (Int, Int, Int, Int) = {
    val allEvents = items.values.toList
    val upcomingEvents = allEvents.count(_.status == EventStatus.UPCOMING)
    val completedEvents = allEvents.count(_.status == EventStatus.COMPLETED)
    val totalParticipants = allEvents.map(_.participants.size).sum
    
    (allEvents.size, upcomingEvents, completedEvents, totalParticipants)
  }
  
  /**
   * 函数式获取事件统计
   * @param state 当前状态
   * @return (总事件数, 即将到来的事件数, 已完成的事件数, 总参与者数)
   */
  def getStatistics(state: EventManagerState): Try[(Int, Int, Int, Int)] = {
    Try {
      val allEvents = state.events.values.toList
      val upcomingEvents = allEvents.count(_.status == EventStatus.UPCOMING)
      val completedEvents = allEvents.count(_.status == EventStatus.COMPLETED)
      val totalParticipants = allEvents.map(_.participants.size).sum
      
      (allEvents.size, upcomingEvents, completedEvents, totalParticipants)
    }.recover {
      case NonFatal(e) =>
        println(s"获取事件统计失败: ${e.getMessage}")
        (0, 0, 0, 0)
    }
  }
  
  /**
   * 函数式获取事件统计 - 安全版本
   */
  def getStatisticsSafe(state: EventManagerState): (Int, Int, Int, Int) = {
    getStatistics(state).getOrElse((0, 0, 0, 0))
  }
  
  /**
   * Update event status based on current time
   * This method should be called periodically to update event statuses
   */
  def updateEventStatuses(): Unit = {
    val now = LocalDateTime.now()
    items.values.foreach { event =>
      event.status match {
        case EventStatus.UPCOMING if event.isOngoing =>
          event.start()
        case EventStatus.ONGOING if event.isPast =>
          event.complete()
        case _ => // No change needed
      }
    }
  }
  
  /**
   * 函数式更新事件状态
   * @param state 当前状态
   * @return 更新后的状态
   */
  def updateEventStatuses(state: EventManagerState): Try[EventManagerState] = {
    Try {
      val now = LocalDateTime.now()
      val updatedEvents = state.events.map { case (eventId, event) =>
        event.status match {
          case EventStatus.UPCOMING if event.isOngoing =>
            event.start()
            eventId -> event
          case EventStatus.ONGOING if event.isPast =>
            event.complete()
            eventId -> event
          case _ =>
            eventId -> event
        }
      }
      state.copy(events = updatedEvents)
    }.recover {
      case NonFatal(e) =>
        println(s"更新事件状态失败: ${e.getMessage}")
        state
    }
  }
  
  /**
   * 函数式更新事件状态 - 安全版本
   */
  def updateEventStatusesSafe(state: EventManagerState): EventManagerState = {
    updateEventStatuses(state).getOrElse(state)
  }
}
