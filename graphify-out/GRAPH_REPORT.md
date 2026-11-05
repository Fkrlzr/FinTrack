# Graph Report - FinTrack  (2026-11-04)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 545 nodes · 1275 edges · 25 communities (11 shown, 14 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 27 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `e014da11`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Community 0
- Community 1
- Community 2
- Community 3
- Community 4
- Community 5
- Community 6
- Community 7
- Community 9
- Community 11
- Community 13
- Community 16
- Community 17
- Community 18
- Community 19
- Community 21

## God Nodes (most connected - your core abstractions)
1. `DashboardViewModel` - 51 edges
2. `Transaction` - 33 edges
3. `TransactionRepositoryImpl` - 27 edges
4. `TransactionRepository` - 26 edges
5. `Category` - 25 edges
6. `TransactionEditorSheet()` - 22 edges
7. `RecurringSheet()` - 22 edges
8. `DashboardUiState` - 21 edges
9. `TransactionType` - 20 edges
10. `FinTrackDatabase` - 18 edges

## Surprising Connections (you probably didn't know these)
- `toDomain()` --calls--> `Transaction`  [EXTRACTED]
  app/src/main/java/com/fintrack/data/local/TransactionEntity.kt → app/src/main/java/com/fintrack/domain/model/Models.kt
- `Deleted` --references--> `Transaction`  [EXTRACTED]
  app/src/main/java/com/fintrack/ui/dashboard/DashboardUiState.kt → app/src/main/java/com/fintrack/domain/model/Models.kt
- `ChartCard()` --calls--> `Row`  [INFERRED]
  app/src/main/java/com/fintrack/ui/dashboard/CashflowChart.kt → app/src/main/java/com/fintrack/data/backup/CsvBackup.kt
- `LegendItem()` --calls--> `Row`  [INFERRED]
  app/src/main/java/com/fintrack/ui/dashboard/CashflowChart.kt → app/src/main/java/com/fintrack/data/backup/CsvBackup.kt
- `BreakdownRow()` --calls--> `Row`  [INFERRED]
  app/src/main/java/com/fintrack/ui/dashboard/CategoryBreakdown.kt → app/src/main/java/com/fintrack/data/backup/CsvBackup.kt

## Import Cycles
- None detected.

## Communities (25 total, 14 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.05
Nodes (19): BudgetEntity, toDomain(), toEntity(), RecurringEntity, toDomain(), toEntity(), toDomain(), Budget (+11 more)

### Community 1 - "Community 1"
Cohesion: 0.06
Nodes (12): CategoryEntity, toDomain(), toEntity(), FinTrackDatabase, SeedCallback, BudgetDao, CategoryDao, RecurringDao (+4 more)

### Community 2 - "Community 2"
Cohesion: 0.05
Nodes (9): MainActivity, AppLockStore, authenticators(), canUseAppLock(), showUnlockPrompt(), AuthenticationCallback, LockScreen(), FinTrackColors (+1 more)

### Community 3 - "Community 3"
Cohesion: 0.06
Nodes (13): TransactionEditorSheet(), TypeToggle(), formatCompact(), formatMonth(), formatPlainAmount(), formatTime(), millisToHour(), millisToMinute() (+5 more)

### Community 4 - "Community 4"
Cohesion: 0.11
Nodes (27): Row, ActionTile(), AmountField(), ChoiceRow(), DropdownField(), SquareButton(), SquareChip(), DashboardContent() (+19 more)

### Community 5 - "Community 5"
Cohesion: 0.09
Nodes (24): Category, WalletSummary, BarCanvas(), CashflowChart(), ChartCard(), ChartReadout(), drawBar(), LegendItem() (+16 more)

### Community 7 - "Community 7"
Cohesion: 0.15
Nodes (6): TransactionDao, CategorySumRow, toDomain(), toEntity(), TransactionEntity, WalletNetRow

### Community 13 - "Community 13"
Cohesion: 0.23
Nodes (6): Transaction, Edit, EditorTarget, New, Controls, millisToLocalDate()

### Community 17 - "Community 17"
Cohesion: 0.44
Nodes (5): SummaryCell(), SummarySection(), formatMoney(), formatShortMonth(), formatSigned()

### Community 19 - "Community 19"
Cohesion: 0.50
Nodes (4): Failed, Load, Loading, Ready

### Community 21 - "Community 21"
Cohesion: 0.67
Nodes (3): DashboardEvent, Deleted, Message

## Knowledge Gaps
- **11 isolated node(s):** `New`, `Loading`, `Message`, `Wallets`, `MONTHLY` (+6 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 162 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **14 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Transaction` connect `Community 13` to `Community 0`, `Community 3`, `Community 4`, `Community 5`, `Community 6`, `Community 7`, `Community 8`, `Community 9`, `Community 10`, `Community 11`, `Community 19`, `Community 21`?**
  _High betweenness centrality (0.155) - this node is a cross-community bridge._
- **What connects `New`, `Loading`, `Message` to the rest of the system?**
  _11 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.05136612021857923 - nodes in this community are weakly interconnected._
- **Why does `DashboardViewModel` connect `Community 11` to `Community 0`, `Community 1`, `Community 2`, `Community 3`, `Community 4`, `Community 5`, `Community 10`, `Community 13`, `Community 18`, `Community 19`, `Community 21`?**
  _High betweenness centrality (0.126) - this node is a cross-community bridge._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.06453634085213032 - nodes in this community are weakly interconnected._
- **Why does `TransactionType` connect `Community 0` to `Community 3`, `Community 4`, `Community 5`, `Community 6`, `Community 7`, `Community 9`, `Community 10`, `Community 13`, `Community 17`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.050980392156862744 - nodes in this community are weakly interconnected._