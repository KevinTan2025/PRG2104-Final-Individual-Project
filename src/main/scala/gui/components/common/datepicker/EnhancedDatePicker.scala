package gui.components.common.datepicker

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.Scene
import scalafx.stage.{Stage, Modality}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.{Font, FontWeight}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.language.implicitConversions

class EnhancedDatePicker {
  private var selectedDate: Option[LocalDate] = None
  private var displayMonth: LocalDate = LocalDate.now().withDayOfMonth(1)
  
  private def updateDisplayText(): Unit = {
    dateField.text = selectedDate.map(_.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
      .getOrElse("")
  }
  
  private val dateField = new TextField {
    promptText = "Select Date"
    editable = false
    style = "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 6;"
  }
  
  private val dateButton = new Button("📅") {
    style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6;"
    onAction = _ => showCalendar()
  }
  
  val control: HBox = new HBox {
    spacing = 5
    children = Seq(dateField, dateButton)
    alignment = Pos.CenterLeft
  }
  
  def getValue: Option[LocalDate] = selectedDate
  
  def setValue(date: LocalDate): Unit = {
    selectedDate = Some(date)
    displayMonth = date.withDayOfMonth(1)
    updateDisplayText()
  }
  
  private val dialog = new Stage {
    title = "Select Date"
    resizable = false
    initModality(Modality.ApplicationModal)
  }
  
  private val calendarGrid = new GridPane {
    hgap = 2
    vgap = 2
    style = "-fx-background-color: white;"
  }
  
  private def updateCalendar(): Unit = {
    monthLabel.text = displayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    calendarGrid.children.clear()
    
    // Add day headers
    val dayHeaders = Array("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    for (col <- dayHeaders.indices) {
      calendarGrid.add(new Label(dayHeaders(col)) {
        font = Font.font("System", FontWeight.Bold, 12)
        style = "-fx-text-fill: #65676b; -fx-padding: 8;"
        prefWidth = 35
        alignment = Pos.Center
      }, col, 0)
    }
    
    // Add calendar days
    val firstDay = displayMonth.withDayOfMonth(1)
    val startDayOfWeek = firstDay.getDayOfWeek.getValue % 7 // Convert to Sunday = 0
    val daysInMonth = displayMonth.lengthOfMonth()
    
    var dayCount = 1
    for (row <- 1 to 6) {
      for (col <- 0 to 6) {
        val cellIndex = (row - 1) * 7 + col
        if (cellIndex >= startDayOfWeek && dayCount <= daysInMonth) {
          val day = dayCount
          val dayButton = new Button(day.toString) {
            prefWidth = 35
            prefHeight = 35
            val cellDate = displayMonth.withDayOfMonth(day)
            val isToday = cellDate.equals(LocalDate.now())
            val isSelected = selectedDate.contains(cellDate)
            
            style = if (isSelected) {
              "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6;"
            } else if (isToday) {
              "-fx-background-color: #e3f2fd; -fx-text-fill: #1877f2; -fx-background-radius: 6;"
            } else {
              "-fx-background-color: white; -fx-text-fill: #1c1e21; -fx-background-radius: 6;"
            }
            
            onAction = _ => {
              selectedDate = Some(cellDate)
              updateDisplayText()
              dialog.close()
            }
          }
          calendarGrid.add(dayButton, col, row)
          dayCount += 1
        }
      }
    }
  }
  
  private val monthLabel = new Label {
    font = Font.font("System", FontWeight.Bold, 16)
    style = "-fx-text-fill: #1877f2;"
  }
  
  private val prevButton = new Button("◀") {
    style = "-fx-background-color: #f0f2f5; -fx-background-radius: 6;"
    onAction = _ => {
      displayMonth = displayMonth.minusMonths(1)
      updateCalendar()
    }
  }
  
  private val nextButton = new Button("▶") {
    style = "-fx-background-color: #f0f2f5; -fx-background-radius: 6;"
    onAction = _ => {
      displayMonth = displayMonth.plusMonths(1)
      updateCalendar()
    }
  }
  
  private val todayButton = new Button("Today") {
    style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6;"
    onAction = _ => {
      val today = LocalDate.now()
      selectedDate = Some(today)
      displayMonth = today.withDayOfMonth(1)
      updateCalendar()
      updateDisplayText()
      dialog.close()
    }
  }
  
  private val cancelButton = new Button("Cancel") {
    style = "-fx-background-color: #e4e6ea; -fx-text-fill: #65676b; -fx-background-radius: 6;"
    onAction = _ => dialog.close()
  }
  
  private def showCalendar(): Unit = {
    val headerBox = new HBox {
      spacing = 10
      padding = Insets(10)
      alignment = Pos.Center
      children = Seq(prevButton, monthLabel, nextButton)
    }
    
    val buttonBox = new HBox {
      spacing = 10
      padding = Insets(10)
      alignment = Pos.CenterRight
      children = Seq(todayButton, cancelButton)
    }
    
    val content = new VBox {
      spacing = 10
      children = Seq(headerBox, calendarGrid, buttonBox)
      style = "-fx-background-color: white;"
    }
    
    updateCalendar()
    
    dialog.scene = new Scene(content)
    dialog.showAndWait()
  }
}
