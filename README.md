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
  A right answer gets a chime, confetti and a spoken "well done"; a wrong one gets a
  gentle shake and "try again", never "wrong".
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
./gradlew assembleRelease   # unsigned unless keystore.properties exists
```

Release signing is optional: create `keystore.properties` (git-ignored) at the repo root
with `storeFile`, `storePassword`, `keyAlias` and `keyPassword`.

Requires Android 12 (API 31) or newer.

## Permissions

None that a user sees. The manifest asks for nothing; the only entry `aapt2 dump
permissions` lists is AndroidX's internal `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`,
which keeps the app's own broadcast receivers private. A `<queries>` entry for the
`TTS_SERVICE` intent lets Android 11+ bind to the speech engine.

## Licence

MIT. See [LICENSE](LICENSE).
