package gui.components.layout

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.TabPane
import scalafx.scene.layout.Region
import scalafx.Includes._
import gui.components.common.public.BaseComponent

/**
 * Generic tab pane component
 * Security Level: PUBLIC - Generic tab pane for dynamic content
 */
class TabPaneComponent extends BaseComponent {
  
  // FXML injected controls
  @FXML private var tabPaneGeneric: TabPane = _
  
  override def build(): Region = {
    val loader = new FXMLLoader(getClass.getResource("/gui/components/layout/TabPaneComponent.fxml"))
    loader.setController(this)
    val root = loader.load[TabPane]()
    
    // Convert JavaFX TabPane to ScalaFX Region
    new Region(root)
  }
  
  /**
   * Add a tab to the tab pane
   * @param tab The tab to add
   */
  def addTab(tab: javafx.scene.control.Tab): Unit = {
    if (tabPaneGeneric != null) {
      tabPaneGeneric.getTabs.add(tab)
    }
  }
  
  /**
   * Remove a tab from the tab pane
   * @param tab The tab to remove
   */
  def removeTab(tab: javafx.scene.control.Tab): Unit = {
    if (tabPaneGeneric != null) {
      tabPaneGeneric.getTabs.remove(tab)
    }
  }
  
  /**
   * Clear all tabs from the tab pane
   */
  def clearTabs(): Unit = {
    if (tabPaneGeneric != null) {
      tabPaneGeneric.getTabs.clear()
    }
  }
  
  /**
   * Get the underlying TabPane
   * @return The JavaFX TabPane
   */
  def tabPane: TabPane = tabPaneGeneric
}