# Score keeper for Carcassonne

A lightweight Android score keeper for the Carcassonne board game (Kotlin + Jetpack Compose, ~1.5 MB release APK, Android 7.0+).

## Features

- **Players**: add 2–8 players and assign each a pawn colour — Yellow, Green, Blue, Black, Red or Pink, or any custom colour (hue/saturation/brightness sliders or a hex code). Two players can't share a colour. The last game's line-up and recent players (with their last colour) are offered as shortcuts.
- **Scoring**: tap **+1** repeatedly (quick taps merge into one entry, e.g. three taps = "+3"), or open the keypad to type a number. Use **− / +** on the keypad to deduct points (e.g. −9) and correct mistakes.
- **Fixing mistakes**: undo the last entry from the toolbar, or remove any entry from the score log (with an Undo snackbar).
- **Keep screen awake**: toggle in Settings.
- **History**: finished games are saved with date, duration, players, final scores and the full score log.
- **All-time leaderboard**: wins per player (ties for first all count as wins), games played, win rate, best and average score. Players are matched by name, ignoring case.

The game in progress is saved after every change, so it survives the app being closed or killed.

## Build

```sh
./gradlew testDebugUnitTest    # unit tests (scoring, leaderboard, save format)
./gradlew lintRelease          # lint; fails the build on errors
./gradlew assembleRelease      # signed APK: app/build/outputs/apk/release/app-release.apk
./gradlew bundleRelease        # signed AAB for Google Play: app/build/outputs/bundle/release/app-release.aab
```

Requires JDK 17+ and the Android SDK (compile SDK 37, target SDK 36, min SDK 24).

## Release signing

Release builds are signed with the upload key described in `keystore.properties`
(copy `keystore.properties.example` to get started). Both that file and the `keystore/`
directory are git-ignored and must never be committed.

**Back up `keystore/upload.jks` and `keystore.properties` somewhere safe** (e.g. a password
manager). Every future update must be signed with the same key. If you publish on Google Play
with Play App Signing, a lost upload key can be reset through Play Console support; for
side-loaded APKs it cannot, and users would have to uninstall to update.

Without `keystore.properties` the release build is produced unsigned.

## Releasing an update

1. Bump `versionCode` (must increase every upload) and `versionName` in `app/build.gradle.kts`.
2. Run the tests, lint and `bundleRelease` (see above).
3. Upload `app-release.aab` to Play Console. Keep `app/build/outputs/mapping/release/mapping.txt`
   with the release so crash stack traces can be de-obfuscated (Play Console accepts it as well).

## Publishing on Google Play

Everything needed for the Play Console is in [`store/`](store/):

- [`store/listing.md`](store/listing.md) — app name, short and full description, category, release notes
- [`store/play-console-declarations.md`](store/play-console-declarations.md) — answers for Data safety,
  content rating, target audience, ads, app access, plus the technical compliance checklist
- `store/graphics/` — 512×512 app icon and 1024×500 feature graphic
- `store/screenshots/` — seven 1080×1920 phone screenshots

The privacy policy page is [`docs/privacy-policy.html`](docs/privacy-policy.html). Host it at a public URL
(for example GitHub Pages serving `docs/`), then set `privacyPolicyUrl` in `gradle.properties` so the
app's Settings screen links to it.

## Data

Stored privately in the app's files directory as JSON (`current_game.json`, `history.json`; pawn colours are saved as `#RRGGBB`, and files from version 1 that stored a palette index are migrated on load); the
keep-awake setting lives in SharedPreferences. Data is included in Android backup and device
transfer. If a saved file is ever unreadable, the readable games are kept and the original file is
preserved as `history-unreadable-*.json`.
