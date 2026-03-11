# Feature Request: Prowlarr Integration

## Description

I'd love to help add Prowlarr support to ArrMatey. I noticed it's on the roadmap and wanted to propose a scope and offer to build it.

## Proposed Scope (MVP)

Prowlarr is fundamentally different from Sonarr/Radarr/Lidarr — it's an indexer manager, not a media library. So rather than forcing it into the existing library UI pattern, I'm thinking a **search-first approach** with its own tab:

### Screens

1. **Indexer List** — View configured indexers with health status, protocol (torrent/usenet), and basic stats
2. **Search** — Query across all indexers with filters for category (Movies, TV, Audio), protocol, and specific indexers. Results show title, indexer, size, age, seeders/leechers. Sortable by age, size, seeders.

### Technical Approach

- **`ProwlarrClient`** extending `BaseArrClient` directly (not implementing `ArrClient`, since the media-library contract doesn't apply)
- Add `Prowlarr` to `InstanceType` enum (port 9696, `api/v1`)
- New Prowlarr-specific models, ViewModels, and UI state classes
- Native UI on both platforms (Compose + SwiftUI) following existing patterns
- Read-only for Phase 1 — no indexer add/edit/delete

### Out of Scope (for now)

- Indexer management (add/edit/remove) — complex provider-specific config
- Grab/download from search results
- App sync configuration
- Notifications

### Estimate

~20 files, ~2000 lines across shared/Android/iOS layers.

## Questions for Maintainer

1. Does this scope align with your vision for Prowlarr support?
2. Any preference on whether `ProwlarrClient` should implement `ArrClient` with no-ops, or stay separate?
3. Should Prowlarr get its own bottom tab, or live under a "More" section?
4. Any naming conventions or patterns I should follow that aren't obvious from the existing code?

Happy to adjust the scope based on your feedback before writing any code.
