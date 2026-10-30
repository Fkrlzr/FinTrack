# Kiro IDE Steering Instructions — FinTrack

## Execution Rules for Kiro IDE

1. **Skill Selection & Activation:**
   - Scan available Kiro IDE skills upon task start.
   - **MUST USE:** Activate and prioritize all available **Anti-Slop Design / Clean UI** skills, **Android/Jetpack Compose** best-practice skills, and **Kotlin Architecture** skills.
   - Refuse any generation that produces generic design templates, unstyled Material 3 default place-holders, or bloated boilerplate code.

2. **Code Generation Principles:**
   - **UI Engine:** Modern Jetpack Compose with strict state hoisting (`Unidirectional Data Flow`).
   - **Local Storage:** Room Database + Kotlin Coroutines & Flow.
   - **No Login Overhead:** Do NOT write auth utilities, token handlers, Firebase auth, or network sync layers.

3. **Anti-Slop Design Enforcement:**
   - Always use tabular/monospaced digits for financial numbers to prevent layout jitter during updates.
   - Use direct, highly functional inputs (e.g., direct inline toggles over multi-page wizards).
   - Ensure haptic feedback touches on action triggers if supported by Jetpack Compose capabilities.