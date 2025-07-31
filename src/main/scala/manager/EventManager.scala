package manager

import model._
import java.time.LocalDateTime

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
   * Get ongoing events
   * @return list of ongoing events
   */
  def getOngoingEvents: List[Event] = {
    items.values.filter(_.status == EventStatus.ONGOING).toList.sortBy(_.startDateTime)
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
   * Get events that a user has RSVP'd to
   * @param userId the user ID
   * @return list of events the user is attending
   */
  def getEventsByParticipant(userId: String): List[Event] = {
    items.values.filter(_.participants.contains(userId)).toList.sortBy(_.startDateTime)
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
   * Get events by location
   * @param location the location to filter by
   * @return list of events in the specified location
   */
  def getEventsByLocation(location: String): List[Event] = {
    items.values.filter(_.location.toLowerCase.contains(location.toLowerCase)).toList.sortBy(_.startDateTime)
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
   * RSVP to an event
   * @param eventId the event ID
   * @param userId the user ID
   * @return true if successfully RSVP'd, false if event is full or not found
   */
  def rsvpToEvent(eventId: String, userId: String): Boolean = {
    get(eventId) match {
      case Some(event) => 
        val (updatedEvent, success) = event.rsvp(userId)
        if (success) {
          add(event.eventId, updatedEvent)
        }
        success
      case None => false
    }
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
        val updatedEvent = event.cancelRsvp(userId)
        add(event.eventId, updatedEvent)
        true
      case _ => false
    }
  }
  
  /**
   * Start an event
   * @param eventId the event ID
   * @return true if successful, false if event not found or not upcoming
   */
  def startEvent(eventId: String): Boolean = {
    get(eventId) match {
      case Some(event) if event.status == EventStatus.UPCOMING =>
        val updatedEvent = event.start()
        add(event.eventId, updatedEvent)
        true
      case _ => false
    }
  }
  
  /**
   * Complete an event
   * @param eventId the event ID
   * @return true if successful, false if event not found or not ongoing
   */
  def completeEvent(eventId: String): Boolean = {
    get(eventId) match {
      case Some(event) if event.status == EventStatus.ONGOING =>
        val updatedEvent = event.complete()
        add(event.eventId, updatedEvent)
        true
      case _ => false
    }
  }
  
  /**
   * Cancel an event
   * @param eventId the event ID
   * @return true if successful, false if event not found
   */
  def cancelEvent(eventId: String): Boolean = {
    get(eventId) match {
      case Some(event) =>
        val updatedEvent = event.cancel()
        add(event.eventId, updatedEvent)
        true
      case None => false
    }
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
   * Update event status based on current time
   * This method should be called periodically to update event statuses
   */
  def updateEventStatuses(): Unit = {
    val now = LocalDateTime.now()
    items.values.foreach { event =>
      event.status match {
        case EventStatus.UPCOMING if event.isOngoing =>
          val updatedEvent = event.start()
          add(event.eventId, updatedEvent)
        case EventStatus.ONGOING if event.isPast =>
          val updatedEvent = event.complete()
          add(event.eventId, updatedEvent)
        case _ => // No change needed
      }
    }
  }
}
