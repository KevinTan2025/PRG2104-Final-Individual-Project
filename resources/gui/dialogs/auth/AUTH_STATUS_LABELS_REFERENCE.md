# Auth Dialog Status Labels Reference

This document lists all the status labels (fx:id) added to the auth dialog FXML files based on the original FacebookStyleAuthDialog.scala validation logic.

## 📋 WelcomeAuthDialog.fxml

**Status Labels**: None (Welcome screen doesn't need validation)

**fx:id Elements**:

- `lblTitle` - Main title
- `lblWelcomeIcon` - Welcome icon
- `lblWelcomeText` - Welcome message
- `sepWelcome1`, `sepWelcome2` - Separators
- `btnWelcomeLogin`, `btnWelcomeRegister`, `btnWelcomeGuest` - Action buttons

---

## 🔐 LoginAuthDialog.fxml

**Status Labels**: 3 new status labels added

**New fx:id Elements**:

- `lblLoginUsernameStatus` - Username/email validation status
- `lblLoginPasswordStatus` - Password validation status
- `lblLoginErrorStatus` - General login error messages
- `vboxUsernameSection`, `vboxPasswordSection` - Container sections

**Validation Messages** (from Scala code):

- Login errors: "Invalid username or password"
- Missing fields: "Please enter both username and password"

**Total fx:id count**: 14 (was 9)

---

## 📝 RegisterAuthDialog.fxml

**Status Labels**: 7 status labels for comprehensive validation

**New fx:id Elements**:

- `lblUsernameStatus` - Username validation

  - ✗ "Username must be at least 3 characters"
  - ✗ "Username can only contain letters, numbers, and underscore"
  - ✗ "Username is already taken"
  - ✓ "Username is available"

- `lblNameStatus` - Full name validation
- `lblContactStatus` - Contact info validation
- `lblEmailStatus` - Email validation

  - ✗ "Invalid email format"
  - ✗ "Email is already registered"
  - ✓ "Valid email format"

- `lblPasswordStatus` - Password complexity validation

  - ✓ "Password meets all requirements"
  - ✗ "Missing: [requirements list]" (8+ chars, uppercase, lowercase, digit, special char)

- `lblConfirmPasswordStatus` - Password confirmation

  - ✓ "Passwords match"
  - ✗ "Passwords do not match"

- `lblOtpStatus` - OTP verification status
  - "Sending verification email..."
  - ✓ "Email verified successfully!"
  - ✗ "Verification cancelled"

**Container Sections**:

- `vboxUsernameSection`, `vboxNameSection`, `vboxContactSection`
- `vboxEmailSection`, `vboxPasswordSection`, `vboxConfirmPasswordSection`

**Total fx:id count**: 28 (was 24)

---

## 📧 OTPVerificationDialog.fxml

**Status Labels**: 2 new status labels added

**New fx:id Elements**:

- `lblOtpInputStatus` - OTP input field validation

  - ✗ "Please enter the verification code from your email"
  - ✗ "Verification code must be exactly 6 digits"
  - ✗ "Please check your email for the correct format"

- `lblVerificationStatus` - Overall verification status
  - ✗ "The verification code you entered is incorrect"
  - ✓ "Your email has been verified successfully!"

**Total fx:id count**: 17 (was 15)

---

## 🎨 CSS Classes for Status Labels

All status labels use these CSS classes:

```css
.status-label {
  -fx-font-size: 11px;
  -fx-padding: 2 0 0 5;
}

.status-label-success {
  -fx-text-fill: #38a169; /* Green for success ✓ */
}

.status-label-error {
  -fx-text-fill: #e53e3e; /* Red for errors ✗ */
}

.status-label-info {
  -fx-text-fill: #3182ce; /* Blue for info messages */
}
```

## 🔧 Input Field Validation States

Input fields change border colors based on validation:

```css
.auth-input-field-success {
  -fx-border-color: #38a169; /* Green border */
}

.auth-input-field-error {
  -fx-border-color: #e53e3e; /* Red border */
}
```

## 📊 Summary

| Dialog                | Original fx:id | Updated fx:id | Status Labels Added |
| --------------------- | -------------- | ------------- | ------------------- |
| WelcomeAuthDialog     | 9              | 9             | 0                   |
| LoginAuthDialog       | 9              | 14            | 3                   |
| RegisterAuthDialog    | 24             | 28            | 7                   |
| OTPVerificationDialog | 15             | 17            | 2                   |
| **Total**             | **57**         | **68**        | **12**              |

All status labels follow the naming convention `lbl[Context]Status` and are ready for controller binding.
