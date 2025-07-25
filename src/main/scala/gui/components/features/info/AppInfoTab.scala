package gui.components.features.info

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.{Font, FontWeight, Text}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.components.common.public.BaseTabComponent


// This tab provides detailed information about the application, including version, author, and technical details.
// It is designed to be informative and user-friendly, with a clean layout and easy navigation.

class AppInfoTab extends BaseTabComponent {
  
  // Application metadata
  private val appName = "Community Engagement Platform"
  private val appVersion = "Version 1.0.0"
  private val appDescription = "A platform to facilitate communication and collaboration among community members for ending hunger, achieving food security, improving nutrition, and promoting sustainable agriculture (UN Goal 2)."
  private val buildDate = "2025-07-17"
  
  override def build(): Tab = {
    val tabContent = createInfoContent()
    
    new Tab {
      text = "App Info"
      content = tabContent
      closable = false
    }
  }
  
  override def refresh(): Unit = {
    // No dynamic content to refresh
  }
  
  override def initialize(): Unit = {
    // No initialization needed
  }
  
  /**
   * Creates the main content layout for the app info tab
   */
  private def createInfoContent(): ScrollPane = {
    val mainVBox = new VBox {
      spacing = 20
      padding = Insets(30)
      alignment = Pos.TopCenter
      
      children = Seq(
        createHeader(),
        createSeparator(),
        createAppInfoSection(),
        createSeparator(),
        createAuthorInfoSection(),
        createSeparator(),
        createLegalSection()
      )
    }
    
    new ScrollPane {
      content = mainVBox
      fitToWidth = true
      hbarPolicy = ScrollPane.ScrollBarPolicy.Never
      vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
    }
  }
  
  /**
   * Creates the application header section
   */
  private def createHeader(): VBox = {
    new VBox {
      spacing = 10
      alignment = Pos.Center
      
      children = Seq(
        new Text {
          text = appName
          font = Font.font("System", FontWeight.Bold, 28)
          style = "-fx-fill: #2E8B57;"
        },
        new Text {
          text = s"Version $appVersion"
          font = Font.font("System", FontWeight.Normal, 16)
          style = "-fx-fill: #666666;"
        }
      )
    }
  }
  
  /**
   * Creates the application information section
   */
  private def createAppInfoSection(): VBox = {
    new VBox {
      spacing = 15
      
      children = Seq(
        createSectionTitle("Application Information"),
        createInfoRow("Name:", appName),
        createInfoRow("Version:", appVersion),
        createInfoRow("Build Date:", buildDate),
        createDescriptionBox("Description:", appDescription)
      )
    }
  }
  
  /**
   * Creates the author information section
   */
  private def createAuthorInfoSection(): VBox = {
    new VBox {
      spacing = 15
      
      children = Seq(
        createSectionTitle("Author Information"),
        createInfoRow("Developer:", "Kevin Tan"),
        createInfoRow("Email:", "me@kevintan.pro"),
        createInfoRow("Institution:", "Sunway University Malaysia"),
        createInfoRow("Course:", "PRG2104 - Object-Oriented Programming")
      )
    }
  }
  
  
  /**
   * Creates the legal information section
   */
  private def createLegalSection(): VBox = {
    new VBox {
      spacing = 15
      
      children = Seq(
        createSectionTitle("Legal Information"),
        createInfoRow("Copyright:", s"© 2025 Kevin Tan"),
        createDescriptionBox("Disclaimer:", 
          "This application is developed for educational purposes as part of the PRG2104 course. " +
          "It is designed to demonstrate object-oriented programming principles using Scala and GUI development with ScalaFX.")
      )
    }
  }
  
  /**
   * Creates a section title
   */
  private def createSectionTitle(title: String): Text = {
    new Text {
      text = title
      font = Font.font("System", FontWeight.Bold, 18)
      style = "-fx-fill: #2E8B57;"
    }
  }
  
  /**
   * Creates an information row with label and value
   */
  private def createInfoRow(label: String, value: String): HBox = {
    new HBox {
      spacing = 10
      
      children = Seq(
        new Label {
          text = label
          font = Font.font("System", FontWeight.Bold, 12)
          minWidth = 120
          style = "-fx-text-fill: #333333;"
        },
        new Label {
          text = value
          font = Font.font("System", FontWeight.Normal, 12)
          style = "-fx-text-fill: #666666;"
          wrapText = true
        }
      )
    }
  }
  
  /**
   * Creates a description box for longer text
   */
  private def createDescriptionBox(label: String, description: String): VBox = {
    new VBox {
      spacing = 5
      
      children = Seq(
        new Label {
          text = label
          font = Font.font("System", FontWeight.Bold, 12)
          style = "-fx-text-fill: #333333;"
        },
        new TextArea {
          text = description
          editable = false
          wrapText = true
          prefRowCount = 3
          style = "-fx-background-color: #f8f8f8; -fx-border-color: #dddddd; -fx-border-radius: 3;"
        }
      )
    }
  }
  
  /**
   * Creates a clickable link row
   */
  private def createLinkRow(label: String, url: String): HBox = {
    new HBox {
      spacing = 10
      
      children = Seq(
        new Label {
          text = label
          font = Font.font("System", FontWeight.Bold, 12)
          minWidth = 120
          style = "-fx-text-fill: #333333;"
        },
        new Hyperlink {
          text = url
          onAction = (_: ActionEvent) => {
            try {
              // Open URL in default browser
              val desktop = java.awt.Desktop.getDesktop
              if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                desktop.browse(new java.net.URI(url))
              }
            } catch {
              case _: Exception => 
                // If unable to open browser, copy to clipboard
                import java.awt.Toolkit
                import java.awt.datatransfer.StringSelection
                val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
                clipboard.setContents(new StringSelection(url), null)
            }
          }
          style = "-fx-text-fill: #0066cc;"
        }
      )
    }
  }
  
  /**
   * Creates a visual separator
   */
  private def createSeparator(): Separator = {
    new Separator {
      style = "-fx-background-color: #dddddd;"
    }
  }
}
