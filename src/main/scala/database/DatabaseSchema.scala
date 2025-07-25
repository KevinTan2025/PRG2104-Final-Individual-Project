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
      if (!isDatabaseMarkedInitialized()) {
        insertSampleData()
        markDatabaseInitialized()
        println("Database initialized successfully!")
      } else {
        println("Database already initialized.")
      }
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
        status TEXT NOT NULL DEFAULT 'PENDING',
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
      
      -- Food stocks table
      CREATE TABLE IF NOT EXISTS food_stocks (
        stock_id TEXT PRIMARY KEY,
        food_name TEXT NOT NULL,
        category TEXT NOT NULL,
        current_quantity REAL NOT NULL DEFAULT 0,
        unit TEXT NOT NULL,
        minimum_threshold REAL NOT NULL DEFAULT 0,
        expiry_date TEXT,
        is_packaged BOOLEAN NOT NULL DEFAULT 0,
        location TEXT NOT NULL DEFAULT 'Main Storage',
        last_modified_by TEXT,
        last_modified_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (last_modified_by) REFERENCES users(user_id)
      );
      
      -- Stock movements table (for tracking all stock changes)
      CREATE TABLE IF NOT EXISTS stock_movements (
        movement_id TEXT PRIMARY KEY,
        stock_id TEXT NOT NULL,
        action_type TEXT NOT NULL,
        quantity REAL NOT NULL,
        previous_quantity REAL NOT NULL,
        new_quantity REAL NOT NULL,
        user_id TEXT NOT NULL,
        notes TEXT DEFAULT '',
        timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (stock_id) REFERENCES food_stocks(stock_id),
        FOREIGN KEY (user_id) REFERENCES users(user_id)
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
      CREATE INDEX IF NOT EXISTS idx_food_stocks_category ON food_stocks(category);
      CREATE INDEX IF NOT EXISTS idx_food_stocks_location ON food_stocks(location);
      CREATE INDEX IF NOT EXISTS idx_food_stocks_expiry ON food_stocks(expiry_date);
      CREATE INDEX IF NOT EXISTS idx_food_stocks_quantity ON food_stocks(current_quantity);
      CREATE INDEX IF NOT EXISTS idx_stock_movements_stock ON stock_movements(stock_id);
      CREATE INDEX IF NOT EXISTS idx_stock_movements_user ON stock_movements(user_id);
      CREATE INDEX IF NOT EXISTS idx_stock_movements_timestamp ON stock_movements(timestamp);
      
      -- System metadata table for tracking initialization state
      CREATE TABLE IF NOT EXISTS system_metadata (
        key TEXT PRIMARY KEY,
        value TEXT NOT NULL,
        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
      );
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
      "",
      "Learn how to start and maintain a community garden. All skill levels welcome!",
      "Community Center Room A",
      DatabaseConnection.formatDateTime(startTime),
      DatabaseConnection.formatDateTime(endTime),
      20,
      DatabaseConnection.formatDateTime(LocalDateTime.now()),
      DatabaseConnection.formatDateTime(LocalDateTime.now())
    )
    
    // Insert sample food stock items
    val sampleFoodStocks = List(
      ("stock1", "Rice", "GRAINS", 25.5, "kg", 5.0, Some(LocalDateTime.now().plusMonths(6)), true, "Dry Storage"),
      ("stock2", "Apples", "FRUITS", 8.0, "kg", 2.0, Some(LocalDateTime.now().plusDays(14)), false, "Refrigerator"),
      ("stock3", "Canned Beans", "PROTEIN", 12.0, "cans", 3.0, Some(LocalDateTime.now().plusYears(1)), true, "Pantry"),
      ("stock4", "Fresh Milk", "DAIRY", 6.0, "liters", 2.0, Some(LocalDateTime.now().plusDays(7)), false, "Refrigerator"),
      ("stock5", "Bread", "GRAINS", 4.0, "loaves", 1.0, Some(LocalDateTime.now().plusDays(3)), true, "Bread Box"),
      ("stock6", "Carrots", "VEGETABLES", 3.5, "kg", 1.0, Some(LocalDateTime.now().plusDays(10)), false, "Refrigerator"),
      ("stock7", "Instant Noodles", "PACKAGED_FOOD", 20.0, "packs", 5.0, Some(LocalDateTime.now().plusMonths(8)), true, "Pantry"),
      ("stock8", "Orange Juice", "BEVERAGES", 2.0, "bottles", 1.0, Some(LocalDateTime.now().plusDays(5)), true, "Refrigerator")
    )
    
    sampleFoodStocks.foreach { case (stockId, foodName, category, quantity, unit, threshold, expiryDate, isPackaged, location) =>
      val expiryDateStr = expiryDate.map(DatabaseConnection.formatDateTime)
      DatabaseConnection.executeUpdate(
        """INSERT OR IGNORE INTO food_stocks 
           (stock_id, food_name, category, current_quantity, unit, minimum_threshold, 
            expiry_date, is_packaged, location, last_modified_by, last_modified_date, created_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        stockId, foodName, category, quantity, unit, threshold,
        expiryDateStr.orNull, isPackaged, location, "admin1",
        DatabaseConnection.formatDateTime(LocalDateTime.now()),
        DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
      
      // Add initial stock movement record
      val movementId = UUID.randomUUID().toString
      DatabaseConnection.executeUpdate(
        """INSERT OR IGNORE INTO stock_movements 
           (movement_id, stock_id, action_type, quantity, previous_quantity, 
            new_quantity, user_id, notes, timestamp) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        movementId, stockId, "STOCK_IN", quantity, 0.0, quantity, "admin1",
        "Initial stock entry", DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
    }
    
    println("Sample data inserted successfully!")
    println("Sample users created with default password: 'Password123!'")
    println("Available users: admin/john_doe/jane_smith")
    println("Sample food stock items created for testing inventory management.")
  }
  
  /**
   * Drop all tables (for testing/reset purposes)
   */
  def dropAllTables(): Unit = {
    val dropTablesScript = """
      DROP TABLE IF EXISTS stock_movements;
      DROP TABLE IF EXISTS food_stocks;
      DROP TABLE IF EXISTS comments;
      DROP TABLE IF EXISTS notifications;
      DROP TABLE IF EXISTS event_rsvps;
      DROP TABLE IF EXISTS events;
      DROP TABLE IF EXISTS discussion_replies;
      DROP TABLE IF EXISTS discussion_topics;
      DROP TABLE IF EXISTS food_posts;
      DROP TABLE IF EXISTS announcements;
      DROP TABLE IF EXISTS users;
      DROP TABLE IF EXISTS system_metadata;
    """
    
    DatabaseConnection.executeSqlScript(dropTablesScript)
    println("All tables dropped successfully!")
  }
  
  /**
   * Reset database completely - drops all tables and recreates with sample data
   */
  def resetDatabase(): Unit = {
    dropAllTables()
    createTables()
    insertSampleData()
    markDatabaseInitialized()
    println("Database reset completed successfully!")
  }
  
  /**
   * Check if database has been marked as initialized
   */
  private def isDatabaseMarkedInitialized(): Boolean = {
    try {
      val rs = DatabaseConnection.executeQuery(
        "SELECT value FROM system_metadata WHERE key = 'database_initialized'"
      )
      val isInitialized = rs.next() && rs.getString("value") == "true"
      rs.close()
      isInitialized
    } catch {
      case _: Exception => false
    }
  }
  
  /**
   * Mark database as initialized
   */
  private def markDatabaseInitialized(): Unit = {
    try {
      import java.time.LocalDateTime
      val now = DatabaseConnection.formatDateTime(LocalDateTime.now())
      DatabaseConnection.executeUpdate(
        """INSERT OR REPLACE INTO system_metadata 
           (key, value, created_at, updated_at) 
           VALUES ('database_initialized', 'true', ?, ?)""",
        now, now
      )
    } catch {
      case e: Exception =>
        println(s"Warning: Could not mark database as initialized: ${e.getMessage}")
    }
  }
}
