# Walkthrough - Updated DetailHeaderBanner startGradient logic

I have updated the `startGradient` logic for `DetailHeaderBanner` to correctly handle tablet layouts with and without the navigation rail visible.

## Changes Made

- **Updated `DetailHeaderBanner` callers**:
    - All screens and components that use `DetailHeaderBanner` (or call headers that use it) now accept a `wideRailIsVisible` parameter.
    - The `startGradient` property of `DetailHeaderBanner` is now calculated as `isExpanded && wideRailIsVisible`.
- **Navigation & Tabs**:
    - Updated `mediaNavEntries` to accept and pass down the `wideRailIsVisible` flag.
    - Updated all tabs (`ArrTab`, `SeerrTab`, `UnifiedLibraryTab`, `CalendarTab`, `DiscoverTab`, `BazarrTab`) to pass the `wideRailIsVisible` flag from the `HomeScreen`.
- **Detail Screens & Components**:
    - `UnifiedMediaDetailsScreen`, `EpisodeDetailsScreen`, `BookDetailsScreen`, `MediaPreviewScreen`, `BazarrDetailsScreen`, and `SeerrPersonDetailsScreen` now correctly handle the rail visibility.
    - `DetailsHeader`, `UnifiedDetailsHeader`, and `PersonDetailsHeader` components were updated to support the new logic.

## Verification Results

- **Build**: Successfully compiled the `:composeApp` module.
- **Logic**: The `startGradient` will now only be shown on expanded screens when the navigation rail (or master pane in dual-pane) is actually visible on the left. This prevents the gradient from appearing when the detail screen is shown in a full-screen overlay (like Settings) on a tablet.
