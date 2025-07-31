package model

import java.time.LocalDateTime

/**
 * Enumeration for event status
 */
enum EventStatus:
  case UPCOMING, ONGOING, COMPLETED, CANCELLED

/**
 * Immutable case class representing a community event
 * @param eventId unique identifier for the event
 * @param organizerId ID of the event organizer
 * @param title event title
 * @param description event description
 * @param location event location
 * @param startDateTime event start date and time
 * @param endDateTime event end date and time
 * @param maxParticipants maximum number of participants (None for unlimited)
 * @param createdAt creation timestamp
 * @param status current event status
 * @param participants list of participant user IDs
 * @param waitingList list of user IDs on waiting list
 * @param likes number of likes
 * @param comments list of comments
 * @param isModerated moderation status
 * @param moderatedBy moderator ID
 * @param moderationDate moderation timestamp
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
  createdAt: LocalDateTime = LocalDateTime.now(),
  status: EventStatus = EventStatus.UPCOMING,
  participants: List[String] = List.empty,
  waitingList: List[String] = List.empty,
  likes: Int = 0,
  comments: List[Comment] = List.empty,
  isModerated: Boolean = false,
  moderatedBy: Option[String] = None,
  moderationDate: Option[LocalDateTime] = None
) extends Likeable with Moderatable {
  
  // Likeable trait implementation
  def withLike: Event = copy(likes = likes + 1)
  def withoutLike: Event = copy(likes = if (likes > 0) likes - 1 else 0)
  def withComment(comment: Comment): Event = copy(comments = comment :: comments)
  
  // Moderatable trait implementation
  def withModeration(adminId: String): Event = copy(
    isModerated = true,
    moderatedBy = Some(adminId),
    moderationDate = Some(LocalDateTime.now())
  )
  
  /**
   * RSVP to the event
   * @param userId ID of the user RSVPing
   * @return (updated Event, success flag)
   */
  def rsvp(userId: String): (Event, Boolean) = {
    if (participants.contains(userId)) {
      (this, false) // Already registered
    } else if (maxParticipants.isEmpty || participants.length < maxParticipants.get) {
      val updatedEvent = copy(
        participants = userId :: participants,
        waitingList = waitingList.filterNot(_ == userId)
      )
      (updatedEvent, true)
    } else {
      // Add to waiting list
      if (!waitingList.contains(userId)) {
        val updatedEvent = copy(waitingList = userId :: waitingList)
        (updatedEvent, false)
      } else {
        (this, false)
      }
    }
  }
  
  /**
   * Cancel RSVP
   * @param userId ID of the user canceling
   * @return updated Event
   */
  def cancelRsvp(userId: String): Event = {
    val updatedParticipants = participants.filterNot(_ == userId)
    // Move someone from waiting list if there's space
    if (waitingList.nonEmpty && (maxParticipants.isEmpty || updatedParticipants.length < maxParticipants.get)) {
      val nextParticipant = waitingList.head
      copy(
        participants = nextParticipant :: updatedParticipants,
        waitingList = waitingList.tail
      )
    } else {
      copy(participants = updatedParticipants)
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
   * @return updated Event with ONGOING status if valid, otherwise unchanged
   */
  def start: Event = {
    if (status == EventStatus.UPCOMING) {
      copy(status = EventStatus.ONGOING)
    } else this
  }
  
  /**
   * Complete the event
   * @return updated Event with COMPLETED status if valid, otherwise unchanged
   */
  def complete: Event = {
    if (status == EventStatus.ONGOING) {
      copy(status = EventStatus.COMPLETED)
    } else this
  }
  
  /**
   * Cancel the event
   * @return updated Event with CANCELLED status
   */
  def cancel: Event = copy(status = EventStatus.CANCELLED)
  
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
