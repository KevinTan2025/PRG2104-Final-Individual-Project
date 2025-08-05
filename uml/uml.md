# Community Engagement Platform - UML Class Diagram

This UML diagram represents the complete class structure of the Community Engagement Platform built in Scala.

```mermaid
classDiagram
    %% Core Model Classes and Traits
    
    class User {
        <<sealed trait>>
        +String userId
        +String username
        +String email
        +String name
        +String contactInfo
        +String passwordHash
        +LocalDateTime registrationDate
        +Boolean isActive
        +String userRole
        +updateProfile(newName: String, newContactInfo: String): Try[User]
        +setPassword(newPassword: String): Try[User]
        +withPasswordHash(hash: String): User
        +verifyPassword(password: String): Boolean
        +resetPassword(currentPassword: String, newPassword: String): Try[User]
        +hasAdminPrivileges(): Boolean
        +toggleActiveStatus(): User
    }
    
    class CommunityMember {
        +String userId
        +String username
        +String email
        +String name
        +String contactInfo
        +String passwordHash
        +LocalDateTime registrationDate
        +Boolean isActive
        +String userRole
        +createFoodRequest(): String
        +createFoodOffer(): String
    }
    
    class AdminUser {
        +String userId
        +String username
        +String email
        +String name
        +String contactInfo
        +String passwordHash
        +LocalDateTime registrationDate
        +Boolean isActive
        +String userRole
        +Boolean hasAdminPrivileges
        +createModerationAction(contentId: String): String
        +createDeletionAction(contentId: String): String
        +createUserManagementAction(targetUserId: String, action: String): String
    }
    
    class Likeable {
        <<trait>>
        +Int likes
        +List[Comment] comments
        +addLike(): Likeable
        +removeLike(): Likeable
        +addComment(comment: Comment): Likeable
    }
    
    class Moderatable {
        <<trait>>
        +Boolean isModerated
        +Option[String] moderatedBy
        +Option[LocalDateTime] moderationDate
        +moderate(adminId: String): Moderatable
    }
    
    class Comment {
        +String commentId
        +String authorId
        +String content
        +LocalDateTime timestamp
        +Int likes
        +List[Comment] comments
        +Boolean isModerated
        +Option[String] moderatedBy
        +Option[LocalDateTime] moderationDate
    }
    
    %% Event Related Classes
    
    class Event {
        +String eventId
        +String organizerId
        +String title
        +String description
        +String location
        +LocalDateTime startDateTime
        +LocalDateTime endDateTime
        +Option[Int] maxParticipants
        +LocalDateTime createdAt
        +EventStatus status
        +List[String] participants
        +List[String] waitingList
        +Int likes
        +List[Comment] comments
        +Boolean isModerated
        +Option[String] moderatedBy
        +Option[LocalDateTime] moderationDate
        +rsvp(userId: String): (Event, Boolean)
        +cancelRsvp(userId: String): Event
        +isFull(): Boolean
        +availableSpots(): Option[Int]
        +start(): Event
        +complete(): Event
        +cancel(): Event
        +isPast(): Boolean
        +isOngoing(): Boolean
    }
    
    class EventStatus {
        <<enumeration>>
        UPCOMING
        ONGOING
        COMPLETED
        CANCELLED
    }
    
    %% Food Related Classes
    
    class FoodPost {
        +String postId
        +String authorId
        +String title
        +String description
        +FoodPostType postType
        +String quantity
        +String location
        +Option[LocalDateTime] expiryDate
        +LocalDateTime timestamp
        +FoodPostStatus status
        +Option[String] acceptedBy
        +Option[LocalDateTime] acceptedDate
        +Option[LocalDateTime] completedDate
        +Int likes
        +List[Comment] comments
        +Boolean isModerated
        +Option[String] moderatedBy
        +Option[LocalDateTime] moderationDate
        +accept(userId: String): FoodPost
        +complete(): FoodPost
        +cancel(): FoodPost
        +isActive(): Boolean
        +isExpired(): Boolean
    }
    
    class FoodPostStatus {
        <<enumeration>>
        PENDING
        ACCEPTED
        COMPLETED
        CANCELLED
    }
    
    class FoodPostType {
        <<enumeration>>
        REQUEST
        OFFER
    }
    
    class FoodStock {
        +String stockId
        +String foodName
        +FoodCategory category
        +Double currentQuantity
        +String unit
        +Double minimumThreshold
        +Option[LocalDateTime] expiryDate
        +Boolean isPackaged
        +String location
        +Option[String] lastModifiedBy
        +LocalDateTime lastModifiedDate
        +LocalDateTime createdAt
        +List[StockMovement] stockHistory
        +stockStatus(): StockStatus
        +isExpired(): Boolean
        +isExpiringSoon(days: Int): Boolean
        +addStock(quantity: Double, userId: String, notes: String): FoodStock
        +removeStock(quantity: Double, userId: String, notes: String): FoodStock
        +adjustStock(newQuantity: Double, userId: String, notes: String): FoodStock
        +markExpired(userId: String): FoodStock
    }
    
    class FoodCategory {
        <<enumeration>>
        VEGETABLES
        FRUITS
        GRAINS
        PROTEIN
        DAIRY
        BEVERAGES
        SNACKS
        PACKAGED_FOOD
        FROZEN_FOOD
        CANNED_FOOD
        OTHER
    }
    
    class StockStatus {
        <<enumeration>>
        IN_STOCK
        LOW_STOCK
        OUT_OF_STOCK
        EXPIRED
    }
    
    class StockMovement {
        +String movementId
        +String stockId
        +StockActionType actionType
        +Double quantity
        +Double previousQuantity
        +Double newQuantity
        +String userId
        +String notes
        +LocalDateTime timestamp
    }
    
    class StockActionType {
        <<enumeration>>
        STOCK_IN
        STOCK_OUT
        EXPIRED_REMOVAL
        ADJUSTMENT
    }
    
    %% Discussion Related Classes
    
    class DiscussionTopic {
        +String topicId
        +String authorId
        +String title
        +String description
        +DiscussionCategory category
        +LocalDateTime timestamp
        +Boolean isActive
        +Boolean isPinned
        +List[Reply] replies
        +Int likes
        +List[Comment] comments
        +Boolean isModerated
        +Option[String] moderatedBy
        +Option[LocalDateTime] moderationDate
        +addReply(reply: Reply): DiscussionTopic
        +pin(): DiscussionTopic
        +unpin(): DiscussionTopic
        +close(): DiscussionTopic
        +replyCount(): Int
    }
    
    class Reply {
        +String replyId
        +String topicId
        +String authorId
        +String content
        +LocalDateTime timestamp
        +Int likes
        +List[Comment] comments
        +Boolean isModerated
        +Option[String] moderatedBy
        +Option[LocalDateTime] moderationDate
    }
    
    class DiscussionCategory {
        <<enumeration>>
        NUTRITION
        SUSTAINABLE_AGRICULTURE
        FOOD_SECURITY
        COMMUNITY_GARDEN
        COOKING_TIPS
        GENERAL
        EVENTS
        ANNOUNCEMENTS
        FOOD_PRESERVATION
        COMMUNITY_PROJECTS
        SUSTAINABILITY
        RESOURCE_SHARING
        EDUCATION
    }
    
    %% Announcement Related Classes
    
    class Announcement {
        +String announcementId
        +String authorId
        +String title
        +String content
        +LocalDateTime timestamp
        +AnnouncementPriority priority
        +List[String] tags
        +Option[LocalDateTime] expiryDate
        +Boolean isActive
        +Boolean isPinned
        +Int likes
        +List[Comment] comments
        +Boolean isModerated
        +Option[String] moderatedBy
        +Option[LocalDateTime] moderationDate
        +pin(): Announcement
        +unpin(): Announcement
        +deactivate(): Announcement
        +activate(): Announcement
        +isExpired(): Boolean
    }
    
    class AnnouncementPriority {
        <<enumeration>>
        LOW
        NORMAL
        HIGH
        URGENT
    }
    
    %% Notification Related Classes
    
    class Notification {
        +String notificationId
        +String recipientId
        +Option[String] senderId
        +String title
        +String message
        +NotificationType notificationType
        +Option[String] relatedItemId
        +LocalDateTime timestamp
        +Boolean isRead
        +Option[LocalDateTime] readAt
        +markAsRead(): Notification
        +markAsUnread(): Notification
        +isRecent(): Boolean
    }
    
    class NotificationType {
        <<enumeration>>
        MESSAGE
        EVENT_REMINDER
        ANNOUNCEMENT
        FOOD_REQUEST
        FOOD_OFFER
        COMMENT
        LIKE
        RSVP_CONFIRMATION
        SYSTEM
    }
    
    %% Activity Feed Related Classes
    
    class ActivityFeedItem {
        +String id
        +ActivityFeedType feedType
        +String title
        +String content
        +String authorId
        +String authorName
        +LocalDateTime timestamp
        +Int likes
        +List[Comment] comments
        +Option[String] category
        +Option[String] location
        +Option[LocalDateTime] eventDateTime
        +Option[LocalDateTime] expiryDate
        +Set[String] likedByUsers
        +Boolean isActive
        +addLike(userId: String): Either[String, ActivityFeedItem]
        +removeLike(userId: String): Either[String, ActivityFeedItem]
        +toggleLike(userId: String): (ActivityFeedItem, Boolean)
        +addComment(comment: Comment): ActivityFeedItem
        +isLikedBy(userId: String): Boolean
        +timeAgo(): String
    }
    
    class ActivityFeedType {
        <<enumeration>>
        ANNOUNCEMENT
        FOOD_SHARING
        DISCUSSION
        EVENT
        USER_ACTIVITY
    }
    
    %% Manager Classes
    
    class Manager {
        <<trait>>
        #ConcurrentHashMap[String, T] items
        +add(id: String, item: T): Unit
        +remove(id: String): Option[T]
        +get(id: String): Option[T]
        +all(): List[T]
        +exists(id: String): Boolean
        +size(): Int
        +clear(): Unit
    }
    
    class UserManager {
        -ConcurrentHashMap[String, Boolean] usernames
        -ConcurrentHashMap[String, Boolean] emails
        +registerUser(user: User): Boolean
        +authenticateUser(username: String, password: String): Option[User]
        +getUserByUsername(username: String): Option[User]
        +getUserByEmail(email: String): Option[User]
        +isUsernameAvailable(username: String): Boolean
        +isEmailAvailable(email: String): Boolean
        +updateUser(userId: String, updatedUser: User): Boolean
        +deactivateUser(userId: String): Boolean
        +activateUser(userId: String): Boolean
        +getAllActiveUsers(): List[User]
        +searchUsers(query: String): List[User]
    }
    
    class EventManager {
        +createEvent(event: Event): String
        +updateEvent(eventId: String, updatedEvent: Event): Boolean
        +deleteEvent(eventId: String): Boolean
        +getEvent(eventId: String): Option[Event]
        +getAllEvents(): List[Event]
        +getEventsByStatus(status: EventStatus): List[Event]
        +getEventsByOrganizer(organizerId: String): List[Event]
        +rsvpToEvent(eventId: String, userId: String): Boolean
        +cancelRsvp(eventId: String, userId: String): Boolean
        +getUpcomingEvents(): List[Event]
        +searchEvents(query: String): List[Event]
    }
    
    class FoodPostManager {
        +createFoodPost(post: FoodPost): String
        +updateFoodPost(postId: String, updatedPost: FoodPost): Boolean
        +deleteFoodPost(postId: String): Boolean
        +getFoodPost(postId: String): Option[FoodPost]
        +getAllFoodPosts(): List[FoodPost]
        +getFoodPostsByAuthor(authorId: String): List[FoodPost]
        +getFoodPostsByType(postType: FoodPostType): List[FoodPost]
        +getFoodPostsByStatus(status: FoodPostStatus): List[FoodPost]
        +acceptFoodPost(postId: String, userId: String): Boolean
        +completeFoodPost(postId: String): Boolean
        +searchFoodPosts(query: String): List[FoodPost]
    }
    
    class FoodStockManager {
        +addFoodStock(stock: FoodStock): String
        +updateFoodStock(stockId: String, updatedStock: FoodStock): Boolean
        +deleteFoodStock(stockId: String): Boolean
        +getFoodStock(stockId: String): Option[FoodStock]
        +getAllFoodStocks(): List[FoodStock]
        +getLowStockItems(): List[FoodStock]
        +getExpiredItems(): List[FoodStock]
        +getExpiringSoonItems(days: Int): List[FoodStock]
        +getFoodStocksByCategory(category: FoodCategory): List[FoodStock]
        +searchFoodStocks(query: String): List[FoodStock]
        +addStock(stockId: String, quantity: Double, userId: String): Boolean
        +removeStock(stockId: String, quantity: Double, userId: String): Boolean
    }
    
    class DiscussionForumManager {
        +createTopic(topic: DiscussionTopic): String
        +updateTopic(topicId: String, updatedTopic: DiscussionTopic): Boolean
        +deleteTopic(topicId: String): Boolean
        +getTopic(topicId: String): Option[DiscussionTopic]
        +getAllTopics(): List[DiscussionTopic]
        +getTopicsByCategory(category: DiscussionCategory): List[DiscussionTopic]
        +getTopicsByAuthor(authorId: String): List[DiscussionTopic]
        +addReply(topicId: String, reply: Reply): Boolean
        +pinTopic(topicId: String): Boolean
        +unpinTopic(topicId: String): Boolean
        +searchTopics(query: String): List[DiscussionTopic]
    }
    
    class AnnouncementBoard {
        +createAnnouncement(announcement: Announcement): String
        +updateAnnouncement(announcementId: String, updatedAnnouncement: Announcement): Boolean
        +deleteAnnouncement(announcementId: String): Boolean
        +getAnnouncement(announcementId: String): Option[Announcement]
        +getAllAnnouncements(): List[Announcement]
        +getActiveAnnouncements(): List[Announcement]
        +getPinnedAnnouncements(): List[Announcement]
        +getAnnouncementsByPriority(priority: AnnouncementPriority): List[Announcement]
        +pinAnnouncement(announcementId: String): Boolean
        +unpinAnnouncement(announcementId: String): Boolean
        +searchAnnouncements(query: String): List[Announcement]
    }
    
    %% Service Classes
    
    class CommunityEngagementService {
        -UserManager userManager
        -EventManager eventManager
        -FoodPostManager foodPostManager
        -FoodStockManager foodStockManager
        -DiscussionForumManager discussionManager
        -AnnouncementBoard announcementBoard
        -Option[User] currentUser
        +registerUser(username: String, email: String, name: String, contactInfo: String, password: String): Boolean
        +loginUser(username: String, password: String): Boolean
        +logoutUser(): Unit
        +getCurrentUser(): Option[User]
        +isLoggedIn(): Boolean
        +createEvent(title: String, description: String, location: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): Boolean
        +createFoodPost(title: String, description: String, postType: FoodPostType, quantity: String, location: String): Boolean
        +createDiscussionTopic(title: String, description: String, category: DiscussionCategory): Boolean
        +createAnnouncement(title: String, content: String, priority: AnnouncementPriority): Boolean
        +getActivityFeed(): List[ActivityFeedItem]
        +initializeSampleData(): Unit
    }
    
    class ActivityFeedService {
        -CommunityEngagementService communityService
        +getUnifiedActivityFeed(): List[ActivityFeedItem]
        +getActivityFeedByType(feedType: ActivityFeedType): List[ActivityFeedItem]
        +searchActivityFeed(query: String): List[ActivityFeedItem]
        +getRecentActivity(hours: Int): List[ActivityFeedItem]
        +getUserActivity(userId: String): List[ActivityFeedItem]
    }
    
    class OTPService {
        -Map[String, String] otpStore
        -Map[String, LocalDateTime] otpExpiry
        +generateOTP(email: String): String
        +verifyOTP(email: String, otp: String): Boolean
        +isOTPValid(email: String, otp: String): Boolean
        +clearExpiredOTPs(): Unit
    }
    
    %% Database Related Classes
    
    class DatabaseConnection {
        <<singleton>>
        -String DB_URL
        -AtomicReference[Option[Connection]] connectionRef
        +connectionSafe(): Try[Connection]
        +executeQuery[T](sql: String, parameters: Any*)(processor: ResultSet => T): Try[T]
        +executeUpdate(sql: String, parameters: Any*): Try[Int]
        +executeTransaction[T](operations: Connection => T): Try[T]
        +close(): Unit
        -ensureDbDirectoryExists(): Unit
    }
    
    class DatabaseError {
        <<sealed trait>>
        +String message
        +Option[Throwable] cause
    }
    
    class DatabaseConnectionError {
        +String message
        +Option[Throwable] cause
    }
    
    class DriverNotFoundError {
        +String message
        +Option[Throwable] cause
    }
    
    class QueryExecutionError {
        +String message
        +Option[Throwable] cause
    }
    
    class DatabaseSchema {
        +String CREATE_USERS_TABLE
        +String CREATE_EVENTS_TABLE
        +String CREATE_FOOD_POSTS_TABLE
        +String CREATE_FOOD_STOCKS_TABLE
        +String CREATE_ANNOUNCEMENTS_TABLE
        +String CREATE_DISCUSSION_TOPICS_TABLE
        +String CREATE_NOTIFICATIONS_TABLE
        +initializeDatabase(): Try[Unit]
        +createTables(): Try[Unit]
        +dropAllTables(): Try[Unit]
    }
    
    %% Utility Classes
    
    class PasswordHasher {
        <<object>>
        +hashPassword(password: String): String
        +verifyPassword(password: String, hash: String): Boolean
        +isPasswordValid(password: String): Boolean
        +generateSalt(): String
        -hashPasswordWithSalt(password: String, salt: String): String
    }
    
    %% GUI Related Classes (Main Structure)
    
    class ModularCommunityEngagementApp {
        <<object>>
        +start(): Unit
    }
    
    class SceneManager {
        -PrimaryStage stage
        -CommunityEngagementService communityService
        +showMainScene(): Unit
        +showAnonymousScene(): Unit
        +showLoginScene(): Unit
    }
    
    class BaseComponent {
        <<trait>>
        +initialize(): Unit
        +cleanup(): Unit
    }
    
    class ThemeableComponent {
        <<trait>>
        +applyTheme(theme: String): Unit
        +getCurrentTheme(): String
    }
    
    class BaseTabComponent {
        <<trait>>
        +getTabTitle(): String
        +getTabContent(): Node
        +onTabSelected(): Unit
        +onTabDeselected(): Unit
    }
    
    %% Relationships
    
    User <|-- CommunityMember
    User <|-- AdminUser
    
    Likeable <|.. Comment
    Moderatable <|.. Comment
    Likeable <|.. Event
    Moderatable <|.. Event
    Likeable <|.. FoodPost
    Moderatable <|.. FoodPost
    Likeable <|.. DiscussionTopic
    Moderatable <|.. DiscussionTopic
    Likeable <|.. Reply
    Moderatable <|.. Reply
    Likeable <|.. Announcement
    Moderatable <|.. Announcement
    
    Manager <|.. UserManager
    Manager <|.. EventManager
    Manager <|.. FoodPostManager
    Manager <|.. FoodStockManager
    Manager <|.. DiscussionForumManager
    Manager <|.. AnnouncementBoard
    
    DatabaseError <|-- DatabaseConnectionError
    DatabaseError <|-- DriverNotFoundError
    DatabaseError <|-- QueryExecutionError
    
    BaseComponent <|.. ThemeableComponent
    BaseComponent <|.. BaseTabComponent
    
    CommunityEngagementService --> UserManager
    CommunityEngagementService --> EventManager
    CommunityEngagementService --> FoodPostManager
    CommunityEngagementService --> FoodStockManager
    CommunityEngagementService --> DiscussionForumManager
    CommunityEngagementService --> AnnouncementBoard
    CommunityEngagementService --> User
    
    ActivityFeedService --> CommunityEngagementService
    ActivityFeedService --> ActivityFeedItem
    
    Event --> EventStatus
    Event --> Comment
    
    FoodPost --> FoodPostStatus
    FoodPost --> FoodPostType
    FoodPost --> Comment
    
    FoodStock --> FoodCategory
    FoodStock --> StockStatus
    FoodStock --> StockMovement
    
    StockMovement --> StockActionType
    
    DiscussionTopic --> DiscussionCategory
    DiscussionTopic --> Reply
    DiscussionTopic --> Comment
    
    Reply --> Comment
    
    Announcement --> AnnouncementPriority
    Announcement --> Comment
    
    Notification --> NotificationType
    
    ActivityFeedItem --> ActivityFeedType
    ActivityFeedItem --> Comment
    
    DatabaseConnection --> DatabaseError
    DatabaseSchema --> DatabaseConnection
    
    ModularCommunityEngagementApp --> SceneManager
    SceneManager --> CommunityEngagementService
    
    UserManager --> User
    UserManager --> PasswordHasher
    EventManager --> Event
    FoodPostManager --> FoodPost
    FoodStockManager --> FoodStock
    DiscussionForumManager --> DiscussionTopic
    DiscussionForumManager --> Reply
    AnnouncementBoard --> Announcement
```

## Package Structure

The project is organized into the following packages:

### 1. **model** Package
Contains all domain models and core business entities:
- **User Hierarchy**: `User` trait with `CommunityMember` and `AdminUser` implementations
- **Content Models**: `Event`, `FoodPost`, `FoodStock`, `DiscussionTopic`, `Announcement`
- **Supporting Models**: `Comment`, `Reply`, `Notification`, `ActivityFeedItem`, `StockMovement`
- **Traits**: `Likeable`, `Moderatable` for common functionality
- **Enumerations**: Various status and type enums for type safety

### 2. **manager** Package
Contains manager classes for business logic:
- **Generic Manager**: `Manager[T]` trait for common CRUD operations
- **Specific Managers**: `UserManager`, `EventManager`, `FoodPostManager`, `FoodStockManager`, `DiscussionForumManager`, `AnnouncementBoard`

### 3. **service** Package
Contains service layer for application logic:
- **Main Service**: `CommunityEngagementService` - central application service
- **Utility Services**: `ActivityFeedService`, `OTPService`

### 4. **database** Package
Contains database access and management:
- **Connection Management**: `DatabaseConnection` singleton
- **Schema Management**: `DatabaseSchema` for table creation
- **Error Handling**: `DatabaseError` hierarchy for functional error handling

### 5. **gui** Package
Contains all GUI components and controllers:
- **Main Application**: `ModularCommunityEngagementApp`
- **Scene Management**: `SceneManager`
- **Component Hierarchy**: `BaseComponent`, `ThemeableComponent`, `BaseTabComponent`
- **Feature Components**: Various tab and dialog components

### 6. **util** Package
Contains utility classes:
- **Security**: `PasswordHasher` for password management

## Design Patterns Used

1. **Sealed Traits**: For type-safe hierarchies (User, DatabaseError)
2. **Case Classes**: Immutable data structures throughout
3. **Singleton Pattern**: DatabaseConnection
4. **Manager Pattern**: For business logic encapsulation
5. **Trait Mixins**: Likeable, Moderatable for cross-cutting concerns
6. **Functional Programming**: Try, Either for error handling
7. **Component Pattern**: GUI architecture with reusable components

## Key Features

- **Type Safety**: Extensive use of enumerations and sealed traits
- **Immutability**: Case classes and functional updates
- **Error Handling**: Functional error handling with Try and Either
- **Modularity**: Well-organized package structure
- **Extensibility**: Trait-based design for easy feature extension
- **Concurrency**: Thread-safe collections in managers