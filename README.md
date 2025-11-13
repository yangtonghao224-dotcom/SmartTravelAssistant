## Smart Travel Assistant
Smart Travel Assistant is a modern Android application built using Kotlin and Jetpack Compose.
It helps users manage itineraries, track travel expenses, monitor budgets with smart alerts, and convert currencies in real time.
The project demonstrates professional Android development practices, including MVVM architecture, Room database, Hilt dependency injection, Retrofit networking, and modern Compose UI.

## Core Features
## Itinerary Management

Add, view, and delete itinerary items

Compose-driven UI with automatic state updates

Persistent local storage using Room

## Expenses and Budget Tracking

Add detailed expense entries (title, amount, category, date)

Automatic total calculation and category-wise statistics

Pie chart visualization (Canvas drawing)

Budget progress indicator with percentage

Budget alerts at 50%, 70%, 90%, and 100%

System notifications using NotificationChannel + NotificationCompat

Supports Android 13+ notification permission workflow

## Real-Time Currency Exchange

Live exchange rates fetched using Retrofit + Moshi

API provider: https://api.frankfurter.app/

Supports multiple currencies and conversion

Swap base and target currency

Error handling for network failures

Offline fallback using cached last-known exchange rate

## Notification and Reminder Settings

Toggleable budget alerts for each threshold

“Test Notification” button available

Integrated and reliable notification system

## Architecture and Technologies

This project follows the MVVM architecture pattern:

UI (Jetpack Compose)
        ↓
ViewModel (State + Business Logic)
        ↓
Repository (Room + API)
        ↓
Data Sources (Database + Network Service)

## Technology Stack
Area	Technology
UI	Jetpack Compose + Material 3
Persistence	Room Database
Dependency Injection	Hilt (Dagger)
Networking	Retrofit + Moshi
State Management	ViewModel + Coroutines
Notifications	NotificationChannel + NotificationCompat
Visualization	Canvas (Pie Chart)
Testing	JUnit4 + Compose UI Test
Version Control	GitHub
## Testing

The project includes both unit tests and instrumented tests to ensure correctness and reliability.

## Unit Tests (test/)

BudgetPercentageTest – verifies budget percentage logic

BudgetUtilTest – tests utility calculation functions

CurrencySymbolTest – validates currency symbol mapping

ExchangeFormatTest – checks formatting of currency output

SimpleViewModelTest – tests ViewModel state updates

## Instrumented Tests (androidTest/)

TravelDaoTest – tests Room DAO insert/delete/read

ReminderScreenTest – tests Reminder screen UI rendering and basic interaction

## Project Structure

app → src → main → java → com.example.smarttravelassistant → model（data classes, DAO, repository）

app → src → main → java → com.example.smarttravelassistant → network（Retrofit API & service）

app → src → main → java → com.example.smarttravelassistant → ui → theme（Compose screens）

app → src → main → java → com.example.smarttravelassistant → MainActivity.kt

app → src → main → java → com.example.smarttravelassistant → TravelApplication.kt

app → src → main → res

app → src → test → BudgetPercentageTest.kt

app → src → test → BudgetUtilTest.kt

app → src → test → CurrencySymbolTest.kt

app → src → test → ExchangeFormatTest.kt

app → src → test → SimpleViewModelTest.kt

app → src → androidTest → TravelDaoTest.kt

app → src → androidTest → ReminderScreenTest.kt



