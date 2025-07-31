package model

import java.time.LocalDateTime

/**
 * Enumeration for event status
 */
enum EventStatus:
  case UPCOMING, ONGOING, COMPLETED, CANCELLED

/**
 * Case class representing a community event
 * @param eventId unique identifier for the event
 * @param organizerId ID of the event organizer
 * @param title event title
 * @param description event description
 * @param location event location
 * @param startDateTime event start date and time
 * @param endDateTime event end date and time
 * @param maxParticipants maximum number of participants (None for unlimited)
 */
case class Event(
  eventId: String,
  organizerId: String,
  title: String,
  description: String,
  location: String,
  startDateTime: LocalDateTime,
  endDateTime: LocalDateTime,
  maxParticipants: Option[Int] = None,
  createdAt: LocalDateTime = LocalDateTime.now()
) extends Likeable with Moderatable {
  
  var status: EventStatus = EventStatus.UPCOMING
  var participants: List[String] = List.empty // List of user IDs
  var waitingList: List[String] = List.empty // List of user IDs on waiting list
  
  /**
   * RSVP to the event
   * @param userId ID of the user RSVPing
   * @return true if successfully added, false if event is full
   */
  def rsvp(userId: String): Boolean = {
    if (participants.contains(userId)) {
      false // Already registered
    } else if (maxParticipants.isEmpty || participants.length < maxParticipants.get) {
      participants = userId :: participants
      // Remove from waiting list if they were on it
      waitingList = waitingList.filterNot(_ == userId)
      true
    } else {
      // Add to waiting list
      if (!waitingList.contains(userId)) {
        waitingList = userId :: waitingList
      }
      false
    }
  }
  
  /**
   * Cancel RSVP
   * @param userId ID of the user canceling
   */
  def cancelRsvp(userId: String): Unit = {
    participants = participants.filterNot(_ == userId)
    // Move someone from waiting list if there's space
    if (waitingList.nonEmpty && (maxParticipants.isEmpty || participants.length < maxParticipants.get)) {
      val nextParticipant = waitingList.head
      waitingList = waitingList.tail
      participants = nextParticipant :: participants
    }
  }
  
  /**
   * Check if the event is full
   * @return true if full, false otherwise
   */
  def isFull: Boolean = {
    maxParticipants.exists(participants.length >= _)
  }
  
  /**
   * Get the number of available spots
   * @return number of available spots, or None if unlimited
   */
  def getAvailableSpots: Option[Int] = {
    maxParticipants.map(_ - participants.length)
  }
  
  /**
   * Start the event
   */
  def start(): Unit = {
    if (status == EventStatus.UPCOMING) {
      status = EventStatus.ONGOING
    }
  }
  
  /**
   * Complete the event
   */
  def complete(): Unit = {
    if (status == EventStatus.ONGOING) {
      status = EventStatus.COMPLETED
    }
  }
  
  /**
   * Cancel the event
   */
  def cancel(): Unit = {
    status = EventStatus.CANCELLED
  }
  
  /**
   * Check if the event is in the past
   * @return true if past, false otherwise
   */
  def isPast: Boolean = {
    endDateTime.isBefore(LocalDateTime.now())
  }
  
  /**
   * Check if the event is currently happening
   * @return true if ongoing, false otherwise
   */
  def isOngoing: Boolean = {
    val now = LocalDateTime.now()
    now.isAfter(startDateTime) && now.isBefore(endDateTime)
  }
}
