# ArrMatey Unsigned iOS IPA (TrollStore) — Design

Date: 2026-10-26
Status: approved in chat (Sec.1 + Sec.2), pending written-spec review
Scope: one implementation plan. App improvements deferred until after IPA testing.

## 1. Intent

- Outcome: build an installable iPhone `.ipa` from Linux via a manual GitHub Actions
  run, with **no paid Apple Developer account** (open-source first).
- Install target: TrollStore (CoreTrust bypass; accepts unsigned / ad-hoc `Payload` IPAs).
- Upstream-friendly: default stays unsigned; maintainers can plug paid signing later
  without rework.
- Contribution: land via the repo's issue → PR flow (`CONTRIBUTING.md`).

What this is NOT: TestFlight / App Store distribution, Ad Hoc provisioning, local-Mac
archive instructions (README already covers that), any app feature work.

## 2. Constraints (verified)

- Build host here is Linux, no `xcodebuild` (`uname`: Linux mission-control,
  `xcodebuild: command not found`). IPA must build on `macos-latest`.
- Existing `.github/workflows/ios.yml` only runs `xcodebuild test` on simulator;
  no archive/export, no signing secrets.
- Xcode project: `iosApp/iosApp.xcworkspace` + `iosApp/iosApp.xcodeproj`, scheme
  `iosApp` (per `ios.yml`; no `.xcscheme` committed — CI relies on Xcode-generated
  scheme, same as today).
- Xcode Build Phase auto-runs `../gradlew :shared:embedAndSignAppleFrameworkForXcode`
  plus a MOKO bundle copy from `shared/build/xcode-frameworks/...`. The workflow must
  not bypass this phase.
- Release config in `project.pbxproj`: bundle `com.owenlejeune.arrmatey`,
  Manual signing, empty team, v0.10.3 (18). `Configuration/Config.xcconfig` dev
  override: `com.bmoon.arrmatey.dev`. This slice changes **neither**.
- `iosAppRelease.entitlements` contains only `com.apple.developer.networking.wifi-info`.
  TrollStore tolerates it; no new entitlements in this slice.
- `origin` currently points at `owenlejeune/ArrMatey`, not the user's fork.
  Implementation needs the fork URL before pushing.

## 3. Design

### 3.1 New workflow: `ios-unsigned-ipa.yml`

Trigger: `workflow_dispatch` only (manual; no push/PR noise). `concurrency` cancel
like `ios.yml`. `permissions: {}` top-level, `contents: read` on jobs.

Jobs (single `build-unsigned-ipa` on `macos-latest`):

1. Checkout (persist-credentials false).
2. JDK 21 (temurin) + Gradle setup + `chmod +x gradlew` (mirror `ios.yml`).
3. Setup Xcode 26.x (mirror `ios.yml`).
4. Optional caches: `~/.konan`, `derived_data` (copy `ios.yml` keys).
5. Sanity: `xcodebuild -list -project iosApp/iosApp.xcodeproj` (fails fast if the
   `iosApp` scheme is not resolvable on the runner).
6. Unsigned device build (NOT archive):
   `xcodebuild build -workspace iosApp/iosApp.xcworkspace -scheme iosApp`
   `-configuration Release -sdk iphoneos -derivedDataPath derived_data`
   `CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY=""`
   `DEVELOPMENT_TEAM=""`
   Rationale: `archive` + `-exportArchive` requires Team/provisioning
   (`ExportOptions.plist` method/teamID) — the paid path. `build` + manual
   `Payload` zip is the standard unsigned/TrollStore route.
7. Package: locate `Release-iphoneos/ArrMatey.app` under `derived_data`
   (`PRODUCT_NAME=ArrMatey` via `Config.xcconfig`; glob `*.app` as fallback),
   `mkdir Payload && cp -R <app> Payload/ && zip -r ArrMatey-unsigned.ipa Payload`.
   Assert `Payload/ArrMatey.app/Info.plist` exists before zipping.
8. Upload artifact `ArrMatey-unsigned-ipa` (+ keep the `.app` for debugging).

### 3.2 Signing plug-point (dormant)

- Add `iosApp/Configuration/ExportOptions.plist.template` (commented `method`,
  `teamID`, `uploadSymbols:false`) + a commented-out `archive/export` job skeleton
  gated on secrets (`APPLE_TEAM_ID`, cert p12, provisioning profile).
- Default path never reads secrets. If upstream supplies a dev account, they
  uncomment/fill — no rework of Sec.3.1.

### 3.3 Contribution slice

- Remotes: `git remote rename origin upstream; git remote add origin <fork-url>`.
- Issue (feature_request template): `chore: unsigned iOS IPA via CI for
  TrollStore/sideloading` — problem / solution / alternatives (local Mac archive,
  paid Ad Hoc/TestFlight). Small/`chore`, no pre-approval wait, but issue required.
- Branch `chore/unsigned-ios-ipa` → PR against `upstream/main`.
  PR template boxes: `chore`, Small change, `Tooling / CI / Scripts` only,
  `No UI changes`, `No breaking changes`. Link `Closes #___`.
- Contents only: new workflow + plist template + short README TrollStore note.
  No app/bundle/entitlement changes → iOS+Android UI parity rule N/A.
- Commits conventional (`chore(ci): …`); run `./gradlew spotlessApply` pre-push;
  request review.

## 4. Error handling

| Failure | Signal | Action |
|---|---|---|
| Scheme unresolvable | `-list` step fails | Commit shared scheme or pin scheme generation; do not proceed to build |
| Shared framework embed fails | Gradle `embedAndSign…` error in build log | Surface log; likely Kotlin/Xcode version drift (pin per `libs.versions.toml`: Kotlin 2.4.10) |
| MOKO bundle missing | `MOKO bundle not found` + exit 1 | Run order preserved (Xcode phase handles it); check `shared/build/xcode-frameworks` |
| Bad IPA layout | TrollStore parse error | Verify `Payload/<name>.app/Info.plist` assert step |
| Maintainer wants signed | asks for Team | Point at Sec.3.2 template; no change to unsigned job |

## 5. Testing / acceptance

- CI: both `ios.yml` (simulator) and new workflow green on dispatch.
- Artifact check: `unzip -l` shows `Payload/ArrMatey.app/Info.plist`.
- Device (TrollStore): install → launch → add instance → dashboard loads.
- No unit-test changes; existing `:shared:iosSimulatorArm64Test` coverage untouched.

## 6. Open inputs needed at implementation

- User's fork URL (to set `origin`).
- Whether README note goes under “Build and Run iOS Application” or Downloads
  (default: short TrollStore paragraph under iOS build section).
