# Design

How Kid Reels works and why. The README says what it does; this is for whoever changes it.

## The goal shapes everything

The app exists to pull young children off addictive reels, not to be a better-made
addiction. So it copies the _form_ of a reels app (full-screen pages, vertical swipe, a
rail of buttons, a reward for every reel) and deliberately leaves out what makes those
apps hard to put down:

- **There is an end.** The parent's daily limit is a core feature, not a setting buried
  three screens deep. The last two minutes get only easy, calm reels and an "Almost done"
  chip, so the stop is expected; then a goodbye reel ("time to eat, play and rest")
  replaces the feed. An abrupt cut-off causes meltdowns; a wind-down does not.
- **No streaks, badges, points or "just one more".** A right answer is celebrated on the
  spot and then it is over. Nothing accumulates that a child would be afraid to lose.
- **Nothing to watch passively.** Every reel needs a tap, a drag or a count.
- **No feeling of failure.** Wrong answers shake and fade with "try again"; the word
  "wrong" never appears. Skipping a puzzle shows a short sad face, not a penalty.

### Familiar, not a copy

The Classic look puts things where children expect them (a right-hand rail with a heart
and a speaker, a channel avatar and hashtags at the bottom, a spinning disc, a progress
line), because a reels-trained toddler accepts that layout at once. It does not use any
real app's name, logo, icons or colours: those would be trade-dress trouble and a Play
Store impersonation rejection, and a child recognises the gesture and rhythm, not the
logo. The progress line along the bottom shows today's time used.

## Modules

- `core/`: plain Kotlin, no UI. Content, reel templates, the feed engine, settings
  storage, the time budget and the check-in rule. Unit-tested on the JVM.
- `ui/`: Compose Multiplatform, foundation only. No Material: the kid-facing look is
  entirely custom anyway, and one fewer library means one fewer version to keep in step.
- `androidApp/`: implements `Host` (storage, clock, speech, sound, keep-screen-on).

Both shared modules also compile for desktop JVM. That target is not shipped; it is there
so the tests run without an emulator and so any Android-only import fails the build.

## Reels are data, made by templates

A `Reel` (`core/Reel.kt`) is plain data: a `Quiz` (prompt, a `Stage` picture, `Choice`s,
the answer, a spoken reveal) or an explore reel (`LetterCard`, `ShapeCard`, `SolidCard`,
`AngleLab`, `CurveLab`, `CountAlong`). The UI decides how each kind looks; the skin
decides the frame.

Reels are not stored; they are generated. Each `Template` (`core/Templates.kt`) knows
which ages it serves and turns a _level_ (an age, 2–10) and a _difficulty_ (1–5 within
that age) into concrete numbers, letters or shapes. "Addition at age 5, difficulty 2"
becomes, say, `3 + 2` with sticks. This is how there are effectively endless reels from
22 templates, and why the tests can check every template at every age and difficulty.

Wrong options are chosen to be instructive: near the answer (off by one, off by ten), the
clock with its hands read the wrong way round, the vegetable among fruits at the hard end.

### Adding a reel type

1. Add a `Topic` (and, if it needs new data, a list in `World.kt`).
2. Add a `Template` in `Templates.all` with its age range. If it is a quiz, it can reuse
   the existing `Stage`/`Choice` kinds and needs no UI work at all. Give it a `say`:
   the question in full, naming what is on screen, and what to do.
3. A new explore reel needs a `Reel` class and a view in `ui/reels/`, plus a branch in
   `ReelView`.
4. `TemplatesTest` picks it up automatically; add a check that the answer is right.

### Adding a language

1. Add it to `Lang`. Every `Txt` falls back to English, so the app runs at once.
2. Fill in its text: `txt(...)` takes one string per language, so the
   test `everyTextHasEveryLanguage` lists everything still missing. Where the
   language has a homely word for little children beside the school one, put it in
   `.simpler(...)`.
3. Add its alphabet to `Alphabets` (word, emoji, and the word in every other language).
4. Map it to a TTS locale in `AndroidSpeaker`.

## How the feed picks the next reel

`core/Feed.kt`, in order:

1. **Retries.** A puzzle swiped past unsolved comes back 3–5 reels later, one difficulty
   lower, once.
2. **Breather.** After a hard reel (difficulty ≥ 4, or above the child's age) or a
   struggle (a skip, or two or more wrong taps), the next reel is easy: difficulty 1–2, at
   or one below the child's age.
3. **Mix of levels.** Otherwise 80% of reels are at the child's age, 10% one above (a
   gentle stretch, kept at difficulty 1–2) and 10% one below (practice that feels easy).
4. **Rhythm.** At most three puzzles in a row before an explore reel, at most two explore
   reels before a puzzle. A topic waits four reels before coming back when the age has
   enough topics for that.
5. **Adaptive difficulty.** Each topic has a skill score from −2 to +2: up a little on a
   first-try answer, down on struggles. Difficulty is 3 + skill ± 1. Moving to a new age
   resets it.
6. **Likes.** A liked topic comes up a little more often, up to 1.5×.
7. **Wind-down.** In the last two minutes before the limit: difficulty 1, mostly explore.

Reels are generated two ahead of the one on screen, so a swipe never waits. The "after a
hard one, an easy one" rule uses the _difficulty_ of the previous reel, which is known
when it is made; outcomes (skips, struggles) affect the reels after that.

## Languages on screen and in the voice

Every `Txt` holds all languages. A reel shows its text in each chosen language, main one
first and large. Speech follows one rule: **speak the main language, unless the reel is
about another language** (its alphabet, its first-letter quiz), which is spoken in that
language (`Reel.voice`). Tapping any line speaks it in that line's own language.

Parent screens use the main language only (the welcome screen shows all, since no
language is chosen yet).

## Age, and checking in on it

The parent picks an age rather than a birth date: it is what the reels are tuned to, and
a parent knows better than a calendar whether a child is ready. Every few months (3 by
default) a "For grown-ups" banner appears on the feed. It does not block the child; a
parent passes the gate to see "Is your child ready for harder reels?" with how often the
child answered first time at the current age, and can move up, stay, or be asked again in
a week. At age 10 there is nothing to move up to, so it never appears.

## Parent gate

Press and hold for three seconds. Toddlers tap and swipe; they rarely hold one spot that
long. It guards settings, the check-in and the "10 more minutes" button.

## Pictures and sound

- Pictures are emoji from the system font or shapes drawn in code: nothing to license,
  and the release APK stays under 2 MB. Every emoji is from Emoji 13.1 or earlier, the
  newest set Android 12 draws.
- The 3D solids use a small renderer (`ui/draw/Solids.kt`): a mesh, rotation,
  perspective, flat shading, and back-face culling, which is enough because every school
  solid is convex.
- Speech is the phone's TTS engine, so no voice recordings ship. A missing voice means
  silence for that language, never Devanagari read by an English voice.
- **The voice talks to the child, not at them.** Read out flat, the screen text ("कितने
  हैं?") sounded curt, and a generic opener in front of it ("Can you help me?") only
  added words. So each reel says what a parent beside the child would: what is on the
  screen and what to do with it. "How many butterflies are there on the phone? Count
  them and tell me!", "3 sticks and 2 sticks: how many sticks in all?", "Look: red,
  blue, red, blue. What comes next?". A quiz carries this as `say`, next to the short
  `prompt` shown on screen; explore reels build it from their data in `Reel.intro`.
  Things to count are `Thing`s with a Hindi plural and gender, because the sentence
  agrees with them: कितने सेब, but कितनी तितलियाँ. The speaker button repeats the same.
- **Tone is pitch and pace.** Engines have no emotion setting, so each spoken line
  carries a `Tone` that the platform turns into the way adults talk to toddlers. Speech
  is a little slower (0.85, or 0.7 with slow speech) and higher than the engine default.
  Praise is brighter, "try again" is softer and slower, and the goodbye reel is low and
  calm. Lines are separated by a 300 ms pause; "A", "for", "Apple" are three lines with
  500 ms between them, so the letter stands apart from the word. Pushed further, the
  voice sounds cartoonish.
- Sound effects are a few notes synthesised into `AudioTrack`s at start-up.

## Small decisions worth knowing

- **Words follow age.** Up to age 5 a child hears the words used at home, not the
  textbook's: "तिकोना" not "त्रिभुज", "नंबर" not "संख्या", "take away" not "minus".
  From 6 they hear the school word their class uses. A `Txt` can carry a simpler
  version (`txt(...).simpler(...)`), and the template picks one for the child's age
  (`Gen.words`, or `forAge` on a shape, solid, category or animal home). Lines every
  reel can say (praise, "try again") are kept simple for all ages; easy words
  suit a nine-year-old too. `littleChildrenHearHomeWordsNotSchoolWords` fails if a
  school word reaches a child of 5 or under.
- **Capital letters only.** The alphabet card shows "A", not "A a": two shapes side by
  side read as two different letters to a child just starting. The first-letter quiz uses
  capitals too.
- **Option count follows age:** 2 choices at age 2, 3 at age 3, 4 from age 4. More is
  noise for a toddler.
- **Swipe threshold is a quarter page**, not the default half: small hands drag short
  and slow.
- **Settings open over the feed**, not instead of it, so the child returns to the same
  reel mid-puzzle. Changing the age or languages is the exception: the reel on screen and
  the ones made ahead are made again, since they no longer fit the child, and the new reel
  is spoken when settings close.
- **Only on-screen feed time counts** toward the limit: not settings, not the gate, not
  the app in the background.
- **Hindi words for numbers in speech are left to the TTS engine** (it reads "3" as
  "तीन"), except count-along, which names numbers up to 20 itself.
- **Clock and sudoku draw on a paper-white card** in every look: they are read by fine
  detail, so contrast beats matching the skin.
