# Unsigned iOS IPA (TrollStore) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a manual CI workflow that builds an unsigned, TrollStore-installable iPhone IPA with no paid Apple signing, plus a dormant signing template and a README note, ready to PR upstream.

**Architecture:** Single `workflow_dispatch` job on a pinned macOS runner mirrors `ios.yml` setup (JDK 21, Xcode 26.x, Gradle, caches), runs an unsigned device `xcodebuild build`, re-applies entitlements with `ldid`, verifies and symlink-safe zips a `Payload` IPA, and uploads it as an artifact.

**Tech Stack:** GitHub Actions, `xcodebuild` (Xcode 26.x), Kotlin Multiplatform `embedAndSignAppleFrameworkForXcode`, `ldid`, TrollStore (install target only).

**Spec:** `docs/superpowers/specs/2026-10-26-unsigned-ios-ipa-design.md`

## Global Constraints

- No paid Apple Developer account, no signing secrets; unsigned path is the default.
- `workflow_dispatch` only; never trigger on push/PR.
- Top-level `permissions: {}`, per-job `contents: read`; checkout with `persist-credentials: false`.
- Runner is `macos-26` (pinned for reproducibility; `ios.yml`'s `macos-latest` resolves to the same image as of Oct 2026).
- Mirror `ios.yml` action pins verbatim: `actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1`, `actions/setup-java@b843ebf9b913146b38da2eaa7aeddf76074ae1ad` (java-version `21`, distribution `temurin`), `gradle/actions/setup-gradle@3f5f9adaf7d9fecd50b5935e5410601425a94e6`, `maxim-lobanov/setup-xcode@ed7a3b1fda3918c0306d1b724322adc0b8cc0a90` (xcode-version `26.x`), `actions/cache@55cc8345863c7cc4c66a329aec7e433d2d1c52a9` (same konan/derived-data keys as `ios.yml`), `actions/upload-artifact@cf430e030ddbb5b0abf93d22962f4752f3646cd9`.
- `CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY="" DEVELOPMENT_TEAM=""` on the device build.
- Package with `zip -qry`, never plain `zip -r`.
- Conventional commits (`chore(ci): …`); run `./gradlew spotlessApply` before push (repo has no Ruby/yaml formatter; YAML validity is checked by parse, not spotless).
- No app, bundle-ID, or entitlement-source changes; `No UI changes`, `No breaking changes`.

## Review Focus

- Runner label `macos-26` retired or renamed by GitHub → dispatch fails at queue time with "label not found"; expect a pin bump, and the `xcodebuild -version` log line is the first thing to check. (Pinned by Task 1's runner field; verified by grep test.)
- Framework symlinks flattened in the IPA → installs via TrollStore but crashes on launch; a reasonable person expects install-OK to mean launch-OK. (Pinned by Task 1's `zip -qry` flag; verified by grep test.)
- wifi-info entitlement silently missing → SSID-based local-network switching stops working with no error; a reasonable person expects the unsigned build to behave like the signed one. (Pinned by Task 1's `ldid -S` step; verified by grep test.)
- Empty artifact upload silently passes → author thinks the build produced an IPA when it did not. (Pinned by Task 1's `if-no-files-found: error`; verified by grep test.)
- Push to `upstream` instead of the fork → PR opens from the wrong repo or push is rejected; a reasonable person expects `git push` to go to their fork. (Pinned by Task 4's remote check.)

---

### Task 1: Unsigned IPA workflow

**Files:**
- Create: `.github/workflows/ios-unsigned-ipa.yml`
- Test: `python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/ios-unsigned-ipa.yml'))"` (repo has no actionlint; parse + key assertions are the test)

**Interfaces:**
- Consumes: `ios.yml` conventions (pins, concurrency, cache keys) — copy verbatim per Global Constraints.
- Produces: workflow file other tasks reference only by name; Task 4 dispatches it via `gh workflow run`.

- [ ] **Step 1: Write `.github/workflows/ios-unsigned-ipa.yml`**

  Header mirrors `ios.yml`: `name: Unsigned iOS IPA`, `on: workflow_dispatch`, `concurrency: group: ${{ github.workflow }}-${{ github.ref }}` + `cancel-in-progress: true`, top-level `permissions: {}`.
  One job `build-unsigned-ipa`, `runs-on: macos-26`, `permissions: contents: read`, steps in order:
  1. Checkout (same `with: persist-credentials: false` as `ios.yml`).
  2. JDK 21 temurin + Gradle setup + `chmod +x gradlew` (same pins/inputs as `ios.yml`).
  3. Setup Xcode `26.x` (same pin) + `run: xcodebuild -version`.
  4. Caches for `~/.konan` and `$RUNNER_TEMP/derived_data` (same keys/restore-keys as `ios.yml`).
  5. `run: xcodebuild -list -workspace iosApp/iosApp.xcworkspace`.
  6. `run: xcodebuild build -workspace iosApp/iosApp.xcworkspace -scheme iosApp -configuration Release -sdk iphoneos -derivedDataPath "$RUNNER_TEMP/derived_data" CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY="" DEVELOPMENT_TEAM=""`.
  7. `run: |` — stage + entitlements: `APP=$(echo "$RUNNER_TEMP"/derived_data/Build/Products/Release-iphoneos/*.app)` then `mkdir -p "$RUNNER_TEMP/Payload" && cp -R "$APP" "$RUNNER_TEMP/Payload/"` then `test -f iosApp/iosApp/iosAppRelease.entitlements` then `ldid -h 2>&1 | grep -qi procursus || brew install ldid-procursus` (never the plain `ldid` formula — wrong fork, conflicts with `ldid-procursus`; a mere `command -v ldid` check could silently accept the wrong binary) then `ldid -SiosApp/iosApp/iosAppRelease.entitlements "$RUNNER_TEMP/Payload/$(basename "$APP")/$(basename "$APP" .app)"`. (Executable name follows `PRODUCT_NAME=ArrMatey`; deriving it from the `.app` name keeps this correct if it ever changes.) Export the exact name for the verify step via `echo "APP_NAME=$(basename "$APP")" >> "$GITHUB_ENV"` instead of re-globbing there. Include `#` comments in the script: unsigned embedded frameworks (e.g. the Kotlin shared framework) work under TrollStore, which fakesigns on install; non-TrollStore sideloading without re-signing is out of scope.
  8. `run: |` with `shell: bash` — verify + zip: first line `set -euo pipefail` (Actions defaults to `bash -e` without `pipefail`, which would let a failing `ldid -e` hide behind `grep`'s exit status); `test -f "$RUNNER_TEMP/Payload/$APP_NAME/Info.plist` and test `"$RUNNER_TEMP/Payload/$APP_NAME/${APP_NAME%.app}"` exists (exact name from step 7 via `$GITHUB_ENV` — no re-glob); verify the entitlement round-tripped with `ldid -e` on that same executable path piped to `grep -q com.apple.developer.networking.wifi-info` (no `|| true` — missing entitlement fails the job; `codesign -d` cannot read back an ldid fakesignature); `cd "$RUNNER_TEMP" && zip -qry ArrMatey-unsigned.ipa Payload`.
  9. `actions/upload-artifact@cf430e0…` (same pin) with `name: ArrMatey-unsigned-ipa`, `path: ${{ runner.temp }}/ArrMatey-unsigned.ipa`, `if-no-files-found: error`, `retention-days: 14`.

- [ ] **Step 2: Validate the workflow file parses and carries the load-bearing keys**

  Prerequisite (local host): `python3 -c "import yaml"` must succeed (pyyaml needed; install with `python3 -m pip install pyyaml` if missing).
  Run (PyYAML uses YAML 1.1, so the bare `on:` key parses as boolean `True` — assert via both keys, and keep the workflow itself idiomatic unquoted):
  `python3 -c "import yaml; d=yaml.safe_load(open('.github/workflows/ios-unsigned-ipa.yml')); trig=d.get(True) or d.get('on') or {}; assert 'workflow_dispatch' in trig, 'trigger'; assert next(iter(d['jobs'].values()))['runs-on']=='macos-26', 'runner'"`
  Expected: exit 0 with no output from the python command.
  Run: `grep -c 'CODE_SIGNING_ALLOWED=NO' .github/workflows/ios-unsigned-ipa.yml && grep -c 'zip -qry' .github/workflows/ios-unsigned-ipa.yml && grep -c 'ldid -S' .github/workflows/ios-unsigned-ipa.yml && grep -c 'ldid-procursus' .github/workflows/ios-unsigned-ipa.yml && grep -c 'ldid -e' .github/workflows/ios-unsigned-ipa.yml && grep -c 'pipefail' .github/workflows/ios-unsigned-ipa.yml && grep -c 'if-no-files-found: error' .github/workflows/ios-unsigned-ipa.yml && grep -c 'xcodebuild -list -workspace' .github/workflows/ios-unsigned-ipa.yml`
  Expected: each prints a count ≥ 1 (unsigned flags, symlink-safe zip, entitlement re-application via the Procursus fork, entitlement round-trip check under `pipefail`, strict upload, workspace sanity check). Also assert no plain-`ldid` install slipped in: `! grep -q 'brew install ldid$' .github/workflows/ios-unsigned-ipa.yml && ! grep -q 'codesign -d --entitlements' .github/workflows/ios-unsigned-ipa.yml`.

- [ ] **Step 3: Commit**

  ```bash
  git add .github/workflows/ios-unsigned-ipa.yml
  git commit -m 'chore(ci): add unsigned iOS IPA workflow for TrollStore'
  ```

### Task 2: Dormant signing template

**Files:**
- Create: `iosApp/Configuration/ExportOptions.plist.template`
- Test: `python3 -c "import plistlib; plistlib.load(open('iosApp/Configuration/ExportOptions.plist.template','rb'))"`

**Interfaces:**
- Consumes: nothing from Task 1 (independent file; same commit series for one PR).
- Produces: template path the future signed job will fill in; no code depends on it.

- [ ] **Step 1: Write `iosApp/Configuration/ExportOptions.plist.template`**

  Valid XML plist dict with commented placeholders only: `<!-- method: development | ad-hoc | app-store -->`, `<!-- teamID: <APPLE_TEAM_ID> -->`, `uploadSymbols: <false/>`. Must parse as plist while comments remain (XML comments are legal in plists).

- [ ] **Step 2: Verify it parses as a plist**

  Run: `python3 -c "import plistlib; print(plistlib.load(open('iosApp/Configuration/ExportOptions.plist.template','rb')))"`
  Expected: prints the dict (e.g. `{'uploadSymbols': False}`), exit 0.

- [ ] **Step 3: Commit**

  ```bash
  git add iosApp/Configuration/ExportOptions.plist.template
  git commit -m 'chore(ci): add dormant ExportOptions template for future signed builds'
  ```

### Task 3: README TrollStore note

**Files:**
- Modify: `README.md` (iOS build section, after the signing-team paragraph, before "Adding Localized String")
- Test: `grep -c 'TrollStore' README.md`

**Interfaces:**
- Consumes: workflow artifact name `ArrMatey-unsigned-ipa` from Task 1.
- Produces: user-facing install pointer; nothing depends on it.

- [ ] **Step 1: Add a 3–5 line paragraph in the iOS build section**

  Copy: unsigned `Payload` IPA from the manual `ios-unsigned-ipa` workflow artifact installs via TrollStore with no Apple Developer account; paid signing stays optional for maintainers (points at `ExportOptions.plist.template`). One scope line: embedded frameworks ship unsigned and rely on TrollStore's install-time fakesigning — other sideloading methods must re-sign. No other README sections change.

- [ ] **Step 2: Verify the note exists and sits in the iOS section**

  Run: `grep -c 'TrollStore' README.md && grep -n 'Adding Localized String' README.md`
  Expected: first count ≥ 1; the TrollStore line number is smaller than the "Adding Localized String" line number.

- [ ] **Step 3: Commit**

  ```bash
  git add README.md
  git commit -m 'docs: note unsigned TrollStore IPA workflow in iOS build section'
  ```

### Task 4: Fork remotes, issue, branch, PR

**Files:**
- None (operator process; verification is command output, not file content).

**Interfaces:**
- Consumes: Tasks 1–3 commits (must exist on the branch); fork URL supplied by the user at execution time.
- Produces: upstream issue number (recorded in the PR body as `Closes #___`) and PR URL.

- [ ] **Step 1: Point remotes at the fork**

  Run: `git remote get-url upstream >/dev/null 2>&1 || git remote rename origin upstream; if git remote get-url origin >/dev/null 2>&1; then git remote set-url origin https://github.com/wetw0rks-c0ding/ArrMatey.git; else git remote add origin https://github.com/wetw0rks-c0ding/ArrMatey.git; fi; git remote -v`
  Expected: `upstream` → `owenlejeune/ArrMatey`, `origin` → the fork. (`set-url` covers the case where `origin` already exists but points at the wrong URL — plain `add` would fail there.) Already done in this checkout; the conditional form keeps this safe on a fresh clone.

- [ ] **Step 2: Open the upstream issue (feature_request template)**

  Title `chore: unsigned iOS IPA via CI for TrollStore/sideloading`; body covers problem (no iPhone install without Mac/paid account), solution (dispatch → unsigned `Payload` IPA + dormant signing template), alternatives (local Mac archive, paid Ad Hoc/TestFlight).
  Run: `gh issue create --title '…' --body '…'` (or file via web UI and record the number).
  Expected: issue URL/number returned; save it for the PR body.

- [ ] **Step 3: Branch, push, open the PR**

  Run: `git checkout -b chore/unsigned-ios-ipa && ./gradlew spotlessApply && git push -u origin chore/unsigned-ios-ipa`
  Expected: push succeeds to the fork (not upstream); `spotlessApply` may amend files — if it does, commit with `git commit -m 'style: apply spotless'`.
  Open PR against `upstream/main` with template boxes: `chore`, Small change, `Tooling / CI / Scripts` only, `No UI changes`, `No breaking changes`, `Closes #<issue>`.

- [ ] **Step 4: Verify PR checks and dispatch once**

  Run: `gh pr checks --watch` (or watch in UI).
  Expected: `ios.yml` simulator job green; then dispatch the new workflow once (`gh workflow run ios-unsigned-ipa.yml --ref chore/unsigned-ios-ipa`) and confirm the `ArrMatey-unsigned-ipa` artifact appears. Device install test (TrollStore → launch → add instance → dashboard → SSID switching) is manual and recorded on the PR.
