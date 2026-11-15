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
- **And a bedtime.** Minutes alone let a child who saved them up watch at 11 pm, and
  screens just before sleep are what paediatric advice warns about most. Each child can
  have a bedtime, from which until 6 am a good-night reel ("brush your teeth, hug your
  teddy") ends the feed, with the same wind-down before it. A grown-up's "10 more
  minutes" there is wall-clock time, ten minutes before bed, not ten minutes of feed.
  6 am rather than later, so a child up at dawn is not told good night.
- **No streaks, badges, points or "just one more".** A right answer is celebrated on the
  spot and then it is over. Nothing accumulates that a child would be afraid to lose.
- **Nothing to watch passively.** Every reel needs a tap, a drag or a count.
- **No feeling of failure.** Wrong answers shake and fade; the word "wrong" never
  appears. Skipping a puzzle shows a short sad face, not a penalty.
- **A miss is explained.** "Try again" alone teaches nothing, so a wrong tap says why
  it is not the answer (`Quiz.whyNot`, one line per choice). Where the question already
  names the answer, the line names it too: "That is green, not blue. Find blue and tap
  it!", "The cat says meow! Who says moo?". Where finding the answer is the puzzle
  (counting, sums, number rows, patterns, the clock, sudoku) it gives the reason and a
  hint but not the answer: "4 is too few: there are more apples than that", "Each
  number is 2 more than the one before", "There is already a 3 in this line".

### Familiar, not a copy

The Classic look puts things where children expect them (a right-hand rail with a heart
and a speaker, a channel avatar and hashtags at the bottom, a spinning disc, a progress
line), because a reels-trained toddler accepts that layout at once. It does not use any
real app's name, logo, icons or colours: those would be trade-dress trouble and a Play
Store impersonation rejection, and a child recognises the gesture and rhythm, not the
logo. The progress line along the bottom shows today's time used.

## Modules

- `core/`: plain Kotlin, no UI. Content, reel templates, the feed engine, settings
  storage, child profiles, the time budget and the check-in rule. Unit-tested on the JVM.
- `ui/`: Compose Multiplatform, foundation only. No Material: the kid-facing look is
  entirely custom anyway, and one fewer library means one fewer version to keep in step.
  `AppState` (screens, children, the gate, bedtime, the feed's limits) is plain Kotlin
  over Compose state, so `AppStateTest` drives it on the JVM with a fake `Host` whose
  clock the test moves: the profile flows are checked without an emulator or rendering.
- `androidApp/`: implements `Host` (storage, clock, speech, sound, keep-screen-on).

Both shared modules also compile for desktop JVM. That target is not shipped; it is there
so the tests run without an emulator and so any Android-only import fails the build.

## Reels are data, made by templates

A `Reel` (`core/Reel.kt`) is plain data: a `Quiz` (prompt, a `Stage` picture, `Choice`s,
the answer, a spoken reveal) or an explore reel (`LetterCard`, `ShapeCard`, `SolidCard`,
`AngleLab`, `CurveLab`, `CountAlong`). The UI decides how each kind looks; the skin
decides the frame.

Reels are not stored; they are generated. Each `Template` (`core/Templates.kt`) knows
which ages it serves and turns a _level_ (an age, 2–12) and a _difficulty_ (1–5 within
that age) into concrete numbers, letters or shapes. "Addition at age 5, difficulty 2"
becomes, say, `3 + 2` with sticks. This is how there are effectively endless reels from
59 templates, and why the tests can check every template at every age and difficulty.

Wrong options are chosen to be instructive: near the answer (off by one, off by ten), the
clock with its hands read the wrong way round, the vegetable among fruits at the hard end.

### Drag games

A `DragGame` is pieces and places (`core/DragGames.kt`): each piece names the place it
belongs in (or none, for a wrong option) and, for every other place, why it does not go
there. A place is full at its `capacity`; the game is solved when every place is full.
One model, five games, drawn by `DragView` according to the `DragKind`:

| Game             | Ages | What changes with age and difficulty                                                                                   |
| ---------------- | ---- | ---------------------------------------------------------------------------------------------------------------------- |
| Feed the animals | 2–4  | 2 animals at 2, then 3                                                                                                 |
| Shape sorter     | 2–5  | 2, 3, then 4 holes; easy boards never hold look-alikes (square and diamond)                                            |
| Fill the basket  | 3–6  | how many to count out, with 2 or 3 spare                                                                               |
| Sort it out      | 4–8  | 2, 3, then 4 per basket; unlike groups first (fruit, vehicles), then near ones (birds, insects)                        |
| Missing piece    | 3–8  | 2×2 then 3×3; wrong pieces from other pictures, then from elsewhere in the same one, then the right piece turned round |

- **The missing piece is cut, not drawn.** The picture is an emoji drawn at the board's
  size, and every piece is the same drawing clipped to one square, so a piece always
  matches its gap exactly, at any screen size.
- **A wrong drop slides back and says why**, as a wrong tap does. A right one stays and
  says its line ("The dog loves the bone!"), or the count in the basket.
- **Pieces only move from where they are**: a drag that starts between pieces turns the
  page as usual, so the feed never feels stuck. A placed piece leaves its slot empty, so
  the others stay under the child's finger.
- **Tapping a piece names it**, like every other picture.

### Reasoning puzzles

The Navodaya entrance test (JNVST, taken at about 10) is mostly figure reasoning, and an
Indian parent knows it. `core/Reasoning.kt` makes its question types as ordinary quizzes,
with easier versions earlier:

| Puzzle               | Ages | Easy end → hard end                                                          |
| -------------------- | ---- | ---------------------------------------------------------------------------- |
| Same figure          | 4–8  | a mark changed, moved or missing → the mirror image and turned copies too    |
| Odd figure out       | 4–10 | one copy changed → three turns of a figure and its mirror image, turned      |
| Complete the pattern | 5–10 | four equal quarters → quarters that mirror each other                        |
| Figure series        | 6–10 | marks moving round the edge → turning as they go (from 8), at two speeds     |
| Mirror image         | 6–10 | always against the original, upside down and half-turned copies              |
| Analogy              | 7–10 | a quarter turn, half turn, mirror or flip, done to a second figure           |
| Complete the square  | 7–10 | the piece the same way round → shown turned, with its mirror image as a trap |
| Hidden figure        | 8–10 | small shapes in few lines → bigger shapes in more lines                      |
| Fold and punch       | 9–10 | one fold → two folds                                                         |

- **Figures live on a grid** (`core/Figures.kt`): marks on a 3 × 3 grid with a direction
  in steps of 45°, pieces and lines on a small lattice, holes in a 4 × 4 square. Turning,
  mirroring and comparing are exact, so the tests can prove that the answer is right and
  that no wrong option looks the same as it (`reasoningAnswersAreRightAndOptionsDiffer`).
  Two marks that look alike are equal (a square turned 90° is the same square), and a
  figure is only used if every turn and flip of it looks different, so a "mirror image"
  option never happens to match the original.
- **Every wrong option is a trap a child really falls into**, and its line names it: the
  original in a mirror question ("a mirror swaps left and right"), the slid holes in a
  paper fold ("mirrored across the fold, not slid along").
- **Black on paper white in every look**, like the clock: the differences are small.
  Every figure puzzle asks about a square (a figure card, a quarter of the pattern, the
  board, the paper), so its answers are square paper tiles too, not the wide cards other
  quizzes use: an answer shaped unlike the gap it fills is one more thing to puzzle over.
- Nine topics weigh half a reel each, so at 10 they are a good share of the mix without
  taking it over; a parent can pick one alone.

### Numbers, as the alphabet

The alphabet card works because it is the same every time: A is always the apple. Number
cards copy it (`core/Numbers.kt`): "1 · O-N-E · one · 1 elephant", and 1 is always the
elephant, 7 always butterflies, up to 20 bees. The English word is spelled in letter
tiles, as the alphabet card shows its letter, and spoken letter by letter from age 4;
Hindi is not spelled, since nobody learns एक as ए-क. Ages 3 to 6, up to 5, 10 and then
20, roughly nursery to class 1. Count-along stays separate: there the child does the
counting.

- **Times tables are tapped through, a line per tap**, with the next line waiting as
  "3 × 5 = ?" so a child can call it out before tapping. All ten lines at once would be
  a page to read, not something to do. In Hindi the voice chants the पहाड़ा as every
  Indian classroom does ("तीन एकम तीन, तीन दूनी छह, तीन तिया नौ"), not "3 गुणा 2":
  that rhythm is how parents learned it and how they will say it along. The quiz that
  asks for a product is "Multiplying" now; "Times tables" is this card.
- **Square numbers fill a square row by row**, saying the running total (5, 10, 15, 20,
  25), so "25 is a square number" is something seen: the tiles make a square. On a
  paper-white card like the clock, as 144 small blue tiles on a coloured background
  were hard to see. "5² = 25" joins "5 × 5 = 25" from age 9.
- **Even and odd start as pairs, not a rule.** Up to age 7 the number is shown as dots
  joined in pairs and the question is "even or odd?": one dot left without a partner is
  what odd means, and the wrong tap says so ("one dot has no partner"). From 8 it is
  which of four two- or three-digit numbers is even, and the hint is the last digit. The
  odd one out already asked about even and odd numbers from 7; this topic teaches it.
- **Primes from 10, with the traps that catch children.** "Which is prime?" with wrong
  options that are easy to rule out at first (even, or ending in 5), then odd numbers
  that only look prime (21, 27, 51, 91) and 1, which most children call prime. A wrong
  tap names a factor pair: "91 is 7 × 13".
- **The sieve is done, not shown.** A board of 1 to 50 (100 from 11); 1 starts crossed
  out, and tapping 2 crosses out its multiples, then 3, 5 and 7, until only primes are
  left and light up. Only the number to tap next does anything, and any other tap says
  which one that is, so a child cannot get lost in a hundred cells.

### Memory games

A fourth kind of thinking beside numbers, letters and figures, in a channel of its own.
Each game has a part to watch that runs by itself and a part to answer that waits.

| Game            | Ages | Easy end → hard end                                      |
| --------------- | ---- | -------------------------------------------------------- |
| Cup shuffle     | 3–12 | 2 cups, 1 or 2 slow swaps → 4 cups, up to 9 quick ones   |
| Find the pairs  | 3–12 | 2 pairs shown face up first → 8 pairs, no peek           |
| Copy the order  | 4–12 | 3 pads, 2 flashes → 4 pads, 7 quick ones                 |
| What's missing? | 3–12 | 3 pictures, a long look → 6 in a new order, a short look |

- **The watching part waits for the child.** The shuffle and the flashes start only on
  the reel on screen, after the spoken instructions, and start again from the top if
  the child swipes away and back: a shuffle half-seen is not a fair puzzle.
- **A swap is an arc, one cup over and one under**, never the same two cups twice
  running (which only undoes the move), except with two cups, where there is no other
  move. A wrong cup lifts to show it is empty.
- **A miss in find-the-pairs is a slip only when the partner had been seen.** Turning
  two cards nobody has seen yet is luck, not memory, so only a pair the child could have
  remembered counts against the topic's skill. A turned card says its name, which helps
  the memory.
- **The pads are colours, not sounds.** The app has a handful of sound effects, not a
  note per pad, and four colours a child can name are easy to tell apart. A wrong pad
  shows the whole order again.
- **What's missing hides its answers while the child looks.** The right answer was on
  screen a moment ago and the others never were, so showing them during the look would
  give it away. From 8 the hard end shows the rest in a new order instead of leaving a
  gap.
- Each weighs 0.75 in the mix: four new topics at once would otherwise crowd out the
  rest.

### Mazes

`core/Maze.kt` makes a maze by a random depth-first walk, so there is exactly one way
between any two squares: no loops to go round in, every square reachable. The goal is
the square furthest from the start along the paths, so the whole maze is used. From
3 × 3 at age 3 to 11 × 8 at 12 (a row more at the hard end).

- **Drag the runner, or tap the square next to it.** A drag must start on the runner,
  as with the drag games, so a drag anywhere else still turns the page. Tapping is for
  hands too small to drag along a path. The runner steps once the finger is half a
  square ahead towards an open side, and stops at a wall however far the finger goes.
- **Going back rubs out the trail**, so the line always shows the way from the start to
  where the runner is, not every wrong turn.
- **A dead end is not a wrong try.** Exploring is what a maze is, so a maze is always
  "right first time" to the feed's skill score.

### The body, drawn

A drawn child (`art/drawn/body.svg`) in short sleeves, shorts and bare feet, so elbows,
knees and toes all show. `core/Body.kt` holds where each of 23 parts is on the drawing,
in its own units: one spot per side and a radius, from which a tap is told apart (the
nearest spot it lands inside) and the part is ringed. A test taps every spot and checks
it lands on its own part, so two parts never sit too close to tell apart.

- **"Parts of the body"** (2 to 6) rings a part and names it, then with the child, and
  from 4 says what it does. Tapping any other part rings and names that one instead.
- **"Find it on the body"** (3 to 8): "Tap the knee!" A wrong part is ringed red for a
  moment and named: "That is the elbow. Where is the knee?".
- **Parts come by age**: eyes, nose, hands and feet at 2; elbows, knees, shoulders and
  toes at 4; wrists, ankles, chin and eyebrows at 6. The quiz leans on the newest ones
  for the age, so a six-year-old is asked about wrists, not noses.
- The emoji cards ("My body") stay: they show the tongue, the teeth and the brain, which
  a picture of a whole child does not.

### Adding a reel type

1. Add a `Topic` (and, if it needs new data, a list in `World.kt`).
2. Add a `Template` in `Templates.all` with its age range. If it is a quiz, it can reuse
   the existing `Stage`/`Choice` kinds and needs no UI work at all. Give it a `say`:
   the question in full, naming what is on screen, and what to do; and a `whyNot` for
   each wrong choice.
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
7. **Wind-down.** In the last two minutes before the limit or bedtime: difficulty 1, mostly explore.

Reels are generated two ahead of the one on screen, so a swipe never waits. The "after a
hard one, an easy one" rule uses the _difficulty_ of the previous reel, which is known
when it is made; outcomes (skips, struggles) affect the reels after that.

### A topic picked by the parent

A parent teaching the alphabet wants the alphabet, A to Z, not one letter every few reels.
Each child's settings page has a "Show only" row; with a topic picked (`Feed.focus`),
none of the rules above apply:

- **Only that topic**, made at the child's age, or the nearest age the topic has: the
  alphabet for an 8-year-old is the age-6 alphabet. Topics made for other ages are still
  offered, with their ages next to them.
- **From the beginning, in order.** Lists that have an order (alphabets, shapes, solids,
  curves, picture cards, count-along's 1, 2, 3…) start at their first item each time a
  topic is picked, on a cursor of their own, so the usual mix's place is not lost. With
  two languages, one alphabet is finished before the next starts.
- **Puzzles go from easy to hard**: difficulty 1 first, one step up after three right
  first time in a row (`Feed.FOCUS_STEP`), one step down after a skip. No retries: they
  would break the order.
- **One topic, not several**: "in order" means nothing across two topics.
- **Gone on the next start.** It lives in memory only. A parent who forgets it is on would
  otherwise leave a child on one topic for days. Android may also end the app in the
  background, which then counts as a new start; for a session-long choice that is fine.
  A chip on the feed says it is on. The time limit and wind-down still apply.
- **"Go to reels" once a topic is picked.** A parent picking a topic usually wants it
  now, and the feed was three Backs away. A button held under the topic list goes
  straight there. Not a dialog after each pick: that interrupts a parent comparing
  topics or undoing a mis-tap, and its "Cancel" would read as undoing the pick. Not
  closing on the pick either: a mis-tap would mean the grown-up gate again. Only for
  the child playing; on another child's page the pick waits for them.

## Children

One phone often serves two or three children of different ages, and a 3-year-old and an
8-year-old want nothing in common from the feed. Each child is a profile: a name, an
animal and a colour (`core/Profiles.kt`), plus everything that was the app's settings
before: age, languages, look, limit, check-in, slow speech, progress, today's time.

- **Storage.** Each child's keys sit under `p<id>.` and the phone's own under `device.`
  (the list of children, who played last). `Settings`, `Progress` and `TimeBudget` did not
  change: they are handed a `PrefixedStore` and read plain keys as before. On the first
  start of a version with profiles, every top-level key moves under `p1.`, so an update
  keeps all of 1.1's settings and progress. Removing a child removes their keys.
- **An animal, not a photo.** A toddler finds their own animal without reading their
  name, and nothing needs the camera. The name is optional; a child without one is called
  by their animal ("Tiger", "बाघ"). A new child starts on an animal and colour nobody
  else has.
- **24 pictures, four rows of six.** The unicorn, peacock, swan, flamingo, ladybird,
  hamster, chick and queen came after the first 16, so girls (and anyone) have more
  they would pick as their own. The queen is not an animal, so the picker says
  "Picture". New ones go at the end: a choice is saved by its name, so order does not
  matter to storage, and a full last row keeps the grid even.
- **One child: no question.** The app opens straight to the feed, as before profiles.
- **More than one: "Who's playing?"** at start, and again after 15 minutes in the
  background (`Profiles.ASK_AGAIN_MILLIS`). Android keeps the app alive for hours, so "at
  start" alone would hardly ever ask, while a shorter gap would ask a child who only
  glanced away. The question is spoken, the faces are big, and the picked child is
  greeted by name before their first reel. Back returns to whoever was playing.
- **The limit is not a sibling away.** Picking needs no gate, except when the child who
  played last has used up today's time and picks someone else: otherwise a child sent to
  bed by the goodbye reel could carry on as their brother. A tile with time up shows a
  moon. A grown-up opening the picker from the feed (the avatar beside the gear, behind
  the gate) can pick anyone.
- **Each child has a settings page.** Settings opens on the list of children; tapping one
  opens their page, whether or not they are the one playing. A new child copies the
  current child's languages, look and limit (siblings usually share them), and saving
  goes straight to their page. Switching who plays stays with the avatar on the feed.
- **Each child's feed is kept while the app runs**, so switching back finds the reel
  they left, mid-puzzle, with their "Show only" topic still on. Voices are the phone's,
  not a child's.

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
a week. At age 12 there is nothing to move up to, so it never appears.

**Ages go to 12, not 10.** Primes, square roots, grammar and photosynthesis are taught in
classes 5 to 7, and a 10-year-old who outgrew the feed had nowhere to go. Past 12 the
content would have to be a different app (algebra, essays) and the reels look would
start to feel babyish, so 12 is the top. At 11 and 12 the number topics get harder
(four-digit sums, two-digit times two-digit, cubes, primes and "each is the sum of the
two before" in number patterns, primes as the odd one out); the figure puzzles and the
explore reels carry on at their age-10 hardest, which the Navodaya test itself (taken
at 10 or 11) does not go past. Play's target audience was already every age group, so
nothing there changed.

Each child's page also has a note about the last 7 days: minutes, days played, puzzles and
the share right first time, then topics the child does well at (80% or more right first
time), finds hard (under 50%) and likes most. A topic needs 4 tries before it is called
either, so one unlucky puzzle is not a verdict. The running totals in `Progress` cannot
say how this week went, so `History` keeps the last 14 days separately, one key per day
and topic, and sweeps older days once a day. "Finds hard" says "try one together"
because the point is a parent sitting with the child, not a score.

## Parent gate

Press and hold for three seconds. Toddlers tap and swipe; they rarely hold one spot that
long. It guards settings, the check-in, the "10 more minutes" button, switching child from
the feed, and picking another child once the last one's time is up.

## Pictures and sound

- **Pictures are emoji first, drawings where emoji fall short.** Emoji from the system
  font cost nothing in the APK, so they stay the default; every emoji is from Emoji 13.1
  or earlier, the newest set Android 12 draws. But emoji show an eye, not an elbow, and
  have no bhindi or lauki at all, and the app should not teach a child with the wrong
  picture. So a word can name a drawing instead (`art:okra`): an SVG in `art/`, turned
  into Kotlin path data by `scripts/art.py` and drawn with the same Canvas as everything
  else, so no image library is added and every platform draws it the same. Each is a few
  KB. Sources, licences and what the script accepts are in `art/README.md`. Speech stays
  the phone's TTS for now; recordings may be bundled later where a voice falls short.
- **Every picture has a name** (`core/Words.kt`), so tapping any picture says "This is an
  elephant" / "यह हाथी है" in the reel's voice. One table covers every emoji the app
  shows, and `everyPictureHasAName` fails when a new one is added without a name: a
  picture that does nothing when tapped teaches a child that tapping is pointless. Answer
  cards are the exception: a tap there is the answer, and a wrong one already names what
  was tapped. A quiz can name its picture its own way: the first-letter quiz says the
  alphabet's word ("This is a ball"), not another name for the same emoji. The same table
  holds the picture cards' groups and facts; English and Hindi plurals are flagged
  separately, since "glasses" is plural and "चश्मा" is not.
- The 3D solids use a small renderer (`ui/draw/Solids.kt`): a mesh, rotation,
  perspective, flat shading, and back-face culling, which is enough because every school
  solid is convex.
- Speech is the phone's TTS engine, so no voice recordings ship. A missing voice means
  silence for that language, never Devanagari read by an English voice. Silence nobody
  can explain is the worst outcome, and settings is only seen by a parent who goes
  looking, so the feed also shows a banner for grown-ups ("no Hindi voice") that leads,
  past the gate, to the engine's install screen. "Not now" quiets it for a week, phone
  wide, since some engines have no voice for the language at all. The age check-in
  banner wins when both are due: it changes what the child sees.
- **The voice talks to the child, not at them.** Read out flat, the screen text ("कितने
  हैं?") sounded curt, and a generic opener in front of it ("Can you help me?") only
  added words. So each reel says what a parent beside the child would: what is on the
  screen and what to do with it. "How many butterflies are there on the phone? Count
  them and tell me!", "3 sticks and 2 sticks: how many sticks in all?", "Look: red,
  blue, red, blue. What comes next?". A quiz carries this as `say`, next to the short
  `prompt` shown on screen; explore reels build it from their data in `Reel.intro`.
  Things to count are `Thing`s with a Hindi plural and gender, because the sentence
  agrees with them: कितने सेब, but कितनी तितलियाँ. Alphabet words carry a gender for
  the same reason: "बतख किस अक्षर से शुरू होती है?". The speaker button repeats the same.
- **Tone is pitch and pace.** Engines have no emotion setting, so each spoken line
  carries a `Tone` that the platform turns into the way adults talk to toddlers. Speech
  is a little slower (0.85, or 0.7 with slow speech) and higher than the engine default.
  Praise is brighter, "try again" is softer and slower, and the goodbye reel is low and
  calm. Lines are separated by a 300 ms pause; "A", "for", "Apple" are three lines with
  500 ms between them, on the card and in the first-letter quiz's answer, so the letter
  stands apart from the word. Pushed further, the voice sounds cartoonish.
- Sound effects are a few notes synthesised into `AudioTrack`s at start-up.

## Small decisions worth knowing

- **Tablets get the phone layout, scaled to fit.** Phones keep the portrait lock, but
  Android 16 and later ignore it on large screens, so a tablet turns. Rather than a
  second layout, `screenScale` draws everything bigger or smaller until the phone layout
  (400 × 880 dp) fits both ways: 1.45× on a 10-inch tablet upright, where it otherwise
  showed phone-sized pictures in a sea of background, and 0.91× on its side, where the
  swipe hint ran into the reel. The reels cap their width, so the sides of a landscape
  tablet are the look's background, as in a reels app on a tablet. Phones (shorter side
  under 600 dp) always stay at 1.

- **Crash reports without the internet.** With no internet permission there is no crash
  service, and a crash on a family's phone would never reach anyone. `KidReelsApplication`
  keeps the last uncaught exception (app and Android version, phone model, time, stack
  trace, cut to 12,000 characters) under `device.crash`, written with `commit()` because
  the process is about to die. Settings shows it with "Copy report" and "Delete"; the
  parent decides whether it goes anywhere. Only one is kept: the latest crash is the one
  worth fixing, and an older one is likely fixed by the next update.

- **Words follow age.** Up to age 5 a child hears the words used at home, not the
  textbook's: "तिकोना" not "त्रिभुज", "नंबर" not "संख्या", "take away" not "minus".
  From 6 they hear the school word their class uses. A `Txt` can carry a simpler
  version (`txt(...).simpler(...)`), and the template picks one for the child's age
  (`Gen.words`, or `forAge` on a shape, solid, category or animal home). Lines every
  reel can say (praise, "try again") are kept simple for all ages; easy words
  suit a nine-year-old too. `littleChildrenHearHomeWordsNotSchoolWords` fails if a
  school word reaches a child of 5 or under.
- **Picture cards are small topics, not one**, so each can be shown on its own. Each
  weighs 0.4 in the mix, about three reels' worth together, so they do not crowd out the
  puzzles at ages 2 to 5. Computer parts are the exception in age, 4 to 8: a toddler has
  no use for "printer", and an older child sees a grown-up's computer every day. "What
  is it for?" (5 to 9) asks them the other way round, and a wrong tap says what the
  tapped part does, so every miss teaches a part. The plug's fact adds "never touch it
  with wet hands".
- **Capital letters only.** The alphabet card shows "A", not "A a": two shapes side by
  side read as two different letters to a child just starting. The first-letter quiz uses
  capitals too.
- **Option count follows age:** 2 choices at age 2, 3 at age 3, 4 from age 4. More is
  noise for a toddler.
- **Swipe threshold is a quarter page**, not the default half: small hands drag short
  and slow.
- **"Swipe up" has a band of its own** under the first four reels of a session, where
  it shows once a reel is done. Laid over the reel at a fixed height, it covered
  whatever was there: a shape's example pictures, a quiz's answer line. Those four
  reels give up 48 dp of height for it; the rest keep it all.
- **Settings are pages, a row each.** The first page lists the children, the phone's
  voices and About; a child's page has one row per choice, saying what is set now
  ("20 min · 12 min left today"), and each row opens that choice on its own page. One
  long page used to hold the playing child's choices with every topic below them, and
  it read as being about the whole phone; the topic list alone grows with every
  release. Topics are folded by channel and list the child's age unless asked for more.
  Age sits with the name and animal: it is who the child is, not a preference. The
  gear always opens on the first page.
- **The usual limits and bedtimes are one tap; any other is "Custom".** Custom sets
  hours and minutes with − and + buttons, in 5 minutes or an hour at a time: no keyboard
  or dial, which are fiddly one-handed. The limit goes up to 6 hours, past which a limit
  limits nothing and "No limit" says so honestly; a stored value past it is read as 6
  hours. A custom bedtime is from 4 pm ("no screens after school") to 11:55 pm, since
  after midnight would end the night it starts; one outside that is read as off.
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
