# Kid Reels

An ad-free Android app that looks and swipes like a short-video reels app, except every
reel is a tiny learning game: counting, adding, letters in English and Hindi, shapes,
3D solids, angles, curves, clocks, patterns, sudoku and the world around us.

## Why it exists

Many young children, including two-year-olds, will not eat unless a phone is playing
reels. Taking the phone away is a fight; leaving it means hours of content built to keep
them scrolling. Kid Reels keeps the part children love (the swipe, the full-screen
reel, the instant reward) and replaces the content with games that teach. It also helps
the parent end the session: there is a daily limit, and a calm goodbye reel when it is
reached.

No ads, no tracking, no account, no internet permission. Free and open source (MIT).

## What a child sees

- A vertical feed: swipe up for the next reel, as in any reels app.
- **Puzzles** to answer by tapping: how many, add, take away, times tables, which is
  more, what comes next, odd one out, what time is it, which colour, which shape, who
  says "moo", where does the fish live, big and small, first letter, one-cell sudoku.
  A right answer gets a chime, confetti and a spoken "well done". A wrong one gets a
  gentle shake and a reason, never "wrong": "That is green, not blue. Find blue and tap
  it!", or a hint where working it out is the puzzle ("4 is too few: there are more
  apples than that").
- **Explore reels** with nothing to get wrong: "A for Apple" / "अ से अनानास", shapes
  whose corners count themselves when tapped, 3D solids to spin with a finger, an angle
  to open and see named, curves that draw themselves, and count-along.
- Numbers under 20 come with matchsticks (तीलियाँ) under them, in fives, with tied
  bundles of ten, the way Indian classrooms count. Multiplication shows a dot array.
- Swiping past a puzzle without trying shows a short sad face, and the puzzle comes back
  a few reels later, a little easier.
- Every line is shown in up to three languages: the main one large, the others smaller
  below. The voice speaks the main language, except on a reel about another language's
  alphabet, which is spoken in that language. Tapping any line says it in its own
  language.

## What a parent sets

On first launch, and later in settings (behind a 3-second press-and-hold gate):

- **Languages**: up to 3, in order; the first is the main one. English and Hindi for now.
- **Age**, 2 to 10. Reels are made for that age, with an occasional one a level above or
  below, and an easy reel always follows a hard one. Up to age 5 the words are the ones
  children hear at home ("तिकोना", "take away"); from 6, the ones used at school
  ("त्रिभुज", "minus").
- **Look**: Classic (dark, laid out like the reels apps children know), Candy (bright
  gradients and bubbles) or Calm (soft colours, nothing extra).
- **Daily time limit**: 10 to 60 minutes, or none. In the last two minutes only easy reels
  appear and an "Almost done" chip shows; then a goodbye reel says it is time to eat and
  play, and the feed stops. A grown-up can hold a button there for 10 more minutes.
- **Age check-in**: every 1, 3 or 6 months (or never), a "For grown-ups" banner asks
  whether the child is ready for the next age, with how they did at this one.
- **Speech**: slow speech, and which voices are installed.

## Project layout

Kotlin Multiplatform, so that other platforms can be added later as a new shell rather
than a rewrite:

| Module        | What                                                                                                                                    | Depends on |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------- | ---------- |
| `core/`       | Reel model, content (alphabets, shapes, animals…), the 22 reel templates, the feed rules, settings, time limit, check-in. Plain Kotlin. | nothing    |
| `ui/`         | Every screen and reel, the three looks, the parent screens. Compose Multiplatform, foundation only (no Material).                       | `core`     |
| `androidApp/` | The Android shell: text-to-speech, sound effects, storage, clock.                                                                       | `ui`       |

`core` and `ui` both also compile for the desktop JVM, which runs the unit tests and
proves nothing Android-only has crept in. An iOS or desktop app would be a sibling of
`androidApp/` implementing the `Host` interface in `ui/…/Host.kt`.

See [`docs/design.md`](docs/design.md) for how the feed decides what comes next, how to
add a reel type or a language, and why things are the way they are.

## Speech

Speech uses Android's built-in `TextToSpeech`: Indian English (`en-IN`, falling back to
`en-GB`/`en-US`) and Hindi (`hi-IN`). No audio files are bundled. If a voice is not
installed, reels in that language stay silent rather than being read by the wrong voice,
and settings shows an **Install voice** button. The voice is slower and a little higher
than the engine's default, says what is on the screen and what to do ("How many
butterflies are there on the phone? Count them and tell me!"), and sounds brighter for
praise and softer for "try again". Sound effects are synthesised on the device at
start-up.

## Build

This Mac has no system JDK; Android Studio's bundled JBR works:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :core:jvmTest assembleDebug
./gradlew assembleRelease   # signed if the key is set up, else unsigned
./scripts/archive-apk.sh    # build, verify, keep the APK and AAB in local/releases/
```

Requires Android 12 (API 31) or newer.

### Signing

`assembleRelease` and `bundleRelease` sign the APK and the AAB when
`local/keystore.properties` exists:

```properties
storeFile=local/kidreels-release.jks
storePassword=…
keyAlias=kidreels
keyPassword=…
```

`local/` is gitignored, so neither the key nor its password reaches the repository.
Without the file, release still builds, unsigned.

**The key cannot be recovered.** Lose it and no sideloaded copy of Kid Reels can be
updated, because Android refuses an update signed by a different key. Keep a backup off
this machine. On Google Play it is only the upload key: Play signs what it delivers with
its own key, so a Play install and a sideloaded one cannot update each other.

### Archiving a release

`scripts/archive-apk.sh` builds the release APK and AAB and copies them into
`local/releases/v<version>/` as `kidreels-v<version>.apk` and `.aab`, each with a
`.sha256`, and the R8 `mapping.txt` that turns a minified stack trace from that build
back into names (upload it to Play with the AAB). The version is read from the built
APK, so the name cannot disagree with what shipped.

It is not wired into `assembleRelease`, so a build made while working cannot overwrite an
archived release. The script also refuses an unsigned APK, archives a dirty tree as
`v<version>-dirty-g<commit>/`, and never replaces an existing archive without `--force`.
Bump `versionCode` and `versionName` in `androidApp/build.gradle.kts` for each release.

### Google Play

The store listing (texts, icon, feature graphic, screenshots) is in
`fastlane/metadata/android/en-GB/`, and the privacy policy in [PRIVACY.md](PRIVACY.md).
What is filled in on Play Console, and why (target audience, the Families policy, data
safety): [docs/play-store.md](docs/play-store.md).

## Permissions

None that a user sees. The manifest asks for nothing; the only entry `aapt2 dump
permissions` lists is AndroidX's internal `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`,
which keeps the app's own broadcast receivers private. A `<queries>` entry for the
`TTS_SERVICE` intent lets Android 11+ bind to the speech engine.

## Licence

MIT. See [LICENSE](LICENSE).
