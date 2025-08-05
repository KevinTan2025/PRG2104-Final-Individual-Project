package gui.components.common.public

import scalafx.scene.layout.Region
import scalafx.scene.control._
import scalafx.scene.layout._
import gui.utils.ThemeManager
import scalafx.Includes._

/**
 * 可主题化组件特质 - 为组件提供主题支持
 * Themeable component trait - Provides theme support for components
 */
trait ThemeableComponent extends BaseComponent {
  
  /**
   * 应用主题到组件
   * Apply theme to the component
   */
  protected def applyTheme(region: Region): Unit = {
    ThemeManager.applyThemeToRegion(region)
    
    // 监听主题变化并自动更新
    // Listen for theme changes and auto-update
    ThemeManager.isDarkMode.onChange { (_, _, _) =>
      updateComponentTheme(region)
    }
  }
  
  /**
   * 更新组件主题
   * Update component theme
   */
  protected def updateComponentTheme(region: Region): Unit = {
    region.style = ThemeManager.getComponentStyle("background")
    updateChildrenTheme(region)
  }
  
  /**
   * 递归更新子组件主题
   * Recursively update children theme
   */
  private def updateChildrenTheme(parent: Region): Unit = {
    // 简化实现，暂时不递归更新子组件
    // 主要通过监听器在组件创建时应用主题
  }
  
  /**
   * 更新标签主题
   * Update label theme
   */
  protected def updateLabelTheme(label: Label): Unit = {
    val currentStyle = label.style.value
    val baseStyle = if (currentStyle.contains("-fx-font-size: 24px") || currentStyle.contains("-fx-font-weight: bold")) {
      ThemeManager.getComponentStyle("label-primary")
    } else {
      ThemeManager.getComponentStyle("label-secondary")
    }
    
    // 保留原有的字体大小和粗细设置，只更新颜色
    val preservedStyles = extractPreservedStyles(currentStyle, Seq("-fx-font-size", "-fx-font-weight", "-fx-text-alignment"))
    label.style = s"$baseStyle $preservedStyles"
  }
  
  /**
   * 更新按钮主题
   * Update button theme
   */
  protected def updateButtonTheme(button: Button): Unit = {
    val currentStyle = button.style.value
    val baseStyle = ThemeManager.getComponentStyle("button")
    val preservedStyles = extractPreservedStyles(currentStyle, Seq("-fx-font-size", "-fx-font-weight", "-fx-pref-width", "-fx-pref-height"))
    button.style = s"$baseStyle $preservedStyles"
  }
  
  /**
   * 更新文本框主题
   * Update text field theme
   */
  protected def updateTextFieldTheme(textField: TextField): Unit = {
    val currentStyle = textField.style.value
    val baseStyle = ThemeManager.getInputFieldStyle
    val preservedStyles = extractPreservedStyles(currentStyle, Seq("-fx-pref-width", "-fx-pref-height", "-fx-font-size"))
    textField.style = s"$baseStyle $preservedStyles"
  }
  
  /**
   * 更新文本区域主题
   * Update text area theme
   */
  protected def updateTextAreaTheme(textArea: TextArea): Unit = {
    val currentStyle = textArea.style.value
    val baseStyle = ThemeManager.getInputFieldStyle
    val preservedStyles = extractPreservedStyles(currentStyle, Seq("-fx-pref-width", "-fx-pref-height", "-fx-font-size"))
    textArea.style = s"$baseStyle $preservedStyles"
  }
  
  /**
   * 提取需要保留的样式属性
   * Extract styles that should be preserved
   */
  private def extractPreservedStyles(currentStyle: String, preserveKeys: Seq[String]): String = {
    val styleMap = currentStyle.split(";").map(_.trim).filter(_.nonEmpty).map { style =>
      val parts = style.split(":")
      if (parts.length == 2) {
        parts(0).trim -> parts(1).trim
      } else {
        "" -> ""
      }
    }.filter(_._1.nonEmpty).toMap
    
    preserveKeys.flatMap(key => styleMap.get(key).map(value => s"$key: $value")).mkString("; ")
  }
  
  /**
   * 创建主题化的统计卡片
   * Create a themed stat card
   */
  protected def createThemedStatCard(title: String, value: String, cardType: String = "default"): VBox = {
    new VBox {
      spacing = 10
      alignment = scalafx.geometry.Pos.Center
      padding = scalafx.geometry.Insets(20)
      prefWidth = 180
      prefHeight = 120
      style = s"-fx-background-color: ${ThemeManager.getCardBackgroundColor(cardType)}; -fx-border-color: ${ThemeManager.getBorderColor}; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;"
      
      children = Seq(
        new Label(value) {
          style = s"-fx-font-size: 28px; -fx-font-weight: bold; ${ThemeManager.getComponentStyle("label-primary")}"
        },
        new Label(title) {
          style = s"-fx-font-size: 14px; ${ThemeManager.getComponentStyle("label-secondary")} -fx-text-alignment: center;"
          wrapText = true
        }
      )
      
      // 监听主题变化
      ThemeManager.isDarkMode.onChange { (_, _, _) =>
        style = s"-fx-background-color: ${ThemeManager.getCardBackgroundColor(cardType)}; -fx-border-color: ${ThemeManager.getBorderColor}; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;"
        // 子组件会通过自己的监听器更新主题
      }
    }
  }
  
  /**
   * 创建主题化的按钮
   * Create a themed button
   */
  protected def createThemedButton(text: String, action: () => Unit): Button = {
    new Button(text) {
      style = ThemeManager.getComponentStyle("button")
      onAction = (_: scalafx.event.ActionEvent) => action()
      
      // 监听主题变化
      ThemeManager.isDarkMode.onChange { (_, _, _) =>
        updateButtonTheme(this)
      }
    }
  }
  
  /**
   * 创建主题化的标签
   * Create a themed label
   */
  protected def createThemedLabel(text: String, isPrimary: Boolean = true, fontSize: String = "14px", isBold: Boolean = false): Label = {
    new Label(text) {
      val fontWeight = if (isBold) "-fx-font-weight: bold;" else ""
      val colorStyle = if (isPrimary) ThemeManager.getComponentStyle("label-primary") else ThemeManager.getComponentStyle("label-secondary")
      style = s"-fx-font-size: $fontSize; $fontWeight $colorStyle"
      
      // 监听主题变化
      ThemeManager.isDarkMode.onChange { (_, _, _) =>
        updateLabelTheme(this)
      }
    }
  }
}