# Tracearr Integration & UI Implementation Proposals

This document details the implemented Tracearr API endpoints, data models, and proposed UI integration strategies for `UnifiedMediaDetailsScreen` across Android (Jetpack Compose) and iOS (SwiftUI).

---

## 1. Implemented Tracearr API Endpoints

The following suspend functions have been implemented in [TracearrClient.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/client/TracearrClient.kt):

- **`getMediaWatchers(ref: String)`**
  - **Endpoint:** `GET /v2/public/media/{ref}/watchers`
  - **Returns:** `NetworkResult<TracearrMediaWatchers>`
  - **Description:** Retrieves the list of top watchers for a media item, including watch time, completion percentage, play counts, and user profile information.

- **`getMediaStats(ref: String)`**
  - **Endpoint:** `GET /v2/public/media/{ref}/stats`
  - **Returns:** `NetworkResult<TracearrMediaStats>`
  - **Description:** Retrieves aggregate media consumption metrics across time windows (`all_time`, `last_30`, `last_7`), unique user counts, and per-server breakdowns.

- **`getMediaHistory(ref: String, cursor: String? = null, pageSize: Int? = null)`**
  - **Endpoint:** `GET /v2/public/media/{ref}/history`
  - **Returns:** `NetworkResult<TracearrHistoryResponse>`
  - **Description:** Retrieves a paginated playback history feed of stream sessions for a media item.

---

## 2. Implemented Data Models

All models are located in `shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/model/`:

### Watcher Models
- [TracearrMediaWatchers.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/model/TracearrMediaWatchers.kt) — Root wrapper object containing media information, window identifier, and the list of watchers.
- [TracearrMediaWatcher.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/model/TracearrMediaWatcher.kt) — Individual watcher item containing playback statistics (plays, watch time in ms, completion %, last watched day, distinct episodes watched).
- [TracearrMediaWatcherUser.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/model/TracearrMediaWatcherUser.kt) — User details for a watcher (`server_user_id`, `user_id`, `username`, `identity_name`).

### Stats Models
- [TracearrMediaStats.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/model/TracearrMediaStats.kt) — Top-level media stats container holding `media_id`, `media_type`, and `windows`.
- [TracearrMediaStatsWindows.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/model/TracearrMediaStatsWindows.kt) — Window container holding `all_time`, `last_30`, and `last_7` stats windows.
- [TracearrMediaStatsWindow.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/model/TracearrMediaStatsWindow.kt) — Holds combined metrics (`TracearrMediaStatsCombined`) and per-server breakdowns (`List<TracearrMediaServerStats>`).
- [TracearrMediaStatsCombined.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/model/TracearrMediaStatsCombined.kt) — Combined metrics (`plays`, `watch_time_ms`, `unique_users`).
- [TracearrMediaServerStats.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonMain/kotlin/com/dnfapps/arrmatey/tracearr/api/model/TracearrMediaServerStats.kt) — Per-server metrics (`server_id`, `server_name`, `plays`, `watch_time_ms`, `unique_users`).

---

## 3. UI Integration Proposals for `UnifiedMediaDetailsScreen`

Below are 3 proposed strategies for integrating Tracearr data into [UnifiedMediaDetailsScreen.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/screens/UnifiedMediaDetailsScreen.kt) (Android) and [UnifiedMediaDetailsScreen.swift](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/iosApp/iosApp/Views/Screens/UnifiedMediaDetailsScreen.swift) (iOS):

### **Suggestion 1: Dedicated "Tracearr Analytics & Activity" Tab Section (Unified Tabbed View)**
- **Concept:** Structure the media details view using a segmented tab control below the header (e.g. `[ Overview | Seasons/Files | Analytics | History ]`) or an inline telemetry section.
- **Header Summary Chip (`getMediaStats` + `getMedia`):** Positioned directly under the title header showing a summary row (`47 Plays • 15.2h Watch Time • 2 Watchers • 2 Servers`).
- **Analytics Tab (`getMediaStats` + `getMediaWatchers`):**
  - **Window Selector:** Segmented filter (`7 Days`, `30 Days`, `All Time`).
  - **Metrics & Leaderboard:** Consumption cards (total plays, watch time, unique viewers, per-server breakdown) and top watchers list with progress rings and completion percentages.
- **History Tab (`getMediaHistory`):**
  - **Paginated Stream Log:** Infinite scrolling activity feed showing play sessions with device/player badges (`Chromecast`, `Plex TV`), resolution, transcode vs direct play tags, user avatars, and progress bars.
- **Best For:** Comprehensive analytics without cluttering the primary media metadata layout.

---

### **Suggestion 2: Contextual Inline Cards with Bottom Sheet / Modal Deep-Dives (Progressive Disclosure)**
- **Concept:** Maintain a clean, single-column scroll view by placing lightweight contextual summary widgets inline, with tap targets opening detailed bottom sheets (Android) / modal sheets (iOS).
- **"Watched By" Facepile Widget (`getMediaWatchers`):** Placed right below the `ItemDescriptionCard` (Overview). Displays an overlapping avatar stack ("Watched by Owen, Dennis +2 others"). Tapping opens a full **Watchers Sheet**.
- **Tracearr Server Availability Card (`getMediaStats` + `getMedia`):** Added as a card in `InfoArea` alongside Arr/Seerr cards. Tapping expands a **Server Breakdown Sheet**.
- **"Recent Streams" Teaser Section (`getMediaHistory`):** Displays the 3 most recent stream sessions under the Seasons/Movie File section, with a "View Full History →" button opening a paginated **Stream History Sheet**.
- **Best For:** Mobile-first designs prioritizing progressive disclosure and clean navigation.

---

### **Suggestion 3: Integrated Media Hierarchy & Contextual Episode Telemetry (In-Context Insights)**
- **Concept:** Embed Tracearr metrics directly inside existing content hierarchies (seasons, episode rows, movie files) so playback context is rendered alongside media management controls.
- **Show/Movie Performance Bar (`getMediaStats` + `getMedia`):** Banner above `SeasonsArea` or `MovieFileView` showing overall consumption metrics.
- **Episode Row Watch Badges (`getMediaHistory` + `getMediaWatchers`):** Subtle badges on episode cards (e.g., `Played 3x • Last by Owen (Sep 10)` + direct play / transcode tags).
- **Who's Watching Carousel (`getMediaWatchers`):** Horizontal avatar carousel placed above `SeerrCreditsSection` (Cast & Crew).
- **Activity Feed Footer (`getMediaHistory`):** Infinite scroll stream timeline appended at the bottom of the details page.
- **Best For:** TV shows and episodic media where consumption context at the episode/season level is most valuable.


### **Ignore this Section**
4) refactor UnifiedMediaDetails separating components into their own files
2) refactor UnifiedMediaDetailsViewModel to extract logic into use cases. 1500 lines is way too long for a single file
