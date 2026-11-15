# Google Play

How Kid Reels is set up on Google Play, and why. Kept here so the next release starts
from what is live rather than from the Play Console's memory: if you change something in
the Console, change it here too.

## Identity

| Field            | Value                                                    |
| ---------------- | -------------------------------------------------------- |
| Account          | Crylo (personal, apps in production since 2020)          |
| Store name       | Kid Reels: Learning Games                                |
| Package          | `com.crylo.kidreels` (can never change)                  |
| Type, price      | App, free (free can never become paid)                   |
| Default language | English (United Kingdom)                                 |
| Category         | Education                                                |
| Contact email    | thecrylocompany@gmail.com                                |
| Website          | https://github.com/vivekg7/kidreels                      |
| Privacy policy   | https://github.com/vivekg7/kidreels/blob/main/PRIVACY.md |

The store name follows the account's "Name: Tagline" pattern; the name under the icon
stays "Kid Reels". The listing is British English (colours, licence), which is also
what Indian schools teach, hence `en-GB`.

The account predates November 2023, so it can publish straight to production without
the 12-tester, 14-day closed test newer accounts need.

## Signing

Play generates and keeps the **app signing key**; `local/kidreels-release.jks` is only
the **upload key** (see the README). A lost upload key can be reset through Play support;
a lost app signing key could not be. The cost: a copy from Play and a sideloaded copy are
signed differently, so neither can update the other. Uninstall one before installing the
other; the settings and progress are lost, which for this app is a minute's setup.

## Listing

The text and graphics are in `fastlane/metadata/android/en-GB/`, which Play Console
takes by copy and upload and F-Droid reads as is:

- `title.txt`, `short_description.txt`, `full_description.txt`, and
  `changelogs/<versionCode>.txt` for each release's "What's new".
- `images/icon.png` (512 × 512) and `images/featureGraphic.png` (1024 × 500): the
  launcher icon's card and star, redrawn as SVG and rendered with headless Chrome. No
  emoji in them: the system emoji font on a Mac is Apple's artwork.
- `images/phoneScreenshots/`: from the emulator at 1080 × 1920 (Play refuses a long
  side more than twice the short one, and the emulator's own 1080 × 2424 is), with a
  demo-mode status bar. They show the Classic look at age 6 and Candy at age 5, English
  first with Hindi under it, and the parent settings last. The emoji in them are
  Android's Noto Color Emoji, under the Apache licence.

AI-generated assets: none.

## App content

- **Privacy policy:** the URL above. Play wants it inside the app too, so Settings has a
  Privacy policy button under About. Settings sit behind the parent gate, so a child
  never reaches the browser, and opening a link needs no internet permission.
- **Ads:** none.
- **App access:** everything works without an account or a login. The parent gate is a
  three-second hold, not credentials, so no instructions are needed.
- **Content rating (IARC):** category "Reference, News, or Educational". No violence,
  fear, sexual content, profanity, drugs, gambling, user-generated content, chat,
  location sharing or purchases. Expected result: Everyone / PEGI 3 / 3+.
- **Target audience: 5 and under, 6–8, 9–12.** Kid Reels is made for children, so
  saying "13 and over" would be false and is exactly what Play's review removes apps
  for. Choosing children brings the **Families policy**, which the app already meets:
  no ads or ad SDKs, no third-party SDKs at all, no data collected, no device
  identifiers, no internet permission, and the only link out (the privacy policy) sits
  behind the parent gate. The store listing makes no claim of being "certified" or
  "approved" by anyone.
- **Appeals to children:** yes (it follows from the target audience).
- **Teacher Approved:** Play considers apps in the Families programme on its own; there
  is nothing to apply for.
- **Data safety:** no data collected, no data shared. "Encrypted in transit" does not
  apply, since nothing is transmitted. Deletion: uninstalling removes everything.
- **Advertising ID:** not used. **Government, financial, health, news features:** none.
- **Permissions:** none. `aapt2 dump permissions` lists only AndroidX's internal
  `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`.

## Each release

1. Bump `versionCode` (Play refuses one it has seen) and `versionName` in
   `androidApp/build.gradle.kts`.
2. Add `fastlane/metadata/android/en-GB/changelogs/<versionCode>.txt` (500 characters at
   most).
3. Commit, then `./scripts/archive-apk.sh` on the clean tree.
4. Upload `local/releases/v<version>/kidreels-v<version>.aab` to the production track,
   with `kidreels-v<version>.apk.mapping.txt` as its deobfuscation file, and paste the
   changelog.
