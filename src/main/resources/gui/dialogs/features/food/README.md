# Food Package Conversion Documentation

## Overview
This directory contains the FXML and CSS files converted from the original Scala GUI components in the `dialogs/features/food` package.

## File Mappings

### FoodPostDetailsDialog
- **Original**: `src/main/sample-gui/dialogs/features/food/FoodPostDetailsDialog.scala`
- **FXML**: `resources/gui/dialogs/features/food/FoodPostDetailsDialog.fxml`
- **CSS**: `resources/gui/dialogs/features/food/FoodPostDetailsDialog.css`

### FoodPostDialog
- **Original**: `src/main/sample-gui/dialogs/features/food/FoodPostDialog.scala`
- **FXML**: `resources/gui/dialogs/features/food/FoodPostDialog.fxml`
- **CSS**: `resources/gui/dialogs/features/food/FoodPostDialog.css`

## fx:id Reference Guide

### FoodPostDetailsDialog.fxml
- `bpMainLayout` - Main BorderPane container
- `vboxHeader` - Header section VBox
- `lblTitle` - Food post title label
- `hboxStatusInfo` - Status information HBox
- `lblPostType` - Post type label (OFFER/REQUEST)
- `lblStatus` - Status label with dynamic styling
- `spMainContent` - Main content ScrollPane
- `grdDetails` - Details GridPane
- `lblQuantityLabel`, `lblQuantityValue` - Quantity field labels
- `lblLocationLabel`, `lblLocationValue` - Location field labels
- `lblPostedLabel`, `lblPostedValue` - Posted date labels
- `lblExpiresLabel`, `lblExpiresValue` - Expiry date labels
- `lblAcceptedByLabel`, `lblAcceptedByValue` - Accepted by labels
- `lblAcceptedDateLabel`, `lblAcceptedDateValue` - Accepted date labels
- `vboxDescription` - Description section VBox
- `lblDescriptionTitle` - Description section title
- `txtDescription` - Description TextArea
- `vboxComments` - Comments section VBox
- `lblCommentsTitle` - Comments section title
- `lstComments` - Comments ListView
- `lblNoComments` - No comments placeholder label
- `hboxButtons` - Button container HBox
- `btnAccept` - Accept post button
- `btnComplete` - Mark complete button
- `btnClose` - Close dialog button

### FoodPostDialog.fxml
- `grdMainLayout` - Main GridPane container
- `lblTitleLabel` - Title field label
- `txtTitle` - Title TextField
- `lblTypeLabel` - Type field label
- `cmbType` - Type ComboBox
- `lblQuantityLabel` - Quantity field label
- `txtQuantity` - Quantity TextField
- `lblLocationLabel` - Location field label
- `txtLocation` - Location TextField
- `lblDescriptionLabel` - Description field label
- `txtDescription` - Description TextArea
- `chkHasExpiry` - Has expiry date CheckBox
- `dpExpiry` - Expiry DatePicker
- `hboxButtons` - Button container HBox
- `btnCreate` - Create button
- `btnCancel` - Cancel button

## CSS Classes

### Status Classes
- `.status-pending` - Yellow background for pending posts
- `.status-accepted` - Green background for accepted posts
- `.status-completed` - Gray background for completed posts
- `.status-cancelled` - Red background for cancelled posts

### Post Type Classes
- `.post-type-offer` - Green background for offer posts
- `.post-type-request` - Blue background for request posts

### Button Classes
- `.accept-button` - Green styling for accept buttons
- `.complete-button` - Blue styling for complete buttons
- `.close-button` - Gray styling for close buttons
- `.create-button` - Green styling for create buttons
- `.cancel-button` - Gray styling for cancel buttons

### Other Classes
- `.header-section` - Light gray background for header sections
- `.title-label` - Dark gray text for titles
- `.detail-label` - Bold dark gray text for detail labels
- `.detail-value` - Regular gray text for detail values
- `.section-title` - Bold dark gray text for section titles
- `.field-label` - Bold dark gray text for form field labels
- `.description-text` - Light gray background for description areas
- `.no-comments-label` - Italic gray text for no comments message

## Scene Builder Compatibility
All FXML files have been validated and should open correctly in Scene Builder. The files use standard JavaFX controls and layouts that are fully supported by Scene Builder.

## Conversion Notes
- Dynamic content (like food post data) is represented with placeholder text and appropriate fx:id tags for controller binding
- Conditional UI elements (like expiry date fields) are included with appropriate visibility controls
- Event handlers are mapped to placeholder method names that should be implemented in the controller classes
- All inline styles from the original Scala code have been extracted to separate CSS files
- CSS classes have been created for different states and dynamic styling needs
- ComboBox items (like post types) should be populated programmatically in the controller for better Scene Builder compatibility