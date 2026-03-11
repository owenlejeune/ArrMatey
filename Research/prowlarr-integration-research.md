# Prowlarr Integration Research for ArrMatey

**Research Date:** February 19, 2026  
**Researcher:** Scout (OpenClaw Agent)  
**Project:** ArrMatey - *arr mobile client

---

## Executive Summary

Adding Prowlarr support to ArrMatey is **moderately complex** and follows similar patterns to existing Sonarr/Radarr/Lidarr integrations. However, Prowlarr serves a fundamentally different purpose—it's an indexer manager/proxy rather than a media library manager—so the integration scope differs significantly.

**Recommended Approach:** Start with a **minimal viable integration** focusing on indexer browsing and search, then expand based on user feedback.

---

## 1. ArrMatey Architecture Analysis

### 1.1 Current Integration Pattern

ArrMatey follows a clean, layered architecture for *arr integrations:

```
┌─────────────────────────────────────────────────────────────┐
│  UI Layer (composeApp)                                       │
│  - Screens, Tabs, Components                                 │
│  - ViewModels bound to Koin DI                               │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│  Shared Layer (shared/src/commonMain)                        │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   ViewModels │  │   UseCases   │  │ Service Layer    │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Repository Layer (InstanceScopedRepository)           │ │
│  │  - Creates typed client (Sonarr/Radarr/Lidarr)         │ │
│  │  - Caches data, exposes StateFlows                     │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  API Client Layer                                      │ │
│  │  - ArrClient (interface)                               │ │
│  │  - BaseArrClient (abstract base)                       │ │
│  │  - SonarrClient, RadarrClient, LidarrClient            │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Model Layer                                           │ │
│  │  - ArrMedia (sealed interface)                         │ │
│  │  - ArrSeries, ArrMovie, Arrtist                        │ │
│  │  - Shared models (QualityProfile, RootFolder, etc.)    │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│  Data Layer                                                  │
│  - Room Database (InstanceDao, ArrMateyDatabase)             │
│  - DataStore (PreferencesStore)                              │
│  - InstanceManager (creates scoped repos)                    │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Key Files for Integration

| Layer | Files | Purpose |
|-------|-------|---------|
| **Instance Type** | `instances/model/Instance.kt` | Add `Prowlarr` to `InstanceType` enum |
| **API Client** | `arr/api/client/ArrClient.kt` | Interface defining common operations |
| **API Client** | `arr/api/client/BaseArrClient.kt` | Abstract base with HTTP helpers |
| **API Client** | `arr/api/client/SonarrClient.kt` | Reference implementation |
| **API Client** | `arr/api/client/ClientFactory.kt` | HttpClient creation with auth |
| **Repository** | `instances/repository/InstanceScopedRepository.kt` | Creates typed client, caching |
| **Models** | `arr/api/model/` | Data classes for API responses |
| **DI** | `di/Modules.kt` | Koin module definitions |
| **Strings** | `MR/strings/` | Moko Resources for localization |

### 1.3 InstanceType Enum Pattern

```kotlin
enum class InstanceType(
    val resource: StringResource,
    val iconKey: String,
    val github: String,
    val website: String,
    val defaultPort: Int,
    val supportsActivityQueue: Boolean,
    val apiBase: String,
    val includeTopLevelAutomaticSearchOption: Boolean,
    val aspectRatio: AspectRatio
) {
    Sonarr(...),
    Radarr(...),
    Lidarr(...)
    // Prowlarr would be added here
}
```

**Note:** Sonarr and Radarr use `api/v3`, Lidarr uses `api/v1`. Prowlarr uses `api/v1`.

### 1.4 Auth Model

All *arr apps use the same authentication pattern:
- Header: `X-Api-Key: <api_key>`
- Base URL: `http(s)://host:port/api/v1` (Prowlarr)
- No OAuth or session management required

---

## 2. Prowlarr API Analysis

### 2.1 API Version

- **Base Path:** `/api/v1`
- **Default Port:** 9696
- **Auth:** API Key via `X-Api-Key` header
- **Spec:** OpenAPI 3.0.4 (auto-generated from source)

### 2.2 Key Endpoints by Category

#### Indexer Management
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/indexer` | GET | List all configured indexers |
| `/api/v1/indexer` | POST | Add new indexer |
| `/api/v1/indexer/{id}` | GET | Get indexer details |
| `/api/v1/indexer/{id}` | PUT | Update indexer |
| `/api/v1/indexer/{id}` | DELETE | Remove indexer |
| `/api/v1/indexer/schema` | GET | Available indexer types/profiles |
| `/api/v1/indexer/test` | POST | Test indexer connection |
| `/api/v1/indexer/testall` | POST | Test all indexers |
| `/api/v1/indexer/categories` | GET | Available categories |

#### Search (Core Feature)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/search` | GET | Search across all indexers |
| `/api/v1/search?query={term}` | GET | Search with query term |

**Search Parameters:**
- `query` - Search term
- `type` - Type filter (movie, tv, etc.)
- `categories` - Category IDs array
- `indexerIds` - Limit to specific indexers

#### Application Management
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/applications` | GET | List connected apps (Sonarr, Radarr, etc.) |
| `/api/v1/applications/{id}` | GET | Get app config |
| `/api/v1/applications/test` | POST | Test app connection |

#### System & Health
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/health` | GET | Health check issues |
| `/api/v1/history` | GET | Search history |
| `/api/v1/command` | GET/POST | Execute commands (sync, test) |
| `/api/v1/system/status` | GET | System status |

#### Download Clients
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/downloadclient` | GET | List download clients |
| `/api/v1/downloadclient/schema` | GET | Available download client types |

### 2.3 Key Models (from OpenAPI)

**IndexerResource:**
```json
{
  "id": 1,
  "name": "NZBGeek",
  "implementation": "Newznab",
  "protocol": "usenet",
  "priority": 25,
  "enable": true,
  "redirect": false,
  "supportsRss": true,
  "supportsSearch": true,
  "tags": [],
  "fields": [...],
  "capabilities": {...}
}
```

**SearchResult:**
```json
{
  "guid": "string",
  "title": "string",
  "indexerId": 1,
  "indexer": "string",
  "protocol": "usenet",
  "publishDate": "2024-01-01T00:00:00Z",
  "size": 123456789,
  "grabs": 0,
  "seeders": 0,
  "leechers": 0,
  "downloadUrl": "string",
  "magnetUrl": "string",
  "infoUrl": "string",
  "categories": [5030],
  "genres": [],
  "categoriesDescription": "TV HD"
}
```

### 2.4 Differences from Sonarr/Radarr

| Aspect | Sonarr/Radarr/Lidarr | Prowlarr |
|--------|---------------------|----------|
| **Purpose** | Media library management | Indexer aggregation/proxy |
| **Library** | Has media library concept | No library - search-only |
| **Content** | Series/Movie/Artist entities | Search results only |
| **Calendar** | Has calendar endpoint | No calendar |
| **Queue** | Activity queue | No queue (indexers don't download) |
| **Quality Profiles** | Extensive | Minimal (AppProfile only) |
| **Root Folders** | Yes | No |
| **Search Results** | Releases for specific items | General search across indexers |

---

## 3. How Other Projects Integrate Prowlarr

### 3.1 LunaSea (Flutter)

**Repository:** https://github.com/jagandeepbrar/lunasea

**Features Implemented:**
- Indexer management (add/edit/delete)
- Manual search across all indexers
- View search results with filtering
- Health check monitoring
- Application sync status

**Architecture Notes:**
- Separate module per *arr app
- Similar repository pattern to ArrMatey
- Uses provider pattern for state management

### 3.2 nzb360 (Android - Closed Source)

**Reference:** Reddit announcement post (v14.5, August 2021)

**Features Implemented:**
- Browse results from all indexers
- "Sexy Movie Layout" for Movie category (poster grid view)
- Search for any item
- Send items to download client
- Sort by: titles, time, grabs, seeders, size
- View item web details
- Save/manage common searches

**Key Insight:** nzb360 treats Prowlarr search like a "universal search" that can feed into Sonarr/Radarr or directly to download clients.

### 3.3 Helmarr

No public repository found with active Prowlarr integration documentation. Most Helm charts simply deploy Prowlarr but don't integrate it at the UI level.

---

## 4. Recommended Scope for Prowlarr PR

### 4.1 Phase 1: MVP (Recommended Start)

**Core Features:**
1. **Instance Management**
   - Add Prowlarr to `InstanceType` enum
   - Instance configuration (same pattern as others)
   - Test connection

2. **Indexer List View**
   - List all configured indexers
   - Show status (enabled/disabled)
   - Show protocol (usenet/torrent)
   - Quick actions (test, enable/disable)

3. **Search Interface**
   - Search input field
   - Category filter dropdown
   - Results list with:
     - Title, indexer name, age
     - Size, seeders/leechers (torrent)
     - Grab count
   - Sort options (relevance, age, size, seeders)

4. **Search Result Actions**
   - Open details/info URL
   - Copy magnet/NZB URL
   - Send to download client (if API supports)

### 4.2 Phase 2: Enhanced (Future)

- Indexer management (add/edit/delete)
- Health check dashboard
- Search history
- Application sync status view
- Download client management
- Custom filter/saved searches

### 4.3 Out of Scope (For Now)

- Full indexer configuration UI (complex forms per indexer type)
- Application management (connecting Sonarr/Radarr to Prowlarr)
- Download client configuration
- Advanced proxy/flare solver settings

---

## 5. Estimated Complexity

### 5.1 Files to Add/Modify

#### New Files (~12 files)
```
arr/api/client/ProwlarrClient.kt          # API client implementation
arr/api/model/prowlarr/
  ├── Indexer.kt                          # Indexer model
  ├── SearchResult.kt                     # Search result model
  ├── IndexerCapability.kt                # Indexer capabilities
  └── ...                                 # Other Prowlarr-specific models
arr/viewmodel/ProwlarrViewModel.kt        # ViewModel
arr/state/ProwlarrSearchUiState.kt        # UI state
composeApp/src/.../ui/screens/prowlarr/
  ├── ProwlarrScreen.kt                   # Main screen
  ├── ProwlarrSearchScreen.kt             # Search interface
  └── components/                         # UI components
```

#### Modified Files (~8 files)
```
instances/model/Instance.kt               # Add Prowlarr to enum
di/Modules.kt                             # Add ViewModel factory
instances/repository/
  └── InstanceScopedRepository.kt         # Add Prowlarr client accessor
arr/api/client/ArrClient.kt               # May need interface adjustment
compose/TabItem.kt                        # Add Prowlarr tab (optional)
MR/strings/                               # Add localization strings
```

### 5.2 Complexity Assessment

| Component | Complexity | Notes |
|-----------|------------|-------|
| API Client | Medium | Similar pattern to others, but different endpoints |
| Models | Low-Medium | Simpler than ArrMedia hierarchy |
| Repository | Low | Reuse existing patterns |
| ViewModel | Medium | New search-focused logic |
| UI Screens | Medium | Search UI is different from library views |
| Navigation | Low | Follow existing patterns |
| DI Wiring | Low | Standard Koin setup |

**Overall: Medium Complexity**
- Estimated effort: 3-5 days for experienced Kotlin dev
- 1-2 days for API/models/client
- 1-2 days for ViewModel/UI
- 1 day for testing/polish

---

## 6. Gotchas and Differences from Sonarr/Radarr

### 6.1 API Differences

1. **No Library Concept**
   - Sonarr/Radarr: `getLibrary()` returns all series/movies
   - Prowlarr: No equivalent - indexers don't store content

2. **Search is Central**
   - Prowlarr's primary function is searching across indexers
   - Search results are transient (not stored)

3. **No Calendar**
   - Prowlarr doesn't track release dates
   - Calendar integration would need to pull from Sonarr/Radarr

4. **No Activity Queue**
   - Prowlarr doesn't manage downloads
   - Queue concept doesn't apply

5. **Different Test Endpoint**
   - Prowlarr: `POST /api/v1/indexer/test` for testing indexers
   - Sonarr/Radarr: System status endpoint

### 6.2 UI/UX Differences

1. **Tab Structure**
   - Current tabs: Shows (Sonarr), Movies (Radarr), Music (Lidarr)
   - Prowlarr doesn't fit the "media library" pattern
   - Options:
     - Add "Indexers" tab (4th position)
     - Integrate search into existing tabs
     - Separate Prowlarr section in drawer

2. **Search Results vs Library Items**
   - Library items have: poster, status, monitoring
   - Search results have: size, seeders, age, indexer
   - Different UI component needed

3. **No Detail View**
   - Series/Movies have detail screens with episodes/files
   - Prowlarr search results only have info URL

### 6.3 Technical Considerations

1. **ArrClient Interface**
   - Current interface assumes library operations
   - May need to make some methods optional or create separate interface
   - Alternative: Create `SearchableClient` interface for search capability

2. **Caching Strategy**
   - Library results are cached in repository
   - Search results should probably NOT be cached (transient)

3. **Background Sync**
   - Sonarr/Radarr sync library in background
   - Prowlarr has nothing to sync - all on-demand

4. **Instance Selection**
   - Current: One active instance per type
   - Prowlarr: Likely same pattern, but only one Prowlarr instance typical

---

## 7. Implementation Recommendations

### 7.1 Architecture Decisions

1. **Client Design**
   ```kotlin
   class ProwlarrClient(
       override val instance: Instance,
       httpClient: HttpClient
   ) : BaseArrClient(httpClient) {
       // Override only what's needed
       // Add Prowlarr-specific methods
       suspend fun search(query: String, categories: List<Int> = emptyList()): NetworkResult<List<SearchResult>>
       suspend fun getIndexers(): NetworkResult<List<Indexer>>
       suspend fun testIndexer(id: Int): NetworkResult<Unit>
   }
   ```

2. **ArrClient Interface Adjustment**
   - Option A: Keep interface, make library methods return empty/error for Prowlarr
   - Option B: Split interface: `ArrClient` (library) + `SearchableClient` (search)
   - **Recommendation:** Option A for simplicity, mark unsupported operations

3. **UI Tab Decision**
   - Add "Indexers" as 4th bottom tab
   - Icon suggestion: `Icons.Default.Search` or custom icon
   - String resource needed

### 7.2 Model Design

```kotlin
// Prowlarr-specific models
@Serializable
data class Indexer(
    val id: Int,
    val name: String,
    val implementation: String,
    val protocol: String, // "usenet" or "torrent"
    val enable: Boolean,
    val priority: Int,
    val supportsSearch: Boolean,
    val supportsRss: Boolean
)

@Serializable
data class SearchResult(
    val guid: String,
    val title: String,
    val indexer: String,
    val protocol: String,
    val publishDate: Instant,
    val size: Long,
    val grabs: Int,
    val seeders: Int?,
    val leechers: Int?,
    val downloadUrl: String?,
    val magnetUrl: String?,
    val infoUrl: String?,
    val categories: List<Int>
)
```

### 7.3 Testing Checklist

- [ ] Instance creation and API key configuration
- [ ] Test connection to Prowlarr
- [ ] Indexer list loads correctly
- [ ] Search returns results
- [ ] Category filtering works
- [ ] Sorting works (age, size, seeders)
- [ ] Result actions work (copy URL, open browser)
- [ ] Error handling (offline, auth failure)
- [ ] Dark/light theme compatibility

---

## 8. Resources

### Prowlarr Documentation
- API Docs: https://prowlarr.com/docs/api/
- OpenAPI Spec: https://raw.githubusercontent.com/Prowlarr/Prowlarr/develop/src/Prowlarr.Api.V1/openapi.json
- GitHub: https://github.com/Prowlarr/Prowlarr
- Wiki: https://wiki.servarr.com/prowlarr

### Reference Implementations
- LunaSea (Flutter): https://github.com/jagandeepbrar/lunasea
- nzb360 (Android): https://nzb360.com/

---

## 9. Conclusion

Prowlarr integration is a valuable addition that would differentiate ArrMatey from other *arr mobile clients. The implementation follows established patterns but requires careful consideration of Prowlarr's search-centric nature.

**Key Success Factors:**
1. Don't force Prowlarr into the "media library" mental model
2. Focus on search as the primary feature
3. Start minimal, expand based on feedback
4. Reuse existing patterns for consistency

**Next Steps:**
1. Create architecture proposal (if needed beyond this doc)
2. Implement `ProwlarrClient` and models
3. Build search UI
4. Add indexer management
5. Test with real Prowlarr instances

---

*Report generated by Scout - OpenClaw Research Agent*
