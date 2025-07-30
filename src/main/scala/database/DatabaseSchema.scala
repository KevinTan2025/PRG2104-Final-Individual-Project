package database

import util.PasswordHasher
import scala.util.{Try, Success, Failure}

/**
 * 数据库模式初始化和管理
 */
object DatabaseSchema {
  
  /**
   * 初始化所有数据库表
   */
  def initializeDatabase(): Unit = {
    if (!DatabaseConnection.isDatabaseInitialized) {
      createTables()
      if (!isDatabaseMarkedInitialized()) {
        insertSampleData()
        markDatabaseInitialized()
        println("数据库初始化成功！")
      } else {
        println("数据库已经初始化。")
      }
    } else {
      println("数据库已经初始化。")
    }
  }
  
  /**
   * 安全初始化数据库，返回 Try 类型
   */
  def initializeDatabaseSafe(): Try[Unit] = {
    Try {
      if (!DatabaseConnection.isDatabaseInitialized) {
        createTablesSafe().get
        if (!isDatabaseMarkedInitialized()) {
          insertSampleDataSafe().get
          markDatabaseInitializedSafe().get
          println("数据库初始化成功！")
        } else {
          println("数据库已经初始化。")
        }
      } else {
        println("数据库已经初始化。")
      }
    }
  }
  
  /**
   * 创建所有必需的表
   */
  private def createTables(): Unit = {
    val createTablesScript = getCreateTablesScript()
    DatabaseConnection.executeSqlScript(createTablesScript)
  }
  
  /**
   * 安全创建所有必需的表
   */
  private def createTablesSafe(): Try[Unit] = {
    val createTablesScript = getCreateTablesScript()
    DatabaseConnection.executeSqlScriptSafe(createTablesScript)
  }
  
  /**
   * 获取创建表的 SQL 脚本
   */
  private def getCreateTablesScript(): String = {
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
    createTablesScript
  }
  
  /**
   * 插入测试样本数据
   */
  private def insertSampleData(): Unit = {
    insertSampleDataSafe().recover {
      case ex => println(s"插入样本数据时出错: ${ex.getMessage}")
    }
  }
  
  /**
   * 安全插入测试样本数据
   */
  private def insertSampleDataSafe(): Try[Unit] = {
    Try {
    import java.time.LocalDateTime
    import java.util.UUID
    
    // Insert sample users with different passwords for admins and users
    val defaultPassword = "Password123!"  // Demo password for regular users
    val adminPassword = "Admin123*"       // Demo password for admin users
    val defaultPasswordHash = PasswordHasher.hashPassword(defaultPassword)
    val adminPasswordHash = PasswordHasher.hashPassword(adminPassword)

    val sampleUsers = List(
      ("admin1", "admin", "admin@community.org", "System Administrator", "admin@community.org", true),
      ("admin2", "community_manager", "manager@community.org", "Community Manager", "manager@community.org", true),
      ("john_doe", "john", "john@example.com", "John Doe", "john@example.com", false),
      ("jane_smith", "jane", "jane@example.com", "Jane Smith", "jane@example.com", false),
      ("mike_wilson", "mike", "mike@example.com", "Mike Wilson", "mike@example.com", false),
      ("sarah_brown", "sarah", "sarah@example.com", "Sarah Brown", "sarah@example.com", false),
      ("david_lee", "david_", "david@example.com", "David Lee", "david@example.com", false),
      ("emily_chen", "emily", "emily@example.com", "Emily Chen", "emily@example.com", false),
      ("alex_garcia", "alex", "alex@example.com", "Alex Garcia", "alex@example.com", false),
      ("lisa_taylor", "lisa", "lisa@example.com", "Lisa Taylor", "lisa@example.com", false),
      ("ryan_johnson", "ryan", "ryan@example.com", "Ryan Johnson", "ryan@example.com", false),
      ("maria_rodriguez", "maria", "maria@example.com", "Maria Rodriguez", "maria@example.com", false)
    )

    sampleUsers.foreach { case (userId, username, email, name, contactInfo, isAdmin) =>
      // Use admin password hash for admin users, regular password hash for others
      val passwordHash = if (isAdmin) adminPasswordHash else defaultPasswordHash
      
      DatabaseConnection.executeUpdate(
        """INSERT OR IGNORE INTO users 
           (user_id, username, email, name, contact_info, is_admin, password_hash, created_at, updated_at) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        userId, username, email, name, contactInfo, isAdmin, passwordHash,
        DatabaseConnection.formatDateTime(LocalDateTime.now()),
        DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
    }    // Insert sample announcements - use fixed IDs to prevent duplicates
    val announcement1Id = "announce-001-welcome"
    val announcement2Id = "announce-002-food-dist"
    val announcement3Id = "announce-003-garden"
    val announcement4Id = "announce-004-holiday"
    val announcement5Id = "announce-005-cooking"

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

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO announcements 
         (announcement_id, author_id, title, content, announcement_type, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      announcement3Id, "admin2",
      "New Community Garden Project",
      "Exciting news! We're starting a community garden project behind the community center. Everyone is welcome to participate and learn sustainable gardening techniques.",
      "COMMUNITY_EVENT",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO announcements 
         (announcement_id, author_id, title, content, announcement_type, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      announcement4Id, "admin1",
      "Holiday Food Drive - Help Needed!",
      "We're organizing a holiday food drive for families in need. Please donate non-perishable items or volunteer to help with distribution.",
      "FOOD_DISTRIBUTION",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(3)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(3))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO announcements 
         (announcement_id, author_id, title, content, announcement_type, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      announcement5Id, "admin2",
      "Cooking Workshop Series Starting Soon",
      "Learn how to prepare healthy, budget-friendly meals! Our cooking workshop series starts next week. Limited spots available.",
      "SKILL_SHARING",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(6)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(6))
    )    // Insert sample food posts - use fixed IDs to prevent duplicates
    val foodPost1Id = "food-post-001-vegetables"
    val foodPost2Id = "food-post-002-canned-goods"
    val foodPost3Id = "food-post-003-bread"
    val foodPost4Id = "food-post-004-family-dinner"
    val foodPost5Id = "food-post-005-farmers-market"
    val foodPost6Id = "food-post-006-baby-food"
    val foodPost7Id = "food-post-007-soup-kitchen"
    val foodPost8Id = "food-post-008-student-pantry"

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO food_posts 
         (post_id, author_id, title, description, post_type, quantity, location, expiry_date, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      foodPost1Id, "john_doe",
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
      foodPost2Id, "jane_smith",
      "Looking for canned goods",
      "Family in need of non-perishable food items.",
      "REQUEST", "Any amount", "North Side",
      DatabaseConnection.formatDateTime(LocalDateTime.now()),
      DatabaseConnection.formatDateTime(LocalDateTime.now())
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO food_posts 
         (post_id, author_id, title, description, post_type, quantity, location, expiry_date, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      foodPost3Id, "mike_wilson",
      "Homemade bread sharing",
      "I bake too much bread every week! Come pick up some fresh loaves.",
      "OFFER", "6 loaves", "Maple Street Community Kitchen",
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(1)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(2)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(2))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO food_posts 
         (post_id, author_id, title, description, post_type, quantity, location, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      foodPost4Id, "sarah_brown",
      "Need ingredients for family dinner",
      "Looking for rice, beans, and vegetables to cook for my elderly neighbors.",
      "REQUEST", "Enough for 8 people", "West End",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(1)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(1))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO food_posts 
         (post_id, author_id, title, description, post_type, quantity, location, expiry_date, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      foodPost5Id, "david_lee",
      "Surplus from farmers market",
      "Got extra fruits and vegetables from the farmers market. Perfect for families!",
      "OFFER", "Mixed produce boxes", "Eastside Parking Lot",
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(3)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO food_posts 
         (post_id, author_id, title, description, post_type, quantity, location, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      foodPost6Id, "emily_chen",
      "Baby food and formula needed",
      "New parent in need of baby food and formula. Any help appreciated!",
      "REQUEST", "Baby supplies", "Central District",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(4)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(4))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO food_posts 
         (post_id, author_id, title, description, post_type, quantity, location, expiry_date, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      foodPost7Id, "alex_garcia",
      "Community soup kitchen donation",
      "Restaurant closing early - have leftover prepared meals to share!",
      "OFFER", "15 prepared meals", "River Street",
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusHours(6)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusMinutes(30)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusMinutes(30))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO food_posts 
         (post_id, author_id, title, description, post_type, quantity, location, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      foodPost8Id, "lisa_taylor",
      "Pantry staples for student housing",
      "College students looking for pantry essentials - pasta, rice, canned goods.",
      "REQUEST", "Pantry basics", "University District",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(2)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(2))
    )
    
    // Insert sample discussion topics - use fixed IDs to prevent duplicates
    val topicId = "topic-001-urban-gardening"
    val topic2Id = "topic-002-food-preservation"
    val topic3Id = "topic-003-cookbook"
    val topic4Id = "topic-004-food-waste"
    val topic5Id = "topic-005-farmers-market"
    val topic6Id = "topic-006-kids-nutrition"

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_topics 
         (topic_id, author_id, title, description, category, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      topicId, "john_doe",
      "Tips for Urban Gardening",
      "Let's share tips and experiences about growing food in urban environments.",
      "SUSTAINABLE_AGRICULTURE",
      DatabaseConnection.formatDateTime(LocalDateTime.now()),
      DatabaseConnection.formatDateTime(LocalDateTime.now())
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_topics 
         (topic_id, author_id, title, description, category, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      topic2Id, "mike_wilson",
      "Best practices for food preservation",
      "What are your favorite methods for preserving fresh produce? Share your techniques!",
      "FOOD_PRESERVATION",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(8)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(8))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_topics 
         (topic_id, author_id, title, description, category, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      topic3Id, "david_lee",
      "Community cookbook project",
      "Anyone interested in creating a community cookbook with budget-friendly recipes?",
      "COMMUNITY_PROJECTS",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_topics 
         (topic_id, author_id, title, description, category, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      topic4Id, "alex_garcia",
      "Reducing food waste in our community",
      "How can we work together to reduce food waste? Share your ideas and success stories.",
      "SUSTAINABILITY",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(2)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(2))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_topics 
         (topic_id, author_id, title, description, category, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      topic5Id, "ryan_johnson",
      "Local farmers market recommendations",
      "Which farmers markets in our area offer the best value and quality? Let's help each other find great deals!",
      "RESOURCE_SHARING",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(12)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(12))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_topics 
         (topic_id, author_id, title, description, category, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?)""",
      topic6Id, "maria_rodriguez",
      "Teaching kids about healthy eating",
      "Parents and educators: how do you encourage children to eat healthy on a budget?",
      "EDUCATION",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(3)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(3))
    )
    
    // Insert sample events - use fixed IDs to prevent duplicates
    val eventId = "event-001-garden-workshop"
    val event2Id = "event-002-cooking-class"
    val event3Id = "event-003-food-distribution"
    val event4Id = "event-004-seed-swap"
    val event5Id = "event-005-preservation-workshop"
    val event6Id = "event-006-potluck-dinner"

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

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO events 
         (event_id, organizer_id, title, description, location, start_datetime, end_datetime, max_participants, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      event2Id, "mike_wilson",
      "Cooking Class: Healthy Meals on a Budget",
      "Learn to prepare nutritious, affordable meals for your family. Ingredients provided!",
      "Community Kitchen",
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(3)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(3).plusHours(2)),
      15,
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO events 
         (event_id, organizer_id, title, description, location, start_datetime, end_datetime, max_participants, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      event3Id, "admin2",
      "Monthly Food Distribution",
      "Free groceries for community members in need. First come, first served!",
      "Community Center Parking Lot",
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(5)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(5).plusHours(4)),
      null,
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(2)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(2))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO events 
         (event_id, organizer_id, title, description, location, start_datetime, end_datetime, max_participants, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      event4Id, "david_lee",
      "Seed Swap and Plant Exchange",
      "Bring your extra seeds, seedlings, or plants to trade with fellow gardeners!",
      "Central Park Pavilion",
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(10)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(10).plusHours(3)),
      25,
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(6)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(6))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO events 
         (event_id, organizer_id, title, description, location, start_datetime, end_datetime, max_participants, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      event5Id, "lisa_taylor",
      "Food Preservation Workshop",
      "Learn canning, pickling, and other preservation techniques to make your food last longer.",
      "Library Meeting Room",
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(14)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(14).plusHours(3)),
      12,
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO events 
         (event_id, organizer_id, title, description, location, start_datetime, end_datetime, max_participants, created_at, updated_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
      event6Id, "maria_rodriguez",
      "Community Potluck Dinner",
      "Bring a dish to share and meet your neighbors! A great way to build community connections.",
      "Community Center Main Hall",
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(12)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().plusDays(12).plusHours(3)),
      50,
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(18)),
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(18))
    )

    // Insert sample discussion replies - use fixed IDs to prevent duplicates
    val reply1Id = "reply-001-container-garden"
    val reply2Id = "reply-002-dehydrating"
    val reply3Id = "reply-003-cookbook-interest"
    val reply4Id = "reply-004-meal-planning"
    val reply5Id = "reply-005-market-tips"

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_replies 
         (reply_id, topic_id, author_id, content, created_at) 
         VALUES (?, ?, ?, ?, ?)""",
      reply1Id, topicId, "sarah_brown",
      "Great question! I've had success with container gardening on my apartment balcony. Cherry tomatoes and herbs work really well in small spaces.",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(2))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_replies 
         (reply_id, topic_id, author_id, content, created_at) 
         VALUES (?, ?, ?, ?, ?)""",
      reply2Id, topic2Id, "emily_chen",
      "I love dehydrating fruits and vegetables! It's a great way to preserve the harvest and they make healthy snacks.",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(4))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_replies 
         (reply_id, topic_id, author_id, content, created_at) 
         VALUES (?, ?, ?, ?, ?)""",
      reply3Id, topic3Id, "jane_smith",
      "I'm definitely interested! I have some amazing family recipes that are both cheap and nutritious. When do we start?",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(6))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_replies 
         (reply_id, topic_id, author_id, content, created_at) 
         VALUES (?, ?, ?, ?, ?)""",
      reply4Id, topic4Id, "ryan_johnson",
      "Meal planning has been a game changer for our family. We waste so much less food now that we plan our weeks in advance.",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(8))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO discussion_replies 
         (reply_id, topic_id, author_id, content, created_at) 
         VALUES (?, ?, ?, ?, ?)""",
      reply5Id, topic5Id, "alex_garcia",
      "The Saturday morning market at Central Square has amazing prices, especially in the last hour before closing!",
      DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(3))
    )

    // Insert sample event RSVPs
    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO event_rsvps (event_id, user_id, rsvp_date) 
         VALUES (?, ?, ?)""",
      eventId, "john_doe", DatabaseConnection.formatDateTime(LocalDateTime.now().minusDays(1))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO event_rsvps (event_id, user_id, rsvp_date) 
         VALUES (?, ?, ?)""",
      eventId, "jane_smith", DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(8))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO event_rsvps (event_id, user_id, rsvp_date) 
         VALUES (?, ?, ?)""",
      event2Id, "sarah_brown", DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(12))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO event_rsvps (event_id, user_id, rsvp_date) 
         VALUES (?, ?, ?)""",
      event2Id, "emily_chen", DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(6))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO event_rsvps (event_id, user_id, rsvp_date) 
         VALUES (?, ?, ?)""",
      event3Id, "lisa_taylor", DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(4))
    )

    // Insert sample notifications - use fixed IDs to prevent duplicates
    val notif1Id = "notif-001-new-event"
    val notif2Id = "notif-002-food-available"
    val notif3Id = "notif-003-discussion-reply"
    val notif4Id = "notif-004-event-reminder"

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO notifications 
         (notification_id, recipient_id, sender_id, type, title, message, related_id, created_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
      notif1Id, "john_doe", "admin1",
      "ANNOUNCEMENT", "New Community Event",
      "A new community garden workshop has been scheduled. Check it out!",
      eventId, DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(2))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO notifications 
         (notification_id, recipient_id, sender_id, type, title, message, related_id, created_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
      notif2Id, "jane_smith", "john_doe",
      "FOOD_POST", "Food Available",
      "Fresh vegetables are available for pickup at the community center.",
      foodPost1Id, DatabaseConnection.formatDateTime(LocalDateTime.now().minusHours(1))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO notifications 
         (notification_id, recipient_id, sender_id, type, title, message, related_id, created_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
      notif3Id, "mike_wilson", "sarah_brown",
      "DISCUSSION", "Reply to Your Post",
      "Someone replied to your discussion about urban gardening tips.",
      topicId, DatabaseConnection.formatDateTime(LocalDateTime.now().minusMinutes(30))
    )

    DatabaseConnection.executeUpdate(
      """INSERT OR IGNORE INTO notifications 
         (notification_id, recipient_id, sender_id, type, title, message, related_id, created_at) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
      notif4Id, "david_lee", "admin2",
      "EVENT_REMINDER", "Event Tomorrow",
      "Don't forget about the cooking class tomorrow at 2 PM!",
      event2Id, DatabaseConnection.formatDateTime(LocalDateTime.now().minusMinutes(15))
    )

    // Insert sample food stock items
    val sampleFoodStocks = List(
      // IN_STOCK items (current quantity > minimum threshold)
      ("stock1", "Rice", "GRAINS", 25.5, "kg", 5.0, Some(LocalDateTime.now().plusMonths(6)), true, "Dry Storage"),
      ("stock2", "Apples", "FRUITS", 8.0, "kg", 2.0, Some(LocalDateTime.now().plusDays(14)), false, "Refrigerator"),
      ("stock3", "Canned Beans", "PROTEIN", 12.0, "cans", 3.0, Some(LocalDateTime.now().plusYears(1)), true, "Pantry"),
      ("stock4", "Fresh Milk", "DAIRY", 6.0, "liters", 2.0, Some(LocalDateTime.now().plusDays(7)), false, "Refrigerator"),
      ("stock5", "Bread", "GRAINS", 4.0, "loaves", 1.0, Some(LocalDateTime.now().plusDays(3)), true, "Bread Box"),
      ("stock6", "Instant Noodles", "PACKAGED_FOOD", 20.0, "packs", 5.0, Some(LocalDateTime.now().plusMonths(8)), true, "Pantry"),
      ("stock7", "Frozen Chicken", "FROZEN_FOOD", 15.0, "kg", 3.0, Some(LocalDateTime.now().plusMonths(3)), true, "Freezer"),
      ("stock8", "Cookies", "SNACKS", 10.0, "boxes", 2.0, Some(LocalDateTime.now().plusDays(30)), true, "Pantry"),
      
      // LOW_STOCK items (current quantity <= minimum threshold but > 0)
      ("stock9", "Carrots", "VEGETABLES", 1.5, "kg", 2.0, Some(LocalDateTime.now().plusDays(10)), false, "Refrigerator"),
      ("stock10", "Orange Juice", "BEVERAGES", 1.0, "bottles", 2.0, Some(LocalDateTime.now().plusDays(5)), true, "Refrigerator"),
      ("stock11", "Canned Tomatoes", "CANNED_FOOD", 2.0, "cans", 3.0, Some(LocalDateTime.now().plusMonths(18)), true, "Pantry"),
      ("stock12", "Cheese", "DAIRY", 0.5, "kg", 1.0, Some(LocalDateTime.now().plusDays(12)), false, "Refrigerator"),
      ("stock13", "Pasta", "GRAINS", 2.0, "kg", 3.0, Some(LocalDateTime.now().plusMonths(12)), true, "Dry Storage"),
      ("stock14", "Bananas", "FRUITS", 1.0, "kg", 2.0, Some(LocalDateTime.now().plusDays(4)), false, "Counter"),
      
      // OUT_OF_STOCK items (current quantity = 0)
      ("stock15", "Potato Chips", "SNACKS", 0.0, "bags", 5.0, Some(LocalDateTime.now().plusDays(45)), true, "Pantry"),
      ("stock16", "Yogurt", "DAIRY", 0.0, "cups", 8.0, Some(LocalDateTime.now().plusDays(8)), false, "Refrigerator"),
      ("stock17", "Frozen Vegetables", "FROZEN_FOOD", 0.0, "bags", 4.0, Some(LocalDateTime.now().plusMonths(6)), true, "Freezer"),
      ("stock18", "Canned Soup", "CANNED_FOOD", 0.0, "cans", 6.0, Some(LocalDateTime.now().plusMonths(24)), true, "Pantry"),
      
      // EXPIRED items (past expiry date but may still have stock)
      ("stock19", "Old Lettuce", "VEGETABLES", 2.0, "heads", 1.0, Some(LocalDateTime.now().minusDays(3)), false, "Refrigerator"),
      ("stock20", "Expired Milk", "DAIRY", 1.5, "liters", 2.0, Some(LocalDateTime.now().minusDays(2)), false, "Refrigerator"),
      ("stock21", "Stale Bread", "GRAINS", 1.0, "loaves", 1.0, Some(LocalDateTime.now().minusDays(5)), true, "Bread Box"),
      ("stock22", "Old Yogurt", "DAIRY", 3.0, "cups", 2.0, Some(LocalDateTime.now().minusDays(1)), false, "Refrigerator"),
      
      // EXPIRING SOON items (expires within 3 days)
      ("stock23", "Fresh Fish", "PROTEIN", 2.5, "kg", 1.0, Some(LocalDateTime.now().plusDays(1)), false, "Refrigerator"),
      ("stock24", "Soft Cheese", "DAIRY", 0.8, "kg", 0.5, Some(LocalDateTime.now().plusDays(2)), false, "Refrigerator"),
      ("stock25", "Fresh Berries", "FRUITS", 1.2, "kg", 0.5, Some(LocalDateTime.now().plusDays(3)), false, "Refrigerator"),
      
      // More diverse categories and locations
      ("stock26", "Cooking Oil", "OTHER", 3.0, "bottles", 1.0, Some(LocalDateTime.now().plusMonths(18)), true, "Pantry"),
      ("stock27", "Green Tea", "BEVERAGES", 5.0, "boxes", 2.0, Some(LocalDateTime.now().plusYears(2)), true, "Dry Storage"),
      ("stock28", "Frozen Pizza", "FROZEN_FOOD", 6.0, "boxes", 2.0, Some(LocalDateTime.now().plusMonths(8)), true, "Freezer"),
      ("stock29", "Peanut Butter", "PROTEIN", 4.0, "jars", 1.0, Some(LocalDateTime.now().plusMonths(12)), true, "Pantry"),
      ("stock30", "Onions", "VEGETABLES", 5.0, "kg", 2.0, Some(LocalDateTime.now().plusDays(21)), false, "Storage Room"),
      ("stock31", "Breakfast Cereal", "GRAINS", 8.0, "boxes", 3.0, Some(LocalDateTime.now().plusMonths(9)), true, "Pantry"),
      ("stock32", "Ice Cream", "FROZEN_FOOD", 2.0, "tubs", 1.0, Some(LocalDateTime.now().plusMonths(6)), true, "Freezer"),
      ("stock33", "Crackers", "SNACKS", 12.0, "boxes", 4.0, Some(LocalDateTime.now().plusMonths(10)), true, "Pantry"),
      ("stock34", "Canned Corn", "CANNED_FOOD", 8.0, "cans", 3.0, Some(LocalDateTime.now().plusYears(2)), true, "Pantry"),
      ("stock35", "Energy Drinks", "BEVERAGES", 24.0, "cans", 6.0, Some(LocalDateTime.now().plusMonths(15)), true, "Storage Room")
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
      
      // Add initial stock movement record - use stock ID based movement ID to prevent duplicates
      val movementId = s"movement-initial-$stockId"
      DatabaseConnection.executeUpdate(
        """INSERT OR IGNORE INTO stock_movements 
           (movement_id, stock_id, action_type, quantity, previous_quantity, 
            new_quantity, user_id, notes, timestamp) 
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        movementId, stockId, "STOCK_IN", quantity, 0.0, quantity, "admin1",
        "Initial stock entry", DatabaseConnection.formatDateTime(LocalDateTime.now())
      )
    }
    
      println("样本数据插入成功！")
      println("样本用户创建，密码如下：")
      println("  - 管理员用户 (admin, community_manager): 'Admin123*'")
      println("  - 普通用户: 'Password123!'")
      println("可用用户: admin/community_manager/john/jane/mike/sarah/david_/emily/alex/lisa/ryan/maria")
      println("样本社区数据包括：")
      println("  - 5个来自管理员和社区成员的公告")
      println("  - 8个食物帖子（提供和请求）")
      println("  - 6个关于各种社区主题的讨论话题")
      println("  - 6个即将举行的社区活动")
      println("  - 讨论回复和活动回复")
      println("  - 用户的样本通知")
      println("为测试库存管理创建的样本食物库存项目。")
    }
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
   * 标记数据库为已初始化
   */
  private def markDatabaseInitialized(): Unit = {
    markDatabaseInitializedSafe().recover {
      case ex => println(s"标记数据库初始化时出错: ${ex.getMessage}")
    }
  }
  
  /**
    * 安全标记数据库为已初始化
    */
   private def markDatabaseInitializedSafe(): Try[Unit] = {
     Try {
       import java.time.LocalDateTime
       val now = DatabaseConnection.formatDateTime(LocalDateTime.now())
       DatabaseConnection.executeUpdate(
         """INSERT OR REPLACE INTO system_metadata 
            (key, value, created_at, updated_at) 
            VALUES ('database_initialized', 'true', ?, ?)""",
         now, now
       )
       () // 返回 Unit
     }
   }
}
