package gui.components

import scalafx.scene.layout.Region
import service.CommunityEngagementService

/**
 * Base trait for all GUI components
 */
trait BaseComponent {
  
  // Access to the service layer
  protected val service: CommunityEngagementService = CommunityEngagementService.getInstance
  
  /**
   * Build and return the component's UI
   */
  def build(): Region
  
  /**
   * Refresh component data
   */
  def refresh(): Unit = {}
  
  /**
   * Initialize component (called after creation)
   */
  def initialize(): Unit = {}
}
