package database.dao

import database.DatabaseConnection
import model._
import java.time.LocalDateTime
import java.sql.ResultSet
import scala.collection.mutable.ListBuffer

/**
 * Data Access Object for Event operations
 */
class EventDAO {
  
  def insert(event: Event): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO events 
           (event_id, organizer_id, title, description, location, start_datetime, end_datetime, 
            max_participants, likes, created_at, updated_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        event.eventId, event.organizerId, event.title, event.description, event.location,
        DatabaseConnection.formatDateTime(event.startDateTime),
        DatabaseConnection.formatDateTime(event.endDateTime),
        event.maxParticipants.orNull, event.likes,
        DatabaseConnection.formatDateTime(LocalDateTime.now()),
        DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error inserting event: ${e.getMessage}")
        false
    }
  }
  
  def update(event: Event): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        """UPDATE events 
           SET title = ?, description = ?, location = ?, start_datetime = ?, end_datetime = ?, 
               max_participants = ?, likes = ?, updated_at = ?
           WHERE event_id = ?""",
        event.title, event.description, event.location,
        DatabaseConnection.formatDateTime(event.startDateTime),
        DatabaseConnection.formatDateTime(event.endDateTime),
        event.maxParticipants.orNull, event.likes,
        DatabaseConnection.formatDateTime(LocalDateTime.now()),
        event.eventId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error updating event: ${e.getMessage}")
        false
    }
  }
  
  def delete(eventId: String): Boolean = {
    try {
      // First delete all RSVPs for this event
      DatabaseConnection.executeUpdate(
        "DELETE FROM event_rsvps WHERE event_id = ?", eventId
      )
      
      // Then delete the event
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM events WHERE event_id = ?", eventId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error deleting event: ${e.getMessage}")
        false
    }
  }
  
  def findById(eventId: String): Option[Event] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM events WHERE event_id = ?", eventId
      )
      
      if (rs.next()) {
        val event = mapResultSetToEvent(rs)
        rs.close()
        Some(event)
      } else {
        rs.close()
        None
      }
    } catch {
      case e: Exception =>
        println(s"Error finding event by id: ${e.getMessage}")
        None
    }
  }
  
  def findAll(): List[Event] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM events ORDER BY start_datetime ASC"
      )
      val events = ListBuffer[Event]()
      
      while (rs.next()) {
        events += mapResultSetToEvent(rs)
      }
      
      rs.close()
      events.toList
    } catch {
      case e: Exception =>
        println(s"Error finding all events: ${e.getMessage}")
        List.empty
    }
  }
  
  def findUpcomingEvents(): List[Event] = {
    try {
      val now = DatabaseConnection.formatDateTime(LocalDateTime.now())
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM events WHERE start_datetime > ? ORDER BY start_datetime ASC", now
      )
      val events = ListBuffer[Event]()
      
      while (rs.next()) {
        events += mapResultSetToEvent(rs)
      }
      
      rs.close()
      events.toList
    } catch {
      case e: Exception =>
        println(s"Error finding upcoming events: ${e.getMessage}")
        List.empty
    }
  }
  
  def findEventsByOrganizer(organizerId: String): List[Event] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT * FROM events WHERE organizer_id = ? ORDER BY start_datetime ASC", organizerId
      )
      val events = ListBuffer[Event]()
      
      while (rs.next()) {
        events += mapResultSetToEvent(rs)
      }
      
      rs.close()
      events.toList
    } catch {
      case e: Exception =>
        println(s"Error finding events by organizer: ${e.getMessage}")
        List.empty
    }
  }
  
  def searchEvents(searchTerm: String): List[Event] = {
    try {
      val searchPattern = s"%${searchTerm.toLowerCase}%"
      val rs = DatabaseConnection.executeQuery(
        """SELECT * FROM events 
           WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ? OR LOWER(location) LIKE ?
           ORDER BY start_datetime ASC""",
        searchPattern, searchPattern, searchPattern
      )
      val events = ListBuffer[Event]()
      
      while (rs.next()) {
        events += mapResultSetToEvent(rs)
      }
      
      rs.close()
      events.toList
    } catch {
      case e: Exception =>
        println(s"Error searching events: ${e.getMessage}")
        List.empty
    }
  }
  
  def rsvpToEvent(eventId: String, userId: String): Boolean = {
    try {
      // Check if user already RSVP'd
      val existingRs = DatabaseConnection.executeQuery(
        "SELECT COUNT(*) FROM event_rsvps WHERE event_id = ? AND user_id = ?",
        eventId, userId
      )
      val alreadyRsvped = existingRs.next() && existingRs.getInt(1) > 0
      existingRs.close()
      
      if (alreadyRsvped) {
        return false // Already RSVP'd
      }
      
      // Check if event is full
      val event = findById(eventId)
      event match {
        case Some(e) if e.maxParticipants.isDefined =>
          val currentRsvps = getRsvpCount(eventId)
          if (currentRsvps >= e.maxParticipants.get) {
            return false // Event is full
          }
        case None => return false // Event not found
        case _ => // No max participants limit
      }
      
      // Add RSVP
      val rowsAffected = DatabaseConnection.executeUpdate(
        """INSERT INTO event_rsvps (event_id, user_id, rsvp_date) 
           VALUES (?, ?, ?)""",
        eventId, userId, DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error RSVP to event: ${e.getMessage}")
        false
    }
  }
  
  def cancelRsvp(eventId: String, userId: String): Boolean = {
    try {
      val rowsAffected = DatabaseConnection.executeUpdate(
        "DELETE FROM event_rsvps WHERE event_id = ? AND user_id = ?",
        eventId, userId
      )
      rowsAffected > 0
    } catch {
      case e: Exception =>
        println(s"Error canceling RSVP: ${e.getMessage}")
        false
    }
  }
  
  def getRsvpCount(eventId: String): Int = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT COUNT(*) FROM event_rsvps WHERE event_id = ?", eventId
      )
      val count = if (rs.next()) rs.getInt(1) else 0
      rs.close()
      count
    } catch {
      case e: Exception =>
        println(s"Error getting RSVP count: ${e.getMessage}")
        0
    }
  }
  
  def getEventRsvps(eventId: String): List[String] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT user_id FROM event_rsvps WHERE event_id = ?", eventId
      )
      val userIds = ListBuffer[String]()
      
      while (rs.next()) {
        userIds += rs.getString("user_id")
      }
      
      rs.close()
      userIds.toList
    } catch {
      case e: Exception =>
        println(s"Error getting event RSVPs: ${e.getMessage}")
        List.empty
    }
  }
  
  def getUserEvents(userId: String): List[Event] = {
    try {
      val rs = DatabaseConnection.executeQuery(
        """SELECT e.* FROM events e
           INNER JOIN event_rsvps r ON e.event_id = r.event_id
           WHERE r.user_id = ?
           ORDER BY e.start_datetime ASC""",
        userId
      )
      val events = ListBuffer[Event]()
      
      while (rs.next()) {
        events += mapResultSetToEvent(rs)
      }
      
      rs.close()
      events.toList
    } catch {
      case e: Exception =>
        println(s"Error getting user events: ${e.getMessage}")
        List.empty
    }
  }
  
  private def mapResultSetToEvent(rs: ResultSet): Event = {
    val eventId = rs.getString("event_id")
    val organizerId = rs.getString("organizer_id")
    val title = rs.getString("title")
    val description = rs.getString("description")
    val location = rs.getString("location")
    val startDateTime = DatabaseConnection.parseDateTime(rs.getString("start_datetime"))
    val endDateTime = DatabaseConnection.parseDateTime(rs.getString("end_datetime"))
    val maxParticipants = Option(rs.getObject("max_participants")).map(_.asInstanceOf[Int])
    val likes = rs.getInt("likes")
    
    // Get RSVPs for this event
    val participants = getEventRsvps(eventId)
    
    // Create event with all properties from database
    Event(
      eventId = eventId,
      organizerId = organizerId,
      title = title,
      description = description,
      location = location,
      startDateTime = startDateTime,
      endDateTime = endDateTime,
      maxParticipants = maxParticipants,
      participants = participants,
      likes = likes
    )
  }
}
