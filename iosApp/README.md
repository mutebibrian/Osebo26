# iOS app shell

This folder doesn't contain an `.xcodeproj` — it's generated from `project.yml`
by [XcodeGen](https://github.com/yonaskolb/XcodeGen), so there's no hand-edited
project file to go stale or get corrupted. The actual UI comes entirely from
the shared Kotlin/Compose Multiplatform code in `../app/src/commonMain` and
`../app/src/iosMain` — `ContentView.swift` here is just a thin wrapper that
hands the window over to it.

## One-time setup (on your Mac)

```bash
brew install xcodegen
```

## Every time you want to open/build the project

```bash
cd iosApp
xcodegen generate
open OseboApp.xcodeproj
```

`xcodegen generate` reads `project.yml` and writes `OseboApp.xcodeproj`
(gitignored — regenerate it any time, you never edit it by hand). The
project's one Build Phase ("Build Kotlin shared framework (Gradle)") runs
`./gradlew :app:embedAndSignAppleFrameworkForXcode` automatically before every
build, so Xcode always has an up-to-date `OseboShared.framework` to link
against — that's what fixes the "No such module 'OseboShared'" error.

## First build: sign in with your Apple ID

Code signing can't be scripted — Xcode needs your own credentials once:

1. Xcode → Settings → Accounts → add your Apple ID (any free Apple ID works)
2. Find your personal Team ID there too (click your Apple ID → it's shown under your name, a 10-character code like `94WKJLAJ5D`)
3. Put that Team ID in two places so it survives `xcodegen generate` regenerating
   the project from scratch (which otherwise wipes out anything you set by hand
   in Signing & Capabilities — this bit us once already):
   - `project.yml` → `targets.OseboApp.settings.base.DEVELOPMENT_TEAM`
   - `ExportOptions.plist` → the `teamID` key

Both are already filled in with the team ID this project was set up with. If
you ever sign in with a *different* Apple ID, update both files to that
account's team ID, or Signing & Capabilities will blank itself out again the
next time someone runs `xcodegen generate`.

## Producing an .ipa for Sideloadly, entirely from Terminal

```bash
cd iosApp
xcodegen generate

xcodebuild -project OseboApp.xcodeproj -scheme OseboApp \
  -configuration Debug -sdk iphoneos \
  -archivePath build/OseboApp.xcarchive \
  archive

xcodebuild -exportArchive \
  -archivePath build/OseboApp.xcarchive \
  -exportPath build/export \
  -exportOptionsPlist ExportOptions.plist
```

The `.ipa` lands in `iosApp/build/export/OseboApp.ipa` — drag that into
Sideloadly. If the archive step still fails with a signing error, the Team ID
in `project.yml`/`ExportOptions.plist` doesn't match the Apple ID currently
signed into Xcode — fix that first (see above) before re-running.
