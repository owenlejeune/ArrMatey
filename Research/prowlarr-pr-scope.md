# Prowlarr Integration - PR Scope

## Context

**Helmarr** (closed source, paid) added Prowlarr in v2.2.0. Their App Store listing confirms support but no public code to reference. They support the full *arr stack plus download clients, SSH, Unraid, Tautulli, etc.

**Prowlarr** is fundamentally different from Sonarr/Radarr/Lidarr — it's an **indexer manager**, not a media library. No library browsing, no media details, no calendar. It's search-first.

## ArrMatey Architecture Summary

The pattern for adding a new service:

1. **`InstanceType` enum** (`instances/model/Instance.kt`) — add `Prowlarr` entry with port 9696, `api/v1`, etc.
2. **Client class** (`arr/api/client/ProwlarrClient.kt`) — extends `BaseArrClient`, but many `ArrClient` methods don't apply (no library, no calendar, no monitor)
3. **Models** (`arr/api/model/`) — Prowlarr-specific data classes
4. **`InstanceScopedRepository`** — needs a `prowlarrClient` accessor + `createClient()` branch
5. **`ClientFactory`** — already handles per-instance HttpClient creation (just works)
6. **ViewModels** — new Prowlarr-specific VMs
7. **UI** — both Android (Compose) and iOS (SwiftUI) screens

### The Problem: `ArrClient` Interface Mismatch

`ArrClient` assumes a media-library service: `getLibrary()`, `getDetail()`, `lookup()`, `addItemToLibrary()`, `getReleases()`, `getMovieCalendar()`, etc.

Prowlarr has **none of these**. Its core operations are:
- List indexers (`GET /api/v1/indexer`)
- Search across indexers (`GET /api/v1/search?query=...&type=search`)
- View indexer stats (`GET /api/v1/indexerstats`)
- Health check (`GET /api/v1/health`)
- History (`GET /api/v1/history`)

### Options:

**Option A: Don't extend `ArrClient`** — Create a separate `ProwlarrClient` that directly extends `BaseArrClient` but does NOT implement `ArrClient`. This avoids forcing 20+ no-op methods.

**Option B: Make `ArrClient` methods optional** — Default implementations that return `NotSupported`. This is messier and changes existing code.

**Recommendation: Option A.** Prowlarr is a different *kind* of service. The existing `ArrClient` is a media-library contract. Prowlarr needs its own contract.

## Prowlarr API v1 - Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/indexer` | GET | List all configured indexers |
| `/api/v1/indexer/{id}` | GET | Get specific indexer |
| `/api/v1/search` | GET | Search across indexers (`query`, `type`, `categories`, `indexerIds`) |
| `/api/v1/indexerstats` | GET | Indexer performance stats |
| `/api/v1/health` | GET | Health checks |
| `/api/v1/history` | GET | Search/grab history |
| `/api/v1/system/status` | GET | System info |

### Search Parameters
- `query` — search term
- `type` — `search` (general), `tvsearch`, `movie`, `music`, `book`
- `categories` — comma-separated category IDs (2000=Movies, 5000=TV, 3000=Audio, etc.)
- `indexerIds` — filter to specific indexers

### Search Result Fields
- `guid`, `title`, `indexer`, `indexerId`
- `size`, `age`, `seeders`, `leechers`, `grabs`
- `publishDate`, `downloadUrl`, `magnetUrl`
- `categories`, `protocol` (torrent/usenet)

## Proposed File Structure

### New Files (~15)

**Shared/Common (Kotlin):**
```
arr/api/client/ProwlarrClient.kt          — HTTP client
arr/api/model/ProwlarrIndexer.kt           — Indexer data class
arr/api/model/ProwlarrSearchResult.kt      — Search result data class
arr/api/model/ProwlarrHealth.kt            — Health check data class
arr/api/model/ProwlarrIndexerStats.kt      — Indexer stats
arr/api/model/ProwlarrSearchParams.kt      — Search query params
arr/state/ProwlarrSearchUiState.kt         — UI state for search screen
arr/state/ProwlarrIndexersUiState.kt       — UI state for indexer list
arr/viewmodel/ProwlarrSearchViewModel.kt   — Search VM
arr/viewmodel/ProwlarrIndexersViewModel.kt — Indexers list VM
```

**iOS (SwiftUI):**
```
iosApp/Views/Tabs/ProwlarrTab.swift              — Tab container
iosApp/Views/Prowlarr/ProwlarrSearchView.swift   — Search interface
iosApp/Views/Prowlarr/ProwlarrIndexersView.swift — Indexer list
iosApp/ViewModel/ProwlarrSearchViewModelS.swift   — iOS VM wrapper
iosApp/ViewModel/ProwlarrIndexersViewModelS.swift — iOS VM wrapper
```

**Android (Compose):** — Follows similar pattern in `composeApp/`

### Modified Files (~8)

```
instances/model/Instance.kt                    — Add Prowlarr to InstanceType enum
instances/repository/InstanceScopedRepository.kt — Add prowlarrClient, createClient branch
di/Modules.kt                                  — Register new VMs
compose/TabItem.kt                             — Add Prowlarr tab item
iosApp/Views/Tabs/BottomTabView.swift          — Add Prowlarr tab
composeApp/.../BottomTabBar.kt                 — Add Prowlarr tab (Android)
shared/schemas/                                — DB migration if needed
```

## MVP Screens

### 1. Indexer List (Home for Prowlarr instances)
- List of configured indexers with name, protocol (torrent/usenet), status
- Color-coded health (green/yellow/red)
- Pull to refresh
- Tap for basic details (stats, last search, error messages)

### 2. Search
- Search bar at top
- Category filter (Movies, TV, Audio, Books, etc.)
- Protocol filter (All, Torrent, Usenet)
- Indexer filter (All, or specific indexers)
- Results list with: title, indexer name, size, age, seeders/leechers (torrent) or grabs (usenet)
- Sort by: age, size, seeders, title
- **No "grab" action in MVP** — Prowlarr search is usually for discovery; the arr apps handle grabbing. Could add later.

### 3. Stats (optional, nice-to-have)
- Indexer performance: avg response time, success rate, number of queries

## What NOT to Include (Phase 1)

- ❌ Indexer add/edit/delete (complex config with provider-specific fields)
- ❌ App sync management (Prowlarr → Sonarr/Radarr sync config)
- ❌ Download client configuration
- ❌ Notification settings
- ❌ Grab/download from search results (nice Phase 2 feature)

## Complexity Estimate

- **Shared Kotlin layer:** ~800-1200 lines (client + models + VMs + state)
- **iOS SwiftUI:** ~400-600 lines (3 views + 2 VM wrappers)
- **Android Compose:** ~400-600 lines (equivalent screens)
- **Integration (enum, DI, tabs):** ~100-200 lines of modifications

**Total: ~1700-2600 lines across ~20 files**
**Time: 3-5 days for an experienced KMP developer**

## Key Design Decisions

1. **Prowlarr gets its own tab** — not shoehorned into library
2. **ProwlarrClient does NOT implement ArrClient** — different service type
3. **Search-first UX** — the search bar is the primary interaction
4. **Read-only MVP** — no write operations (no indexer management)
5. **Follow existing patterns** for networking, error handling, state management

## PR Strategy

- **Title:** `feat: Add Prowlarr integration (indexer list + search)`
- **Branch:** `feature/prowlarr-support`
- Open an issue first to discuss with the maintainer
- Keep it clean: one PR, well-documented, follows existing code style
- Include screenshots of both Android and iOS
