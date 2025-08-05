package gui.utils

import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.scene.Scene
import scalafx.scene.layout.Region
import scalafx.Includes._

/**
 * Theme Manager - Manages dark/light mode for the application
 */
object ThemeManager {
  
  // Property indicating if dark mode is currently active
  val isDarkMode: BooleanProperty = BooleanProperty(false)
  
  // Current theme name property
  val currentTheme: StringProperty = StringProperty("light")
  
  /**
   * Toggle between dark and light theme
   */
  def toggleTheme(): Unit = {
    isDarkMode.value = !isDarkMode.value
    currentTheme.value = if (isDarkMode.value) "dark" else "light"
  }
  
  /**
   * Set dark mode
   */
  def setDarkMode(enabled: Boolean): Unit = {
    isDarkMode.value = enabled
    currentTheme.value = if (enabled) "dark" else "light"
  }
  
  /**
   * Get primary background color
   */
  def getPrimaryBackgroundColor: String = {
    if (isDarkMode.value) "#2b2b2b" else "#f5f5f5"
  }
  
  /**
   * Get secondary background color
   */
  def getSecondaryBackgroundColor: String = {
    if (isDarkMode.value) "#3c3c3c" else "#ffffff"
  }
  
  /**
   * Get primary text color
   */
  def getPrimaryTextColor: String = {
    if (isDarkMode.value) "#ffffff" else "#2c3e50"
  }
  
  /**
   * Get secondary text color
   */
  def getSecondaryTextColor: String = {
    if (isDarkMode.value) "#b0b0b0" else "#6c757d"
  }
  
  /**
   * Get border color
   */
  def getBorderColor: String = {
    if (isDarkMode.value) "#555555" else "#dee2e6"
  }
  
  /**
   * Get button background color
   */
  def getButtonBackgroundColor: String = {
    if (isDarkMode.value) "#4a4a4a" else "#007bff"
  }
  
  /**
   * Get button text color
   */
  def getButtonTextColor: String = {
    if (isDarkMode.value) "#ffffff" else "#ffffff"
  }
  
  /**
   * Get card background colors for different types
   */
  def getCardBackgroundColor(cardType: String = "default"): String = {
    if (isDarkMode.value) {
      cardType match {
        case "success" => "#1e4d3a"
        case "info" => "#1e3a5f"
        case "warning" => "#5d4e1e"
        case "primary" => "#4a1e5d"
        case _ => "#404040"
      }
    } else {
      cardType match {
        case "success" => "#e8f5e8"
        case "info" => "#e3f2fd"
        case "warning" => "#fff3e0"
        case "primary" => "#f3e5f5"
        case _ => "#ffffff"
      }
    }
  }
  
  /**
   * Get input field styles
   */
  def getInputFieldStyle: String = {
    if (isDarkMode.value) {
      s"-fx-background-color: ${getSecondaryBackgroundColor}; -fx-text-fill: ${getPrimaryTextColor}; -fx-border-color: ${getBorderColor};"
    } else {
      s"-fx-background-color: ${getSecondaryBackgroundColor}; -fx-text-fill: ${getPrimaryTextColor}; -fx-border-color: ${getBorderColor};"
    }
  }
  
  /**
   * Get complete component style string
   */
  def getComponentStyle(componentType: String = "default"): String = {
    componentType match {
      case "background" => s"-fx-background-color: ${getPrimaryBackgroundColor};"
      case "card" => s"-fx-background-color: ${getSecondaryBackgroundColor}; -fx-border-color: ${getBorderColor}; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;"
      case "button" => s"-fx-background-color: ${getButtonBackgroundColor}; -fx-text-fill: ${getButtonTextColor}; -fx-border-radius: 5; -fx-background-radius: 5;"
      case "label-primary" => s"-fx-text-fill: ${getPrimaryTextColor};"
      case "label-secondary" => s"-fx-text-fill: ${getSecondaryTextColor};"
      case _ => s"-fx-background-color: ${getSecondaryBackgroundColor}; -fx-text-fill: ${getPrimaryTextColor};"
    }
  }
  
  /**
   * Apply theme to a scene
   */
  def applyThemeToScene(scene: Scene): Unit = {
    // Load global theme CSS file
    val themeStylesheet = getClass.getResource("/gui/themes/global-theme.css")
    if (themeStylesheet != null) {
      val stylesheetUrl = themeStylesheet.toExternalForm
      if (!scene.stylesheets.contains(stylesheetUrl)) {
        scene.stylesheets.add(stylesheetUrl)
      }
    }
    
    // Apply theme class to root node
    val root = scene.root.value
    if (isDarkMode.value) {
      if (!root.styleClass.contains("dark-theme")) {
        root.styleClass.add("dark-theme")
      }
    } else {
      root.styleClass.remove("dark-theme")
    }
  }
  
  /**
   * Apply theme to a region component
   */
  def applyThemeToRegion(region: Region): Unit = {
    region.style = getComponentStyle("background")
  }
}