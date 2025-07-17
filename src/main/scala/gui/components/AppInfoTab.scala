package gui.components

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.{Font, FontWeight, Text}
import scalafx.event.ActionEvent
import scalafx.Includes._

/**
 * Application information tab component
 * Displays app version, author information, GitHub repository, and other metadata
 */
class AppInfoTab extends BaseTabComponent {
  
  // Application metadata
  private val appName = "Community Engagement Platform"
  private val appVersion = "0.1.0-SNAPSHOT"
  private val appDescription = "A platform to facilitate communication and collaboration among community members for ending hunger, achieving food security, improving nutrition, and promoting sustainable agriculture (UN Goal 2)."
  private val authorName = "Kevin Tan"
  private val authorEmail = "kevin.tan@student.example.com"
  private val githubRepo = "https://github.com/KevinTan2025/PRG2104-Final-Individual-Project"
  private val scalaVersion = "3.3.4"
  private val javaVersion = System.getProperty("java.version")
  private val buildDate = "2025-07-17"
  private val license = "MIT License"
  
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
        createTechnicalInfoSection(),
        createSeparator(),
        createLinksSection(),
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
        createInfoRow("Developer:", authorName),
        createInfoRow("Email:", authorEmail),
        createInfoRow("Institution:", "Singapore Institute of Technology"),
        createInfoRow("Course:", "PRG2104 - Object-Oriented Programming")
      )
    }
  }
  
  /**
   * Creates the technical information section
   */
  private def createTechnicalInfoSection(): VBox = {
    new VBox {
      spacing = 15
      
      children = Seq(
        createSectionTitle("Technical Information"),
        createInfoRow("Language:", "Scala"),
        createInfoRow("Scala Version:", scalaVersion),
        createInfoRow("Java Version:", javaVersion),
        createInfoRow("GUI Framework:", "ScalaFX"),
        createInfoRow("Database:", "SQLite"),
        createInfoRow("Build Tool:", "SBT (Simple Build Tool)")
      )
    }
  }
  
  /**
   * Creates the links section
   */
  private def createLinksSection(): VBox = {
    new VBox {
      spacing = 15
      
      children = Seq(
        createSectionTitle("Project Links"),
        createLinkRow("GitHub Repository:", githubRepo),
        createLinkRow("Scala Official:", "https://www.scala-lang.org/"),
        createLinkRow("ScalaFX:", "https://www.scalafx.org/"),
        createLinkRow("UN SDG Goal 2:", "https://sdgs.un.org/goals/goal2")
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
        createInfoRow("License:", license),
        createInfoRow("Copyright:", s"© 2025 $authorName"),
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
