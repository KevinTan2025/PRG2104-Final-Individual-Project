package gui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import service.CommunityEngagementService

/**
 * Main GUI application class for the Community Engagement Platform
 */
object CommunityEngagementApp extends JFXApp3 {
  
  // Service instance
  private val service = CommunityEngagementService.getInstance
  
  // Pending registration data for OTP verification
  private val pendingRegistration: java.util.concurrent.atomic.AtomicReference[Option[RegistrationData]] = new java.util.concurrent.atomic.AtomicReference(None)
  
  case class RegistrationData(
    username: String,
    email: String,
    name: String,
    contact: String,
    password: String
  )
  
  // Main application stage
  override def start(): Unit = {
    stage = new PrimaryStage {
      title = "Community Engagement Platform"
      scene = createLoginScene()
      minWidth = 800
      minHeight = 600
    }
    stage.centerOnScreen()
  }
  
  /**
   * Create the login scene
   */
  private def createLoginScene(): Scene = {
    val usernameField = new TextField {
      promptText = "Username"
      prefWidth = 250
    }
    
    val passwordField = new PasswordField {
      promptText = "Password"
      prefWidth = 250
    }
    
    val loginButton = new Button("Login") {
      prefWidth = 100
      onAction = (_: ActionEvent) => {
        val username = usernameField.text.value
        val password = passwordField.text.value
        
        service.login(username, password) match {
          case Some(user) =>
            showAlert(Alert.AlertType.Information, "Login Successful", s"Welcome, ${user.name}!")
            stage.scene = createMainScene()
          case None =>
            showAlert(Alert.AlertType.Error, "Login Failed", "Invalid username or password.")
            passwordField.text = ""
        }
      }
    }
    
    val registerButton = new Button("Register") {
      prefWidth = 100
      onAction = (_: ActionEvent) => {
        stage.scene = createRegistrationScene()
      }
    }
    
    val loginBox = new VBox {
      spacing = 15
      alignment = Pos.Center
      children = Seq(
        new Label("Community Engagement Platform") {
          style = "-fx-font-size: 24px; -fx-font-weight: bold;"
        },
        new Label("Facilitating community collaboration for food security") {
          style = "-fx-font-size: 14px; -fx-text-fill: gray;"
        },
        new Separator(),
        usernameField,
        passwordField,
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(loginButton, registerButton)
        },
        new Label("Demo credentials: admin/Admin123*, john/Password123!") {
          style = "-fx-font-size: 12px; -fx-text-fill: gray;"
        }
      )
      padding = Insets(50)
    }
    
    val rootPane = new BorderPane {
      center = loginBox
      style = "-fx-background-color: #f5f5f5;"
    }
    
    new Scene(rootPane, 800, 600)
  }
  
  /**
   * Create the registration scene
   */
  private def createRegistrationScene(): Scene = {
    val usernameField = new gui.components.common.public.EnhancedTextField("Username")
    val emailField = new gui.components.common.public.EnhancedTextField("Email")
    val nameField = new gui.components.common.public.EnhancedTextField("Full Name")
    val contactField = new gui.components.common.public.EnhancedTextField("Contact Info")
    val passwordField = new PasswordField { promptText = "Password (8+ chars, letter, digit, special char)" }
    val confirmPasswordField = new PasswordField { promptText = "Confirm Password" }
    
    val registerButton = new Button("Register") {
      onAction = (_: ActionEvent) => {
        val username = usernameField.getUserInput
        val email = emailField.getUserInput
        val name = nameField.getUserInput
        val contact = contactField.getUserInput
        val password = passwordField.text.value
        val confirmPassword = confirmPasswordField.text.value
        
        // Basic validation
        if (username.isEmpty || email.isEmpty || name.isEmpty || password.isEmpty) {
          showAlert(Alert.AlertType.Warning, "Incomplete Information", "Please fill in all required fields.")
        } else if (!isValidEmail(email)) {
          showAlert(Alert.AlertType.Error, "Invalid Email", "Please enter a valid email address.")
        } else if (password != confirmPassword) {
          showAlert(Alert.AlertType.Error, "Password Mismatch", "Password and confirmation do not match.")
        } else if (!util.PasswordHasher.isPasswordValid(password)) {
          showAlert(Alert.AlertType.Error, "Invalid Password", util.PasswordHasher.getPasswordRequirements)
        } else if (!service.isUsernameAvailable(username)) {
          showAlert(Alert.AlertType.Error, "Username Taken", s"The username '$username' is already taken. Please choose a different one.")
        } else if (!service.isEmailAvailable(email)) {
          showAlert(Alert.AlertType.Error, "Email Already Registered", s"The email '$email' is already registered. Please use a different email or try logging in.")
        } else {
          // Store registration data and start OTP verification
          pendingRegistration.set(Some(RegistrationData(username, email, name, contact, password)))
          
          val otpDialog = new gui.dialogs.auth.OTPVerificationDialog(stage, email)
          otpDialog.show(
            onSuccess = () => {
              pendingRegistration.get() match {
                case Some(data) =>
                  if (service.registerUser(data.username, data.email, data.name, data.contact, data.password, isAdmin = false)) {
                    showAlert(Alert.AlertType.Information, "Registration Successful", "Account created successfully! You can now log in.")
                    // Clear form
                    usernameField.clearInput()
                    emailField.clearInput()
                    nameField.clearInput()
                    contactField.clearInput()
                    passwordField.clear()
                    confirmPasswordField.clear()
                    stage.scene = createLoginScene()
                  } else {
                    showAlert(Alert.AlertType.Error, "Registration Failed", "An error occurred during registration. Please try again.")
                  }
                  pendingRegistration.set(None)
                case None =>
                  showAlert(Alert.AlertType.Error, "Registration Error", "Registration data not found. Please try again.")
              }
            },
            onFailure = () => {
              pendingRegistration.set(None)
              showAlert(Alert.AlertType.Warning, "Registration Cancelled", "Email verification was cancelled. Please try again.")
            }
          )
        }
      }
    }
    
    val backButton = new Button("Back to Login") {
      onAction = (_: ActionEvent) => {
        stage.scene = createLoginScene()
      }
    }
    
    val formBox = new VBox {
      spacing = 15
      alignment = Pos.Center
      children = Seq(
        new Label("Register New Account") {
          style = "-fx-font-size: 20px; -fx-font-weight: bold;"
        },
        usernameField,
        emailField,
        nameField,
        contactField,
        passwordField,
        confirmPasswordField,
        new HBox {
          spacing = 10
          alignment = Pos.Center
          children = Seq(registerButton, backButton)
        }
      )
      padding = Insets(50)
    }
    
    val rootPane = new BorderPane {
      center = formBox
      style = "-fx-background-color: #f5f5f5;"
    }
    
    new Scene(rootPane, 800, 600)
  }
  
  /**
   * Create the main application scene
   */
  private def createMainScene(): Scene = {
    val tabPane = new TabPane {
      tabs = Seq(
        createDashboardTab(),
        createAnnouncementsTab(),
        createFoodSharingTab(),
        createDiscussionTab(),
        createEventsTab(),
        createNotificationsTab()
      )
    }
    
    val menuBar = createMenuBar()
    
    val rootPane = new BorderPane {
      top = menuBar
      center = tabPane
    }
    
    new Scene(rootPane, 1000, 700)
  }
  
  /**
   * Create the menu bar
   */
  private def createMenuBar(): MenuBar = {
    val fileMenu = new Menu("File") {
      items = Seq(
        new MenuItem("Logout") {
          onAction = (_: ActionEvent) => {
            service.logout()
            showAlert(Alert.AlertType.Information, "Logged Out", "You have been logged out successfully.")
            stage.scene = createLoginScene()
          }
        },
        new SeparatorMenuItem(),
        new MenuItem("Exit") {
          onAction = (_: ActionEvent) => {
            stage.close()
          }
        }
      )
    }
    
    val userInfo = service.getCurrentUser match {
      case Some(user) => s"${user.name} (${user.getUserRole})"
      case None => "Guest"
    }
    
    val userMenu = new Menu(userInfo) {
      items = Seq(
        new MenuItem("Profile") {
          onAction = (_: ActionEvent) => {
            showProfileDialog()
          }
        },
        new MenuItem("Settings") {
          onAction = (_: ActionEvent) => {
            showAlert(Alert.AlertType.Information, "Settings", "Settings coming soon!")
          }
        }
      )
    }
    
    new MenuBar {
      menus = Seq(fileMenu, userMenu)
    }
  }
  
  /**
   * Create the dashboard tab based on user role
   */
  private def createDashboardTab(): Tab = {
    service.getCurrentUser match {
      case Some(user) if user.getUserRole == "AdminUser" =>
        new gui.components.dashboards.AdminDashboard(service).build()
      case _ =>
        new gui.components.dashboards.UserDashboard(service).build()
    }
  }

  /**
   * Create the announcements tab
   */
  private def createAnnouncementsTab(): Tab = {
    val announcementsList = new ListView[String]()
    
    val refreshAnnouncements = () => {
      val announcements = service.getAnnouncements
      val items = announcements.map(a => s"[${a.announcementType}] ${a.title} - ${a.timestamp.toLocalDate}")
      announcementsList.items = scalafx.collections.ObservableBuffer(items: _*)
    }
    
    refreshAnnouncements()
    
    val createButton = new Button("Create Announcement") {
      onAction = (_: ActionEvent) => {
        val dialog = createAnnouncementDialog()
        dialog.showAndWait() match {
          case Some(_) => refreshAnnouncements()
          case None => // User cancelled
        }
      }
    }
    
    val searchField = new TextField {
      promptText = "Search announcements..."
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => {
        val searchTerm = searchField.text.value
        if (searchTerm.nonEmpty) {
          val results = service.searchAnnouncements(searchTerm)
          val items = results.map(a => s"[${a.announcementType}] ${a.title} - ${a.timestamp.toLocalDate}")
          announcementsList.items = scalafx.collections.ObservableBuffer(items: _*)
        } else {
          refreshAnnouncements()
        }
      }
    }
    
    val addCommentButton = new Button("Add Comment") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = announcementsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val announcements = service.getAnnouncements
          if (selectedIndex < announcements.length) {
            val announcement = announcements(selectedIndex)
            val dialog = createCommentDialog(announcement.title)
            dialog.showAndWait() match {
              case Some(comment) =>
                if (service.addCommentToAnnouncement(announcement.announcementId, comment.toString)) {
                  showAlert(Alert.AlertType.Information, "Success", "Comment added successfully!")
                } else {
                  showAlert(Alert.AlertType.Error, "Failed", "Could not add comment.")
                }
              case None => // User cancelled
            }
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select an announcement to comment on.")
        }
      }
    }
    
    val likeButton = new Button("Like") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = announcementsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val announcements = service.getAnnouncements
          if (selectedIndex < announcements.length) {
            val announcement = announcements(selectedIndex)
            if (service.likeAnnouncement(announcement.announcementId)) {
              showAlert(Alert.AlertType.Information, "Success", "Announcement liked!")
            }
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select an announcement to like.")
        }
      }
    }
    
    val tabContent = new BorderPane {
      top = new HBox {
        spacing = 10
        padding = Insets(10)
        children = Seq(createButton, searchField, searchButton, addCommentButton, likeButton)
      }
      center = announcementsList
    }
    
    new Tab {
      text = "Announcements"
      content = tabContent
      closable = false
    }
  }
  
  /**
   * Create announcement dialog
   */
  private def createAnnouncementDialog(): Dialog[String] = {
    val dialog = new Dialog[String]()
    dialog.title = "Create Announcement"
    dialog.headerText = "Create a new community announcement"
    
    val titleField = new TextField { promptText = "Announcement title" }
    val contentArea = new TextArea { 
      promptText = "Announcement content"
      prefRowCount = 5
    }
    
    val typeCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer("GENERAL", "FOOD_DISTRIBUTION", "EVENTS", "TIPS", "EMERGENCY")
      value = "GENERAL"
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Title:"), 0, 0)
      add(titleField, 1, 0)
      add(new Label("Type:"), 0, 1)
      add(typeCombo, 1, 1)
      add(new Label("Content:"), 0, 2)
      add(contentArea, 1, 2)
    }
    
    dialog.dialogPane().content = grid
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    
    dialog.resultConverter = dialogButton => {
      if (dialogButton == ButtonType.OK) {
        import model.AnnouncementType
        val announcementType = typeCombo.value.value match {
          case "FOOD_DISTRIBUTION" => AnnouncementType.FOOD_DISTRIBUTION
          case "EVENTS" => AnnouncementType.EVENTS
          case "TIPS" => AnnouncementType.TIPS
          case "EMERGENCY" => AnnouncementType.EMERGENCY
          case _ => AnnouncementType.GENERAL
        }
        
        service.createAnnouncement(titleField.text.value, contentArea.text.value, announcementType)
        "created"
      } else {
        ""
      }
    }
    
    dialog
  }
  
  /**
   * Create comment dialog
   */
  private def createCommentDialog(itemTitle: String): Dialog[String] = {
    val dialog = new Dialog[String]()
    dialog.title = "Add Comment"
    dialog.headerText = s"Comment on: $itemTitle"
    
    val contentArea = new TextArea { 
      promptText = "Your comment..."
      prefRowCount = 3
      prefWidth = 400
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Comment:"), 0, 0)
      add(contentArea, 0, 1)
    }
    
    dialog.dialogPane().content = grid
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    
    dialog.resultConverter = dialogButton => {
      if (dialogButton == ButtonType.OK) {
        contentArea.text()
      } else {
        null
      }
    }
    
    dialog
  }
  
  /**
   * Create the food sharing tab
   */
  private def createFoodSharingTab(): Tab = {
    val foodPostsList = new ListView[String]()
    
    val refreshFoodPosts = () => {
      val posts = service.getFoodPosts
      val items = posts.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
      foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
    }
    
    refreshFoodPosts()
    
    val createButton = new Button("Create Food Post") {
      onAction = (_: ActionEvent) => {
        val dialog = createFoodPostDialog()
        dialog.showAndWait() match {
          case Some(_) => refreshFoodPosts()
          case None => // User cancelled
        }
      }
    }
    
    val filterCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer("All", "OFFER", "REQUEST")
      value = "All"
    }
    
    val filterButton = new Button("Filter") {
      onAction = (_: ActionEvent) => {
        val filterType = filterCombo.value.value
        if (filterType == "All") {
          refreshFoodPosts()
        } else {
          import model.FoodPostType
          val postType = if (filterType == "OFFER") FoodPostType.OFFER else FoodPostType.REQUEST
          val filtered = service.getFoodPostsByType(postType)
          val items = filtered.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
          foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
        }
      }
    }
    
    val acceptButton = new Button("Accept Selected") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = foodPostsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val posts = if (filterCombo.value.value == "All") {
            service.getFoodPosts
          } else {
            import model.FoodPostType
            val postType = if (filterCombo.value.value == "OFFER") FoodPostType.OFFER else FoodPostType.REQUEST
            service.getFoodPostsByType(postType)
          }
          if (selectedIndex < posts.length) {
            val post = posts(selectedIndex)
            if (service.acceptFoodPost(post.postId)) {
              showAlert(Alert.AlertType.Information, "Success", "Food post accepted successfully!")
              refreshFoodPosts()
            } else {
              showAlert(Alert.AlertType.Warning, "Failed", "Could not accept food post. It may already be accepted.")
            }
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select a food post to accept.")
        }
      }
    }
    
    val searchField = new TextField {
      promptText = "Search food posts..."
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => {
        val searchTerm = searchField.text.value
        if (searchTerm.nonEmpty) {
          val results = service.searchFoodPosts(searchTerm)
          val items = results.map(p => s"[${p.postType}] ${p.title} - ${p.location} (${p.status})")
          foodPostsList.items = scalafx.collections.ObservableBuffer(items: _*)
        } else {
          refreshFoodPosts()
        }
      }
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(createButton, filterCombo, filterButton, acceptButton)
    }
    
    val searchControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(searchField, searchButton)
    }
    
    val mainContent = new BorderPane {
      top = new VBox {
        children = Seq(topControls, searchControls)
      }
      center = foodPostsList
    }
    
    new Tab {
      text = "Food Sharing"
      content = mainContent
      closable = false
    }
  }
  
  /**
   * Create food post dialog
   */
  private def createFoodPostDialog(): Dialog[String] = {
    val dialog = new Dialog[String]()
    dialog.title = "Create Food Post"
    dialog.headerText = "Create a new food sharing post"
    
    val titleField = new TextField { promptText = "Post title" }
    val descriptionArea = new TextArea { 
      promptText = "Description"
      prefRowCount = 3
    }
    val quantityField = new TextField { promptText = "Quantity" }
    val locationField = new TextField { promptText = "Location" }
    
    val typeCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer("OFFER", "REQUEST")
      value = "OFFER"
    }
    
    val expiryCheck = new CheckBox("Has expiry date")
    val expiryPicker = new TextField { 
      promptText = "Days until expiry"
      disable = true
    }
    
    expiryCheck.onAction = (_: ActionEvent) => {
      expiryPicker.disable = !expiryCheck.selected.value
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Title:"), 0, 0)
      add(titleField, 1, 0)
      add(new Label("Type:"), 0, 1)
      add(typeCombo, 1, 1)
      add(new Label("Quantity:"), 0, 2)
      add(quantityField, 1, 2)
      add(new Label("Location:"), 0, 3)
      add(locationField, 1, 3)
      add(new Label("Description:"), 0, 4)
      add(descriptionArea, 1, 4)
      add(expiryCheck, 0, 5)
      add(expiryPicker, 1, 5)
    }
    
    dialog.dialogPane().content = grid
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    
    dialog.resultConverter = dialogButton => {
      if (dialogButton == ButtonType.OK) {
        import model.FoodPostType
        import java.time.LocalDateTime
        
        val postType = if (typeCombo.value.value == "OFFER") FoodPostType.OFFER else FoodPostType.REQUEST
        val expiryDate = if (expiryCheck.selected.value && expiryPicker.text.value.nonEmpty) {
          try {
            Some(LocalDateTime.now().plusDays(expiryPicker.text.value.toLong))
          } catch {
            case _: NumberFormatException => None
          }
        } else None
        
        service.createFoodPost(
          titleField.text.value,
          descriptionArea.text.value,
          postType,
          quantityField.text.value,
          locationField.text.value,
          expiryDate
        )
        "created"
      } else {
        null
      }
    }
    
    dialog
  }
  
  private def createDiscussionTab(): Tab = {
    val topicsList = new ListView[String]()
    
    val refreshTopics = () => {
      val topics = service.getDiscussionTopics
      val items = topics.map(t => s"[${t.category}] ${t.title} - ${t.getReplyCount} replies")
      topicsList.items = scalafx.collections.ObservableBuffer(items: _*)
    }
    
    refreshTopics()
    
    val createTopicButton = new Button("Create Topic") {
      onAction = (_: ActionEvent) => {
        val dialog = createTopicDialog()
        dialog.showAndWait() match {
          case Some(_) => refreshTopics()
          case None => // User cancelled
        }
      }
    }
    
    val categoryCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer(
        "All", "NUTRITION", "SUSTAINABLE_AGRICULTURE", "FOOD_SECURITY", 
        "COMMUNITY_GARDEN", "COOKING_TIPS", "GENERAL"
      )
      value = "All"
    }
    
    val filterButton = new Button("Filter by Category") {
      onAction = (_: ActionEvent) => {
        val category = categoryCombo.value.value
        if (category == "All") {
          refreshTopics()
        } else {
          import model.DiscussionCategory
          val cat = category match {
            case "NUTRITION" => DiscussionCategory.NUTRITION
            case "SUSTAINABLE_AGRICULTURE" => DiscussionCategory.SUSTAINABLE_AGRICULTURE
            case "FOOD_SECURITY" => DiscussionCategory.FOOD_SECURITY
            case "COMMUNITY_GARDEN" => DiscussionCategory.COMMUNITY_GARDEN
            case "COOKING_TIPS" => DiscussionCategory.COOKING_TIPS
            case _ => DiscussionCategory.GENERAL
          }
          val filtered = service.getTopicsByCategory(cat)
          val items = filtered.map(t => s"[${t.category}] ${t.title} - ${t.getReplyCount} replies")
          topicsList.items = scalafx.collections.ObservableBuffer(items: _*)
        }
      }
    }
    
    val replyButton = new Button("Add Reply") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = topicsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val topics = if (categoryCombo.value.value == "All") {
            service.getDiscussionTopics
          } else {
            import model.DiscussionCategory
            val cat = categoryCombo.value.value match {
              case "NUTRITION" => DiscussionCategory.NUTRITION
              case "SUSTAINABLE_AGRICULTURE" => DiscussionCategory.SUSTAINABLE_AGRICULTURE
              case "FOOD_SECURITY" => DiscussionCategory.FOOD_SECURITY
              case "COMMUNITY_GARDEN" => DiscussionCategory.COMMUNITY_GARDEN
              case "COOKING_TIPS" => DiscussionCategory.COOKING_TIPS
              case _ => DiscussionCategory.GENERAL
            }
            service.getTopicsByCategory(cat)
          }
          
          if (selectedIndex < topics.length) {
            val topic = topics(selectedIndex)
            val dialog = createReplyDialog(topic.title)
            dialog.showAndWait() match {
              case Some(content) => 
                if (service.addReplyToTopic(topic.topicId, content.toString)) {
                  showAlert(Alert.AlertType.Information, "Success", "Reply added successfully!")
                  refreshTopics()
                } else {
                  showAlert(Alert.AlertType.Error, "Failed", "Could not add reply.")
                }
              case None => // User cancelled
            }
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select a topic to reply to.")
        }
      }
    }
    
    val likeButton = new Button("Like Topic") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = topicsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val topics = if (categoryCombo.value.value == "All") {
            service.getDiscussionTopics
          } else {
            import model.DiscussionCategory
            val cat = categoryCombo.value.value match {
              case "NUTRITION" => DiscussionCategory.NUTRITION
              case "SUSTAINABLE_AGRICULTURE" => DiscussionCategory.SUSTAINABLE_AGRICULTURE
              case "FOOD_SECURITY" => DiscussionCategory.FOOD_SECURITY
              case "COMMUNITY_GARDEN" => DiscussionCategory.COMMUNITY_GARDEN
              case "COOKING_TIPS" => DiscussionCategory.COOKING_TIPS
              case _ => DiscussionCategory.GENERAL
            }
            service.getTopicsByCategory(cat)
          }
          
          if (selectedIndex < topics.length) {
            val topic = topics(selectedIndex)
            if (service.likeTopic(topic.topicId)) {
              showAlert(Alert.AlertType.Information, "Success", "Topic liked!")
              refreshTopics()
            }
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select a topic to like.")
        }
      }
    }
    
    // Admin-only moderation button
    val moderateButton = new Button("Moderate Topic") {
      visible = service.isCurrentUserAdmin
      onAction = (_: ActionEvent) => {
        val selectedIndex = topicsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val topics = service.getDiscussionTopics
          if (selectedIndex < topics.length) {
            val topic = topics(selectedIndex)
            if (service.moderateContent(topic.topicId, "topic")) {
              showAlert(Alert.AlertType.Information, "Success", "Topic moderated successfully!")
              refreshTopics()
            }
          }
        }
      }
    }
    
    val searchField = new TextField {
      promptText = "Search topics..."
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => {
        val searchTerm = searchField.text.value
        if (searchTerm.nonEmpty) {
          val results = service.searchTopics(searchTerm)
          val items = results.map(t => s"[${t.category}] ${t.title} - ${t.getReplyCount} replies")
          topicsList.items = scalafx.collections.ObservableBuffer(items: _*)
        } else {
          refreshTopics()
        }
      }
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(createTopicButton, categoryCombo, filterButton, replyButton, likeButton, moderateButton)
    }
    
    val searchControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(searchField, searchButton)
    }
    
    val mainContent = new BorderPane {
      top = new VBox {
        children = Seq(topControls, searchControls)
      }
      center = topicsList
    }
    
    new Tab {
      text = "Discussion Forum"
      content = mainContent
      closable = false
    }
  }
  
  /**
   * Create topic dialog
   */
  private def createTopicDialog(): Dialog[String] = {
    val dialog = new Dialog[String]()
    dialog.title = "Create Discussion Topic"
    dialog.headerText = "Create a new discussion topic"
    
    val titleField = new TextField { promptText = "Topic title" }
    val descriptionArea = new TextArea { 
      promptText = "Topic description"
      prefRowCount = 5
    }
    
    val categoryCombo = new ComboBox[String] {
      items = scalafx.collections.ObservableBuffer(
        "NUTRITION", "SUSTAINABLE_AGRICULTURE", "FOOD_SECURITY", 
        "COMMUNITY_GARDEN", "COOKING_TIPS", "GENERAL"
      )
      value = "GENERAL"
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Title:"), 0, 0)
      add(titleField, 1, 0)
      add(new Label("Category:"), 0, 1)
      add(categoryCombo, 1, 1)
      add(new Label("Description:"), 0, 2)
      add(descriptionArea, 1, 2)
    }
    
    dialog.dialogPane().content = grid
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    
    dialog.resultConverter = dialogButton => {
      if (dialogButton == ButtonType.OK) {
        import model.DiscussionCategory
        val category = categoryCombo.value.value match {
          case "NUTRITION" => DiscussionCategory.NUTRITION
          case "SUSTAINABLE_AGRICULTURE" => DiscussionCategory.SUSTAINABLE_AGRICULTURE
          case "FOOD_SECURITY" => DiscussionCategory.FOOD_SECURITY
          case "COMMUNITY_GARDEN" => DiscussionCategory.COMMUNITY_GARDEN
          case "COOKING_TIPS" => DiscussionCategory.COOKING_TIPS
          case _ => DiscussionCategory.GENERAL
        }
        
        service.createDiscussionTopic(titleField.text.value, descriptionArea.text.value, category)
        "created"
      } else {
        null
      }
    }
    
    dialog
  }
  
  /**
   * Create reply dialog
   */
  private def createReplyDialog(topicTitle: String): Dialog[String] = {
    val dialog = new Dialog[String]()
    dialog.title = "Add Reply"
    dialog.headerText = s"Reply to: $topicTitle"
    
    val contentArea = new TextArea { 
      promptText = "Your reply..."
      prefRowCount = 5
      prefWidth = 400
    }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Reply:"), 0, 0)
      add(contentArea, 0, 1)
    }
    
    dialog.dialogPane().content = grid
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    
    dialog.resultConverter = dialogButton => {
      if (dialogButton == ButtonType.OK) {
        contentArea.text()
      } else {
        null
      }
    }
    
    dialog
  }
  
  private def createEventsTab(): Tab = {
    val eventsList = new ListView[String]()
    
    val refreshEvents = () => {
      val events = service.getUpcomingEvents
      val items = events.map(e => s"${e.title} - ${e.location} (${e.startDateTime.toLocalDate}) - ${e.participants.size} participants")
      eventsList.items = scalafx.collections.ObservableBuffer(items: _*)
    }
    
    refreshEvents()
    
    val createEventButton = new Button("Create Event") {
      onAction = (_: ActionEvent) => {
        val dialog = createEventDialog()
        dialog.showAndWait() match {
          case Some(_) => refreshEvents()
          case None => // User cancelled
        }
      }
    }
    
    val rsvpButton = new Button("RSVP to Event") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = eventsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val events = service.getUpcomingEvents
          if (selectedIndex < events.length) {
            val event = events(selectedIndex)
            if (service.rsvpToEvent(event.eventId)) {
              showAlert(Alert.AlertType.Information, "Success", "RSVP successful!")
              refreshEvents()
            } else {
              showAlert(Alert.AlertType.Warning, "Failed", "Could not RSVP. Event may be full or you may already be registered.")
            }
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select an event to RSVP.")
        }
      }
    }
    
    val cancelRsvpButton = new Button("Cancel RSVP") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = eventsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val events = service.getUpcomingEvents
          if (selectedIndex < events.length) {
            val event = events(selectedIndex)
            if (service.cancelRsvp(event.eventId)) {
              showAlert(Alert.AlertType.Information, "Success", "RSVP cancelled successfully!")
              refreshEvents()
            } else {
              showAlert(Alert.AlertType.Warning, "Failed", "Could not cancel RSVP.")
            }
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select an event to cancel RSVP.")
        }
      }
    }
    
    val viewMyEventsButton = new Button("My Events") {
      onAction = (_: ActionEvent) => {
        service.getCurrentUser match {
          case Some(user) =>
            val myEvents = service.getMyEvents(user.userId)
            val items = myEvents.map(e => s"${e.title} - ${e.location} (${e.startDateTime.toLocalDate}) - ${e.participants.size} participants")
            eventsList.items = scalafx.collections.ObservableBuffer(items: _*)
          case None =>
            showAlert(Alert.AlertType.Warning, "Not Logged In", "Please log in to view your events.")
        }
      }
    }
    
    val viewAllButton = new Button("All Events") {
      onAction = (_: ActionEvent) => {
        refreshEvents()
      }
    }
    
    val searchField = new TextField {
      promptText = "Search events..."
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => {
        val searchTerm = searchField.text.value
        if (searchTerm.nonEmpty) {
          val results = service.searchEvents(searchTerm)
          val items = results.map(e => s"${e.title} - ${e.location} (${e.startDateTime.toLocalDate}) - ${e.participants.size} participants")
          eventsList.items = scalafx.collections.ObservableBuffer(items: _*)
        } else {
          refreshEvents()
        }
      }
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(createEventButton, rsvpButton, cancelRsvpButton, viewMyEventsButton, viewAllButton)
    }
    
    val searchControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(searchField, searchButton)
    }
    
    val mainContent = new BorderPane {
      top = new VBox {
        children = Seq(topControls, searchControls)
      }
      center = eventsList
    }
    
    new Tab {
      text = "Events"
      content = mainContent
      closable = false
    }
  }
  
  /**
   * Create event dialog
   */
  private def createEventDialog(): Dialog[String] = {
    val dialog = new Dialog[String]()
    dialog.title = "Create Event"
    dialog.headerText = "Create a new community event"
    
    val titleField = new TextField { promptText = "Event title" }
    val descriptionArea = new TextArea { 
      promptText = "Event description"
      prefRowCount = 3
    }
    val locationField = new TextField { promptText = "Event location" }
    val startDateField = new TextField { promptText = "Start date (YYYY-MM-DD)" }
    val startTimeField = new TextField { promptText = "Start time (HH:MM)" }
    val endDateField = new TextField { promptText = "End date (YYYY-MM-DD)" }
    val endTimeField = new TextField { promptText = "End time (HH:MM)" }
    val maxParticipantsField = new TextField { promptText = "Max participants (optional)" }
    
    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20)
      
      add(new Label("Title:"), 0, 0)
      add(titleField, 1, 0)
      add(new Label("Location:"), 0, 1)
      add(locationField, 1, 1)
      add(new Label("Start Date:"), 0, 2)
      add(startDateField, 1, 2)
      add(new Label("Start Time:"), 0, 3)
      add(startTimeField, 1, 3)
      add(new Label("End Date:"), 0, 4)
      add(endDateField, 1, 4)
      add(new Label("End Time:"), 0, 5)
      add(endTimeField, 1, 5)
      add(new Label("Max Participants:"), 0, 6)
      add(maxParticipantsField, 1, 6)
      add(new Label("Description:"), 0, 7)
      add(descriptionArea, 1, 7)
    }
    
    dialog.dialogPane().content = grid
    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    
    dialog.resultConverter = dialogButton => {
      if (dialogButton == ButtonType.OK) {
        try {
          import java.time.LocalDateTime
          import java.time.LocalDate
          import java.time.LocalTime
          
          val startDate = LocalDate.parse(startDateField.text.value)
          val startTime = LocalTime.parse(startTimeField.text.value)
          val endDate = LocalDate.parse(endDateField.text.value)
          val endTime = LocalTime.parse(endTimeField.text.value)
          
          val startDateTime = LocalDateTime.of(startDate, startTime)
          val endDateTime = LocalDateTime.of(endDate, endTime)
          
          val maxParticipants = if (maxParticipantsField.text.value.nonEmpty) {
            Some(maxParticipantsField.text.value.toInt)
          } else None
          
          service.createEvent(
            titleField.text.value,
            descriptionArea.text.value,
            locationField.text.value,
            startDateTime,
            endDateTime,
            maxParticipants
          )
          "created"
        } catch {
          case e: Exception =>
            showAlert(Alert.AlertType.Error, "Invalid Input", s"Please check your date/time format: ${e.getMessage}")
            null
        }
      } else {
        null
      }
    }
    
    dialog
  }
  
  private def createNotificationsTab(): Tab = {
    val notificationsList = new ListView[String]()
    
    val refreshNotifications = () => {
      val notifications = service.getNotifications
      val items = notifications.map { n =>
        val readStatus = if (n.isRead) "✓" else "●"
        s"$readStatus [${n.notificationType}] ${n.title} - ${n.timestamp.toLocalDate}"
      }
      notificationsList.items = scalafx.collections.ObservableBuffer(items: _*)
    }
    
    refreshNotifications()
    
    val markReadButton = new Button("Mark as Read") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = notificationsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val notifications = service.getNotifications
          if (selectedIndex < notifications.length) {
            val notification = notifications(selectedIndex)
            if (service.markNotificationAsRead(notification.notificationId)) {
              refreshNotifications()
            }
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select a notification to mark as read.")
        }
      }
    }
    
    val markAllReadButton = new Button("Mark All as Read") {
      onAction = (_: ActionEvent) => {
        service.getCurrentUser match {
          case Some(user) =>
            val count = service.markAllNotificationsAsRead
            showAlert(Alert.AlertType.Information, "Success", s"Marked $count notifications as read.")
            refreshNotifications()
          case None =>
            showAlert(Alert.AlertType.Warning, "Not Logged In", "Please log in to mark notifications as read.")
        }
      }
    }
    
    val viewUnreadButton = new Button("Unread Only") {
      onAction = (_: ActionEvent) => {
        val unread = service.getUnreadNotifications
        val items = unread.map { n =>
          val readStatus = if (n.isRead) "✓" else "●"
          s"$readStatus [${n.notificationType}] ${n.title} - ${n.timestamp.toLocalDate}"
        }
        notificationsList.items = scalafx.collections.ObservableBuffer(items: _*)
      }
    }
    
    val viewAllButton = new Button("All Notifications") {
      onAction = (_: ActionEvent) => {
        refreshNotifications()
      }
    }
    
    val deleteButton = new Button("Delete Selected") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = notificationsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val notifications = service.getNotifications
          if (selectedIndex < notifications.length) {
            val notification = notifications(selectedIndex)
            if (service.deleteNotification(notification.notificationId)) {
              showAlert(Alert.AlertType.Information, "Success", "Notification deleted.")
              refreshNotifications()
            }
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select a notification to delete.")
        }
      }
    }
    
    val viewDetailsButton = new Button("View Details") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = notificationsList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0) {
          val notifications = service.getNotifications
          if (selectedIndex < notifications.length) {
            val notification = notifications(selectedIndex)
            showNotificationDetails(notification)
          }
        } else {
          showAlert(Alert.AlertType.Warning, "No Selection", "Please select a notification to view details.")
        }
      }
    }
    
    // Stats display
    val unreadCount = service.getUnreadNotificationCount
    val statsLabel = new Label(s"Unread notifications: $unreadCount") {
      style = "-fx-font-weight: bold; -fx-text-fill: #2196F3;"
    }
    
    val topControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(markReadButton, markAllReadButton, viewUnreadButton, viewAllButton, deleteButton, viewDetailsButton)
    }
    
    val statsBox = new HBox {
      padding = Insets(10)
      children = Seq(statsLabel)
    }
    
    val mainContent = new BorderPane {
      top = new VBox {
        children = Seq(statsBox, topControls)
      }
      center = notificationsList
    }
    
    new Tab {
      text = s"Notifications ($unreadCount)"
      content = mainContent
      closable = false
    }
  }
  
  /**
   * Show notification details dialog
   */
  private def showNotificationDetails(notification: model.Notification): Unit = {
    val dialog = new Dialog[Unit]()
    dialog.title = "Notification Details"
    dialog.headerText = notification.title
    
    val detailsText = s"""
Type: ${notification.notificationType}
From: ${notification.senderId.getOrElse("System")}
Date: ${notification.timestamp}
Read: ${if (notification.isRead) "Yes" else "No"}
${if (notification.readAt.isDefined) s"Read at: ${notification.readAt.get}" else ""}

Message:
${notification.message}
"""
    
    val textArea = new TextArea {
      text = detailsText
      editable = false
      prefRowCount = 10
      prefWidth = 400
      wrapText = true
    }
    
    dialog.dialogPane().content = textArea
    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    
    dialog.showAndWait()
  }
  
  /**
   * Show profile management dialog
   */
  private def showProfileDialog(): Unit = {
    service.getCurrentUser match {
      case Some(user) =>
        val dialog = new Dialog[Unit]()
        dialog.title = "User Profile"
        dialog.headerText = s"Edit profile for ${user.username}"
        
        val nameField = new TextField {
          text = user.name
          promptText = "Full Name"
        }
        
        val contactField = new TextField {
          text = user.contactInfo
          promptText = "Contact Info"
        }
        
        val emailField = new TextField {
          text = user.email
          promptText = "Email"
          editable = false  // Email usually shouldn't be changed
        }
        
        val roleLabel = new Label(user.getUserRole) {
          style = "-fx-font-weight: bold;"
        }
        
        val regDateLabel = new Label(user.registrationDate.toLocalDate.toString)
        
        // Password change section
        val currentPasswordField = new PasswordField {
          promptText = "Current Password"
        }
        
        val newPasswordField = new PasswordField {
          promptText = "New Password (8+ chars, letter, digit, special char)"
        }
        
        val confirmPasswordField = new PasswordField {
          promptText = "Confirm New Password"
        }
        
        val changePasswordButton = new Button("Change Password") {
          onAction = _ => {
            val currentPass = currentPasswordField.text.value
            val newPass = newPasswordField.text.value
            val confirmPass = confirmPasswordField.text.value
            
            if (currentPass.isEmpty || newPass.isEmpty || confirmPass.isEmpty) {
              showAlert(Alert.AlertType.Warning, "Missing Fields", "Please fill in all password fields.")
            } else if (newPass != confirmPass) {
              showAlert(Alert.AlertType.Error, "Password Mismatch", "New password and confirmation do not match.")
            } else if (!util.PasswordHasher.isPasswordValid(newPass)) {
              showAlert(Alert.AlertType.Error, "Invalid Password", util.PasswordHasher.getPasswordRequirements)
            } else {
              if (service.resetPassword(currentPass, newPass)) {
                showAlert(Alert.AlertType.Information, "Success", "Password changed successfully!")
                currentPasswordField.text = ""
                newPasswordField.text = ""
                confirmPasswordField.text = ""
              } else {
                showAlert(Alert.AlertType.Error, "Failed", "Failed to change password. Please check your current password.")
              }
            }
          }
        }
        
        val grid = new GridPane {
          hgap = 10
          vgap = 10
          padding = Insets(20)
          
          // Profile information
          add(new Label("Username:"), 0, 0)
          add(new Label(user.username) { style = "-fx-font-weight: bold;" }, 1, 0)
          add(new Label("Email:"), 0, 1)
          add(emailField, 1, 1)
          add(new Label("Full Name:"), 0, 2)
          add(nameField, 1, 2)
          add(new Label("Contact Info:"), 0, 3)
          add(contactField, 1, 3)
          add(new Label("Role:"), 0, 4)
          add(roleLabel, 1, 4)
          add(new Label("Registration Date:"), 0, 5)
          add(regDateLabel, 1, 5)
          
          // Password change section
          add(new Separator(), 0, 6, 2, 1)
          add(new Label("Change Password") { style = "-fx-font-weight: bold; -fx-font-size: 14px;" }, 0, 7, 2, 1)
          add(new Label("Current Password:"), 0, 8)
          add(currentPasswordField, 1, 8)
          add(new Label("New Password:"), 0, 9)
          add(newPasswordField, 1, 9)
          add(new Label("Confirm Password:"), 0, 10)
          add(confirmPasswordField, 1, 10)
          add(changePasswordButton, 1, 11)
        }
        
        dialog.dialogPane().content = grid
        dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
        
        dialog.resultConverter = dialogButton => {
          if (dialogButton == ButtonType.OK) {
            if (service.updateUserProfile(nameField.text.value, contactField.text.value)) {
              showAlert(Alert.AlertType.Information, "Success", "Profile updated successfully!")
            } else {
              showAlert(Alert.AlertType.Error, "Failed", "Could not update profile.")
            }
          }
        }
        
        dialog.showAndWait()
        
      case None =>
        showAlert(Alert.AlertType.Warning, "Not Logged In", "Please log in to view your profile.")
    }
  }
  
  /**
   * Utility method to show alerts
   */
  private def showAlert(alertType: Alert.AlertType, alertTitle: String, message: String): Unit = {
    new Alert(alertType) {
      initOwner(stage)
      title = alertTitle
      headerText = None
      contentText = message
    }.showAndWait()
  }
  
  /**
   * Show users management dialog (admin only)
   */
  private def showUsersDialog(): Unit = {
    val dialog = new Dialog[Unit]()
    dialog.title = "User Management"
    dialog.headerText = "All registered users"
    
    val usersList = new ListView[String]()
    val users = service.getAllUsers
    val items = users.map(u => s"${u.username} (${u.name}) - ${u.getUserRole} - ${if (u.isActive) "Active" else "Inactive"}")
    usersList.items = scalafx.collections.ObservableBuffer(items: _*)
    
    val vbox = new VBox {
      spacing = 10
      padding = Insets(20)
      children = Seq(
        new Label(s"Total Users: ${users.length}"),
        usersList
      )
      prefWidth = 500
      prefHeight = 400
    }
    
    dialog.dialogPane().content = vbox
    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    
    dialog.showAndWait()
  }
  
  /**
   * Show system statistics dialog (admin only)
   */
  private def showSystemStatsDialog(): Unit = {
    val dialog = new Dialog[Unit]()
    dialog.title = "System Statistics"
    dialog.headerText = "Detailed platform statistics"
    
    val stats = service.getDashboardStatistics
    val detailedStats = service.getDetailedStatistics
    
    val statsText = s"""
Platform Overview:
- Total Users: ${stats("totalUsers")}
- Community Members: ${stats("communityMembers")}
- Administrators: ${stats("adminUsers")}

Content Statistics:
- Active Announcements: ${stats("activeAnnouncements")}
- Total Food Posts: ${stats("totalFoodPosts")}
- Active Food Posts: ${stats("activeFoodPosts")}
- Completed Food Posts: ${stats("completedFoodPosts")}

Events:
- Total Events: ${stats("totalEvents")}
- Upcoming Events: ${stats("upcomingEvents")}
- Completed Events: ${stats("completedEvents")}

User Activity:
- Total Notifications Sent: ${detailedStats("totalNotifications")}
- Total Comments: ${detailedStats("totalComments")}
- Total Likes: ${detailedStats("totalLikes")}
"""
    
    val textArea = new TextArea {
      text = statsText
      editable = false
      prefRowCount = 15
      prefWidth = 400
      wrapText = true
    }
    
    dialog.dialogPane().content = textArea
    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    
    dialog.showAndWait()
  }
  
  /**
   * Show content moderation dialog (admin only)
   */
  private def showModerationDialog(): Unit = {
    val dialog = new Dialog[Unit]()
    dialog.title = "Content Moderation"
    dialog.headerText = "Moderate platform content"
    
    val contentList = new ListView[String]()
    val moderationItems = service.getContentForModeration
    val items = moderationItems.map { case (id, contentType, title) =>
      s"[$contentType] $title (ID: $id)"
    }
    contentList.items = scalafx.collections.ObservableBuffer(items: _*)
    
    val moderateButton = new Button("Moderate Selected") {
      onAction = (_: ActionEvent) => {
        val selectedIndex = contentList.selectionModel().selectedIndex.value
        if (selectedIndex >= 0 && selectedIndex < moderationItems.length) {
          val (id, contentType, title) = moderationItems(selectedIndex)
          if (service.moderateContent(id, contentType)) {
            showAlert(Alert.AlertType.Information, "Success", s"Content '$title' has been moderated.")
            dialog.close()
          } else {
            showAlert(Alert.AlertType.Error, "Failed", "Could not moderate the selected content.")
          }
        }
      }
    }
    
    val vbox = new VBox {
      spacing = 10
      padding = Insets(20)
      children = Seq(
        new Label("Select content to moderate:"),
        contentList,
        moderateButton
      )
      prefWidth = 500
      prefHeight = 400
    }
    
    dialog.dialogPane().content = vbox
    dialog.dialogPane().buttonTypes = Seq(ButtonType.Close)
    
    dialog.showAndWait()
  }
  
  /**
   * Validate email format
   */
  private def isValidEmail(email: String): Boolean = {
    val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
    emailRegex.matches(email)
  }
}
