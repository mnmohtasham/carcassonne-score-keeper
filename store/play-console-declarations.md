# Play Console declarations — Score keeper for Carcassonne

Answers for Play Console → **Policy and programs → App content**, based on what the app actually
does (verified from the release build: no permissions, no network access, no third-party SDKs,
no ads, data stored only on the device).

## Privacy policy
- URL: {{PRIVACY_POLICY_URL}} (host `docs/privacy-policy.html` publicly, e.g. GitHub Pages; it must be
  reachable without login, not a PDF, and must name the developer exactly as on the Play listing)

## App access
- **All functionality is available without special access** (no login).

## Ads
- **No, my app does not contain ads.**

## Content rating (IARC questionnaire)
- Category: **Reference, News, or Educational**? No — choose **"All Other App Types"** (utility).
- Violence, sexuality, language, controlled substances, gambling, horror: **No** to all.
- Does the app allow users to interact or exchange content? **No.**
- Does the app share the user's current physical location? **No.**
- Does the app allow purchases of digital goods? **No.**
- Expected result: **Everyone / PEGI 3 / USK 0**.

## Target audience and content
- Target age groups: **13–15, 16–17, 18 and over**.
  (Selecting an under-13 group puts the app under the Families policy and extra review. The app would
  comply — no ads, no data collection — but it is not designed for children, so 13+ is the accurate
  and simpler choice.)
- Could the app unintentionally appeal to children? **No** — it is a score sheet for a board game.

## Data safety
- Does your app collect or share any of the required user data types? **No.**
  - Player names and scores are stored only on the device and never sent off it, so under Play's
    definitions they are not "collected". (Android Auto Backup is handled by the OS on the user's
    behalf and does not count as collection by the app.)
- Is all of the user data collected by your app encrypted in transit? **Not applicable** (no data collected).
- Do you provide a way for users to request that their data is deleted? **Not applicable** — nothing
  is collected; on-device data is deleted via Settings → Clear game history or by uninstalling.
- Result shown on the listing: **"No data collected" · "No data shared with third parties"**.

## Advertising ID
- **No**, the app does not use an advertising ID (no `AD_ID` permission in the manifest).

## Government apps / Financial features / Health / News / COVID-19
- Not a government app. No financial features. No health features. Not a news app.

## Other declarations that do NOT apply (no special permissions requested)
- Sensitive permissions (SMS, Call log, Location, Photo/Video, All files access,
  Exact alarms, Full-screen intent, Foreground service, Accessibility, VPN): **none used**.

## Technical requirements (already met by the build)
| Requirement | Status |
|---|---|
| Android App Bundle (.aab) | `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab` |
| Target API level 36 (required for new apps and updates since 31 Aug 2026) | targetSdk 36 |
| 16 KB memory page support (apps with native code) | Only native library is AndroidX `libandroidx.graphics.path.so`; all ABIs 16 KB aligned (zip + ELF LOAD 0x4000) |
| Signed with upload key; Play App Signing | Upload key in `keystore/upload.jks`; enrol in Play App Signing on first upload |
| R8 mapping for crash reports | Upload `app/build/outputs/mapping/release/mapping.txt` with each release |
| Version code increases every upload | `versionCode` in `app/build.gradle.kts` |
| Edge-to-edge (enforced when targeting 35+) | Handled; content respects system bars, cutouts and keyboard |
| No debuggable release, no unnecessary permissions | Verified with `aapt2 dump badging` |

## Account-level steps (cannot be done from the code)
1. **Developer account identity verification** in Play Console (required for all developers).
2. **New personal accounts** (created after 13 Nov 2023): before Production unlocks, run a
   **closed test with at least 12 opted-in testers for 14 consecutive days**, then apply for
   production access. Organisation accounts are exempt.
3. Set up the store listing (`store/listing.md`), upload graphics (`store/graphics`,
   `store/screenshots`), complete everything above, then create the release with the AAB.
