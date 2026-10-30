# Design & Aesthetic Guidelines — FinTrack

> **Strict Directive:** Avoid boilerplate "AI-slop design" patterns (e.g., oversized pastel rounded cards with no contrast, generic multi-colored icons inside tinted circles, unnecessary multi-step modal dialogs, and dead whitespace).

---

## 1. Visual Hierarchy & Theme
- **Color Palette:** High-contrast, deliberate system colors.
  - Neutral Base: Deep dark background (`#121212`) or clean light mode (`#F8F9FA`).
  - Expense Accent: Crisp Crimson (`#E53935`) for outflow.
  - Income Accent: Vibrant Emerald (`#43A047`) for inflow.
  - Primary Action: High-contrast Accent (`#2563EB`).
- **Typography:** Bold numeric representations for financial metrics (`JetBrains Mono` or tabular `Roboto` digits so values line up vertically).
- **Component Styling:**
  - Compact cards with clean, subtle borders (1px outline) instead of generic floating drop shadows.
  - Explicit inline balance indicators (`+ $120.00` in green, `- $45.50` in red).

---

## 2. Interaction & Micro-UX
- **Keyboard Behavior:** Opening the "Add Transaction" modal immediately focuses the numeric keypad. No extra taps to start typing amounts.
- **List Density:** Use compact list rows. Prioritize showing Category + Note + Amount upfront without hiding essential info behind multi-level navigation.