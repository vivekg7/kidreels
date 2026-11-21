# Plan: fun reels, to make the switch from Shorts easier

**Status:** phase 1 built (2026-11-21), not yet released; phases 2 and 3 planned. What
phase 1 does and why is now in [design.md](design.md#fun-reels); when a later phase ships,
its decisions move there too and this file says so.

## The problem

Internal testers report that children used to YouTube Shorts do not take to Kid Reels:
every reel asks them something, and next to Shorts that feels like homework. A child who
refuses the app goes back to Shorts, and then the app has helped nobody, however good its
puzzles are.

We cannot out-entertain YouTube, and should not try: being more gripping than Shorts is
the thing [design.md](design.md#the-goal-shapes-everything) says the app must not be. The
aim is narrower: **fun enough that the swap does not feel like a punishment**, with more
of it in the first weeks and less once the child has settled in. The parent's limit and
the wind-down still do the hard part, ending the session; fun only has to get the child
to start one.

## What kind of fun is allowed

The design already draws the line; this applies it to fun. Shorts hold a child with
**endless novelty, nothing that ends, and rewards that come at random**. Fun reels get
their delight from other things:

| Allowed                                                                                                    | Not allowed                                                                                          |
| ---------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| **The child makes it happen.** Tap the cow and it jumps; nothing happens on its own for long               | Cartoons, autoplay, anything to sit and stare at                                                     |
| **It ends by itself.** A song finishes, the bubbles run out, the tower falls                               | Toys that build up the longer you play, cliffhangers, "to be continued"                              |
| **The same songs and toys come back.** Toddlers want "Twinkle" again, and repeating a favourite is healthy | An endless stream of new surprises; surprise eggs and unboxing, which are that genre's worst example |
| **Delight now, nothing kept.** Laughs, music, silly voices, a dance                                        | Stickers to collect, unlocks, "solve 5 to earn a song", streaks, points                              |
| **Celebrations that vary but are all the same size**                                                       | A rare jackpot celebration: a reward that comes at random is a slot machine                          |
| **A character with jokes that come back**                                                                  | A pet that needs care, or a mascot that says "don't go, I'll miss you"                               |
| **Lively, then calm before the end**                                                                       | Flashing (never more than three flashes a second), sudden loud sounds, fast cuts                     |

Two more rules keep the fun from taking over:

- **The feed never learns to hold the child longer.** How much fun a child gets follows
  the days they have played and the parent's setting, never how much they skip or how
  long they stay. A feed that adds fun when a child gets bored is exactly how an
  engagement-optimised app works.
- **The end of the day does not move.** Fun reels count towards the limit like any reel,
  stop at bedtime like any reel, and in the wind-down only calm ones appear. Nothing here
  adds time, delays the goodbye reel or makes it easier to argue with.

### Where a current rule bends

| Rule today                                                             | With fun reels                                                                                                                                                                                                                                                                        |
| ---------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| "Nothing to watch passively. Every reel needs a tap"                   | A fun reel may have a short payoff to watch (a riddle's reveal, a magic trick, a tower falling), **started by the child's tap and under 8 seconds**. Movement reels have the child look away from the screen to jump or clap; that is the opposite of passive watching, so it is fine |
| Every reel teaches something                                           | Some fun reels are pure fun, and the catalogue says which. They are a minority even in the first days, and they are why the child stays for the reels that do teach                                                                                                                   |
| Puzzles and explore reels                                              | A third kind, fun. A fun reel can be a puzzle in costume (a riddle), but it never counts towards skill or the weekly puzzle numbers, has no sad face and never comes back as a retry: fun should not feel like a test                                                                 |
| The voice stays close to normal pitch ("pushed further, … cartoonish") | Fun reels may use a cartoonish voice on purpose (a tiny mouse, a giant elephant). Learning reels keep the voice as it is                                                                                                                                                              |

## Fun reels

What the app already has, so nothing here repeats it: confetti, a chime and a spoken "well
done" on every solve; hearts on a like; the sad face on a skip; animal sounds as a quiz;
count-along (tap each thing, hear the count); the memory games, mazes, drag games, science
animations, spinning solids, and corners that count themselves. Most of these are calm
and quiet. What is missing is **noise, silliness, music and bodies moving**.

Ages are where each fits; the Shorts-trained children this is for are mostly 3 to 8. Cost is S (a few
days), M (a week or two) or L (more, or work outside the code). APK cost is next to
the 2.7 MB of 1.3. Risk is to the "not an addiction" goal.

| Reel                                  | Ages | Learning                                     | Needs                       | Cost | APK          | Risk    | Phase |
| ------------------------------------- | ---- | -------------------------------------------- | --------------------------- | ---- | ------------ | ------- | ----- |
| 1. Bigger celebrations                | all  | none (rewards the learning around it)        | motion helper, synth        | S–M  | 0            | low     | 1     |
| 2. Silly animals                      | 2–6  | names, animal sounds, body parts             | motion helper, silly voices | S    | 0            | low     | 1     |
| 3. Bubble pop                         | 2–7  | counting, colours, numbers in order          | Canvas, note synth          | S    | 0            | low–med | 1     |
| 4. Tap the tune                       | 2–7  | rhymes, rhythm, reading along                | note synth, song data       | M    | ~2 KB/song   | low     | 1     |
| 5. Copy me (sitting)                  | 2–8  | body parts, counting, following instructions | TTS, beat                   | S    | 0            | low     | 1     |
| 6. The mascot                         | 2–9  | none by itself; carries 7, 8, 13             | 5–6 drawn poses             | M    | ~30 KB       | medium  | 2     |
| 7. Silly mistakes                     | 3–8  | whatever the question is about               | mascot, existing quiz       | S    | 0            | low     | 2     |
| 8. What's silly here?                 | 3–7  | world knowledge, reasoning                   | a scene stage               | S–M  | 0            | low     | 2     |
| 9. Riddles and पहेलियाँ                 | 4–10 | vocabulary, describing, reasoning            | existing quiz, writing      | S    | 0            | low     | 2     |
| 10. Drums and xylophone               | 2–8  | colours, high and low, rhythm                | note and drum synth         | S–M  | 0            | low     | 2     |
| 11. Peekaboo                          | 2–4  | object permanence, in, on, under, behind     | flap animation              | S    | 0            | low     | 2     |
| 12. Big voice, small voice            | 2–5  | big and small, high and low                  | silly voices                | S    | 0            | low     | 2     |
| 13. Dress up                          | 3–7  | clothes, body parts, following instructions  | free-drop drag              | S–M  | ~10 KB       | low     | 2     |
| 14. Colour it in                      | 2–7  | colour names, "colour the sun yellow"        | regions in `art.py`         | M    | ~4 KB/page   | low     | 3     |
| 15. Sparkle tracing                   | 3–6  | writing letters and numbers                  | stroke matching             | M    | 0            | low     | 3     |
| 16. Build it up, knock it down        | 2–6  | counting, big and small, patterns            | tumble animation, crash     | M    | 0            | low     | 3     |
| 17. Magic tricks                      | 4–12 | subtraction, number tricks, binary           | existing kinds, scripts     | S–M  | 0            | low     | 3     |
| 18. Tiny stories                      | 3–8  | vocabulary, order of events, sometimes none  | a small scene format        | L    | ~1 KB/story  | medium  | 3     |
| 19. Move like an animal, freeze dance | 3–8  | counting, animal movement                    | standing setting (see Q4)   | S    | 0            | low     | 3     |
| 20. Sung songs                        | 2–7  | rhymes                                       | recorded voice, audio files | L    | ~250 KB/song | low     | later |

### 1. Bigger celebrations

**What the child sees.** Today every solve ends the same way: confetti from the middle
and a chime. Instead, **the things in the puzzle celebrate**: the five counted apples jump
and spin, the fed dog wags and barks, the matched cards flip and bow, the maze mouse
dances on the cheese. Around that, one of about eight bursts (confetti, fireworks,
balloons rising, a rainbow sweep, stars spinning, bubbles), each with its own short
synthesised fanfare.

**Why it is fun.** Shorts-trained children expect a big payoff for a small action. Making
the puzzle's own pictures come alive also ties the delight to the thing just learnt, not
to a generic reward.

**Rules.** Picked in turn, not at random, so no burst is rarer than another; all about
1.5 seconds; never holds up the next swipe; none in the wind-down, where the plain chime
stays.

**Built with.** A small motion helper in `ui/common/` (hop, squash and stretch, wobble,
spin, shake, wag) on top of what `popOn` and `Confetti` already do, usable on any emoji or
drawing. Fanfares are note lists for the synth in `Sounds.kt`.

### 2. Silly animals

**What the child sees.** One big animal. Tap its nose and it sneezes ("A-a-a-CHOO!"), its
tummy and it giggles, its feet and it hops, its ears and they flap. Its sound comes out in
a silly voice ("MOOOOO!", tiny for a mouse, booming for a lion). Tapping anywhere else
makes it look round. From 4, the voice names the part tapped: "That's the cow's nose!".

**Why it is fun.** Cause and effect is what toddlers find funniest, and a sneeze never gets
old. It is the closest thing here to the instant payoff of a reel.

**Learning.** Animal names and sounds, and a body-part word on every tap.

**Built with.** Emoji (no new art) with the motion helper, a few tap zones per animal (the
emoji's layout is fixed, so nose and tail are at known spots), synthesised boings and
slide whistles, and two new `Tone`s for silly voices (12). A handful of animals at first.

**Risk.** Low. It never escalates: the hundredth tap is as funny as the first, not more.
The swipe hint shows after a few taps.

### 3. Bubble pop

**What the child sees.** Bubbles float up, each with a fruit or animal inside. Popping one
lets it out and says the count: "One! Two! Three!". Each pop plays the next note up a
scale, so popping makes a tune. After the age's count (5 at 2, 10 at 4, 20 from 5) no new
bubbles come and the last count is cheered: "Ten bubbles!". From 4, variants: pop only the
red ones; pop the numbers 1 to 10 in order; pop the letters of a short word.

**Learning.** Counting, colours, numbers in order. It is count-along's livelier cousin and
uses its `Thing`s and counts.

**Built with.** Canvas circles with a sheen, the existing `POP` sound, and the note synth.

**Risk.** Low to medium: bubble popping is a classic time-waster, so it has a fixed count
and stops making bubbles.

### 4. Tap the tune

**What the child sees.** A public-domain rhyme, its words on screen and a picture for each
line (a star for "Twinkle"). **Every tap anywhere plays the next note**, as on a music
box, and the word for that note lights up with a bouncing ball over it. Tap fast and the
song goes fast; stop and it waits. At the end of a line the picture does its thing (the
star twinkles, the sheep says baa). The voice says each line once before it is played,
so the child hears the words. Once the song ends the reel is done; start over plays it
again.

**Why it is fun.** The child plays a song they know, by themselves, and it sounds right
however they tap. This is the most important reel in the plan: songs are what children
most miss from YouTube, and here the song only moves while they do something.

**Learning.** Rhymes and their words, rhythm, and from 5 reading along with the lit words.

**Songs.** Only rhymes whose words and tune are both out of copyright, and only our own
playing of them: a recording of a public-domain song is still someone's copyright, so
nothing is copied from YouTube. English candidates: Twinkle Twinkle Little Star, Baa Baa
Black Sheep (same tune), Mary Had a Little Lamb, Row Row Row Your Boat, Old MacDonald
(with an animal to tap for each verse), Humpty Dumpty, Hickory Dickory Dock, Jack and
Jill, London Bridge, Itsy Bitsy Spider, Head Shoulders Knees and Toes (which links to the
body reels). Hindi needs more care: many "nursery rhymes" Indian children know are film
songs still in copyright, such as लकड़ी की काठी (_Masoom_, 1983) and चंदा मामा दूर के
(_Vachan_, 1955). Folk rhymes such as मछली जल की रानी है and आलू कचालू are likely free, but
each needs checking before it ships. Avoid modern arrangements of folk songs as well
(Baby Shark is Pinkfong's), and Wheels on the Bus, which may still be in copyright.

**Built with.** A song as data in `core/` (notes, a syllable per note, a picture per line,
in each language it exists in). The synth renders about two octaves of one music-box sound
at start-up (around 15 notes of 400 ms each at 22 kHz, about 260 KB of memory) and plays
one on demand. That **note player is the main new capability** of phase 1; bubbles,
drums, the xylophone and fanfares reuse it. `Host` gains a call to play a note.

**Risk.** Low: it ends with the song, and the same few songs come back.

### 5. Copy me

**What the child sees.** "Copy me! Touch your nose!" The drawn child from the body reels
(`art/drawn/body.svg`) does it, with the part ringed; then "Clap three times! One, two,
three!" to a drum beat; "Wiggle your fingers!", "Hands up high!", "Pat your tummy!". Four
or five moves, then "Great copying!". From 5, a "Simon says" round: "Simon says touch your
ears… Touch your toes!" (gotcha: Simon did not say). The child taps "Go" to start;
after that the moves wait for nothing, as the point is to look away.

**Why it is fun.** Silly actions with a grown-up voice to follow, and the child's body is
the toy.

**Learning.** Body parts (the same parts by age as the body diagram), counting, listening
and following an instruction.

**Built with.** The body diagram and its rings, TTS, the drum synth. **Sitting moves only**
in phase 1: the app is often used at meals, and a child told to jump at the table is a
spilt plate (see decision 4).

**Risk.** Low: it ends after a few moves, and it gets the eyes off the screen.

### 6. The mascot

**What it is.** One character of our own, drawn in five or six poses (happy, surprised,
silly, sad, sleepy, waving) as SVGs through `scripts/art.py`, a few KB each, and moved with
the same motion helper. Shorts-trained children love Peppa, Chhota Bheem and Motu Patlu;
we can use none of them (licences, and the trade-dress trouble
[Familiar, not a copy](design.md#familiar-not-a-copy) warns of), so we give them a
character of our own. Suggested: a baby elephant called **Gappu / गप्पू**: ears and trunk
give it a big comic range (a trunk that sneezes, sprays and trumpets), every Indian child
knows an elephant, and it is not one of the 24 profile pictures, so no child's own animal
is the one that gets things wrong.

**Where it appears.** Cheering in the celebrations (1); the silly one in "silly mistakes"
(7); doing the tricks (17); the short sad face on a skipped puzzle, which it takes over
from the plain bubble (sad about the puzzle, for a moment, never about the child
leaving). Its jokes come back: its hat blows off, it sneezes at pepper, it gets its trunk in
a knot.

**And it helps the stop.** In the wind-down, Gappu yawns and gets slower; on the goodbye
reel it waves, says it is going to eat too, and curls up to sleep on the good-night reel.
A character the child likes going to rest is the gentlest signal that the day's reels are
over, and it **makes the end easier, not harder**.

**Never.** It does not need feeding or care, never asks the child to stay, and is never
sad that they leave.

**Risk.** Medium, because a loved character is a hook. It is a safe hook only if it is
never used against the stop, so the rules above are part of the design, not polish.

### 7. Silly mistakes

**What the child sees.** "Gappu says 2 + 2 is 5! Is he right?" or "Gappu thinks the cow
says quack. What does the cow say?" The child corrects him with the usual quiz choices;
he slaps his forehead and laughs.

**Learning.** Whatever the question is about; any quiz template can be turned into one. Children
love correcting a silly grown-up, and checking an answer is a skill of its own.

**Built with.** The existing `Quiz`, with the mascot as the stage and a wrong claim in
`say`. Cheap once the mascot exists.

### 8. What's silly here?

**What the child sees.** A scene with one thing wrong: a fish in a tree, a car with square
wheels, an umbrella in the bath, a cat barking. "Something is silly! Tap it!". The silly
thing wobbles and the voice says why: "Fish live in water, not in trees!".

**Learning.** World knowledge and reasoning, ages 3 to 7: the "absurd pictures" every
pre-school workbook has.

**Built with.** A new `Stage` that places several emoji on a simple background, and a
hand-written list of scenes with the silly thing and the reason. It is a quiz, so wrong
taps already have their `whyNot` ("The bird in the tree is right where it lives!").

### 9. Riddles and पहेलियाँ

**What the child sees.** "I am yellow and long, and monkeys love me. What am I?" with
picture choices; the answer jumps up with a boing and Gappu laughs. From 7, riddles
with a twist ("What has hands but cannot clap? A clock!") and, from 8, a joke to reveal
with a tap.

**Learning.** Vocabulary and describing things; pure fun at the joke end, and the plan
says so. Hindi पहेलियाँ are a tradition of their own, not translations, so each language gets
its own list.

**Built with.** The existing quiz; content is the cost (writing, then a native speaker's
check, as for all Hindi text).

### 10. Drums and xylophone

**What the child sees.** Four big pads (drum, clap, shaker, bell) or eight coloured bars.
Every tap plays its sound and the bar bounces; the voice names it once ("Red! The red one
is high!"). Free play; the swipe hint shows after a few seconds.

**Learning.** Colours, high and low, rhythm. It also lets the memory game "copy the order"
give each pad a note, which [design.md](design.md#memory-games) left out only because the
app had too few sounds.

**Built with.** The note player plus synthesised drums: a sine falling in pitch for the
drum, a burst of noise for the clap and shaker. No audio files.

### 11. Peekaboo

**What the child sees.** A blanket, a door or a bush. Tap it, it lifts: "Peekaboo!" and an
animal waves. From 3, "Where is the cat?" with three hiding places; from 4, the answer
names where: "Under the table!".

**Learning.** That things still exist when hidden (2-year-olds love that), then in, on,
under and behind.

**Not a surprise egg.** The same friends hide each time; the fun is finding them, not
what you might get.

### 12. Big voice, small voice

**What the child sees.** A mouse, a cat and an elephant. Tap each to hear "Hello!" in its
voice: tiny and quick, middling, deep and slow. Then "Who said that?" from the voice
alone.

**Learning.** Big and small, high and low.

**Built with.** Two new tones (`TINY`, `GIANT`), mapped in `AndroidSpeaker` to pitch and
pace past today's limits. Engines differ in how far they go, so it needs testing on the
Google and Samsung engines before it ships.

### 13. Dress up

**What the child sees.** An animal or Gappu, and a row of hats, glasses, a moustache, a
scarf, shoes. Drag them on anywhere and it reacts (a hat on its foot gets a giggle). From
4, "Put the glasses on the cow's eyes!".

**Learning.** Clothes words (the "Clothes" picture cards' list), body parts, following an
instruction.

**Built with.** The drag games' pieces, with drops that stick anywhere for free play.

### 14. Colour it in

**What the child sees.** A simple outline (a fish, a house, a rocket). Tap a colour, then
tap a part to fill it: no colouring inside the lines, which small hands cannot do yet.
When every part is filled, the picture comes alive (the fish swims, the rocket takes off).
From 3, "Colour the sun yellow!".

**Learning.** Colour names, said when a colour is picked; following an instruction.

**Built with.** SVGs with named regions; `scripts/art.py` keeps the region names. Nothing
is saved: a gallery would be a collection to grow.

### 15. Sparkle tracing

**What the child sees.** A letter or number in dots. Drag along it and a sparkly trail
follows; finished, the letter dances and says itself ("A for Apple!").

**Learning.** Writing letters and numbers, the one thing the alphabet cards do not teach.

**Built with.** The maze's drag along a path, with a tolerance for wobbly lines, and a
stroke order for each letter and digit (English and Hindi).

### 16. Build it up, knock it down

**What the child sees.** Tap to drop blocks onto a tower, counted aloud ("Five blocks!").
At the age's number, a button (or Gappu's sneeze) knocks it down in a crash. From 4: big
blocks at the bottom, or a colour pattern to build.

**Learning.** Counting, big and small, patterns. The knock-down itself is pure fun.

**Built with.** Canvas blocks and a worked-out tumble (each block's arc and spin), not a
physics engine; a crash from synthesised noise.

### 17. Magic tricks

**What the child sees.** For 4 to 6: "Five apples. Abracadabra!", tap the wand, one
vanishes in a puff: "Four apples!" (taking away). For 7 to 9: "Think of a number, add 3,
double it, take away 6, halve it… you are back where you started!" with the steps shown. For
9 to 12: the magic number cards ("Is your number on this card?" four times, and the app
names it), and then how it works (binary).

**Learning.** Taking away, number sense, binary; the "how it works" is the lesson.

### 18. Tiny stories

**What the child sees.** Four to six pictures, each waiting for a tap: tap the door to
open it, pick whether Gappu takes the bridge or the boat. Spoken, under a minute, always a
happy ending, **finished within the reel**: no "next time".

**Learning.** Vocabulary and the order of events, sometimes a small lesson (sharing,
saying sorry), sometimes none.

**Built with.** A small scene format (emoji placed on a background, a line to say, what to
tap, which scene comes next); the format is M, each story is writing. L in all, and the
part most likely to turn into a pull for "what happens next", hence phase 3.

### 19. Move like an animal, freeze dance

**What the child sees.** "Jump like a frog! One, two, three, four, five!" with the frog
hopping to the beat; "Flap like a bird!", "Stomp like an elephant!". Freeze dance: a tune
plays, the child dances, it stops, Gappu freezes, and so does the child.

**Learning.** Counting and animal movement; mostly it gets a child off the sofa.

**Built with.** The same as Copy me. Standing moves only where a parent turns them on
(decision 4).

### 20. Sung songs

A real voice singing, with the words bouncing in time and something to tap on the beat.
TTS cannot sing, and a synthesised tune with a spoken line before it is a stand-in. If
testers say Tap the tune is not enough, this is the next step: our own recording (a
family member's voice or a paid singer, with a written licence to us), Opus mono at 32
kbps, about 250 KB a minute, played by Android's own player with no library. Ten songs
would add about 3 MB to a 2.7 MB app, so it is held until it proves its worth (question 2).

### Considered and left out

- **Anything using the microphone or camera**: blowing out candles, a parrot that repeats
  you in a silly voice, AR. Kids love them, but the app has no permissions at all, and
  that matters more to the parents who choose it ([PRIVACY.md](../PRIVACY.md)).
- **Tilting the phone** (a marble run, a ball to roll). The accelerometer needs no
  permission, but it means waving a phone around at the dinner table.
- **Video or cartoons**, even short ones: nothing to do, and a big APK.
- **Surprise eggs, unboxing, mystery prizes, collections, daily rewards**: random rewards
  and things to hoard, which is the hook we are getting the child off.
- **Licensed characters and songs**: licences, trade dress and Play's impersonation rules.

## The feed

`core/Feed.kt` gets a fun slot. Fun templates are flagged (`Template.fun`, and
`Template.calm` for the ones that suit the wind-down) and grouped in two new channels, so
the avatar and the spinning disc show something new: **Songs / गाने** 🎵 and **Play /
मस्ती** 🎈.

1. **Every Nth reel is fun**, N set by the child's fun setting (below). The gap is fixed,
   not random: a fun reel that might come at any moment is a reason to keep swiping, while
   one that comes along steadily is not.
2. **Only reels the child did something with move the count.** A reel touched, or on
   screen for 5 seconds (an explore card can be just listened to), counts; a puzzle
   flicked past does not. So swiping quickly through puzzles does not bring the next fun
   reel sooner, and the Shorts habit of hunting for the good one does not pay. The UI
   tells the feed when a reel counted, as it already reports outcomes.
3. **No fun reel for a struggle.** The breather after a skip or two misses stays an easy
   learning reel, as now. A fun reel after a skip would teach skipping.
4. **The first reel of a session is fun while easing in**, since the first seconds decide
   whether a child stays. After that, fun reels follow rule 1.
5. **Rhythm.** A fun reel ends a run of puzzles, as an explore reel does. Two fun reels
   never come in a row, and the four-reel wait before a topic comes back applies to fun
   topics too, so the same toy does not come round every few minutes.
6. **Likes** raise a fun topic within the fun slots, by the same up-to-1.5× as other
   likes, and **never raise how many fun reels there are**. Otherwise a child hearting
   every song would turn the feed into songs.
7. **Wind-down:** only calm fun (a slow tune, slow bubbles, peekaboo), no new excitement;
   the celebrations go back to the plain chime; Gappu yawns. The goodbye and good-night
   reels are the same, with Gappu waving or asleep.
8. **Time limit, bedtime and "10 more minutes"**: unchanged. Fun reels use the same
   minutes as every reel.
9. **Fun reels are outside the learning numbers.** No skill score, no retries, no sad face
   on a skip, not in the age check-in's per-level stats or the weekly note's puzzle line.
10. **"Show only"** offers fun topics under their channels like any other. A parent who
    wants just songs while a child eats can pick them; as now, it is gone on the next start.

### Easing in

A new child starts with a lot of fun, which tapers to a settled share, so the switch is
gentle and the app ends up mostly learning. The parent picks how long the taper takes (see
[What the parent sets](#what-the-parent-sets)): the span is cut into four equal stages,
1 in 2, 1 in 3, 1 in 4 and 1 in 5, and after it the share stays at 1 in 6. With the
default of three weeks (21 days played):

| Days played | Fun reels       |
| ----------- | --------------- |
| 1–5         | 1 in 2          |
| 6–10        | 1 in 3          |
| 11–15       | 1 in 4          |
| 16–21       | 1 in 5          |
| 22 on       | 1 in 6, settled |

- **Days played, not calendar days**, counted as the weekly note counts them (a minute on
  the feed). A child who plays once a week is eased in at their own pace, not tapered to
  "settled" before they have settled.
- **It never goes back up on its own.** Only a parent can raise it, or start easing in
  again (after a holiday on YouTube, say). See the first rule above.
- **"Settled" is not zero.** At 1 in 6, a 20-minute session has a few songs and toys in
  it. For a 3-year-old, singing a rhyme is learning; for everyone, it keeps the app a
  place they like coming back to, with nothing that makes them feel they must.
- At 9 and over there are fewer fun templates (riddles, tricks, music, jokes), so a slot
  that finds no fun template for the age gets a usual reel instead.

A test makes 2,000 reels per stage and checks the share, and checks that fast skips do not
change it, that likes do not change it, that the wind-down has no lively fun, and that
"Show only" is unaffected.

## What the parent sets

Each child's page gets a **Fun reels** row, in the style of the others: one line saying
what is set now, opening a page of its own with two ways to set it (Vivek's choice,
2026-11-21: the share is tuned by the parent, not fixed in code).

- **Automatic** (the default): from 1 in 2 down to 1 in 6 over a span the parent picks,
  2, 3, 4, 6 or 8 weeks of play (3 by default), as in [Easing in](#easing-in). The row
  says where it has got to: "Automatic · day 9 of 21, now 1 in 3". The page says why:
  lots of fun at first so the switch from other apps is easy, then less. A button starts
  it again from 1 in 2 (after a holiday on YouTube, say).
- **Fixed**: 1 in 2, 1 in 3, 1 in 4, 1 in 5 or 1 in 6, held until changed, for a parent
  who knows what their child needs; or **Off**, learning reels only. The celebrations stay
  either way; they are part of every puzzle.
- **Jumping games** (off by default), a switch on the same page: standing and jumping
  moves in "Copy me" and, later, freeze dance. Sitting moves are always in.

A week is seven days played, not seven calendar days, for the reason under Easing in. A
new child copies the current child's setting, as with languages and look, except that
automatic starts again from day 1 for them. Existing children start automatic from day 1
on the update.

### The weekly note

One more line, so a parent can see the fun and is not surprised by it:

> 112 reels, 31 of them fun (songs and toys). Automatic: day 9 of 21, now 1 in 3.

And, since singing with a child is worth more than any reel:

> Their favourite song this week: Twinkle Twinkle. Sing it with them!

This is the "try one together" idea again: the app pointing the parent at something to do
with the child, away from the phone. Fun reels need a count of their own in `History` (one
field per day and topic, for plays) so they stay out of the puzzle numbers.

## Phases

### Phase 1: a small first batch, to test with the internal testers

**Built** (2026-11-21), except the Hindi rhyme; how it works is in
[design.md](design.md#fun-reels). Where it differs from this plan or filled in a detail:

- A fun reel is made at the moment a reel is finished well (an explore reel engaged with,
  a puzzle right first time) and put straight after it, rather than being "every Nth
  reel" when the feed makes reels two ahead: made ahead, it could land after a skip, and
  counting only what had happened would turn 1 in 2 into 1 in 4. A reel counts as engaged
  on a tap (not a swipe) or 5 seconds on screen. A skip earlier does not hold back a fun
  reel after a later reel went well; the skip's breather comes one reel later.
- `Template.isFun` comes from the channel (`fun` is a Kotlin keyword), with `Template.calm`.
- No fun reels at 9 and over yet, and none in an 8-year-old's wind-down (Copy me is not
  calm): those slots get usual reels. Songs are English only, so a child without
  English gets the Play channel only.
- Silly animals are face emoji (🐮 🦁 🐷 🐭 🐱 🐶) on a drawn body with a tummy and feet:
  full-body emoji face different ways in different fonts, so fixed tap zones would miss.
- Bubbles: 7 at age 3; from 4 a colour game (5 or 8 to pop), 1 to 5 in order at 4 and 1
  to 10 from 5, a short English word's letters from 5. Old MacDonald is one verse a
  reel, with a farm animal picked each time. Songs' Hindi titles are transliterated.
- Celebrations: the fanfare follows the existing chime rather than replacing it; in the
  wind-down it is today's confetti and chime. The pictures that celebrate: a quiz's
  picture, the counted things, the right answer card, the fed animals, the maze runner,
  the found pairs.
- Copy me: standing moves (with Jumping games) come last, then "Now sit down again!".
  Simon says in Hindi is "साइमन कहता है", for the native speaker's check.
- Start over is on fun reels too (it plays the song again). The weekly note's reels are
  the reels engaged with; a favourite song needs two plays in the week. Picking Fixed
  starts from the share automatic is at.
- Gappu is not in phase 1 (its art is being designed); nothing assumes a mascot.

The cheapest things with the most delight, and the feed change that lets them in:

1. **The fun slot and easing in** (feed rules above), the Fun reels row and the weekly
   note line. M.
2. **Bigger celebrations** (1): every puzzle feels better, including the ones a child
   already has. S–M.
3. **Silly animals** (2), with the two silly-voice tones from (12). S.
4. **Bubble pop** (3). S.
5. **Tap the tune** (4) with four English songs (Twinkle, Baa Baa Black Sheep, Old
   MacDonald, Row Row Row Your Boat) and one Hindi folk rhyme once its source is checked. M,
   mostly the note player. **TODO:** the Hindi folk rhyme (मछली जल की रानी है or आलू
   कचालू), once its words and tune are confirmed out of copyright; it goes in
   `core/Songs.kt` as another `Song` with `Lang.HI`.
6. **Copy me**, sitting moves only (5). S.

New capabilities: the note player and drum sounds in `Sounds.kt`, the motion helper, two
tones. APK cost: a few KB. No new permission, no audio file, no library.

**What to ask the testers** after a week or two, since the app has no analytics:

- Does the child ask for the app, or accept it when offered instead of Shorts?
- Which reels do they laugh at, ask for again, or skip?
- Does the goodbye reel still end the session as calmly as before? If the fun makes the
  stop harder, the batch has failed whatever else it did.
- Do parents read the fun line in the weekly note, and does the share feel right?

### Phase 2: the mascot and humour

Gappu (6) and the reels built on him: silly mistakes (7), Gappu in the wind-down and the
goodbye reels, then what's silly (8), riddles and पहेलियाँ (9), drums and xylophone (10),
with notes in "copy the order", peekaboo (11), big voice, small voice (12), dress up (13).
Mostly content once the mascot is drawn.

### Phase 3: making and stories

Colour it in (14), sparkle tracing (15), build it up (16), magic tricks (17), tiny stories
(18), and standing movement with the parent setting (19). Each is worth shipping on its
own; stop where the testers stop asking.

### Later, only if needed

Sung songs (20), if testers say tap the tune is not enough.

## Decisions

Vivek answered the open questions on 2026-11-21:

1. **How much fun**: a parent setting, not a number in code. Automatic from 1 in 2 to 1 in
   6 over a span the parent picks (default three weeks of play), or a fixed share, or off.
   Settled stays above zero. See [What the parent sets](#what-the-parent-sets).
2. **Tunes or a singing voice**: synthesised tunes in phase 1; sung recordings only if
   the testers ask.
3. **A mascot**: yes, Gappu / गप्पू the baby elephant. Its design is worked out with
   Gemini's image model (Nano Banana) for a more detailed character than a hand-drawn SVG
   would be, then turned into SVG poses for `scripts/art.py`; where the art came from goes
   in `art/README.md` like every other picture.
4. **Moving games**: sitting moves for everyone; jumping only with the parent's Jumping
   games switch, off by default.
5. **Children who already play**: start automatic from day 1 on the update.
