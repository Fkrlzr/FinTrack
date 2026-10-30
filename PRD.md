# Product Requirements Document (PRD) — FinTrack

## 1. Product Overview
**FinTrack** is a lightweight, local-first Android application designed for personal income and expense tracking. It prioritizes speed, privacy, and frictionless manual logging without unnecessary complexity.

---

## 2. Core Philosophy & Constraints
- **Zero Friction:** No account creation, login, or internet connectivity required.
- **Privacy First:** All data stays 100% on the user's local device storage.
- **Anti-Slop UX:** High utility density, intentional visual hierarchy, zero generic filler, crisp feedback, and immediate interaction readiness upon launch.

---

## 3. Key Feature Specifications

### 3.1 Quick Transaction Logging
- Primary action to add an **Income** or **Expense**.
- **Fields:**
  - Amount (Numeric input, auto-focused on sheet/screen open)
  - Type (Segmented toggle: Expense / Income)
  - Category (Single select: Food, Transport, Utilities, Salary, Shopping, Others)
  - Note / Description (Optional string, single line)
  - Date (Defaults to current timestamp)

### 3.2 Main Dashboard & History
- **Summary Cards:** Total Balance, Total Monthly Income, Total Monthly Expense.
- **Transaction Feed:** Clean chronological list grouped by date.
- **Filters:** Quick filter by month/year and category.
- **Actions:** Tap item to edit/view details, swipe-to-delete with undo toast.

### 3.3 Data Persistence
- Built using **Room Database**.
- Automatic migration setup and structured entity mapping.

---

## 4. Non-Functional Requirements
- **Cold Start:** Under 1.5 seconds directly into the main entry/dashboard view.
- **Target Platform:** Android (Kotlin, Jetpack Compose).
- **Architecture:** MVVM / Clean Architecture (Data layer, Domain layer, Presentation layer).