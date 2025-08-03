#!/usr/bin/env python3
"""
Simple FXML validation script to check XML syntax and basic structure
"""
import xml.etree.ElementTree as ET
import os
import sys

def validate_fxml_file(filepath):
    """Validate a single FXML file"""
    print(f"Validating {filepath}...")
    
    try:
        # Parse the XML file
        tree = ET.parse(filepath)
        root = tree.getroot()
        
        # Check if it's a valid FXML structure
        if root.tag not in ['BorderPane', 'GridPane', 'VBox', 'HBox', 'AnchorPane', 'StackPane']:
            print(f"  WARNING: Root element '{root.tag}' might not be a typical JavaFX container")
        
        # Check for fx:id attributes
        fx_ids = []
        for elem in root.iter():
            fx_id = elem.get('{http://javafx.com/fxml/1}id')
            if fx_id:
                if fx_id in fx_ids:
                    print(f"  ERROR: Duplicate fx:id '{fx_id}' found")
                    return False
                fx_ids.append(fx_id)
        
        print(f"  ✓ Valid XML structure")
        print(f"  ✓ Found {len(fx_ids)} unique fx:id attributes")
        
        # Check for stylesheets reference
        stylesheets_found = False
        for elem in root.iter():
            if elem.tag == 'stylesheets' or elem.tag.endswith('}stylesheets'):
                stylesheets_found = True
                break
        
        if stylesheets_found:
            print(f"  ✓ Stylesheet reference found")
        else:
            print(f"  WARNING: No stylesheet reference found")
        
        return True
        
    except ET.ParseError as e:
        print(f"  ERROR: XML parsing failed - {e}")
        return False
    except Exception as e:
        print(f"  ERROR: Validation failed - {e}")
        return False

def validate_css_file(filepath):
    """Basic CSS file validation"""
    print(f"Validating {filepath}...")
    
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Basic checks
        if not content.strip():
            print(f"  ERROR: CSS file is empty")
            return False
        
        # Count CSS rules (simple heuristic)
        rule_count = content.count('{')
        if rule_count == 0:
            print(f"  ERROR: No CSS rules found")
            return False
        
        print(f"  ✓ CSS file contains {rule_count} rules")
        
        # Check for common JavaFX CSS properties
        fx_properties = ['-fx-background-color', '-fx-text-fill', '-fx-padding', '-fx-border-color']
        found_properties = [prop for prop in fx_properties if prop in content]
        
        if found_properties:
            print(f"  ✓ Found JavaFX CSS properties: {', '.join(found_properties)}")
        else:
            print(f"  WARNING: No common JavaFX CSS properties found")
        
        return True
        
    except Exception as e:
        print(f"  ERROR: CSS validation failed - {e}")
        return False

def main():
    """Main validation function"""
    food_dir = "resources/gui/dialogs/features/food"
    
    if not os.path.exists(food_dir):
        print(f"ERROR: Directory {food_dir} does not exist")
        return False
    
    files_to_validate = [
        ("FoodPostDetailsDialog.fxml", validate_fxml_file),
        ("FoodPostDetailsDialog.css", validate_css_file),
        ("FoodPostDialog.fxml", validate_fxml_file),
        ("FoodPostDialog.css", validate_css_file)
    ]
    
    all_valid = True
    
    for filename, validator in files_to_validate:
        filepath = os.path.join(food_dir, filename)
        if os.path.exists(filepath):
            if not validator(filepath):
                all_valid = False
        else:
            print(f"ERROR: File {filepath} does not exist")
            all_valid = False
        print()
    
    if all_valid:
        print("✓ All files in food package validated successfully!")
        return True
    else:
        print("✗ Some validation errors found")
        return False

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)