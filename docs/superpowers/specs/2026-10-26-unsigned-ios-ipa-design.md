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
  `xcodebuild: command not found`). IPA must build on a pinned macOS runner
  (`macos-26`; see Sec.3.1).
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

Runner: `macos-26` (pinned for reproducibility; `ios.yml` uses `macos-latest` —
intentional divergence, revisit when `ios.yml` pins). Keep the Setup Xcode 26.x
step explicit and add an `xcodebuild -version` diagnostic so the exact compiler
is visible in logs.

Jobs (single `build-unsigned-ipa`):

1. Checkout (persist-credentials false).
2. JDK 21 (temurin) + Gradle setup + `chmod +x gradlew` (mirror `ios.yml`).
3. Setup Xcode 26.x (mirror `ios.yml`) + `xcodebuild -version`.
4. Optional caches: `~/.konan`, derived data (copy `ios.yml` keys).
   Use `$RUNNER_TEMP/derived_data` for build output and `$RUNNER_TEMP/Payload`
   for staging so the checkout stays clean.
5. Sanity: `xcodebuild -list -workspace iosApp/iosApp.xcworkspace` (fails fast if
   the `iosApp` scheme is not resolvable; `-workspace` matches the build
   invocation — `-project` can false-fail since no `.xcscheme` is committed).
6. Unsigned device build (NOT archive):
   `xcodebuild build -workspace iosApp/iosApp.xcworkspace -scheme iosApp`
   `-configuration Release -sdk iphoneos -derivedDataPath "$RUNNER_TEMP/derived_data"`
   `CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY=""`
   `DEVELOPMENT_TEAM=""`
   Rationale: `archive` + `-exportArchive` requires Team/provisioning
   (`ExportOptions.plist` method/teamID) — the paid path. `build` + manual
   `Payload` zip is the standard unsigned/TrollStore route.
7. Re-apply entitlements (REQUIRED, not optional): with signing disabled Xcode
   does not embed `iosAppRelease.entitlements`, but the app calls
   `CNCopyCurrentNetworkInfo` at runtime (`IOSNetworkUtils.getCurrentWifiSsid`,
   used by local-network switching and SSID matching), which returns nil on
   iOS 13+ without `com.apple.developer.networking.wifi-info`. After staging:
   `ldid -SiosApp/iosApp/iosAppRelease.entitlements "$RUNNER_TEMP/Payload/ArrMatey.app/ArrMatey"`
   (`brew install ldid` first if the runner lacks it; TrollStore fakesigns
   whatever entitlements are present).
8. Verify, then package: assert both `$RUNNER_TEMP/Payload/ArrMatey.app/Info.plist`
   AND `$RUNNER_TEMP/Payload/ArrMatey.app/ArrMatey` exist
   (`PRODUCT_NAME=ArrMatey` via `Config.xcconfig`; glob `*.app` as fallback);
   spot-check with `codesign -d --entitlements :- <binary> || true`.
   Zip with symlink preservation — plain `zip -r` follows/flattens framework
   symlinks (`Versions/Current`, …) and can produce install-OK/crash-on-launch
   IPAs: `cd "$RUNNER_TEMP" && zip -qry ArrMatey-unsigned.ipa Payload`
   (or `ditto -c -k --sequesterRsrc --keepParent Payload ArrMatey-unsigned.ipa`).
9. Upload artifact `ArrMatey-unsigned-ipa` (+ keep the `.app` for debugging)
   with `if-no-files-found: error` and `retention-days: 14`.

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
| Framework symlinks flattened | App installs but crashes on launch | Packaging must use `zip -qry` or `ditto`, never plain `zip -r`; re-zip and re-upload |
| Entitlement stripped | `codesign -d --entitlements` shows empty/missing wifi-info; SSID-based local switching silently stops working | Re-apply via `ldid -S` with `iosAppRelease.entitlements` (Sec.3.1 step 7) |
| Artifact upload empty | No IPA in artifact | `if-no-files-found: error` fails the job; verify `Payload` path and zip step |
| Bad IPA layout | TrollStore parse error | Verify `Payload/<name>.app/Info.plist` + executable assert step |
| Maintainer wants signed | asks for Team | Point at Sec.3.2 template; no change to unsigned job |

## 5. Testing / acceptance

- CI: both `ios.yml` (simulator) and new workflow green on dispatch.
- Artifact check: `unzip -l` shows `Payload/ArrMatey.app/Info.plist`.
- Device (TrollStore): install → launch → add instance → dashboard loads →
  verify SSID-based local switching still works (wifi-info entitlement path).
- No unit-test changes; existing `:shared:iosSimulatorArm64Test` coverage untouched.

## 6. Open inputs needed at implementation

- User's fork URL (to set `origin`).
- Whether README note goes under “Build and Run iOS Application” or Downloads
  (default: short TrollStore paragraph under iOS build section).
