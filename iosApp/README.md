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
2. Open the project, select the `OseboApp` target → **Signing & Capabilities**
3. Check "Automatically manage signing" and pick your personal team from the dropdown

After that, ⌘B (build) or ⌘R (run on Simulator) should work normally.

## Producing an .ipa for Sideloadly, entirely from Terminal

Once signing is set up once in the GUI (step above), the rest can be scripted:

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
Sideloadly.

If the archive step fails with a signing/provisioning error even after
setting up your team in the GUI, add your Team ID to `ExportOptions.plist`
(find it in Xcode → Settings → Accounts → your Apple ID → your team) and to
`project.yml` under `settings.base.DEVELOPMENT_TEAM`, then rerun
`xcodegen generate` before retrying the commands above.
