package gui.components.common.public

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
  
  // Track if user has actually typed anything using property
  private val hasUserInputProperty = scalafx.beans.property.BooleanProperty(false)
  
  // Base style - will be overridden by parent components if needed
  private val basePromptTextStyle = "-fx-prompt-text-fill: #999999;"
  
  // Store the external style to preserve it during focus changes using property
  private val externalStyleProperty = StringProperty("")
  
  // Handle focus events for visual feedback while preserving external styles
  focused.onChange { (_, _, newValue) =>
    val promptTextColor = if (newValue) "#BBBBBB" else "#999999"
    // Combine external style with prompt text color
    if (externalStyleProperty.value.nonEmpty) {
      style = s"${externalStyleProperty.value} -fx-prompt-text-fill: $promptTextColor;"
    } else {
      style = s"-fx-prompt-text-fill: $promptTextColor;"
    }
  }
  
  // Override style property to preserve external styles
  override def style_=(value: String): Unit = {
    externalStyleProperty.value = value
    // Apply the style immediately with current prompt text color
    val promptTextColor = if (focused.value) "#BBBBBB" else "#999999"
    super.style_=(s"$value -fx-prompt-text-fill: $promptTextColor;")
  }
  
  // Track actual user input
  text.onChange { (_, _, newValue) =>
    hasUserInputProperty.value = newValue.trim.nonEmpty
  }
  
  /**
   * Get the actual user input
   */
  def userInput: String = {
    text.value.trim
  }
  
  /**
   * Set the user input programmatically
   */
  def userInput_=(value: String): Unit = {
    text = value.trim
    hasUserInputProperty.value = value.trim.nonEmpty
  }
  
  /**
   * Clear the field
   */
  def clearInput(): Unit = {
    text = ""
    hasUserInputProperty.value = false
  }
  
  /**
   * Check if user has entered any input
   */
  def hasInput: Boolean = hasUserInputProperty.value
}
