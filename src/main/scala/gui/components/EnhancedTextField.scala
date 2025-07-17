package gui.components

import scalafx.scene.control.TextField
import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.scene.paint.Color

/**
 * Enhanced TextField with improved placeholder behavior
 * The placeholder remains visible when focused and only disappears when user starts typing
 */
class EnhancedTextField(placeholderText: String) extends TextField {
  
  private val placeholderProperty = StringProperty(placeholderText)
  private var isPlaceholderVisible = true
  private var hasUserInput = false
  
  // Set initial state
  text = placeholderText
  style = "-fx-text-fill: #999999;" // Gray color for placeholder
  
  // Handle focus events
  focused.onChange { (_, _, newValue) =>
    if (newValue) {
      // When gaining focus, keep placeholder visible if no user input
      if (!hasUserInput) {
        // Keep placeholder visible but change color to indicate focus
        style = "-fx-text-fill: #BBBBBB;"
      }
    } else {
      // When losing focus, show placeholder if field is empty
      if (text.value.trim.isEmpty || text.value == placeholderText) {
        showPlaceholder()
      }
    }
  }
  
  // Handle text changes
  text.onChange { (_, oldValue, newValue) =>
    // If user is typing (not just setting placeholder)
    if (focused.value) {
      if (isPlaceholderVisible && newValue != placeholderText) {
        // User started typing, remove placeholder
        if (newValue.length > placeholderText.length || 
            !placeholderText.startsWith(newValue)) {
          hidePlaceholder()
          text = newValue.replace(placeholderText, "")
          hasUserInput = true
        }
      } else if (!isPlaceholderVisible) {
        // User is typing normal text
        hasUserInput = newValue.trim.nonEmpty
        if (!hasUserInput) {
          showPlaceholder()
        }
      }
    }
  }
  
  private def showPlaceholder(): Unit = {
    isPlaceholderVisible = true
    hasUserInput = false
    text = placeholderText
    style = "-fx-text-fill: #999999;"
  }
  
  private def hidePlaceholder(): Unit = {
    isPlaceholderVisible = false
    style = "-fx-text-fill: black;"
  }
  
  /**
   * Get the actual user input (excluding placeholder)
   */
  def getUserInput: String = {
    if (isPlaceholderVisible || text.value == placeholderText) {
      ""
    } else {
      text.value
    }
  }
  
  /**
   * Set the user input programmatically
   */
  def setUserInput(value: String): Unit = {
    if (value.trim.nonEmpty) {
      hidePlaceholder()
      text = value
      hasUserInput = true
    } else {
      showPlaceholder()
    }
  }
  
  /**
   * Clear the field and reset to placeholder state
   */
  def clearInput(): Unit = {
    showPlaceholder()
  }
}
