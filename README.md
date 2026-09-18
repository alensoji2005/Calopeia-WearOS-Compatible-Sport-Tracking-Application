# Calopeia (SportOS)
### Pro Multi-Sport Operating System & Kinematics Telemetry for Wear OS

[![Platform](https://img.shields.io/badge/Platform-Wear%20OS%20(Standalone)-4285F4?logo=android&logoColor=white)](https://developer.android.com/wear)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20for%20Wear%20OS-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-30%20(Wear%20OS%203+)-00E676)](#requirements)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Wear%20OS%204%2F5)-00E5FF)](#requirements)
[![Room](https://img.shields.io/badge/Database-Room%20%2B%20SQLCipher-FF9100)](#architecture)

**Calopeia (SportOS)** is a high-performance, standalone smartwatch application built exclusively for Wear OS. Unlike conventional fitness apps that treat every activity as a generic step and heart-rate counter, Calopeia deploys **modular biomechanical and kinematic engines** tailored to individual sports: **Running, Basketball, Football, Cricket, and Tennis**.

Designed from the ground up for circular watch dials, high-contrast outdoor visibility, sweaty-finger ergonomics, and hardware haptic/acoustic feedback.

---

## 🏃 Supported Sports & Kinematic Capabilities

```
                  ┌─────────────────────────────────────┐
                  │          CALOPEIA / SPORTOS         │
                  │        Modular Sports Engine        │
                  └──────────────────┬──────────────────┘
                                     │
     ┌──────────────┬────────────────┼──────────────┬──────────────┐
     ▼              ▼                ▼              ▼              ▼
  RUNNING       BASKETBALL        FOOTBALL       CRICKET         TENNIS
  • Ghost Duel   • Jump Hangtime  • Match HUD    • Arm Speed    • Stroke AI
  • OSM Dark Map • PlayerLoad     • HIRD >15km/h • Spell Overs  • Rally Timer
  • Live Splits  • Shot Logger    • Whistle Ref  • Bat Sprints  • Top Serve
```

### 1. 🏃 Running (Strava-Grade Engine)
- **Ghost Pacer Duel**: Real-time avatar benchmark showing exact seconds ahead/behind and distance gap along with an interactive track progress bar.
- **OSMDroid Tactical Map**: Native OpenStreetMap / CartoDB Dark Matter tile engine running with an OLED high-contrast filter, dynamic runner orientation cone, concentric range rings, and rotary crown bezel zoom (25m–800m).
- **Live Strava Segments**: Real-time PR countdown with distance remaining and live split feedback.
- **Kilometer Splits**: Per-kilometer lap tracking with average pace, elevation gain, and heart rate zone tagging.
- **GPX 1.1 Export**: Generates compliant GPX files with Garmin `TrackPointExtension` (HR and cadence) and automatic privacy zone geo-fencing.

### 2. 🏀 Basketball (Kinematic IMU)
- **Vertical Jump Kinematics**: Uses projectile freefall-to-impact physics ($h = \frac{1}{8} g t^2$) across a 50Hz accelerometer pipeline to calculate jump height (inches) and hang time (ms).
- **Catapult-Style PlayerLoad**: Continuous mechanical workload metric calculated from 3D acceleration vector differentials.
- **Dual Operating Modes**:
  - **5v5 Game Automatic**: Kinematic jump detection paired with angular wrist-release snap classification for automatic shot attempts.
  - **Practice Shot Logger**: High-contrast, tactile `[+ MADE]` and `[- MISS]` score logging with dynamic shooting percentages and streak counters.

### 3. ⚽ Football / Soccer (Match Command HUD)
- **Match Period Management**: Full match halves tracking (1st Half, Half Time, 2nd Half, Extra Time) with integrated referee whistle audio and haptic patterns.
- **High-Intensity Running Distance (HIRD)**: Tracks distance covered at speeds exceeding $15\text{ km/h}$.
- **Sprint Counter**: Real-time sprint detection for bursts over $20\text{ km/h}$ with hysteresis filtering.
- **Speedometer HUD**: Instantaneous speed, top sprint speed, and average velocity.

### 4. 🏏 Cricket (Spell & Batting Workload)
- **Bowling Spell Telemetry**: Automatic ball-by-ball delivery tracking, overs, maiden overs, runs conceded, and wickets taken.
- **Arm Angular Speed Estimation**: Uses high-rate 3-axis gyroscope angular velocity ($> 12\text{ rad/s}$) to calculate bowler release speed ($\text{km/h}$).
- **Batting Workload**: Detects high-speed burst sprints between the wickets ($> 17\text{ km/h}$) and balls faced.

### 5. 🎾 Tennis (Stroke Intelligence)
- **IMU Stroke Classifier**: Analyzes 3D accelerometer and gyroscope axes to classify strokes in real time:
  - **Overhead Serve**: Positive $Z$-acceleration + high $X$-angular velocity.
  - **Forehand**: Positive $Z$-gyroscope rotation.
  - **Backhand**: Negative $Z$-gyroscope rotation.
- **Racket Speed Estimation**: Calculates instantaneous and top racket head speed ($\text{km/h}$).
- **Rally Timer**: Single-tap rally timer tracking active rally duration, total rallies, and longest rally records.

---

## ⚡ Technical Architecture

### Tech Stack
| Component | Technology |
| :--- | :--- |
| **OS Target** | Wear OS 3.0+ (`minSdk 30`, `compileSdk 34`, `targetSdk 34`) |
| **UI Framework** | Jetpack Compose for Wear OS (Material3 Wear `1.0.0-alpha18`) |
| **Wear Extensions** | Google Horologist `0.6.5` (Responsive layouts, rotary input, screen scaffolds) |
| **Sensors & Biometrics** | AndroidX Health Services Client (`1.1.0-alpha02`), `SensorManager` (50Hz IMU) |
| **Mapping Engine** | OSMDroid `6.1.20` with hardware OLED dark-mode ColorMatrix filter |
| **Local Persistence** | Room Database `2.6.1` + KSP + SQLCipher encryption ready |
| **Networking** | Ktor Client `2.3.7` (CIO engine with WebSockets) |
| **Background Processing**| Foreground `LifecycleService` + Wear OS `OngoingActivity` |
| **Audio & Haptics** | Android `ToneGenerator` (whistles/buzzers) + `VibratorManager` |

### Architecture Highlights
- **Strategy Pattern Sport Engines**: Base `SportEngine` interface decoupled from the UI. Engines handle high-frequency IMU telemetry (50Hz) and low-rate Health Services updates independently.
- **Background WorkoutService & Auto-Reattach**: Workouts execute inside a foreground `LifecycleService` bound to Wear OS `OngoingActivity`. If the app process restarts while exercising, the UI automatically detects the active service and resumes the workout HUD.
- **Real-Time Simulation Engine**: Includes `TelemetrySimulator` for live emulator development without requiring connected physical sensors.
- **Local Database & Milestone PRs**: Room database stores workout sessions, durations, calories, heart rates, and sport-specific statistics, with automatic detection and storage of all-time Personal Records (PRs).
- **Health Connect Integration**: Manifest-compliant permission rationale handling (`HealthConnectSettingsActivity`) ready to sync workout sessions to Google Fit, Samsung Health, and Strava.

---

## 🗂️ Project Structure

```
app/src/main/java/com/sportos/watch/
├── MainActivity.kt                  # NavHost, permission management & auto-reconnect
├── core/
│   ├── audio/
│   │   └── SportAudioToneManager.kt # Referee whistle & stadium buzzer tones
│   ├── export/
│   │   └── GpxExporter.kt           # GPX 1.1 exporter with Garmin extensions
│   ├── haptics/
│   │   └── SportHapticManager.kt    # Tactile feedback patterns for Wear OS
│   ├── healthservices/
│   │   └── HealthServicesManager.kt # AndroidX Health Services client wrapper
│   ├── imu/
│   │   └── ImuSensorManager.kt      # 50Hz Accelerometer & Gyroscope manager
│   ├── network/
│   │   └── LiveBeaconClient.kt      # Ktor WebSocket streaming client
│   ├── security/
│   │   ├── CryptoManager.kt         # AES-GCM AndroidKeyStore cryptographic manager
│   │   └── PrivacyZoneFilter.kt     # GPS geo-fence privacy zone coordinate mask
│   └── simulator/
│       └── TelemetrySimulator.kt    # Dynamic sensor simulation for emulator testing
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt           # Room Database singleton
│   │   ├── dao/WorkoutDao.kt        # Reactive Flow queries for sessions & PRs
│   │   └── entity/
│   │       ├── WorkoutSessionEntity.kt # Session persistence entity
│   │       └── PersonalRecordEntity.kt # Milestone PR entity
│   └── repository/
│       └── WorkoutRepositoryImpl.kt # Repository implementation & PR evaluation
├── domain/
│   └── repository/
│       └── WorkoutRepository.kt     # Domain repository contract
├── presentation/
│   ├── components/
│   │   ├── CircularGaugeArc.kt      # Curved bezel heart rate gauge arc
│   │   ├── MetricTile.kt            # High-contrast tactical metric card
│   │   └── PrebuiltMapView.kt       # OSMDroid native map with dark-mode filter
│   ├── screens/
│   │   ├── MainMenuScreen.kt        # Sports selection, sub-modes & history launcher
│   │   ├── ActiveWorkoutScreen.kt   # Multi-page HUD (Tactical, Map, Splits, Controls)
│   │   ├── WorkoutSummaryScreen.kt  # Post-workout stats, GPX export & database sync
│   │   └── WorkoutHistoryScreen.kt  # Past sessions log & personal bests showcase
│   ├── social/
│   │   └── HealthConnectSettingsActivity.kt # Health Connect permission rationale
│   └── theme/
│       ├── SportIcons.kt            # Custom Canvas vector sport iconography
│       └── SportOSTheme.kt          # Material3 Wear theme & physiological HR zones
└── sports/
    ├── SportEngine.kt               # Base engine contract & StateFlow definition
    ├── basketball/                  # Basketball kinematics, hangtime & shot logger
    ├── cricket/                     # Cricket bowling spells & batting sprints
    ├── football/                    # Football match halves, HIRD & sprint tracker
    ├── running/                     # Ghost pacer, live segments & splits
    └── tennis/                      # Tennis stroke AI classifier & rally timer
```

---

## 🚀 Getting Started

### Prerequisites
1. **Android Studio**: Android Studio Hedgehog (2023.1.1) or newer.
2. **JDK**: Java Development Kit 17.
3. **Android SDK**: Android API 34 (Wear OS 4 / 5 emulator or physical watch).

### Build & Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/SportOS-watch.git
   cd SportOS-watch
   ```
2. **Compile Kotlin & KSP sources**:
   ```bash
   ./gradlew compileDebugKotlin
   ```
3. **Assemble the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
4. **Deploy to a Wear OS watch or emulator**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

> [!TIP]
> **Testing on Emulator**:
> By default, the app enables **Simulation Engine** in the Main Menu, dynamically generating physiologically and kinematically realistic sensor data so you can test dials, maps, ghost pacers, and graphs without needing a physical watch or real sensors. Toggle this off in the Main Menu to switch to physical watch hardware sensors.

---

## 🔒 Permissions & Privacy
Calopeia is built with privacy-first principles:
- **Zero Mandatory Cloud Dependencies**: Works 100% standalone offline on the watch.
- **Privacy Zones**: Automatically masks GPS trackpoints within a configurable radius around sensitive locations (home, office).
- **Health Connect**: Full user control over health metrics permissions.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
