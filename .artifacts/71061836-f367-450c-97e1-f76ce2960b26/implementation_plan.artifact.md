# Implement Dual Panel for Discover Tab

Update `DiscoverTab.kt` to support a dual-panel layout, allowing details to be shown in a side panel for both the main discovery screen and the search overlay.

## Proposed Changes

### UI Components

#### [MODIFY] [DiscoverTab.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/tabs/DiscoverTab.kt)

- Replace `NavDisplay` with `TwoPaneMasterDetailNavDisplay` in `DiscoverTab` composable.
- Provide a custom `isMasterScreen` lambda to `TwoPaneMasterDetailNavDisplay` that considers `DiscoverScreen.Home` as a master screen.
- Update `DiscoverHomeScreen` and `DiscoverSearchOverlay` to accept and handle `isExpanded` and `wideRailIsVisible` parameters.
- Ensure `isExpanded` is passed down to `mediaNavEntries`.

## Verification Plan

### Automated Tests
- N/A (UI layout changes are best verified manually)

### Manual Verification
1. Open the Discover tab on a large screen device (or emulator).
2. Verify that the layout is still single-pane in compact mode.
3. Verify that on an expanded screen, clicking an item from the main discovery sections opens the details in a side panel.
4. Verify that expanding the search bar and clicking an item from the search results also opens the details in a side panel.
