package gui

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import gui.scenes.SceneManager
import gui.utils.GuiUtils

/**
 * Modular main GUI application class for the Community Engagement Platform
 * This replaces the monolithic CommunityEngagementApp with a component-based architecture
 */
object ModularCommunityEngagementApp extends JFXApp3 {
  
  override def start(): Unit = {
    stage = new PrimaryStage {
      title = "Community Engagement Platform"
      minWidth = 800
      minHeight = 600
      resizable = true
    }
    
    try {
      val communityService = new service.CommunityEngagementService()
      val sceneManager = new SceneManager(stage, communityService)
      sceneManager.showLoginScene()
      stage.centerOnScreen()
    } catch {
      case e: Exception =>
        GuiUtils.showError("Startup Error", s"Failed to start application: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}
