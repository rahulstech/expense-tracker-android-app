# Expense Tracker

![App Icon](./icon_expense_tracker_app.webp)

A simple, clean, no-nonsense Android app for tracking daily credits, debits, balances, and groups. The app focuses on privacy (no networking), offline reliability, and smooth UX across modern Android versions.

## ✨ Features

**Core**

* Track multiple **account balances**
* Add **credit**, **debit**, and **transfer** entries
* Create **groups** to organize transactions
* Local **backup & restore** (JSON or file-based)
* **Automatic backup** using WorkManager
* Fully offline – **no network access**

**UI & Experience**

* Material 3 styling
* Light & dark theme (follows system setting)
* Smooth UI backed by state management with Kotlin Flow and SharedFlow
* Responsive layouts using ConstraintLayout + RecyclerView + CardView

## 🔧 Tech Stack

**Platform:** Ubuntu 24.04 LTS
**Android Studio:** Norwhal | Kotlin 2.1.0
**SDK:** Target 35, Min 26
**Tested On:**

* AVD Android 28, Android 34
* Physical Android 28

**Architecture:** MVVM
**Language:** Kotlin

**Android Jetpack:**

* LiveData
* ViewModel
* Room
* Navigation
* Data Binding
* WorkManager
* RecyclerView
* ConstraintLayout

**Async:** Coroutines + Flow
**DI:** None
**Testing:** JUnit

## 🧩 Technical Challenges & Solutions

**RecyclerView item selection**
Implemented a custom item-selection mechanism for predictable behavior across list updates.

**Automatic backup using WorkManager**
Set up periodic work with constraints and carefully handled SDK-level behavior differences.

**Storage access across Android versions**
Handled scoped storage and platform quirks for saving and restoring backup files reliably.

**UI state management with Kotlin Flow**
Used `SharedFlow` + sealed UI states (`UILoading`, `UISuccess`, `UIError`) for predictable and testable UI updates.

## 🌓 Theming

* Material 3
* Both light and dark variants
* App theme follows system setting

## 🚀 Upcoming Features (Next 2–3 Releases)

* Income & expense categories
* Budget creation + monthly summaries
* PDF and Excel export
* Charts for income, expense, and budget insights
* Track unit-based assets (stocks, MF, gold, etc.)
* Google Drive cloud backup + restore
* In-app calculator

## 📦 Project Links

**GitHub:** [Visit Github](https://github.com/rahulstech)
**LinkedIn:** [Visit LinkedIn](https://www.linkedin.com/in/rahul-bagchi-176a63212/)

