package gui.scenes

import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane
import scalafx.stage.Stage
import gui.components.auth.{LoginComponent, RegisterComponent, WelcomeComponent}
import gui.components.layout.{MenuBarComponent, MainTabPane, AnonymousMenuBarComponent, AnonymousMainTabPane}
import gui.utils.GuiUtils
import service.CommunityEngagementService

/**
 * Scene manager for handling different application scenes
 */
class SceneManager(stage: Stage, private val service: CommunityEngagementService) {
  
  GuiUtils.setMainStage(stage)
  
  def showWelcomeScene(): Unit = {
    val welcomeComponent = new WelcomeComponent(
      onLoginClick = () => showLoginScene(),
      onRegisterClick = () => showRegisterScene(),
      onBrowseAnonymouslyClick = () => showAnonymousScene()
    )
    
    val scene = new Scene(welcomeComponent.build(), 800, 600)
    stage.scene = scene
  }
  
  def showLoginScene(): Unit = {
    val loginComponent = new LoginComponent(
      onLoginSuccess = () => showMainScene(),
      onRegisterClick = () => showRegisterScene(),
      onBrowseAnonymouslyClick = Some(() => showAnonymousScene())
    )
    
    val scene = new Scene(loginComponent.build(), 800, 600)
    stage.scene = scene
  }
  
  def showRegisterScene(): Unit = {
    val registerComponent = new RegisterComponent(
      onRegistrationSuccess = () => showLoginScene(),
      onBackClick = () => showLoginScene()
    )
    
    val scene = new Scene(registerComponent.build(), 800, 600)
    stage.scene = scene
  }
  
  def showAnonymousScene(): Unit = {
    // Enable anonymous mode in service
    service.enableAnonymousMode()
    
    val menuBar = new AnonymousMenuBarComponent(
      onLoginClick = () => showLoginScene(),
      onRegisterClick = () => showRegisterScene(),
      onExitAnonymousMode = () => {
        service.disableAnonymousMode()
        showLoginScene()
      }
    )
    
    val tabPane = new AnonymousMainTabPane(
      onLoginPrompt = () => {
        GuiUtils.showLoginPrompt(() => showLoginScene(), () => showRegisterScene())
      }
    )
    
    val rootPane = new BorderPane {
      top = menuBar.build()
      center = tabPane.build()
    }
    
    val scene = new Scene(rootPane, 1000, 700)
    stage.scene = scene
  }
  
  def showMainScene(): Unit = {
    val menuBar = new MenuBarComponent(onLogout = () => {
      service.logout()
      showLoginScene()
    })
    val tabPane = new MainTabPane()
    
    val rootPane = new BorderPane {
      top = menuBar.build()
      center = tabPane.build()
    }
    
    val scene = new Scene(rootPane, 1000, 700)
    stage.scene = scene
  }
}
