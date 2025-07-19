package gui.components.common.public

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets

/**
 * 简化的组件构建器 - 每个组件完全自包含
 * 安全级别: PUBLIC - 所有用户都可以使用这些通用UI组件构建器
 */
object SimpleComponentBuilder {
  
  /**
   * 创建标准按钮 - 统一样式和行为
   */
  def button(text: String, action: () => Unit): Button = {
    new Button(text) {
      onAction = _ => action()
      minWidth = 100
      style = "-fx-font-size: 12px;"
    }
  }
  
  /**
   * 创建搜索文本框 - 统一样式
   */
  def searchBox(placeholder: String = "Search...", boxWidth: Double = 200): TextField = {
    new TextField {
      promptText = placeholder
      prefWidth = boxWidth
      maxWidth = boxWidth
    }
  }
  
  /**
   * 创建筛选下拉框 - 统一样式
   */
  def filterBox(itemList: String*): ComboBox[String] = {
    new ComboBox[String] {
      this.items = scalafx.collections.ObservableBuffer(itemList: _*)
      value = itemList.headOption.getOrElse("All")
      prefWidth = 120
    }
  }
  
  /**
   * 创建列表视图 - 统一样式
   */
  def listView[T](boxWidth: Double = 600, boxHeight: Double = 400): ListView[T] = {
    new ListView[T] {
      prefWidth = boxWidth
      prefHeight = boxHeight
    }
  }
  
  /**
   * 创建按钮行 - 自动间距
   */
  def buttonRow(buttons: Button*): HBox = {
    new HBox {
      spacing = 10
      padding = Insets(10)
      children = buttons
    }
  }
  
  /**
   * 创建搜索栏
   */
  def searchRow(searchField: TextField, searchButton: Button): HBox = {
    new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(searchField, searchButton)
    }
  }
  
  /**
   * 创建完整的标签页布局
   */
  def tabLayout(
    title: String,
    topButtons: HBox,
    searchRow: Option[HBox] = None,
    listView: ListView[_],
    panelWidth: Double = 700,
    panelHeight: Double = 500
  ): Tab = {
    new Tab {
      text = title
      content = new BorderPane {
        top = new VBox {
          children = Seq(topButtons) ++ searchRow.toSeq
        }
        center = listView
        prefWidth = panelWidth
        prefHeight = panelHeight
      }
      closable = false
    }
  }
}
