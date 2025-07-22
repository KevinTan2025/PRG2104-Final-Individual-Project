package gui.components.features.foodstock

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import gui.utils.GuiUtils
import gui.dialogs.features.foodstock.{FoodStockDialog, StockMovementDialog}
import gui.components.common.public.BaseTabComponent
import service.CommunityEngagementService
import model.{FoodStock, FoodCategory, StockStatus}

/**
 * Food Stock Management tab component
 * 安全级别: USER - 注册用户可以管理食物库存
 */
class FoodStockTab extends BaseTabComponent {
  
  private val stocksList = new ListView[String]()
  private val categoryCombo = new ComboBox[String] {
    items = scalafx.collections.ObservableBuffer(
      ("All" :: FoodCategory.values.map(_.toString).toList): _*
    )
    value = "All"
  }
  private val statusCombo = new ComboBox[String] {
    items = scalafx.collections.ObservableBuffer(
      "All", "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK", "EXPIRED"
    )
    value = "All"
  }
  private val searchField = new TextField {
    promptText = "Search food items..."
  }
  
  override def build(): Tab = {
    refreshStocks()
    
    val addStockButton = new Button("➕ Add New Stock") {
      style = "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => handleAddStock()
    }
    
    val editStockButton = new Button("✏️ Edit Stock") {
      style = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => handleEditStock()
    }
    
    val manageStockButton = new Button("📦 Manage Stock") {
      style = "-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => handleManageStock()
    }
    
    val deleteStockButton = new Button("🗑️ Delete Stock") {
      style = "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => handleDeleteStock()
    }
    
    val viewHistoryButton = new Button("📊 View History") {
      style = "-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => handleViewHistory()
    }
    
    val filterButton = new Button("🔍 Filter") {
      onAction = (_: ActionEvent) => handleFilter()
    }
    
    val searchButton = new Button("Search") {
      onAction = (_: ActionEvent) => handleSearch()
    }
    
    val refreshButton = new Button("🔄 Refresh") {
      onAction = (_: ActionEvent) => refreshStocks()
    }
    
    val exportButton = new Button("📋 Export") {
      onAction = (_: ActionEvent) => handleExport()
    }
    
    val alertsButton = new Button("⚠️ Alerts") {
      style = "-fx-background-color: #fd7e14; -fx-text-fill: white; -fx-font-weight: bold;"
      onAction = (_: ActionEvent) => handleShowAlerts()
    }
    
    // Main action buttons
    val mainControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(addStockButton, editStockButton, manageStockButton, deleteStockButton, viewHistoryButton)
    }
    
    // Filter controls
    val filterControls = new HBox {
      spacing = 10
      padding = Insets(10)
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("Category:"),
        categoryCombo,
        new Label("Status:"),
        statusCombo,
        filterButton,
        refreshButton,
        alertsButton,
        exportButton
      )
    }
    
    // Search controls
    val searchControls = new HBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(searchField, searchButton)
    }
    
    // Side panel with stats and info
    val sidePanel = createSidePanel()
    
    val mainContent = new HBox {
      spacing = 20
      padding = Insets(20)
      children = Seq(
        new VBox {
          spacing = 10
          children = Seq(
            mainControls,
            filterControls, 
            searchControls,
            stocksList
          )
          HBox.setHgrow(this, Priority.Always)
        },
        sidePanel
      )
    }
    
    new Tab {
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
    val stocks = service.getAllFoodStocks
    updateStocksList(stocks)
  }
  
  private def updateStocksList(stocks: List[FoodStock]): Unit = {
    val items = stocks.map { stock =>
      val status = stock.getStockStatus
      val statusIcon = status match {
        case StockStatus.IN_STOCK => "✅"
        case StockStatus.LOW_STOCK => "⚠️"
        case StockStatus.OUT_OF_STOCK => "❌"
        case StockStatus.EXPIRED => "💀"
      }
      
      val expiryInfo = stock.getDaysUntilExpiry match {
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
  
  private def handleAddStock(): Unit = {
    val dialog = new FoodStockDialog(None, () => refreshStocks())
    dialog.showAndWait()
  }
  
  private def handleEditStock(): Unit = {
    getSelectedStock() match {
      case Some(stock) =>
        val dialog = new FoodStockDialog(Some(stock), () => refreshStocks())
        dialog.showAndWait()
      case None =>
        GuiUtils.showWarning("No Selection", "Please select a stock item to edit.")
    }
  }
  
  private def handleManageStock(): Unit = {
    getSelectedStock() match {
      case Some(stock) =>
        val dialog = new StockMovementDialog(stock, () => refreshStocks())
        dialog.showAndWait()
      case None =>
        GuiUtils.showWarning("No Selection", "Please select a stock item to manage.")
    }
  }
  
  private def handleDeleteStock(): Unit = {
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
        val movements = service.getStockMovements(stock.stockId)
        val historyText = if (movements.nonEmpty) {
          movements.map { movement =>
            s"${movement.timestamp.toLocalDate} ${movement.timestamp.toLocalTime.toString.take(8)} - " +
            s"${movement.getDescription} by ${movement.userId}"
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
    
    var filteredStocks = service.getAllFoodStocks
    
    // Filter by category
    if (category != "All") {
      val categoryEnum = FoodCategory.valueOf(category)
      filteredStocks = filteredStocks.filter(_.category == categoryEnum)
    }
    
    // Filter by status
    if (status != "All") {
      val statusEnum = StockStatus.valueOf(status)
      filteredStocks = filteredStocks.filter(_.getStockStatus == statusEnum)
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
    val stocks = service.getAllFoodStocks
    val csvContent = "Food Name,Category,Quantity,Unit,Status,Location,Expiry Date\n" +
      stocks.map { stock =>
        val expiryStr = stock.expiryDate.map(_.toString).getOrElse("No expiry")
        s"${stock.foodName},${stock.category},${stock.currentQuantity},${stock.unit}," +
        s"${stock.getStockStatus},${stock.location},$expiryStr"
      }.mkString("\n")
    
    GuiUtils.showInfo("Export Stock Data", s"CSV Export:\n\n$csvContent")
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
    val selectedIndex = stocksList.selectionModel().selectedIndex.value
    if (selectedIndex >= 0) {
      val allStocks = service.getAllFoodStocks
      // Apply current filters to get the displayed list
      val category = categoryCombo.value.value
      val status = statusCombo.value.value
      val searchTerm = searchField.text.value.trim
      
      var filteredStocks = allStocks
      
      if (category != "All") {
        val categoryEnum = FoodCategory.valueOf(category)
        filteredStocks = filteredStocks.filter(_.category == categoryEnum)
      }
      
      if (status != "All") {
        val statusEnum = StockStatus.valueOf(status)
        filteredStocks = filteredStocks.filter(_.getStockStatus == statusEnum)
      }
      
      if (searchTerm.nonEmpty) {
        filteredStocks = service.searchFoodStocks(searchTerm)
      }
      
      if (selectedIndex < filteredStocks.length) {
        Some(filteredStocks(selectedIndex))
      } else None
    } else None
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
    val (total, lowStock, outOfStock, expired) = service.getStockStatistics
    
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
