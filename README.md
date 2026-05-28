# European Stocks Analyzer

A Kotlin desktop application for analyzing and visualizing historical stock market data. The project focuses on European
stock exchange instruments, but it uses Financial Modeling Prep ticker symbols directly, so the available instruments
depend on the external API.

## Main Features

- Desktop graphical user interface built with Compose Multiplatform.
- Ticker input and historical OHLCV candle download from the Financial Modeling Prep API.
- Candlestick chart drawn in the application.
- SMA, EMA, and RSI indicators calculated locally by the application.
- Basic statistics summary: latest close, total return, min/max close, average close, average volume, and simple volatility.
- Configurable SMA, EMA, and RSI calculation periods using UI sliders.
- Visibility toggles for SMA, EMA, and RSI.
- Latest SMA, EMA, and RSI values displayed below the chart.
- RSI panel and summary interpretation with 30 and 70 reference levels.
- Chart panning and mouse-wheel zooming.
- Predefined visible ranges: 1M, 3M, 6M, 1Y, and 5Y/full range.
- Daily, weekly, and monthly candle views. Weekly and monthly candles are aggregated locally from downloaded daily data.
- Side-by-side comparison mode with two analyzer panels and synchronized visible range/measurement state. It is not a normalized benchmark comparison chart.
- Simple chart annotation by drawing trend lines.
- Percentage measurement tool for comparing price movement between two candles.
- CSV export of candles and calculated indicator values.
- Unit tests for SMA, EMA, RSI, CSV export, statistics, candle aggregation, and chart state edge cases.

## Functional Programming Approach

The project applies functional programming principles where they fit the desktop UI architecture:

- Technical indicators are implemented as pure functions in `src/commonMain/kotlin/indicators`.
- `analyzeCandles` transforms candle data into an immutable `StockAnalysis` value.
- Basic statistics and candle timeframe aggregation are implemented as pure functions in `src/commonMain/kotlin/analysis`.
- Data models such as `Candle`, `ChartState`, `TrendLine`, and `StockAnalysis` are immutable Kotlin data classes.
- API responses are represented with the sealed `ApiResult` type instead of throwing errors into the UI layer.
- CSV export is implemented as a pure transformation from `StockAnalysis` to `String`.
- UI state is held at Compose boundaries and recomputed with derived state where practical. Compose-specific mutable state is kept in the UI layer.
- Platform-specific mutable operations, such as saving a file through AWT, are hidden behind the `expect`/`actual`
  `saveTextFile` wrapper.

## Technologies Used

- Kotlin 2.0.20
- Kotlin Multiplatform with a JVM desktop target
- Compose Multiplatform Desktop 1.6.11
- Ktor Client 2.3.7 with CIO engine
- kotlinx.serialization JSON 1.6.2
- Gradle Kotlin DSL
- Kotlin test
- Financial Modeling Prep API

## Project Structure

```text
src/
  commonMain/kotlin/
    App.kt                         Main Compose application layout
    Config.kt                      Common API key declaration
    analysis/                      Stock analysis and CSV export
    data/                          API provider, models, and chart state
    file/                          Common file-saving abstraction
    indicators/                    Pure SMA, EMA, and RSI functions
    ui/                            Compose UI, charts, gestures, controls
  desktopMain/kotlin/
    Main.kt                        Desktop entry point and window setup
    file/FileSaver.desktop.kt      Desktop file-save implementation
  commonTest/kotlin/
    analysis/                      CSV export tests
    indicators/                    Indicator tests
```

## Setup

Prerequisites:

- JDK 17 or newer
- Internet access for downloading Gradle dependencies and API data
- A Financial Modeling Prep API key

Clone the repository and use the included Gradle wrapper:

```bash
git clone https://github.com/KamilMarszalek/pf
cd pf
```

On Windows:

```powershell
.\gradlew.bat desktopRun
```

On macOS/Linux:

```bash
./gradlew desktopRun
```

## API Key Configuration

The application reads the Financial Modeling Prep API key from either `local.properties` or the `FMP_API_KEY`
environment variable.

Option 1: create `local.properties` in the project root:

```properties
fmp.api.key=YOUR_FMP_API_KEY
```

Option 2: set an environment variable:

```bash
FMP_API_KEY=YOUR_FMP_API_KEY
```

The Gradle build passes the key to the desktop application as the `fmp.api.key` JVM system property.

## Running the Application

Start the desktop application:

```bash
./gradlew desktopRun
```

Then:

1. Enter a ticker symbol supported by Financial Modeling Prep.
2. Click `Analyze`.
3. Use the Daily, Weekly, and Monthly buttons to change the candle timeframe.
4. Use the chart controls to change visible range, pan, zoom, toggle indicators, draw trend lines, or measure percentage
   movement.
5. Enable `Comparing mode` to display two analyzer panels side by side.

## CSV Export

CSV export is implemented.

After a ticker has been successfully analyzed, click `Export to csv`. The application opens a native save dialog and
writes a CSV file containing:

- date
- open, high, low, close
- volume
- SMA value for the selected period
- EMA value for the selected period
- RSI value for the selected period

## Known Limitations

- The application depends on the Financial Modeling Prep API and its symbol format, rate limits, and available data.
- Missing or invalid API keys are handled as API errors at runtime.
- Comparison mode is side-by-side only; it does not normalize prices or calculate benchmark-relative performance.
- Trend lines and measurements exist only in memory and are lost after clearing or reloading.
- Some UI text/rendering is minimal and oriented toward project functionality rather than a polished product release.
- There is no offline cache.
- There is no automated UI test coverage.

## Possible Future Improvements

- Add normalized comparison against another stock or market index.
- Add more indicators, such as MACD, Bollinger Bands, ATR, and volume-based indicators.
- Add additional statistics such as drawdown and moving average crossovers.
- Persist user preferences and chart annotations.
- Add data caching and better handling of API limits.
- Add packaging tasks for distributable desktop builds.
- Improve UI layout, accessibility, and chart tooltips.
