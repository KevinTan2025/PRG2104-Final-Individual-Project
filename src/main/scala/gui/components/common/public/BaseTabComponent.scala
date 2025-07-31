package gui.components.common.public

import service.CommunityEngagementService
import scalafx.scene.control.{Tab => ScalaFXTab}

/**
 * Base trait for tab components
 */
trait BaseTabComponent {
  
  // Access to the service layer
  protected val service: CommunityEngagementService = CommunityEngagementService.getInstance
  
  /**
   * Build and return the tab component's UI
   */
  def build(): ScalaFXTab
  
  /**
   * Refresh component data
   */
  def refresh(): Unit
  
  /**
   * Initialize component (called after construction)
   */
  def initialize(): Unit
}
