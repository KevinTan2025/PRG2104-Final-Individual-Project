package gui.components.common.datepicker

import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.event.ActionEvent
import scalafx.Includes._
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.stage.{Modality, Stage}
import scalafx.scene.Scene
import scalafx.beans.property.ObjectProperty
import java.time.{LocalTime}
import java.time.format.DateTimeFormatter

/**
 * Enhanced Time Picker component with time selection popup
 */
class EnhancedTimePicker(placeholderText: String = "Select time") {
  
  private val selectedTimeProperty = ObjectProperty[Option[LocalTime]](None)
  private val timeField = new TextField {
    promptText = placeholderText
    editable = false
    style = "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 6;"
  }
  
  private val timeButton = new Button("🕐") {
    style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6;"
    onAction = _ => showTimePopup()
  }
  
  val control: HBox = new HBox {
    spacing = 5
    children = Seq(timeField, timeButton)
    alignment = Pos.CenterLeft
  }
  
  def getValue: Option[LocalTime] = selectedTimeProperty.value
  
  def setValue(time: LocalTime): Unit = {
    selectedTimeProperty.value = Some(time)
    updateDisplayText()
  }
  
  def getText: String = timeField.text.value
  
  private def updateDisplayText(): Unit = {
    selectedTimeProperty.value match {
      case Some(time) => 
        timeField.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
      case None => 
        timeField.text = ""
    }
  }
  
  private def showTimePopup(): Unit = {
    val dialog = new Stage()
    dialog.title = "Select Time"
    dialog.initModality(Modality.ApplicationModal)
    dialog.resizable = false
    
    val currentTime = selectedTimeProperty.value.getOrElse(LocalTime.now())
    
    val hourSpinner = new Spinner[Integer](0, 23, currentTime.getHour) {
      prefWidth = 70
      style = "-fx-font-size: 14px;"
    }
    
    val minuteSpinner = new Spinner[Integer](0, 59, currentTime.getMinute) {
      prefWidth = 70
      style = "-fx-font-size: 14px;"
    }
    
    val presetTimes = Seq(
      ("9:00", LocalTime.of(9, 0)),
      ("12:00", LocalTime.of(12, 0)),
      ("14:00", LocalTime.of(14, 0)),
      ("18:00", LocalTime.of(18, 0)),
      ("20:00", LocalTime.of(20, 0))
    )
    
    val presetButtons = presetTimes.map { case (label, time) =>
      new Button(label) {
        style = "-fx-background-color: #f0f2f5; -fx-background-radius: 6; -fx-padding: 5 10;"
        onAction = _ => {
          hourSpinner.valueFactory.value.setValue(time.getHour)
          minuteSpinner.valueFactory.value.setValue(time.getMinute)
        }
      }
    }
    
    val nowButton = new Button("Now") {
      style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6;"
      onAction = _ => {
        val now = LocalTime.now()
        hourSpinner.valueFactory.value.setValue(now.getHour)
        minuteSpinner.valueFactory.value.setValue(now.getMinute)
      }
    }
    
    val okButton = new Button("OK") {
      style = "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-background-radius: 6;"
      onAction = _ => {
        val hour = hourSpinner.value.value.intValue()
        val minute = minuteSpinner.value.value.intValue()
        selectedTimeProperty.value = Some(LocalTime.of(hour, minute))
        updateDisplayText()
        dialog.close()
      }
    }
    
    val cancelButton = new Button("Cancel") {
      style = "-fx-background-color: #e4e6ea; -fx-text-fill: #65676b; -fx-background-radius: 6;"
      onAction = _ => dialog.close()
    }
    
    val timeControls = new HBox {
      spacing = 10
      alignment = Pos.Center
      children = Seq(
        new Label("Hour:") { font = Font.font("System", 12) },
        hourSpinner,
        new Label("Minute:") { font = Font.font("System", 12) },
        minuteSpinner
      )
    }
    
    val presetBox = new VBox {
      spacing = 10
      children = Seq(
        new Label("Quick Times:") { 
          font = Font.font("System", FontWeight.Bold, 12)
          style = "-fx-text-fill: #65676b;"
        },
        new HBox {
          spacing = 5
          children = presetButtons
          alignment = Pos.Center
        }
      )
    }
    
    val buttonBox = new HBox {
      spacing = 10
      padding = Insets(10)
      alignment = Pos.CenterRight
      children = Seq(nowButton, cancelButton, okButton)
    }
    
    val content = new VBox {
      spacing = 20
      padding = Insets(20)
      children = Seq(
        new Label("Select Time") {
          font = Font.font("System", FontWeight.Bold, 16)
          style = "-fx-text-fill: #1877f2;"
        },
        timeControls,
        presetBox,
        buttonBox
      )
      style = "-fx-background-color: white;"
    }
    
    dialog.scene = new Scene(content)
    dialog.showAndWait()
  }
}
