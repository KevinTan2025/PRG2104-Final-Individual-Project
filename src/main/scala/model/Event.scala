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
  createdAt: LocalDateTime = LocalDateTime.now(),
  
  // Immutable fields for functional style
  status: EventStatus = EventStatus.UPCOMING,
  participants: List[String] = List.empty, // List of user IDs
  waitingList: List[String] = List.empty, // List of user IDs on waiting list
  
  // Fields required by Likeable trait
  /** Number of likes this event has received */
  likes: Int = 0,
  /** List of comments on this event */
  comments: List[Comment] = List.empty,
  
  // Fields required by Moderatable trait
  /** Whether this event has been moderated */
  isModerated: Boolean = false,
  /** ID of the admin who moderated this event */
  moderatedBy: Option[String] = None,
  /** Date and time when this event was moderated */
  moderationDate: Option[LocalDateTime] = None
) extends Likeable with Moderatable {
  
  /**
   * RSVP to the event
   * @param userId ID of the user RSVPing
   * @return (updated Event, success flag)
   */
  def rsvp(userId: String): (Event, Boolean) = {
    if (participants.contains(userId)) {
      (this, false) // Already registered
    } else if (maxParticipants.isEmpty || participants.length < maxParticipants.get) {
      val updatedEvent = this.copy(
        participants = userId :: participants,
        waitingList = waitingList.filterNot(_ == userId)
      )
      (updatedEvent, true)
    } else {
      // Add to waiting list
      if (!waitingList.contains(userId)) {
        val updatedEvent = this.copy(waitingList = userId :: waitingList)
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
      val updatedWaitingList = waitingList.tail
      val finalParticipants = nextParticipant :: updatedParticipants
      this.copy(participants = finalParticipants, waitingList = updatedWaitingList)
    } else {
      this.copy(participants = updatedParticipants)
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
   * @return updated Event with ONGOING status
   */
  def start(): Event = {
    if (status == EventStatus.UPCOMING) {
      this.copy(status = EventStatus.ONGOING)
    } else {
      this
    }
  }
  
  /**
   * Complete the event
   * @return updated Event with COMPLETED status
   */
  def complete(): Event = {
    if (status == EventStatus.ONGOING) {
      this.copy(status = EventStatus.COMPLETED)
    } else {
      this
    }
  }
  
  /**
   * Cancel the event
   * @return updated Event with CANCELLED status
   */
  def cancel(): Event = {
    this.copy(status = EventStatus.CANCELLED)
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
  
  // Implementation of Likeable trait methods
  
  /**
   * Add a like to this event
   * @return updated Event with incremented likes
   */
  def addLike(): Event = {
    this.copy(likes = likes + 1)
  }
  
  /**
   * Remove a like from this event
   * @return updated Event with decremented likes
   */
  def removeLike(): Event = {
    this.copy(likes = math.max(0, likes - 1))
  }
  
  /**
   * Add a comment to this event
   * @param comment the comment to add
   * @return updated Event with the new comment
   */
  def addComment(comment: Comment): Event = {
    this.copy(comments = comment :: comments)
  }
  
  // Implementation of Moderatable trait method
  
  /**
   * Moderate this event
   * @param adminId ID of the admin performing moderation
   * @return updated Event marked as moderated
   */
  def moderate(adminId: String): Event = {
    this.copy(
      isModerated = true,
      moderatedBy = Some(adminId),
      moderationDate = Some(LocalDateTime.now())
    )
  }
}
