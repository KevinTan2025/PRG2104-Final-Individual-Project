package gui.components

import scalafx.scene.control.Tab
import service.CommunityEngagementService

/**
 * Base trait for tab components
 */
trait BaseTabComponent {
  
  // Access to the service layer
  protected val service: CommunityEngagementService = CommunityEngagementService.getInstance
  
  /**
   * Build and return the tab component's UI
   */
  def build(): Tab
  
  /**
   * Refresh component data
   */
  def refresh(): Unit
  
  /**
   * Initialize component (called after construction)
   */
  def initialize(): Unit
}
