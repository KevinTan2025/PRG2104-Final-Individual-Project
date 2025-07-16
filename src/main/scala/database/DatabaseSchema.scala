package database

import util.PasswordHasher

/**
 * Database schema initialization and management
 */
object DatabaseSchema {
  
  /**
   * Initialize all database tables
   */
  def initializeDatabase(): Unit = {
    if (!DatabaseConnection.isDatabaseInitialized) {
      createTables()
      insertSampleData()
      println("Database initialized successfully!")
    } else {
      println("Database already initialized.")
    }
  }
  
  /**
   * Create all required tables
   */
  private def createTables(): Unit = {
    val createTablesScript = """
      -- Users table
      CREATE TABLE IF NOT EXISTS users (
        user_id TEXT PRIMARY KEY,
        username TEXT UNIQUE NOT NULL,
        email TEXT UNIQUE NOT NULL,
        name TEXT NOT NULL,
        contact_info TEXT NOT NULL,
        is_admin BOOLEAN NOT NULL DEFAULT 0,
        password_hash TEXT NOT NULL DEFAULT 'default',
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
      );
      
      -- Announcements table
      CREATE TABLE IF NOT EXISTS announcements (
        announcement_id TEXT PRIMARY KEY,
        author_id TEXT NOT NULL,
        title TEXT NOT NULL,
        content TEXT NOT NULL,
        announcement_type TEXT NOT NULL,
        is_moderated BOOLEAN NOT NULL DEFAULT 0,
        moderator_id TEXT,
        likes INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (author_id) REFERENCES users(user_id),
        FOREIGN KEY (moderator_id) REFERENCES users(user_id)
      );
      
      -- Food posts table
      CREATE TABLE IF NOT EXISTS food_posts (
        post_id TEXT PRIMARY KEY,
        author_id TEXT NOT NULL,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        post_type TEXT NOT NULL,
        quantity TEXT NOT NULL,
        location TEXT NOT NULL,
        expiry_date TEXT,
        status TEXT NOT NULL DEFAULT 'OPEN',
        accepted_by TEXT,
        is_moderated BOOLEAN NOT NULL DEFAULT 0,
        moderator_id TEXT,
        likes INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (author_id) REFERENCES users(user_id),
        FOREIGN KEY (accepted_by) REFERENCES users(user_id),
        FOREIGN KEY (moderator_id) REFERENCES users(user_id)
      );
      
      -- Discussion topics table
      CREATE TABLE IF NOT EXISTS discussion_topics (
        topic_id TEXT PRIMARY KEY,
        author_id TEXT NOT NULL,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        category TEXT NOT NULL,
        is_moderated BOOLEAN NOT NULL DEFAULT 0,
        moderator_id TEXT,
        likes INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (author_id) REFERENCES users(user_id),
        FOREIGN KEY (moderator_id) REFERENCES users(user_id)
      );
      
      -- Discussion replies table
      CREATE TABLE IF NOT EXISTS discussion_replies (
        reply_id TEXT PRIMARY KEY,
        topic_id TEXT NOT NULL,
        author_id TEXT NOT NULL,
        content TEXT NOT NULL,
        likes INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (topic_id) REFERENCES discussion_topics(topic_id),
        FOREIGN KEY (author_id) REFERENCES users(user_id)
      );
      
      -- Events table
      CREATE TABLE IF NOT EXISTS events (
        event_id TEXT PRIMARY KEY,
        organizer_id TEXT NOT NULL,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        location TEXT NOT NULL,
        start_datetime TEXT NOT NULL,
        end_datetime TEXT NOT NULL,
        max_participants INTEGER,
        is_moderated BOOLEAN NOT NULL DEFAULT 0,
        moderator_id TEXT,
        likes INTEGER NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (organizer_id) REFERENCES users(user_id),
        FOREIGN KEY (moderator_id) REFERENCES users(user_id)
      );
      
      -- Event RSVPs table
      CREATE TABLE IF NOT EXISTS event_rsvps (
        rsvp_id INTEGER PRIMARY KEY AUTOINCREMENT,
        event_id TEXT NOT NULL,
        user_id TEXT NOT NULL,
        rsvp_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (event_id) REFERENCES events(event_id),
        FOREIGN KEY (user_id) REFERENCES users(user_id),
        UNIQUE(event_id, user_id)
      );
      
      -- Notifications table
      CREATE TABLE IF NOT EXISTS notifications (
        notification_id TEXT PRIMARY KEY,
        recipient_id TEXT NOT NULL,
        sender_id TEXT,
        type TEXT NOT NULL,
        title TEXT NOT NULL,
        message TEXT NOT NULL,
        related_id TEXT,
        is_read BOOLEAN NOT NULL DEFAULT 0,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (recipient_id) REFERENCES users(user_id),
        FOREIGN KEY (sender_id) REFERENCES users(user_id)
      );
      
      -- Comments table (for announcements, food posts, events)
      CREATE TABLE IF NOT EXISTS comments (
        comment_id TEXT PRIMARY KEY,
        content_type TEXT NOT NULL,
        content_id TEXT NOT NULL,
        author_id TEXT NOT NULL,
        content TEXT NOT NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (author_id) REFERENCES users(user_id)
      );
      
      -- Create indexes for better performance
      CREATE INDEX IF NOT EXISTS idx_announcements_author ON announcements(author_id);
      CREATE INDEX IF NOT EXISTS idx_food_posts_author ON food_posts(author_id);
      CREATE INDEX IF NOT EXISTS idx_food_posts_type ON food_posts(post_type);
      CREATE INDEX IF NOT EXISTS idx_food_posts_status ON food_posts(status);
      CREATE INDEX IF NOT EXISTS idx_discussion_topics_author ON discussion_topics(author_id);
      CREATE INDEX IF NOT EXISTS idx_discussion_topics_category ON discussion_topics(category);
      CREATE INDEX IF NOT EXISTS idx_discussion_replies_topic ON discussion_replies(topic_id);
      CREATE INDEX IF NOT EXISTS idx_events_organizer ON events(organizer_id);
      CREATE INDEX IF NOT EXISTS idx_events_datetime ON events(start_datetime);
      CREATE INDEX IF NOT EXISTS idx_event_rsvps_event ON event_rsvps(event_id);
      CREATE INDEX IF NOT EXISTS idx_event_rsvps_user ON event_rsvps(user_id);
      CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id);
      CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(is_read);
      CREATE INDEX IF NOT EXISTS idx_comments_content ON comments(content_type, content_id);
    """
    
    DatabaseConnection.executeSqlScript(createTablesScript)
  }
  
  /**
   * Insert sample data for testing
   */
  private def insertSampleData(): Unit = {
    import java.time.LocalDateTime
    import java.util.UUID
    
    // Insert sample users with default passwords
    val defaultPassword = "Password123!"  // Demo password for all sample users
    val defaultPasswordHash = PasswordHasher.hashPassword(defaultPassword)
    
    val sampleUsers = List(
      ("admin1", "admin", "admin@community.org", "System Administrator", "admin@community.org", true),
      ("user1", "john_doe", "john@example.com", "John Doe", "john@example.com", false),
      ("user2", "jane_smith", "jane@example.com", "Jane Smith", "jane@example.com", false)
    )
    
    sampleUsers.foreach { case (userId, username, email, name, contactInfo, isAdmin) =>
      DatabaseConnection.executeUpdate(
        """INSERT OR IGNORE INTO users 
           (user_id, username, email, name, contact_info, is_admin, password_hash, created_at, updated_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        userId, username, email, name, contactInfo, isAdmin, defaultPasswordHash,
        DatabaseConnection.formatDateTime(LocalDateTime.now()),
        DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
    }
    
    // Insert sample announcements
    val announcement1Id = UUID.randomUUID().toString
    val announcement2Id = UUID.randomUUID().toString
    
    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO announcements 
         (announcement_id, author_id, title, content, announcement_type, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      announcement1Id, "admin1", 
      "Welcome to Community Engagement Platform",
      "Welcome everyone! This platform helps our community share resources and collaborate for food security.",
      "GENERAL",
      DatabaseConnection.formatDateTime(LocalDateTime.now()),
      DatabaseConnection.formatDateTime(LocalDateTime.now())
    )
    
    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO announcements 
         (announcement_id, author_id, title, content, announcement_type, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      announcement2Id, "admin1",
      "Food Distribution Event This Weekend",
      "Join us this Saturday for our monthly food distribution event at the community center.",
      "FOOD_DISTRIBUTION",
      DatabaseConnection.formatDateTime(LocalDateTime.now()),
      DatabaseConnection.formatDateTime(LocalDateTime.now())
    )
    
    // Insert sample food posts
    val foodPost1Id = UUID.randomUUID().toString
    val foodPost2Id = UUID.randomUUID().toString
    
    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO food_posts 
         (post_id, author_id, title, description, post_type, quantity, location, expiry_date, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      foodPost1Id, "user1",
      "Fresh vegetables available",
      "I have excess vegetables from my garden. Free to good home!",
      "OFFER", "5 bags", "Downtown Community Center",
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(2)),
      DatabaseConnection.formatDateTime(LocalDateTime.now()),
      DatabaseConnection.formatDateTime(LocalDateTime.now())
    )
    
    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO food_posts 
         (post_id, author_id, title, description, post_type, quantity, location, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      foodPost2Id, "user2",
      "Looking for canned goods",
      "Family in need of non-perishable food items.",
      "REQUEST", "Any amount", "North Side",
      DatabaseConnection.formatDateTime(LocalDateTime.now()),
      DatabaseConnection.formatDateTime(LocalDateTime.now())
    )
    
    // Insert sample discussion topic
    val topicId = UUID.randomUUID().toString
    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_topics 
         (topic_id, author_id, title, description, category, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      topicId, "user1",
      "Tips for Urban Gardening",
      "Let's share tips and experiences about growing food in urban environments.",
      "SUSTAINABLE_AGRICULTURE",
      DatabaseConnection.formatDateTime(LocalDateTime.now()),
      DatabaseConnection.formatDateTime(LocalDateTime.now())
    )
    
    // Insert sample event
    val eventId = UUID.randomUUID().toString
    val startTime = LocalDateTime.now().plusDays(7)
    val endTime = startTime.plusHours(3)
    
    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO events 
         (event_id, organizer_id, title, description, location, start_datetime, end_datetime, max_participants, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      eventId, "admin1",
      "Community Garden Workshop",
      "Learn how to start and maintain a community garden. All skill levels welcome!",
      "Community Center Room A",
      DatabaseConnection.formatDateTime(startTime),
      DatabaseConnection.formatDateTime(endTime),
      20,
      DatabaseConnection.formatDateTime(LocalDateTime.now()),
      DatabaseConnection.formatDateTime(LocalDateTime.now())
    )
    
    println("Sample data inserted successfully!")
    println("Sample users created with default password: 'Password123!'")
    println("Available users: admin/john_doe/jane_smith")
  }
  
  /**
   * Drop all tables (for testing/reset purposes)
   */
  def dropAllTables(): Unit = {
    val dropTablesScript = """
      DROP TABLE IF EXISTS comments;
      DROP TABLE IF EXISTS notifications;
      DROP TABLE IF EXISTS event_rsvps;
      DROP TABLE IF EXISTS events;
      DROP TABLE IF EXISTS discussion_replies;
      DROP TABLE IF EXISTS discussion_topics;
      DROP TABLE IF EXISTS food_posts;
      DROP TABLE IF EXISTS announcements;
      DROP TABLE IF EXISTS users;
    """
    
    DatabaseConnection.executeSqlScript(dropTablesScript)
    println("All tables dropped successfully!")
  }
}
