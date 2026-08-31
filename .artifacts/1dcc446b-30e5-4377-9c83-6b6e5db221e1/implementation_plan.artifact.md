# Update DetailHeaderBanner startGradient logic

Update `DetailHeaderBanner` and its callers to use a more precise logic for `startGradient`.
The gradient should be shown if:
- In dual panel: `isExpanded` is true.
- In single panel: `isExpanded` is true AND `wideRailIsVisible` is true.

## Proposed Changes

### [Navigation]

#### [MODIFY] [mediaNavEntries.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/components/navigation/MediaNavEntries.kt)
- Add `wideRailIsVisible: Boolean` parameter to `mediaNavEntries`.
- Pass `wideRailIsVisible` to screens: `UnifiedMediaDetailsScreen`, `EpisodeDetailsScreen`, `BookDetailsScreen`, `MediaPreviewScreen`.

### [Screens]

#### [MODIFY] [UnifiedMediaDetailsScreen.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/screens/UnifiedMediaDetailsScreen.kt)
- Add `wideRailIsVisible: Boolean` parameter.
- Update `UnifiedDetailsHeader` and `DetailsHeader` calls to use the new logic for `isExpanded` (passing a calculated `startGradient` value or passing both flags).

#### [MODIFY] [EpisodeDetailsScreen.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/screens/EpisodeDetailsScreen.kt)
- Add `wideRailIsVisible: Boolean` parameter.
- Update `DetailHeaderBanner` call.

#### [MODIFY] [BookDetailsScreen.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/screens/BookDetailsScreen.kt)
- Add `wideRailIsVisible: Boolean` parameter.
- Update `DetailHeaderBanner` call.

#### [MODIFY] [MediaPreviewScreen.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/screens/MediaPreviewScreen.kt)
- Add `wideRailIsVisible: Boolean` parameter.
- Update `UnifiedDetailsHeader` call.

### [Components]

#### [MODIFY] [DetailsHeader.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/components/DetailsHeader.kt)
- Update `DetailsHeader` to accept `wideRailIsVisible: Boolean`.
- Update `DetailHeaderBanner` call.

#### [MODIFY] [UnifiedDetailsHeader.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/components/UnifiedDetailsHeader.kt)
- Update `UnifiedDetailsHeader` to accept `wideRailIsVisible: Boolean`.
- Update `DetailHeaderBanner` call.

#### [MODIFY] [PersonDetailsHeader.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/components/PersonDetailsHeader.kt)
- Update to accept `wideRailIsVisible: Boolean`.
- Update `DetailHeaderBanner` call.

### [Tabs]

#### [MODIFY] [ArrTab.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/tabs/ArrTab.kt)
- Pass `wideRailIsVisible` to `mediaNavEntries`.

#### [MODIFY] [SeerrTab.kt](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/composeApp/src/androidMain/kotlin/com/dnfapps/arrmatey/ui/tabs/SeerrTab.kt)
- Pass `wideRailIsVisible` to `mediaNavEntries`.

## Logic Detail

For each detail component, `startGradient` will be calculated as:
```kotlin
val showStartGradient = if (isDualPanel) isExpanded else (isExpanded && wideRailIsVisible)
```
Wait, in `UnifiedMediaDetailsScreen`, how do we know if it's dual panel?
`ArrTab` is dual panel if `isExpanded` is true.
`SeerrTab` is NEVER dual panel.

I should probably pass an `isDualPanel` flag or just pass the desired `startGradient` value from the tab/entry provider.

Actually, passing `wideRailIsVisible` and using it in the screen seems better.

If I'm in `ArrTab` (dual panel), and `isExpanded` is true, then `wideRailIsVisible` is ALSO true.
So `isExpanded && wideRailIsVisible` is same as `isExpanded`.

Wait, in `SeerrTab` (single panel), and `isExpanded` is true, then `wideRailIsVisible` is ALSO true.
So `isExpanded && wideRailIsVisible` is ALSO same as `isExpanded`.

Wait, when is `wideRailIsVisible` FALSE when `isExpanded` is true?
ONLY when there is an overlay (like Settings or custom webpage).

If I'm in an overlay, I'm effectively in single panel mode (the rail is hidden).
In this case, `wideRailIsVisible` is false.
So `isExpanded && wideRailIsVisible` will be FALSE.
This is what we want for single panel!

What about dual panel in an overlay?
There is no dual panel in an overlay currently. `SettingsTabNavHost` is single panel.

So the logic `isExpanded && wideRailIsVisible` seems to cover EVERYTHING correctly.
- Single panel, wide screen, rail visible: `true && true` -> `true`. (Correct)
- Single panel, wide screen, rail hidden (overlay): `true && false` -> `false`. (Correct)
- Single panel, narrow screen: `false && false` -> `false`. (Correct)
- Dual panel: `isExpanded` is true, `wideRailIsVisible` is true -> `true && true` -> `true`. (Correct)

Wait, what if `ArrTab` is in dual panel, but `wideRailIsVisible` is false?
This shouldn't happen because if it's dual panel, the rail is shown (in `HomeScreen`).

So I will use `isExpanded && wideRailIsVisible` everywhere.

## Verification Plan

### Automated Tests
- N/A (UI logic)

### Manual Verification
- Verify on tablet:
    - In `ArrTab` (dual panel), the details screen should have a start gradient.
    - In `SeerrTab` (single panel), the details screen should have a start gradient.
    - In an overlay (e.g. Settings), the details screen (if any) should NOT have a start gradient.
- Verify on phone:
    - No start gradient.
