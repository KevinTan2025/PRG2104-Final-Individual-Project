package gui.components

import scalafx.scene.control.TextField
import scalafx.Includes._
import scalafx.beans.property.StringProperty

/**
 * Enhanced TextField with improved placeholder behavior
 * Uses proper promptText that remains visible when focused until user starts typing
 */
class EnhancedTextField(placeholderText: String) extends TextField {
  
  // Use the built-in promptText property for proper placeholder behavior
  promptText = placeholderText
  
  // Track if user has actually typed anything
  private var hasUserInput = false
  
  // Enhanced style for better UX
  style = "-fx-prompt-text-fill: #999999;"
  
  // Handle focus events for visual feedback
  focused.onChange { (_, _, newValue) =>
    if (newValue) {
      // When focused, make prompt text slightly lighter to show it's active
      style = "-fx-prompt-text-fill: #BBBBBB;"
    } else {
      // When focus lost, return to normal prompt text color
      style = "-fx-prompt-text-fill: #999999;"
    }
  }
  
  // Track actual user input
  text.onChange { (_, _, newValue) =>
    hasUserInput = newValue.trim.nonEmpty
  }
  
  /**
   * Get the actual user input
   */
  def getUserInput: String = {
    text.value.trim
  }
  
  /**
   * Set the user input programmatically
   */
  def setUserInput(value: String): Unit = {
    text = value.trim
    hasUserInput = value.trim.nonEmpty
  }
  
  /**
   * Clear the field
   */
  def clearInput(): Unit = {
    text = ""
    hasUserInput = false
  }
  
  /**
   * Check if user has entered any input
   */
  def hasInput: Boolean = hasUserInput
}
