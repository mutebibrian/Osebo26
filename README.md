[README.md](https://github.com/user-attachments/files/32243160/README.md)
# Osebo

Kotlin Multiplatform (KMP) rewrite of the Osebo app — Compose Multiplatform UI, Koin dependency injection, shared code targeting Android and iOS.

## Stack
- Kotlin Multiplatform (androidMain / commonMain / iosMain)
- Jetpack Compose + Compose Multiplatform (shared UI in commonMain)
- Koin for dependency injection
- Room for local persistence
- GitHub Actions CI: lint, unit tests, instrumentation tests, gated deploys to dev/production

## Requirements
- JDK 21 (Temurin recommended)
- Android Studio (latest stable) with the Kotlin Multiplatform plugin
- (iOS work only) Xcode + a Mac

## Getting started
1. Clone the repo and open it in Android Studio.
2. Let Gradle sync (first sync can take a while — it's a multiplatform project).
3. Run the `app` configuration on an emulator/device.

## Building & testing locally
```
./gradlew lintDebug                  # static analysis
./gradlew testDebugUnitTest          # unit tests
./gradlew connectedDebugAndroidTest  # instrumentation tests (needs emulator/device)
./gradlew assembleDebug              # debug APK
```

## Project layout
- `app/src/commonMain` — shared Kotlin + Compose Multiplatform UI (theme, components, screens), used by Android and iOS
- `app/src/androidMain` — Android-specific code (fragments, Android DI wiring)
- `app/src/iosMain` — iOS entry point / platform actuals
- `app/src/androidUnitTest`, `app/src/androidInstrumentedTest` — tests

## UI work
- Global styling: `app/src/commonMain/kotlin/com/devbrian/osebo/ui/theme/` (`Color.kt`, `Typography.kt`, `Shape.kt`, `Theme.kt`)
- Shared components: `app/src/commonMain/kotlin/com/devbrian/osebo/ui/components/`
- Screens: `app/src/commonMain/kotlin/com/devbrian/osebo/ui/screens/`

## CI/CD
See `.github/workflows/android-ci.yml`. Pushes/PRs to `main`/`dev` run lint + unit tests; `dev` pushes deploy a debug build; `main` pushes build a signed release (gated by GitHub Environment approval).

## Contributing
See `CONTRIBUTING.md`.
