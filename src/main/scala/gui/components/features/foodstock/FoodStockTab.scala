package gui.components.features.foodstock

import scalafx.scene.control.{Tab => ScalaFXTab, TabPane => ScalaFXTabPane, Button, Label, ListView, ScrollPane, TextField, ComboBox, CheckBox, TextArea, Separator}
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.dialogs.features.foodstock.{FoodStockDialog, StockMovementDialog, ExportStockDialog}
import gui.components.common.public.BaseTabComponent
import service.CommunityEngagementService
import model.{FoodStock, FoodCategory, StockStatus}

/**
 * Food Stock Management tab component
 * 安全级别: USER - 注册用户可以管理食物库存
 */
class FoodStockTab extends BaseTabComponent {
  
  // Styled combo boxes
  private val categoryCombo = new ComboBox[String] {
    items = scalafx.collections.ObservableBuffer(
      ("All" :: FoodCategory.values.map(_.toString).toList): _*
    )
    value = "All"
    prefWidth = 150
  }
  
  private val statusCombo = new ComboBox[String] {
    items = scalafx.collections.ObservableBuffer(
      "All", "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK", "EXPIRED"
    )
    value = "All"
    prefWidth = 150
  }
  
  private val searchField = new TextField {
    promptText = "Search food items..."
    prefWidth = 250
  }
  
  private lazy val stocksList: ListView[String] = new ListView[String]() {
    prefHeight = 400
  }
  
  override def build(): ScalaFXTab = {
    // Enhanced stock list with better styling
    
    // Now we can safely refresh stocks
    refreshStocks()
    
    val addStockButton = new Button("Add New Stock") {
      prefWidth = 120
      onAction = (_: ActionEvent) => handleAddStock()
    }
    
    val editStockButton = new Button("Edit Stock") {
      prefWidth = 120
      onAction = (_: ActionEvent) => handleEditStock()
    }
    
    val manageStockButton = new Button("Manage Stock") {
      prefWidth = 120
      onAction = (_: ActionEvent) => handleManageStock()
    }
    
    val deleteStockButton = new Button("Delete Stock") {
      prefWidth = 120
      onAction = (_: ActionEvent) => handleDeleteStock()
    }
    
    val viewHistoryButton = new Button("View History") {
      prefWidth = 120
      onAction = (_: ActionEvent) => handleViewHistory()
    }
    
    val filterButton = new Button("Filter") {
      prefWidth = 80
      onAction = (_: ActionEvent) => handleFilter()
    }
    
    val searchButton = new Button("Search") {
      prefWidth = 80
      onAction = (_: ActionEvent) => handleSearch()
    }
    
    val refreshButton = new Button("Refresh") {
      prefWidth = 80
      onAction = (_: ActionEvent) => refreshStocks()
    }
    
    val exportButton = new Button("Export") {
      prefWidth = 80
      onAction = (_: ActionEvent) => handleExport()
    }
    
    val alertsButton = new Button("Alerts") {
      prefWidth = 80
      onAction = (_: ActionEvent) => handleShowAlerts()
    }
    
    // Main action buttons - arranged in a grid for better organization
    val topButtonRow = new HBox {
      spacing = 15
      padding = Insets(15)
      alignment = Pos.Center
      children = Seq(addStockButton, editStockButton, manageStockButton)
    }
    
    val bottomButtonRow = new HBox {
      spacing = 15
      padding = Insets(0, 15, 15, 15)
      alignment = Pos.Center
      children = Seq(deleteStockButton, viewHistoryButton)
    }
    
    // Filter controls with better spacing and alignment
    val filterControls = new HBox {
      spacing = 15
      padding = Insets(15)
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("Category:") {
          prefWidth = 80
        },
        categoryCombo,
        new Label("Status:") {
          prefWidth = 60
        },
        statusCombo,
        filterButton,
        new Region { HBox.setHgrow(this, Priority.Always) }, // Spacer
        refreshButton,
        alertsButton,
        exportButton
      )
    }
    
    // Search controls
    val searchControls = new HBox {
      spacing = 10
      padding = Insets(0, 15, 15, 15)
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("Search:") {
          prefWidth = 60
        },
        searchField, 
        searchButton
      )
    }
    
    // Side panel with stats and info
    val sidePanel = createSidePanel()
    
    val mainContent = new HBox {
      spacing = 20
      padding = Insets(10)
      children = Seq(
        new VBox {
          spacing = 0
          children = Seq(
            topButtonRow,
            bottomButtonRow,
            new Separator(),
            filterControls,
            searchControls,
            new VBox {
              spacing = 10
              padding = Insets(15)
              children = Seq(
                new Label("Food Stock Items"),
                stocksList
              )
            }
          )
          HBox.setHgrow(this, Priority.Always)
        },
        sidePanel
      )
    }
    
    new ScalaFXTab {
      text = "📦 Food Inventory"
      content = new ScrollPane {
        content = mainContent
        fitToWidth = true
        hbarPolicy = ScrollPane.ScrollBarPolicy.Never
        vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
      }
      closable = false
    }
  }
  
  override def refresh(): Unit = {
    refreshStocks()
  }
  
  override def initialize(): Unit = {
    // Initial setup if needed
  }
  
  private def refreshStocks(): Unit = {
    val stocks = service.allFoodStocks
    updateStocksList(stocks)
  }
  
  private def updateStocksList(stocks: List[FoodStock]): Unit = {
    if (stocksList != null) {
      val items = stocks.map { stock =>
        val status = stock.stockStatus
        val statusIcon = status match {
          case StockStatus.IN_STOCK => "✅"
          case StockStatus.LOW_STOCK => "⚠️"
          case StockStatus.OUT_OF_STOCK => "❌"
          case StockStatus.EXPIRED => "💀"
        }
        
        val expiryInfo = stock.daysUntilExpiry match {
          case Some(days) if days <= 0 => " (EXPIRED)"
          case Some(days) if days <= 7 => s" (${days}d left)"
          case Some(days) => s" (${days}d left)"
          case None => ""
        }
        
        s"$statusIcon [${stock.category}] ${stock.foodName} - ${stock.currentQuantity} ${stock.unit} " +
        s"(Min: ${stock.minimumThreshold}) @ ${stock.location}$expiryInfo"
      }
      stocksList.items = scalafx.collections.ObservableBuffer(items: _*)
    }
  }
  
  private def handleAddStock(): Unit = {
    if (!service.isLoggedIn) {
      GuiUtils.showWarning("Login Required", "Please login to add new stock items.")
      return
    }
    
    val dialog = new FoodStockDialog(None, () => refreshStocks())
    dialog.showAndWait()
  }

  private def handleEditStock(): Unit = {
    if (!service.isLoggedIn) {
      GuiUtils.showWarning("Login Required", "Please login to edit stock items.")
      return
    }
    
    getSelectedStock() match {
      case Some(stock) =>
        val dialog = new FoodStockDialog(Some(stock), () => refreshStocks())
        dialog.showAndWait()
      case None =>
        GuiUtils.showWarning("No Selection", "Please select a stock item to edit.")
    }
  }

  private def handleManageStock(): Unit = {
    if (!service.isLoggedIn) {
      GuiUtils.showWarning("Login Required", "Please login to manage stock quantities.")
      return
    }
    
    getSelectedStock() match {
      case Some(stock) =>
        val dialog = new StockMovementDialog(stock, () => refreshStocks())
        dialog.showAndWait()
      case None =>
        GuiUtils.showWarning("No Selection", "Please select a stock item to manage.")
    }
  }

  private def handleDeleteStock(): Unit = {
    if (!service.isLoggedIn) {
      GuiUtils.showWarning("Login Required", "Please login to delete stock items.")
      return
    }
    
    getSelectedStock() match {
      case Some(stock) =>
        val confirm = GuiUtils.showConfirmation(
          "Delete Stock Item",
          s"Are you sure you want to delete '${stock.foodName}'? This action cannot be undone."
        )
        if (confirm) {
          if (service.deleteFoodStock(stock.stockId)) {
            GuiUtils.showInfo("Success", "Stock item deleted successfully!")
            refreshStocks()
          } else {
            GuiUtils.showError("Error", "Failed to delete stock item.")
          }
        }
      case None =>
        GuiUtils.showWarning("No Selection", "Please select a stock item to delete.")
    }
  }
  
  private def handleViewHistory(): Unit = {
    getSelectedStock() match {
      case Some(stock) =>
        val movements = service.stockMovements(stock.stockId)
        val historyText = if (movements.nonEmpty) {
          movements.map { movement =>
            s"${movement.timestamp.toLocalDate} ${movement.timestamp.toLocalTime.toString.take(8)} - " +
            s"${movement.description} by ${movement.userId}"
          }.mkString("\n")
        } else {
          "No movement history available."
        }
        
        GuiUtils.showInfo("Stock Movement History", s"History for ${stock.foodName}:\n\n$historyText")
      case None =>
        GuiUtils.showWarning("No Selection", "Please select a stock item to view history.")
    }
  }
  
  private def handleFilter(): Unit = {
    val category = categoryCombo.value.value
    val status = statusCombo.value.value
    
    val allStocks = service.allFoodStocks
    
    // Filter by category using functional approach
    val categoryFiltered = if (category != "All") {
      val categoryEnum = FoodCategory.valueOf(category)
      allStocks.filter(_.category == categoryEnum)
    } else {
      allStocks
    }
    
    // Filter by status
    val filteredStocks = if (status != "All") {
      val statusEnum = StockStatus.valueOf(status)
      categoryFiltered.filter(_.stockStatus == statusEnum)
    } else {
      categoryFiltered
    }
    
    updateStocksList(filteredStocks)
  }
  
  private def handleSearch(): Unit = {
    val searchTerm = searchField.text.value.trim
    if (searchTerm.nonEmpty) {
      val results = service.searchFoodStocks(searchTerm)
      updateStocksList(results)
    } else {
      refreshStocks()
    }
  }
  
  private def handleExport(): Unit = {
    if (!service.isLoggedIn) {
      GuiUtils.showWarning("Login Required", "Please login to export stock data.")
      return
    }
    
    val stocks = service.allFoodStocks
    val exportDialog = new ExportStockDialog(stocks)
    exportDialog.showAndWait()
  }
  
  private def handleShowAlerts(): Unit = {
    val alerts = service.generateStockAlerts
    val alertText = if (alerts.nonEmpty) {
      alerts.mkString("\n")
    } else {
      "✅ No stock alerts at this time!\nAll items are properly stocked."
    }
    
    GuiUtils.showInfo("Stock Alerts", s"Current Stock Alerts:\n\n$alertText")
  }
  
  private def getSelectedStock(): Option[FoodStock] = {
    Option(stocksList).flatMap { list =>
      val selectedIndex = list.selectionModel().selectedIndex.value
      if (selectedIndex >= 0) {
        val allStocks = service.allFoodStocks
        // Apply current filters to get the displayed list
        val category = categoryCombo.value.value
        val status = statusCombo.value.value
        val searchTerm = searchField.text.value.trim
        
        // Apply filters functionally
        val categoryFiltered = if (category != "All") {
          val categoryEnum = FoodCategory.valueOf(category)
          allStocks.filter(_.category == categoryEnum)
        } else {
          allStocks
        }
        
        val statusFiltered = if (status != "All") {
          val statusEnum = StockStatus.valueOf(status)
          categoryFiltered.filter(_.stockStatus == statusEnum)
        } else {
          categoryFiltered
        }
        
        val filteredStocks = if (searchTerm.nonEmpty) {
          service.searchFoodStocks(searchTerm)
        } else {
          statusFiltered
        }
        
        if (selectedIndex < filteredStocks.length) {
          Some(filteredStocks(selectedIndex))
        } else None
      } else None
    }
  }
  
  private def createSidePanel(): VBox = {
    new VBox {
      spacing = 15
      prefWidth = 280
      
      children = Seq(
        createStockStatsCard(),
        createQuickActionsCard(),
        createAlertsCard()
      )
    }
  }
  
  private def createStockStatsCard(): VBox = {
    val (total, lowStock, outOfStock, expired) = service.stockStatistics
    
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("📊 Stock Statistics") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        createStatRow("Total Items", total.toString),
        createStatRow("Low Stock", lowStock.toString, if (lowStock > 0) "#ffc107" else "#28a745"),
        createStatRow("Out of Stock", outOfStock.toString, if (outOfStock > 0) "#dc3545" else "#28a745"),
        createStatRow("Expired", expired.toString, if (expired > 0) "#6c757d" else "#28a745")
      )
    }
  }
  
  private def createQuickActionsCard(): VBox = {
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: #f0f2f5; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("⚡ Quick Actions") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        },
        new Button("View Low Stock") {
          style = "-fx-background-color: #ffc107; -fx-text-fill: black; -fx-background-radius: 6; -fx-pref-width: 200;"
          onAction = _ => {
            statusCombo.value = "LOW_STOCK"
            handleFilter()
          }
        },
        new Button("View Expired Items") {
          style = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 6; -fx-pref-width: 200;"
          onAction = _ => {
            statusCombo.value = "EXPIRED"
            handleFilter()
          }
        },
        new Button("View Out of Stock") {
          style = "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 6; -fx-pref-width: 200;"
          onAction = _ => {
            statusCombo.value = "OUT_OF_STOCK"
            handleFilter()
          }
        }
      )
    }
  }
  
  private def createAlertsCard(): VBox = {
    val alerts = service.generateStockAlerts.take(5) // Show only first 5 alerts
    
    new VBox {
      spacing = 10
      padding = Insets(15)
      style = "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
      
      children = Seq(
        new Label("🚨 Recent Alerts") {
          style = "-fx-font-weight: bold; -fx-text-fill: #1877f2; -fx-font-size: 14px;"
        }
      ) ++ (if (alerts.nonEmpty) {
        alerts.map { alert =>
          new Label(alert) {
            style = "-fx-text-fill: #dc3545; -fx-font-size: 11px;"
            wrapText = true
          }
        }
      } else {
        Seq(new Label("No alerts at this time") {
          style = "-fx-text-fill: #28a745; -fx-font-size: 12px;"
        })
      })
    }
  }
  
  private def createStatRow(label: String, value: String, color: String = "#1877f2"): HBox = {
    new HBox {
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new Label(label) {
          style = "-fx-text-fill: #65676b; -fx-font-size: 12px;"
        },
        new Region { HBox.setHgrow(this, Priority.Always) },
        new Label(value) {
          style = s"-fx-text-fill: $color; -fx-font-weight: bold; -fx-font-size: 12px;"
        }
      )
    }
  }
}
