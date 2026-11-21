# Design

How Kid Reels works and why. The README says what it does; this is for whoever changes it.

## The goal shapes everything

The app exists to pull young children off addictive reels, not to be a better-made
addiction. So it copies the _form_ of a reels app (full-screen pages, vertical swipe, a
rail of buttons, a reward for every reel) and deliberately leaves out what makes those
apps hard to put down:

- **There is an end.** The parent's daily limit is a core feature, not a setting buried
  three screens deep. The last two minutes get only easy, calm reels, an "Almost done"
  chip and [Gappu](#gappu) yawning beside it, so the stop is expected; then a goodbye
  reel ("time to eat, play and rest", and Gappu goes to eat too) replaces the feed. An
  abrupt cut-off causes meltdowns; a wind-down does not.
- **And a bedtime.** Minutes alone let a child who saved them up watch at 11 pm, and
  screens just before sleep are what paediatric advice warns about most. Each child can
  have a bedtime, from which until 6 am a good-night reel ("brush your teeth, hug your
  teddy", with Gappu asleep already) ends the feed, with the same wind-down before it.
  A grown-up's "10 more minutes" there is wall-clock time, ten minutes before bed, not
  ten minutes of feed.
  6 am rather than later, so a child up at dawn is not told good night.
- **No streaks, badges, points or "just one more".** A right answer is celebrated on the
  spot and then it is over. Nothing accumulates that a child would be afraid to lose.
- **Nothing to watch passively.** Every reel needs a tap, a drag or a count. Fun reels
  bend this only as far as [Fun reels](#fun-reels) says: a payoff the child's own tap
  starts, and Copy me, where looking away from the screen to copy is the point.
- **No feeling of failure.** Wrong answers shake and fade; the word "wrong" never
  appears. Skipping a puzzle shows Gappu's sad face for a moment, not a penalty.
- **Some fun, on purpose.** Children used to Shorts found a feed of nothing but questions
  felt like homework, and a child who refuses the app goes back to Shorts. So there are
  songs and toys between the learning reels, more in the first weeks and fewer once a
  child has settled in, with the parent setting how many: see [Fun reels](#fun-reels).
- **Any puzzle can start over.** A puzzle reel has a start-over button beside the
  speaker, for a child who wants to play a game they liked again or who is stuck
  halfway. It brings back the same puzzle, says the question again (a memory game's
  timing waits for it) and keeps that it was solved: no sad face on the way out, and the
  second solve is celebrated but not counted again, or replaying an easy game would
  push its topic's difficulty up. Started over before the first solve, the puzzle can no
  longer be solved first time: a memory game watched twice is a second look. It is a
  button of its own rather than the speaker's job because children tap the speaker
  mid-game to hear the question, and that must not wipe the board.
- **A miss is explained.** "Try again" alone teaches nothing, so a wrong tap says why
  it is not the answer (`Quiz.whyNot`, one line per choice). Where the question already
  names the answer, the line names it too: "That is green, not blue. Find blue and tap
  it!", "The cat says meow! Who says moo?". Where finding the answer is the puzzle
  (counting, sums, number rows, patterns, the clock, sudoku) it gives the reason and a
  hint but not the answer: "4 is too few: there are more apples than that", "Each
  number is 2 more than the one before", "There is already a 3 in this line".

### Familiar, not a copy

The Classic look puts things where children expect them (a right-hand rail with a heart
and a speaker, plus a start-over arrow on puzzles, a channel avatar and hashtags at the
bottom, a spinning disc, a progress line), because a reels-trained toddler accepts that layout at once. It does not use any
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
75 templates, and why the tests can check every template at every age and difficulty.

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
- **A miss in find-the-pairs is a slip only when the child could have known better**:
  the first card's partner had been seen, or the second card had been seen and so was
  known not to match. A second card never seen is luck, not memory, even if its own
  partner was seen earlier: the child could not know what it was until it turned. Only
  a pair the child could have remembered counts against the topic's skill. A turned
  card says its name, which helps the memory.
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
knees and toes all show. `core/Diagrams.kt` holds where each of 23 parts is on the drawing,
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

### Words and grammar

A channel of its own (`core/Grammar.kt`), all built from the existing quiz and card
kinds, so it is content more than code.

| Topic                     | Ages | Language                  |
| ------------------------- | ---- | ------------------------- |
| Opposites                 | 5–10 | every chosen one          |
| Words in a sentence       | 5–9  | every chosen one (a card) |
| Naming, doing, describing | 6–10 | every chosen one          |
| One and many              | 5–8  | English or Hindi, its own |
| Male and female           | 6–9  | English or Hindi, its own |
| Rhyming words             | 4–7  | English only              |
| मात्राएँ                    | 5–7  | Hindi only                |

- **Grammar is about one language, so it is spoken in it**, as an alphabet is: "one
  mouse, many mice" is English grammar and "एक लड़का, कई लड़के" Hindi grammar, and
  neither translates. A template names its languages (`Template.langs`); the feed only
  makes it when the child has one of them, the topic list only offers it then, and a
  picked topic whose language is taken away is set aside rather than made in no
  language. Opposites and kinds of word are the same idea in every language, so they
  show every chosen language like any quiz.
- **Wrong plurals are the mistakes children make**: the usual ending where it does not
  belong ("foots", "कुर्सीयाँ"), an ending on an irregular plural ("mices",
  "childrens"), or no change at all. They are written out by hand, three for each word:
  made by rule, an ending turns into a real word as often as a mistake ("applies";
  पत्तियाँ are leaves too, घोड़ियाँ are mares) or into spellings no child would guess
  ("caies"). Never a real word from the lists either: लड़कियाँ is not a wrong plural of
  लड़का, it is girls.
- **Every miss teaches another pair**: "Sad is the opposite of happy. What is the
  opposite of sweet?", "Queen is the female of king. What about lion?".
- **School words from 6.** "संज्ञा, क्रिया, विशेषण" and "विलोम" are what a class uses; a
  five-year-old hears "नाम बताने वाला शब्द" and "उल्टा", as everywhere else.
- **A मात्रा on its own shows on a dotted circle** (ि), the way Hindi textbooks print it.

### Science

A channel of its own (`core/Science.kt`, `ui/reels/ScienceViews.kt`).

- **Processes are animations a child steps through**, not films: "Tap the sun", and its
  light runs to the leaves; "Tap the cloud", and water rises through the roots and the
  stem; then the air, then a leaf, and sugar runs down the stem while oxygen bubbles
  out. The water cycle goes sun, sky, cloud, river. Only the next thing to tap does
  anything, ringed so it can be found, as in the prime sieve, and each step keeps moving
  once started, so the whole process is on screen at the end. Every step's word is
  pinned by what it names. Nothing to watch passively, still.
- **Science words from the age a class uses them**: carbon dioxide, oxygen and
  photosynthesis from 10, evaporation and condensation from 9; before that the same
  steps in plain words ("the leaves take in air", "the water makes a cloud").
- **The plant is a diagram like the child** (`Diagram.PLANT`): the same ring-and-tap
  code, so "Parts of a plant" and "Find it on the plant" came for the price of a drawing
  and five spots. Its roots show through the cut-away soil, which is also where the
  photosynthesis water starts.
- **Float or sink, and magnets, answer with the picture.** Once answered, the thing drops
  into the tank and bobs at the top or settles on the bottom, or jumps up to the magnet
  or only wobbles, and the line says why ("A coconut has air inside"). Only iron and
  steel things are magnetic here: a "metal" spoon or coin may be stainless steel that a
  magnet does not pull, and the app should not be wrong in front of a child testing it.
- **Solid, liquid or gas** is three fixed choices with the rule for each: a wrong tap
  says that state's rule and asks if it fits.
- **Living or not** says what living things do (grow, eat, breathe) rather than
  listing animals.
- **Planets are cards**, in order from the Sun, with the Moon after the Earth: drawn
  balls, Saturn with its rings, each with its place ("the 4th planet from the Sun") and
  one fact.

### Fun reels

Two channels of their own, **Songs / गाने** 🎵 and **Play / मस्ती** 🎈 (`Channel.isFun`),
with songs and toys rather than something to learn (`core/FunReels.kt`, `core/Songs.kt`,
`ui/reels/FunViews.kt`). The plan they came from, with what was left for later, is
[plan-entertainment.md](plan-entertainment.md). The line they keep to: Shorts hold a
child with endless novelty, nothing that ends and rewards that come at random, so a fun
reel is something **the child makes happen**, that **ends by itself** or never escalates,
where **the same songs and toys come back** and **nothing is kept**.

| Reel          | Ages | What the child does                                                                        |
| ------------- | ---- | ------------------------------------------------------------------------------------------ |
| Tap the tune  | 2–7  | Every tap anywhere plays the next note of a rhyme; the word for it lights up               |
| Silly animals | 2–6  | Tap the nose and it sneezes, the tummy and it giggles, the feet and it hops, the ears flap |
| Bubble pop    | 2–7  | Pop bubbles, counting; from 4 only the red ones, 1 to 10 in order, or a word's letters     |
| Copy me       | 2–8  | Copy the drawn child: touch your nose, clap three times; from 5 sometimes Simon says       |

- **Not puzzles.** Every fun reel has `needsSolving` false: no skill score, no retry, no
  sad face when swiped past, not in the age check-in or the weekly puzzle numbers. A
  wrong bubble wobbles and the voice says what to look for; nothing is "wrong". One that
  ends by itself (a song played through, the last bubble, the last move) "solves" only
  to be cheered.
- **Tap the tune** plays our own synth, never a recording: a recording of a public-domain
  song is still someone's copyright. Four rhymes whose words and tune are both out of
  copyright: Twinkle Twinkle, Baa Baa Black Sheep (the same tune), Row Row Row Your Boat
  and Old MacDonald, one verse with a farm animal picked each time. A song is data: a
  note and a syllable per tap, a picture per line, that hops and spins at the end of its
  line. The voice says each line before it is played, so the child hears the words. Tap
  fast and the song goes fast; stop and it waits. It ends with the song, and start over
  plays it again. English only for now, so a child without English gets no songs; Hindi
  needs care, as many rhymes children know are film songs still in copyright.
- **The music box is the main new sound.** Fifteen notes, C major from the G below middle
  C to the G two octaves up, 400 ms each, synthesised once at start-up (about 260 KB of
  memory) in `androidApp/…/Sounds.kt` and played by `Host.playNote`. Songs, the bubbles'
  rising scale and the celebrations' fanfares all play on it, and every song is written
  in those notes (a test checks). Drum, clap, boing and sneeze are synthesised too: a
  sine falling in pitch, bursts of noise, a sliding pitch.
- **Silly animals are emoji on a drawn body.** A face emoji (🐮, 🦁, 🐷, 🐭, 🐱, 🐶) is
  drawn front-on by every font, so its ears and nose are where the tap zones expect them;
  a full-body emoji faces left in one font and right in another. The body under it is an
  oval drawn in the animal's colour with a lighter tummy and two feet, so there is a
  tummy to tickle and feet to hop on. Each speaks in a silly voice, `Tone.TINY` for the
  small ones and `Tone.GIANT` for the big. From 4 a tap names the part ("That's the
  cow's nose!" / "यह गाय की नाक है!"), and the animal is named and bridged like a picture
  card ("This is a cow!" · "cow means" … "गाय"). It never escalates: the hundredth sneeze
  is as funny as the first.
- **Bubbles have a fixed count**, 5 at 2, 7 at 3, 10 at 4 and 20 from 5, as count-along
  climbs, and then no new ones come: bubble popping is a classic time-waster. Each pop
  says the count and plays the next note up the scale, so popping makes a tune. The
  in-order games (numbers, a short English word's letters) hold their bubbles in place,
  bobbing, so the next one can be found; the others float up and come round again until
  popped.
- **Copy me waits for nothing once Go is tapped**: the point is to look away and copy,
  so the moves go on by the clock, timed to roughly how long each line takes to say (the
  engine does not report when it is done). The part is ringed on the drawn child from the
  body reels, which bounces, wobbles or hops with the move, and claps and pats are
  counted to a drum. **Sitting moves only**, unless a parent turns on Jumping games: the
  app is often used at meals, and a child told to jump at the table is a spilt plate.
  With it on, two standing moves come last and the game ends "Now sit down again!". In
  Simon says, one or two moves (never the first) come without "Simon says", the drawn
  child stays still, and the voice says "Gotcha!". Swiped away and back, it waits for
  Go again: a game half seen is no game.
- **The voice may be cartoonish here, and only here.** `Tone.TINY` and `Tone.GIANT` push
  pitch and pace past the four learning tones, which stay as they are. Engines clamp
  pitch differently, so a silly voice is less silly on some phones; it never sounds wrong.
- **The mascot, Gappu, is not a fun reel of his own yet**: he is in the celebrations, the
  sad face and the end of the day (see [Gappu](#gappu)). The silly animals stay emoji.

#### Bigger celebrations

A right answer used to end the same way every time: confetti from the middle and a
chime. Now **the things in the puzzle celebrate**: the counted apples jump one after
another and spin, a quiz's picture hops, the right answer card bounces, the fed animals
wag, the maze runner dances on the goal, the found pairs bow (`Modifier.moveOn` in
`ui/common/Motion.kt`: hop, squash and stretch, wobble, spin, shake, wag). Around that,
one of ten bursts (confetti, fireworks, balloons, a rainbow, stars, bubbles, streamers,
sparkles, and [Gappu](#gappu) cheering or dancing), each about 1.5 seconds with a short
fanfare of its own after the chime (`Burst` in `Motion.kt`).

- **Taken in turn, not at random**, and all the same size. A celebration that is
  sometimes bigger for no reason is a slot machine. Gappu's two are peers of the others,
  not a rare treat: two turns in ten, never one after the other (`MotionTest`).
- **In the wind-down, the plain chime and confetti**, and no puzzle picture moves: the
  last minutes get no new excitement.
- **Never in the way.** A burst takes no taps, so the next swipe is never held up, and no
  sparkle flashes more than once, well under three flashes a second.
- Celebrations belong to every puzzle, so they stay when fun reels are turned off.

#### Gappu

Gappu / गप्पू is the app's own character: a chubby blue-grey baby elephant with pink ears
and an orange cap (`core/Gappu.kt`, `ui/common/Gappu.kt`). Children used to Shorts love a
character, and we can use none of theirs (licences, and the trade dress
[Familiar, not a copy](#familiar-not-a-copy) warns of). An elephant has a big comic range,
every Indian child knows one, and it is not one of the profile animals, so no child's own
animal is the one that is sad or sleepy. Nine poses are drawn (`GappuPose`); where the art
came from is in `art/README.md`.

| Where                | Pose                   | What he does                                                                              |
| -------------------- | ---------------------- | ----------------------------------------------------------------------------------------- |
| A celebration        | cheer, dance           | Pops up low beside the puzzle, hops or sways on one foot twice, fades: 1.5 s              |
| A puzzle swiped past | sad                    | In the sad bubble instead of 🥺, for the same 1.8 s, with "Aww, you didn't try that one!" |
| The wind-down        | yawn                   | Small, beside "Almost done", stretching slowly, each stretch slower than the last         |
| The goodbye reel     | happy (waving), eating | Waves, then eats a banana: "Gappu is going to eat too" / "गप्पू भी खाना खाने जा रहा है"       |
| The good-night reel  | asleep                 | Curled up, "z"s rising: "Gappu is asleep already" / "गप्पू सो गया है"                        |

Surprised and sneeze (his cap flies off) are drawn for the phase 2 reels built on him
(silly mistakes, riddles). The silly animals stay emoji: their tap zones are fixed spots
on a front-on face and an oval body, and a full-body drawing whose sneeze moves its
outline does not fit them.

A loved character is a hook, so these rules are part of the design, not polish:

- **He is only ever used for the stop, never against it.** He yawns, eats and sleeps: a
  character the child likes going to rest is the gentlest sign that the day's reels are
  over. He never asks the child to stay, never says he will miss them and is never sad
  that they leave (`GappuTest` checks the goodbye lines). His one sad face is about a
  puzzle, for a moment, and it takes no taps.
- **Nothing to look after.** He needs no feeding or care, and nothing about him builds up:
  no outfits to unlock, no Gappu who is happier the more the child plays.
- **No bigger or rarer Gappu.** His celebrations are two of the ten taking turns, the same
  size and length as confetti, and in the wind-down he does not celebrate at all.
- **Calm at the end.** In the wind-down he is small and in the header, not on the reel or
  loud; on the goodbye reels the voice mentions him once, in the same low, slow tone.
- **A picture with a name.** Tapping him where he can be tapped (the wind-down, the
  goodbye reels) says "This is Gappu!" / "यह गप्पू है!" like every picture, in the
  reel's voice and with no bridge into the other language: a name is not a word to
  learn (`Word.proper`). In a celebration or the sad bubble he takes no taps, so he
  never holds up a swipe or takes an answer meant for the card under him.

### Adding a reel type

1. Add a `Topic` (and, if it needs new data, a list in `World.kt`).
2. Add a `Template` in `Templates.all` with its age range. If it is a quiz, it can reuse
   the existing `Stage`/`Choice` kinds and needs no UI work at all. Give it a `say`:
   the question in full, naming what is on screen, and what to do; and a `whyNot` for
   each wrong choice.
3. A new explore reel needs a `Reel` class and a view in `ui/reels/`, plus a branch in
   `ReelView`.
4. `TemplatesTest` picks it up automatically; add a check that the answer is right.
5. A fun reel is a topic in a fun channel (Songs or Play): the feed then gives it the fun
   slot and keeps it out of the learning mix and numbers by itself. Mark it `calm` if it
   suits the wind-down, and make it end by itself.

### Adding a language

1. Add it to `Lang`. Every `Txt` falls back to English, so the app runs at once.
2. Fill in its text: `txt(...)` takes one string per language, so the
   test `everyTextHasEveryLanguage` lists everything still missing. Where the
   language has a homely word for little children beside the school one, put it in
   `.simpler(...)`.
3. Add its alphabet to `Alphabets` (word, emoji, and the word in every other language).
4. Add its bridge frame to `Phrases.means` ("Apple means", "सेब यानी"). `Bridge.borrowed`
   only compares Latin letters with Devanagari, so a borrowed word in a new script is
   bridged until it learns that script's sounds.
5. Map it to a TTS locale in `AndroidSpeaker`.

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
8. **The fun slot.** One reel in N is a fun reel, N from the child's setting (below);
   fun reels never come from the mix itself.

Reels are generated two ahead of the one on screen, so a swipe never waits. The "after a
hard one, an easy one" rule uses the _difficulty_ of the previous reel, which is known
when it is made; outcomes (skips, struggles) affect the reels after that.

### The fun slot

How many fun reels a child gets follows the days they have played and the parent's
setting, **never how much they skip or how long they stay**: a feed that adds fun when a
child gets bored is how an engagement-optimised app works.

- **A fixed gap, not a random one.** With 1 in 3, two learning reels then a fun one,
  every time. A fun reel that might come at any moment is a reason to keep swiping; one
  that comes along steadily is not.
- **Only reels the child did something with move the count**: touched (a tap anywhere on
  the reel, a swipe is not one), or on screen for 5 seconds, since an explore card can be
  just listened to (`Feed.engaged`). A puzzle flicked past does not, so swiping fast does
  not bring the next fun reel sooner, and the Shorts habit of hunting for the good one
  does not pay.
- **Straight after a reel finished well, and only then** (`Feed.funAfter`): an explore
  reel engaged with, a puzzle right first time. The feed makes reels two ahead, decided
  before the child got there, so a fun reel made then could land after a skip and teach
  skipping, and counting only what had already happened would make 1 in 2 come out as 1
  in 4. So the fun reel is made at that moment and put next, on the page below the one on
  screen, which is not visible yet. No reel is thrown away for it (that would skip a
  letter of the alphabet): the reels made ahead just come one later. A struggle's
  breather is still made, one reel later.
- **The first reel of a session is fun while easing in**, since the first seconds decide
  whether a child stays.
- **Never two in a row**, and the four-reel wait before a topic comes back applies to fun
  topics too, so the same toy does not come round every few minutes.
- **Likes raise a fun topic within the fun slots**, by the same up to 1.5×, and never the
  number of fun reels, or a child hearting every song would turn the feed into songs.
- **Wind-down: only calm fun**, a gentle song or slow bubbles, never a silly animal or
  Copy me. An age with no fun reel that fits (9 and over for now; 8 in the wind-down)
  just gets its usual reels.
- **The end of the day does not move.** Fun reels use the same minutes, stop at bedtime,
  and the goodbye reel comes as before.

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
- **Fun topics can be picked too**, under their channels: a parent who wants just songs
  while a child eats can have them, Twinkle first. There is no fun slot then.
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
  (the list of children, who played last, the app's language). `Settings`, `Progress`
  and `TimeBudget` did not change: they are handed a `PrefixedStore` and read plain keys
  as before. On the first start of a version with profiles, every top-level key moves
  under `p1.`, so an update keeps all of 1.1's settings and progress. Removing a child
  removes their keys.
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
- **Each child has a settings page.** Settings opens on the list of children; tapping
  one opens their page, whether or not they are the one playing. A new child copies the
  current child's languages, look, limit and fun reels setting (siblings usually share
  them), and saving goes straight to their page. Automatic fun starts again from day 1 for
  them: easing in is about that child's first days. Switching who plays stays with the
  avatar on the feed.
- **Each child's feed is kept while the app runs**, so switching back finds the reel
  they left, mid-puzzle, with their "Show only" topic still on. Voices are the phone's,
  not a child's.

## Languages on screen and in the voice

Every `Txt` holds all languages. A reel shows its text in each chosen language, main one
first and large. Speech follows one rule: **speak the main language, unless the reel is
about another language** (its alphabet, its first-letter quiz), which is spoken in that
language (`Reel.voice`), **and bridge the one word the reel names into the others**.
Tapping any line speaks it in that line's own language.

- **The bridge is how Indian schools teach across languages**: "A for Apple, apple
  means सेब", "हाथी को अंग्रेज़ी में elephant कहते हैं". Before it, a child with English
  and Hindi saw सेब under "Apple" but never heard it: the second language was only on
  screen, for a child who mostly cannot read yet. Now, after a reel names its thing, it
  says it once more in each other language, in that language's own voice, in the
  child's language order (`Bridge.lines`): "A" "for" "Apple" · "Apple means" … "सेब" ·
  "Say it with me!" · "A" "for" "Apple". Where a teacher would put it: after the name,
  before "say it with me", once per reel, so reels do not get longer than one line.
- **Two lines, two voices.** "Apple means" is the English voice and "सेब" the Hindi
  one, 200 ms apart (`Bridge.PAUSE`, under the usual 300 so it sounds like one phrase
  across the change of voice), never Devanagari in an English line. Each half carries
  the other's language (`Say.alsoNeeds`), and the speaker drops both when either voice
  is missing: "Apple means" with nothing after it is worse than no bridge.
- **The frame is the reel's voice's**: "Apple means", "सेब यानी" (`Phrases.means`), word
  first, so the word just heard leads into the new one. The Hindi alphabet heard by an
  English-first child bridges in Hindi too ("अ से अनानास" · "अनानास यानी" … "pineapple"),
  as the card is spoken in Hindi. "यानी" is how Hindi glosses a word aloud and is as short
  as "means"; the textbook "सेब को अंग्रेज़ी में … कहते हैं" switches voice twice in one
  phrase and sounds chopped. Neither frame names the language, so a third language
  only adds its own frame.
- **What is bridged**: the alphabet, number, picture, body and plant, shape, solid,
  curve and planet cards and "words in a sentence"; a right answer that names one thing (a
  colour, the animal that made the sound, the animal's home, a shape, the first letter's
  word, a part found on the body or plant, the opposite, the kind-of-word word, solid,
  liquid or gas, "alive"); a tapped picture ("This is an elephant!" · "elephant means" …
  "हाथी"); and the silly animal, when it is introduced. The word is the one the child's
  age hears: तिकोना at 4, त्रिभुज at 7.
- **What is not**: a reel about one language's grammar (plurals, male and female,
  rhymes, मात्राएँ: they do not translate), counting, sums and number puzzles (the word is
  a numeral the voice already reads), figure reasoning, memory games, mazes, processes and
  the other puzzles whose answer is a reason rather than a name (float or sink, magnets,
  odd one out, sizes). Nor a song: it is in one language and sung in it, as an alphabet is
  spoken in its own, so Twinkle is English for a Hindi-first child too. Nor the
  first-letter quiz's picture before it is answered: "ball means गेंद" before "Ball starts
  with which letter?" sends the child to ग. Angles are named as the arm moves, too often
  to bridge each time.
- **Nor a word that is the same in both.** A `Txt.same`, a missing translation, or a
  borrowed word: "Bus means बस" and "कंप्यूटर यानी computer" teach nothing, and a quarter of
  the picture cards' words are borrowed. `Bridge.borrowed` tells them by sound rather than a
  list, so a new picture needs no flag: both words boiled down to their consonants (ph
  and फ़ are f, m and ं one nasal, vowels left out), equal, or one sound apart in a long
  word ("croissant" / "क्रोसाँ"). `BridgeTest` pins words it must catch and translations
  it must leave ("bucket" / "बाल्टी", "Parabola" / "परवलय").
- **With one language nothing changes**, and there is no setting: a parent who chose two
  languages chose this.

**What a grown-up reads is in the app's language; what is said or shown to the child is
in the child's.** The app's language is one setting for the whole phone
(`device.uiLang`, `AppState.uiLang`), English until a grown-up changes it, on the first
settings page or with the switch at the top of the welcome screen. It covers settings,
setup, the gate, the check-in and missing-voice banners and their cards, the weekly
note, the crash report, the "Show only" chip and the "hold for 10 more minutes" button.
The reels keep the child's languages: their text and voice, their buttons (Like, Again,
Start over), "Swipe up", the sad face, "Almost done", and the goodbye and good-night
reels. So do "Who's playing?" and the greeting, which are said to a child.

It used to be one: the main language ran the parent screens too. But the reels' language
is about what the child is learning, and a family that wants Hindi reels may well have a
parent who reads English better, or the other way round. The setting is the phone's,
not each child's, because one grown-up sets up every child. Existing installs start on
English as well rather than taking the playing child's main language: which child is
playing says nothing about who reads settings. The welcome screen used to show every
language, as none was chosen yet; now it shows one, with each language listed by its own
name at the top, so a parent who reads only Hindi can still find theirs.

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

Each child's page also has a note about the last 7 days: minutes, days played, puzzles,
the share right first time and how many were swiped past, then topics the child does well
at (80% or more right first time), finds hard (under 50%), often swipes past (half or
more) and likes most. A topic needs 4 tries before it is called any of these, so one
unlucky puzzle is not a verdict. The running totals in `Progress` cannot say how this
week went, so `History` keeps the last 14 days separately, one key per day and topic, and
sweeps older days once a day. "Finds hard" says "try one together" because the point is
a parent sitting with the child, not a score.

The note also counts the reels played (engaged with, as the fun slot counts them) and how
many of them were fun, with where the fun setting has got to: "112 reels, 31 of them fun
(songs and toys). Automatic: day 9 of 21, now 1 in 3." A parent should see the fun and not
be surprised by it. A song played at least twice in the week is named, "Their favourite
song this week: Twinkle Twinkle Little Star. Sing it with them!": singing with a child is
worth more than any reel, and it is the "try one together" idea again. Fun reels only add
plays and likes to `History` (a fifth number per day and topic, and a count per song), so
they stay out of the puzzle numbers; days stored before plays were kept read as none.

**A skip is not a wrong answer.** A skip counts as tried and not right first time, so
without its number a child who mostly scrolls on reads like one who gets everything
wrong. Every puzzles line says how many were swiped past, and "does well at" and "finds
hard" look only at the puzzles answered: "try one together" for a topic the child never
starts would send the parent after the wrong thing. A topic mostly swiped past gets its
own line instead, which may mean too hard to start or just not their thing; the parent
can tell which, the app cannot. Days stored before skips were kept read as none skipped,
and are out of the week within seven days.

Below the note, a line per age (the child's and the ones either side) gives the running
totals the same way: puzzles tried, the share right first time and the number swiped past.

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
  KB, except Gappu, the one picture worth the detail (30 to 60 KB of SVG a pose).
  Sources, licences and what the script accepts are in `art/README.md`. Speech stays the
  phone's TTS for now; recordings may be bundled later where a voice falls short.
- **Every picture has a name** (`core/Words.kt`), so tapping any picture says "This is an
  elephant" / "यह हाथी है" in the reel's voice, then bridges the name into the child's
  other language ("elephant means" … "हाथी"). A name is not bridged: Gappu is just
  "This is Gappu!". One table covers every emoji the app shows (and every pose of
  Gappu), and `everyPictureHasAName` fails when a new one is added without a name: a
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
  stands apart from the word; a bridge's "Apple means" … "सेब" has 200 ms, so it sounds
  like one phrase. Pushed further, the voice sounds cartoonish, which only the fun
  reels' two silly voices (`Tone.TINY`, `Tone.GIANT`) do.
- Sound effects are a few notes synthesised into `AudioTrack`s at start-up, and so is
  the fun reels' music box: see [Fun reels](#fun-reels).

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
  tapped part does, so every miss teaches a part. When the answer is the desktop or
  the laptop, the other is not a choice: both are computers with a screen, so a question
  for one fits the other too. The plug's fact adds "never touch it with wet hands".
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
- **Settings are pages, a row each.** The first page lists the children, the app's
  language, the phone's voices and About; a child's page has one row per choice, saying
  what is set now ("20 min · 12 min left today"), and each row opens that choice on its
  own page. One long page used to hold the playing child's choices with every topic
  below them, and it read as being about the whole phone; the topic list alone grows
  with every release. Topics are folded by channel and list the child's age unless
  asked for more. Age sits with the name and animal: it is who the child is, not a
  preference. The gear always opens on the first page.
- **Fun reels are a row on each child's page**, "Automatic · day 9 of 21, now 1 in 3",
  opening a page of three choices (decided by the owner, 2026-11-21: the share is the
  parent's, not a number in code):
  - **Automatic** (the default): from 1 in 2 down to 1 in 6 over 2, 3, 4, 6 or 8 weeks of
    play (3 by default), cut into four equal stages of 1 in 2, 3, 4 and 5, then settled at
    1 in 6. With three weeks: days 1–5 one in 2, 6–10 one in 3, 11–15 one in 4, 16–21 one
    in 5. A week is seven **days played** (a minute on the feed, as the weekly note
    counts), not seven calendar days, so a child who plays once a week is eased in at
    their own pace. It never goes back up by itself; "Start again" returns to day 1,
    after a holiday on YouTube, say. Settled is not zero: a few songs in a session keep
    the app a place a child likes coming back to. `FunDays` keeps the count apart from
    `History`, which forgets after two weeks while a taper can run for eight. Children
    who played before fun reels existed start at day 1, as a new child does: there was
    no record of their days to start from, and the taper is the gentle way in for them
    too.
  - **Fixed**, 1 in 2 to 1 in 6, held until changed; picking it starts from where
    automatic is now, so nothing jumps.
  - **Off**: learning reels only. Celebrations stay; they are part of every puzzle.
  - **Jumping games**, off by default: standing moves in Copy me.
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
