# Plan: classes 6 to 12, general knowledge and exam prep

**Status:** planned, not started (written 2026-11-17). Nothing here is built yet; when a
phase ships, its decisions move into [design.md](design.md) and this file says so.

## What we are doing

Take the app past age 12. Up to about 12 a child still picks an **age**, as now; classes
1 to 5 are covered by age alone, with no class picker. From there a parent or the student
picks a **class, 6 to 12**, and the feed shows reels from
that class's syllabus. Next to the classes sit **general knowledge**, **current affairs**
and **exam prep** (JNVST, Olympiads, NEET, JEE, SSC, banking, UPSC).

- **Indian syllabi only to start:** NCERT (CBSE and the many state boards that use its
  books) and ICSE. Other countries come later, and only if the app takes off.
- **English and Hindi only**, the two languages the app already has. More languages wait
  for a real user base, as decided for the primary ages.
- **The goal does not change.** It is still to teach, and still not a better-made
  addiction. The difference is who it is for: a student who wants to revise in five-minute
  bursts as well as a child being weaned off reels.
- **We do not cover everything.** Each phase is worth shipping on its own; we stop where it
  stops paying.
- **The app gets a grown-up name** when the first phase ships (see [The rename](#the-rename)).

## What stays the same, and what bends for older students

These rules from [design.md](design.md#the-goal-shapes-everything) keep applying at every
class: nothing to watch passively, every miss explained (`Quiz.whyNot`), no streaks,
badges or points, and an end to every session.

Some rules bend, because a 16-year-old doing JEE revision is not a toddler:

| Today (ages 2–12)                      | From class 6                                                                                                                          |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| The parent sets the daily limit        | Still a limit by default, but a student profile may set its own. No profile is ever "no end": a session still winds down and stops    |
| Parent gate on settings                | Kept for profiles a parent made under 13; a student's own profile has no gate                                                         |
| Sad face on a skipped puzzle           | Off. An older student skipping is choosing, not failing; the skip still counts in the weekly note                                     |
| Animal avatars, Candy look             | A plain, calmer look for class profiles                                                                                               |
| Every reel spoken aloud                | Mostly silent: an older student reads the screen faster than a voice talks. Speech stays only where hearing is the lesson (see below) |
| Home words up to 5, school words after | School and exam words always; Hindi uses the standard terms its textbooks use (प्रकाश संश्लेषण, not "पौधे का खाना बनाना")                  |

**Where the voice stays.** Speech in the primary ages exists because a young child cannot
read the question yet; an older one can, and a voice reading out what is already on the
screen slows them down and sounds patronising. So class reels are silent by default, and
speak only where the sound is the thing being taught: pronouncing an English or Hindi word,
a poem's rhythm, a language reel's alphabet or spelling. That also removes most of the
work of writing a spoken line (`say`) for every question.

## Phases

Order follows one observation about the app as it is: **reels are made by templates**, and
templates are strongest where the content is rules and numbers (maths, reasoning, quant)
and weakest where it is facts (history, biology, polity), which have to be written one by
one. So we start where the existing engine already does the work, and add written content
step by step.

### Phase 0: foundations (no new subjects yet)

1. **Class in the profile.** A profile holds either an age (2–12) or a class (6–12), plus
   a board (NCERT or ICSE) and, for classes 11 and 12, a stream (science PCM, science PCB,
   commerce, humanities). Ages 10–12 are offered "or pick a class" too, since an 11-year-old
   in class 6 should be able to see class 6 content. The age check-in becomes "move to the
   next class?" once a year, in April.
2. **Written content as data.** A question bank format beside the templates: one file per
   chapter-topic, with the question, choices, answer, a `whyNot` per wrong choice, a
   source (which chapter of which book edition), and a reviewer. A test
   fails on a missing field, as `everyTextHasEveryLanguage` does now. These banks feed the
   same `Quiz` and card kinds the templates do, so the UI needs little.
3. **Tag by syllabus topic, not by chapter number.** NCERT is replacing its books under NCF
   2023 (new class 6–8 books from 2024–25 onwards, e.g. _Ganita Prakash_, _Curiosity_), and
   chapters move between editions. A reel tagged "Fractions: equivalent fractions" is
   remapped to a new chapter list in one place; one tagged "Class 6 chapter 7" is rewritten.
4. **Formulas on screen.** Fractions, powers, roots, subscripts (H₂O, CO₂),
   arrows in chemical equations. A small layout in `ui/draw/` like the solids renderer, so
   no maths library is added. Since class reels are mostly silent, formulas need no spoken
   form; the few spoken reels that show one carry its words ("x squared plus two x") in
   `say`, as TTS reads "x²" badly.
5. **A student look** and the relaxed rules in the table above.
6. **Rename** (display name, store listing, README, docs).

### Phase 1: class 6–8 maths, reasoning and JNVST

The cheapest new content, because it is mostly templates the app nearly has:

- **Maths, classes 6–8:** integers, fractions and decimals, ratio and percentage, simple
  algebra (solve for x), perimeter and area, angles and triangles, data handling. The age
  11–12 number topics already reach some of this.
- **JNVST class 6 entry:** finish what the reasoning puzzles began (they cover the figure
  section): add the arithmetic and language sections. **JNVST class 9 lateral entry**
  follows from class 8 maths and science.
- **General knowledge, static:** capitals, states and their capitals, rivers, national
  symbols, inventors, firsts of India. Written banks, small, and a good test of the format
  in Phase 0.

### Phase 2: class 6–10 science and social science

Written content, chapter by chapter, using what the science channel already does
(step-through processes, tap-the-diagram, answer-with-the-picture) for the diagrams that
matter: the cell, the eye, the heart, the circuit, the layers of the earth. Class 9 and 10
come before 11 and 12 because board exams make them the years students most want to
revise. ICSE is mapped onto the same topic tags; only the topics ICSE adds need new
content.

### Phase 3: classes 11–12, NEET and JEE

Physics, chemistry, maths and biology by stream. **NEET leans heavily on NCERT biology**,
so class 11–12 biology banks double as NEET prep; JEE maths is mostly templates again.
Commerce and humanities come after science, because the exam-prep demand is there first.

### Phase 4: competitive exams

- **SSC and banking:** quant (percentages, profit and loss, time and work, speed and
  distance) and reasoning (series, coding-decoding, blood relations, syllogisms, seating)
  are almost all generatable, so this is cheaper than it looks.
- **Olympiads:** harder versions of the class topics, a difficulty step above the class.
- **UPSC:** polity, history, geography, economy, environment as written banks; much of it
  overlaps the class 6–12 social science banks (UPSC aspirants read NCERT books for exactly
  this reason).

An exam is a filter over topics plus its own extras, not a separate content tree: "SSC" is
quant + reasoning + GK + English, at a level.

### Current affairs (separate track)

Current affairs are the one thing that goes stale, which is why they get their own track:
see the open question about the internet below. Until it is settled, they are left out.

## Where the content comes from

- **From the syllabus, never the book's text.** NCERT books are © NCERT, and ICSE books are
  private publishers' (Selina, Frank and others); CISCE only sets the syllabus. Facts and
  syllabus topics are free to use; the books' wording, diagrams and exercises are not. Every
  explanation and question is our own, and a bank records which chapter it covers, not
  what it copied.
- **The repo is public (MIT).** Content banks will be in it too, so they need a licence of
  their own; CC BY-SA 4.0 is the usual fit for open educational content.
- **Drafting with AI, publishing after a person checks it.** Drafting a chapter's questions
  is fast; a wrong fact shown to a student preparing for an exam is not acceptable. So a
  bank is merged only with a reviewer's name on it, and the tests check what can be
  checked mechanically (the answer is among the choices, every wrong choice has its line,
  both languages are present).
- **A "report this reel" button.** It shares the reel's id and text through Android's
  share sheet (email, WhatsApp), so it works without the `INTERNET` permission.

## The rename

The package `com.crylo.kidreels` stays: Play never lets it change, and a new package would
be a new app with no installs. What changes is the name on the phone and in the store,
the README, PRIVACY.md and these docs. The GitHub repo can be renamed too; GitHub
redirects the old URL.

The name should work in English and Hindi, not say "kids", and not clash with an existing
app. Already taken and to avoid: _ePathshala_ (NCERT's own app), _DIKSHA_, _Seekho_. A
few to check before choosing: **Gyaan Reels**, **Kaksha**, **Padho**, **Swipe Shala**.
Before picking, search Play and the Indian trademark registry for each.

## Open questions

1. **Internet or not?** The app has no permissions at all today ([PRIVACY.md](../PRIVACY.md)),
   which is a selling point to parents. Current affairs need fresh content every week, and
   class 6–12 banks may grow past a comfortable APK size. Options: (a) stay offline and ship
   current affairs as a monthly app update; (b) add `INTERNET` only to download signed,
   static content packs (no accounts, no analytics, nothing sent), and update PRIVACY.md and
   Play's data safety form to match. My recommendation: (a) at first, (b) only when current
   affairs or the APK size force it.
2. **Who reviews content?** One teacher per subject group (maths, science, social
   science, Hindi) at minimum. Without this, Phase 2 should not ship.
3. **Exam prep for adults.** SSC, banking and UPSC aspirants are 18 and over. Play's
   target audience already covers every age ([play-store.md](play-store.md)), so nothing
   changes there, but the store listing and the name should not read as a children's app
   once these ship.
4. **Hindi-medium vs English-medium.** Is the main language enough to tell them apart, or
   does a Hindi-medium student want Hindi reels with English terms alongside (and the
   other way round)? The two-language display may already answer this.

## Things worth adding

- **Revision of what you missed.** A student revising wants the questions they got wrong
  back the next day, not only 3–5 reels later. The retry rule in the feed grows into a small
  "come back tomorrow" queue, kept per profile. It is not a streak: nothing is lost by not
  doing it.
- **Saved reels.** The heart already marks a liked topic; for a student it could also save
  that reel to revisit before an exam. Generated reels need their seed stored for this.
- **A chapter view.** "Show only" already exists; for a class profile it becomes "show only
  this chapter", from easy to hard, which is how a student revises before a test.
- **Measuring "popular".** With no analytics, Play Console installs, ratings and reviews
  are the only signal. Decide the number (installs or active users) that would justify
  more languages before it comes up.
