## Project Preview link
**https://drive.google.com/drive/folders/1aYmfCJEt9xIhPpzLok6xSQHBWnkyJofi?usp=sharing**
---

## Overview

PureGuard is a multi-layered Android content protection app designed to block adult and inappropriate content across all browsers in real time. It combines **VPN-level DNS filtering**, **browser accessibility monitoring**, **on-device image analysis**, and **URL scoring** into a single, cohesive shield — without relying on any external cloud AI service.

---

## Features

### 🛡️ Multi-Layer Protection Engine

| Layer | Technology | What It Does |
|---|---|---|
| **URL Scoring** | `UrlScoringEngine` | Scores every URL using NSFW keyword matching, adult TLD detection, and slug pattern analysis |
| **DNS Safety Check** | `DnsSafetyChecker` | Queries **Cloudflare Family DNS** and **AdGuard Family DNS** in parallel via DNS-over-HTTPS |
| **On-Device Image Scan** | `OnDeviceImageScanner` | Downloads and scores page images using a skin-tone heuristic — no cloud calls needed |
| **Safe Search Enforcement** | `SafeSearchRewriter` | Rewrites search queries for Google, Bing, DuckDuckGo, Yahoo, Yandex, Brave, and Startpage |
| **Metadata Analysis** | `MetadataAnalyzer` | Inspects page metadata signals for additional content classification |

### 🔒 Dual Service Architecture

- **VPN Service** — intercepts DNS traffic at the OS level using Android's `VpnService` API, providing system-wide blocking that works even when the browser is not in focus.
- **Accessibility Service** — monitors active browser URLs in real time, evaluating each navigation event through the full protection pipeline before the page renders.

### ⚙️ Flexible Settings

- **Sensitivity levels** — configure how aggressively content is flagged (affects URL score threshold and image scan threshold)
- **Whitelist & Blacklist** — add custom domains to always allow or always block
- **Safe Search toggle** — enforce safe search on all major search engines automatically
- **Image scanning toggle** — enable or disable the on-device visual scanner
- **Fast scan mode** — limit the number of images scanned per page for performance

### 📊 Analytics Dashboard

- View total URLs evaluated, blocked, and allowed
- Monitor active protection layers (VPN, Accessibility, DNS)
- Review recent block events with reasons

### 🔐 App Lock

- PIN-based protection powered by `PasswordHasher` (secure local hashing)
- `UnlockSessionManager` handles session state to prevent repeated unlocking during active use

### 🎨 Modern UI

- Built entirely with **Jetpack Compose** and **Material 3**
- Dark-themed, clean design with animated shield on the home screen
- Onboarding flow with guided permission setup for VPN and Accessibility services

---

## Tech Stack

| Category | Library / Tool |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM + Clean Architecture (domain / data / presentation) |
| DI | Hilt |
| Navigation | Jetpack Navigation Compose |
| Networking | OkHttp, Retrofit + Gson |
| Storage | Jetpack DataStore (Preferences) |
| Build | Gradle with Kotlin DSL (`.kts`) |
| Min SDK | 24 (Android 7.0 Nougat) |
| Target SDK | 35 (Android 15) |
| Java Compatibility | Java 17 |

---

## Architecture

PureGuard follows **Clean Architecture** with a feature-based package structure:

```
com.pureguard.mobile
├── core/
│   ├── common/          # Base classes, extensions, result wrappers
│   ├── datastore/       # PrefsManager, ModeManager
│   ├── network/         # OkHttp client, Retrofit API calls
│   └── security/        # PasswordHasher, UnlockSessionManager
│
├── domain/
│   └── engine/          # UrlScoringEngine, DnsSafetyChecker,
│                        # OnDeviceImageScanner, SafeSearchRewriter,
│                        # MetadataAnalyzer
│
├── features/
│   └── blocking/
│       ├── data/        # ProtectionCoordinator, repositories, mappers
│       └── domain/      # Models (ProtectionSettings, ProtectionDecision…)
│                        # Repository interfaces, UseCases
│
├── services/
│   ├── local/
│   │   ├── Vpn/         # ServiceVpn (VpnService)
│   │   ├── accessibility/ # BrowserAccessibilityService
│   │   └── background/  # BrowserBlockBridge
│   └── BrowserPackageCatalog
│
└── ui/
    ├── features/
    │   ├── home/        # HomeScreen
    │   ├── settings/    # SettingsScreen
    │   ├── analytics/   # AnalyticsScreen
    │   └── onboarding/  # OnboardingScreen, PermissionSetupScreen
    ├── theme/           # Color, Theme
    └── AppRoot.kt       # Navigation host
```

---

## How the Protection Pipeline Works

When a browser navigates to a URL, the `ProtectionCoordinator` runs the following decision tree:

```
New URL detected
      │
      ▼
 Skip-list / trusted domain? ──► ALLOW
      │ No
      ▼
 User blacklist? ──────────────► BLOCK
      │ No
      ▼
 User whitelist? ──────────────► ALLOW
      │ No
      ▼
 URL Score ≥ threshold? ───────► BLOCK
      │ No
      ▼
 DNS Safety Check (CF + AdGuard) sinkhole? ──► BLOCK
      │ No
      ▼
 Page signals + Image Scan ≥ threshold? ─────► BLOCK
      │ No
      ▼
     ALLOW
```

Results are cached (TTL = 30 minutes) to avoid redundant checks on revisited domains.

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android device or emulator running API 24+

### Build & Run

```bash
# Clone the repository
git clone https://github.com/<your-org>/PureGuard.git
cd PureGuard

# Open in Android Studio and sync Gradle, or build from CLI:
./gradlew assembleDebug
```

### Required Permissions

PureGuard requires the user to grant two special permissions during onboarding:

1. **VPN Permission** — allows PureGuard to create a local VPN tunnel for DNS filtering. Prompted via `VpnService.prepare()`.
2. **Accessibility Permission** — allows PureGuard to read the active URL from the browser's address bar. The user must enable it manually in *Settings → Accessibility → PureGuard*.

No root access is required.

---

## Supported Browsers

The Accessibility Service monitors URLs from all browsers listed in `BrowserPackageCatalog`, including Chrome, Firefox, Brave, Samsung Internet, Opera, and more.

---

## Privacy

- **No data leaves the device** for content classification. URL scoring, image analysis, and metadata inspection are all performed locally.
- DNS-over-HTTPS queries are sent to **Cloudflare Family** (`family.cloudflare-dns.com`) and **AdGuard Family** (`family.adguard-dns.com`) — only the hostname is shared, not the full URL.
- No analytics, tracking SDKs, or third-party ad networks are included.

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

---

## License

This project is proprietary. All rights reserved.
