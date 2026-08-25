# Implementation Plan - Additional Unit Tests

Implement comprehensive unit tests for core logic areas: extensions, use cases, persistence, view models, and navigation.

## Proposed Changes

### Shared Extensions
#### [NEW] [TimeExtensionsTest](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonTest/kotlin/com/dnfapps/arrmatey/extensions/TimeExtensionsTest.kt)
- Test `LocalDate` and `Instant` extensions (`isToday`, `isAfterToday`, `isBetween`).
- Verify timezone-aware logic.

#### [NEW] [MediaExtensionsTest](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonTest/kotlin/com/dnfapps/arrmatey/extensions/MediaExtensionsTest.kt)
- Test `getUpcomingDateString` for different media types (Series, Movie, Artist).
- Test internal `formatAirTime` logic (12h/24h formats).

### Shared Use Cases
#### [NEW] [GetInstancePresencesUseCaseTest](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonTest/kotlin/com/dnfapps/arrmatey/arr/usecase/GetInstancePresencesUseCaseTest.kt)
- Test merging of media presence across multiple server instances.
- Verify "Unified Library" state building.

### Data Persistence
#### [NEW] [PreferencesStoreTest](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonTest/kotlin/com/dnfapps/arrmatey/datastore/PreferencesStoreTest.kt)
- Test tab order migration logic.
- Verify dashboard customization serialization.

### ViewModels
#### [NEW] [UnifiedMediaDetailsViewModelTest](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonTest/kotlin/com/dnfapps/arrmatey/viewmodel/UnifiedMediaDetailsViewModelTest.kt)
- Test state transitions based on server responses.
- Verify user permissions logic (Admin vs Regular User).

### Navigation
#### [NEW] [NavigationManagerTest](file:///Users/owen.lejeune/AndroidStudioProjects/ArrMatey/shared/src/commonTest/kotlin/com/dnfapps/arrmatey/navigation/NavigationManagerTest.kt)
- Test navigation destination flow.
- Ensure cross-platform consistency.

## Verification Plan

### Automated Tests
- Run all tests via Gradle: `./gradlew :shared:testDebugUnitTest`
- Verify 100% pass rate for new suites.
