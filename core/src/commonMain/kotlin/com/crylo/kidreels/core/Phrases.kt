package com.crylo.kidreels.core

import kotlin.random.Random

/** Everything the reels say that is not part of one reel's content. */
object Phrases {
    val praise: List<Txt> = listOf(
        txt("Well done!", "शाबाश!"),
        txt("Super! You got it!", "बहुत बढ़िया! सही जवाब!"),
        txt("Yay! That's right!", "वाह! बिल्कुल सही!"),
        txt("Great job!", "बहुत अच्छे!"),
        txt("Wow, you did it!", "अरे वाह, तुमने कर दिखाया!"),
    )

    /*
     * Lines every reel can say are in words a three-year-old knows: they suit older
     * children too. Words a child only meets at school ("संख्या", "minus") get a simpler
     * version for the little ones instead (see Txt.simpler), so a seven-year-old still
     * hears the word their class uses.
     */

    /** Never "wrong": a pre-schooler hears that as "you are bad". */
    val tryAgain: List<Txt> = listOf(
        txt("Oops! Try again.", "उफ़! फिर से करो।"),
        txt("Not that one! Try another.", "वो नहीं! दूसरा देखो।"),
        txt("Hmm, have another go!", "हम्म, एक बार और करो!"),
    )

    /*
     * What the voice says when a reel arrives names what is on the screen and says what to
     * do with it, the way a parent beside the child would: "How many butterflies are there
     * on the phone? Count them and tell me!" Never a filler line ("Can you help me?") that
     * says nothing about the reel.
     */

    /** The second half of most spoken questions: what to do once the child knows. */
    val tapIt = txt("Tap it!", "उसे छुओ!")
    val findAndTap = txt("Find it and tap it!", "ढूँढकर उसे छुओ!")
    val workItOut = txt("Work it out and tell me!", "सोचकर बताओ!")
    val countAndTell = txt("Count them and tell me!", "गिनकर बताओ!")
    val sayWithMe = txt("Say it with me!", "मेरे साथ बोलो!")

    /** Shown, not spoken, when a puzzle is swiped past: the next reel is already talking. */
    val skipped = txt("Aww, you didn't try that one!", "अरे, वो तो किया ही नहीं!")

    val swipeUp = txt("Swipe up", "ऊपर सरकाओ")

    /** Under the goodbye reel: what comes after the phone. */
    val goodbyeThings = listOf("🍽️", "⚽", "🧸", "📚", "🛏️")

    /** What tapping a picture says: see [Word.thisIs]. */
    fun thisIs(name: Txt, plural: Boolean, pluralHi: Boolean, article: String?, colour: Boolean): Txt {
        val en = name.en
        val a = article ?: if (en.first().lowercaseChar() in "aeiou") "an" else "a"
        return txt(
            when {
                colour -> "This is the colour $en!"
                plural -> "These are $en!"
                a.isEmpty() -> "This is $en!"
                else -> "This is $a $en!"
            },
            when {
                colour -> "यह ${name.hi} रंग है!"
                pluralHi -> "ये ${name.hi} हैं!"
                else -> "यह ${name.hi} है!"
            },
        )
    }
    fun letsCount(t: Thing): Txt = txt("Let's count the ${t.many.en}!", "चलो ${t.many.hi} गिनें!")

    fun countAlong(t: Thing): Txt = letsCount(t) + txt("Tap them one by one.", "एक-एक करके सबको छुओ।")

    fun counted(n: Int, t: Thing): Txt =
        if (n == 1) txt("1 ${t.one.en}! Well done!", "1 ${t.one.hi}! शाबाश!")
        else txt("${t.of(n).en}! You counted them all!", "${t.of(n).hi}! तुमने सब गिन ${t.agree("लिए", "लीं")}!")

    fun howMany(t: Thing): Txt = txt("How many ${t.many.en}?", "${t.agree("कितने", "कितनी")} ${t.many.hi} हैं?")

    fun howManyOnPhone(t: Thing): Txt =
        txt("How many ${t.many.en} are there on the phone?", "फ़ोन पर ${t.agree("कितने", "कितनी")} ${t.many.hi} हैं?") + countAndTell

    fun thereAre(n: Int, t: Thing): Txt =
        if (n == 1) txt("There is ${t.of(1).en}!", "${t.of(1).hi} है!") else txt("There are ${t.of(n).en}!", "${t.of(n).hi} हैं!")

    val whatNext = txt("What comes next?", "आगे क्या आएगा?")
    val whatMissing = txt("What is missing?", "बीच में क्या छूटा है?")
    val oddOneOut = txt("Which one is different?", "इनमें से अलग कौन है?")
    val whatTime = txt("What time is it?", "घड़ी में कितने बजे हैं?")
    fun whichMore(t: Thing): Txt = txt("Which has more ${t.many.en}?", "किसमें ज़्यादा ${t.many.hi} हैं?")

    fun whichMoreSay(t: Thing): Txt = txt("Look at both.", "दोनों को देखो।") + whichMore(t) + tapIt

    val whichBigger = txt("Which number is bigger?", "कौन सी संख्या बड़ी है?").simpler("Which number is bigger?", "कौन सा नंबर बड़ा है?")
    val whichSmaller = txt("Which number is smaller?", "कौन सी संख्या छोटी है?").simpler("Which number is smaller?", "कौन सा नंबर छोटा है?")

    fun whichBiggerSay(x: Int, y: Int): Txt =
        txt("Which number is bigger, $x or $y? Tap the bigger one!", "$x और $y में कौन सी संख्या बड़ी है? बड़ी वाली को छुओ!")
            .simpler("Which number is bigger, $x or $y? Tap the bigger one!", "$x और $y में कौन सा नंबर बड़ा है? बड़े वाले को छुओ!")

    fun whichSmallerSay(x: Int, y: Int): Txt =
        txt("Which number is smaller, $x or $y? Tap the smaller one!", "$x और $y में कौन सी संख्या छोटी है? छोटी वाली को छुओ!")
            .simpler("Which number is smaller, $x or $y? Tap the smaller one!", "$x और $y में कौन सा नंबर छोटा है? छोटे वाले को छुओ!")

    /** "Which apple is big?" / "Which butterfly is the biggest?", in two sizes or three. */
    fun whichSize(t: Thing, big: Boolean, ofThree: Boolean): Txt {
        val hiSize = if (big) t.agree("बड़ा", "बड़ी") else t.agree("छोटा", "छोटी")
        val which = t.agree("कौन सा", "कौन सी")
        return when {
            !ofThree -> txt("Which ${t.one.en} is ${if (big) "big" else "small"}?", "$which ${t.one.hi} $hiSize है?")
            else -> txt("Which ${t.one.en} is the ${if (big) "biggest" else "smallest"}?", "सबसे $hiSize ${t.one.hi} $which है?")
        }
    }

    val sudokuNumber = txt("Which number goes in the empty box?", "खाली खाने में कौन सी संख्या आएगी?")
        .simpler("Which number goes in the empty box?", "खाली डिब्बे में कौन सा नंबर आएगा?")
    val sudokuPicture = txt("Which one goes in the empty box?", "खाली खाने में कौन सा आएगा?")
        .simpler("Which one goes in the empty box?", "खाली डिब्बे में कौन सा आएगा?")
    val sudokuRule = txt("Each row, column and box has each one once.", "हर पंक्ति, स्तंभ और खंड में हर एक बस एक बार।")
    val dragAngle = txt("Drag the arm to make an angle!", "भुजा को खींचकर कोण बनाओ!")
    val stretchIt = txt("Slide to change it", "बदलने के लिए सरकाओ")

    val clockSay = txt("Look at the clock. What time is it? Tap the right time!", "घड़ी देखो। कितने बजे हैं? सही समय छुओ!")
    val oddOneOutSay = txt("One of these is not like the others. Which one? Tap it!", "इनमें से एक बाकी सबसे अलग है। कौन सा? उसे छुओ!")
    val whatMissingSay = txt("One number is missing from the row. Which one goes in the gap? Tap it!", "एक नंबर छूट गया है। खाली जगह में कौन सा आएगा? छूकर बताओ!")

    /** "Look: 2, 4, 6, 8. What comes next?", reading out what is on screen. */
    fun whatNextSay(shown: List<Txt>): Txt =
        txt("Look: ${shown.joinToString(", ") { it.en }}. What comes next? Tap it!", "देखो: ${shown.joinToString(", ") { it.hi }}। आगे क्या आएगा? छूकर बताओ!")

    val sudokuNumberSay = sudokuNumber + txt("Each row and column has every number once.", "हर पंक्ति और स्तंभ में हर संख्या एक बार आती है।")
        .simpler("Each line has every number once.", "हर लाइन में हर नंबर एक बार आता है।")
    val sudokuPictureSay = sudokuPicture + txt("Each row and column has every picture once.", "हर पंक्ति और स्तंभ में हर चीज़ एक बार आती है।")
        .simpler("Each line has every picture once.", "हर लाइन में हर चीज़ एक बार आती है।")

    /** "This is a triangle. 3 sides, 3 corners. Tap it and count the corners!" */
    fun thisShape(s: ShapeInfo): Txt = txt(
        "This is ${a(s.name.en)}. ${s.fact.en}. " + if (s.sides > 0) "Tap it and count the corners!" else "Tap it and see!",
        "यह ${s.name.hi} है। ${s.fact.hi}। " + if (s.sides > 0) "छूकर कोने गिनो!" else "छूकर देखो!",
    )

    fun thisSolid(s: SolidInfo, withFact: Boolean): Txt = txt(
        "This is ${a(s.name.en)}. " + (if (withFact) "${s.fact.en}. " else "") + "Spin it with your finger!",
        "यह ${s.name.hi} है। " + (if (withFact) "${s.fact.hi}। " else "") + "उँगली से घुमाकर देखो!",
    )

    fun thisCurve(c: CurveInfo): Txt =
        txt("This is ${a(c.name.en)}. ${c.fact.en}. Slide to change it!", "यह ${c.name.hi} है। ${c.fact.hi}। सरकाकर बदलो!")

    fun makeAngleSay(kind: AngleKind): Txt = makeAngle(kind) + txt("Drag the arm until it fits!", "भुजा को खींचो, जब तक ठीक न बैठे!")

    /** Sticks under the numbers make a sum something to count: "3 sticks and 2 sticks". */
    fun sumSay(a: Int, op: Op, b: Int, aids: Boolean): Txt = when {
        aids && op == Op.ADD -> txt(
            "${Stick.of(a).en} and ${Stick.of(b).en}: how many sticks in all?",
            "${Stick.of(a).hi} और ${Stick.of(b).hi}, सब मिलाकर कितनी तीलियाँ हुईं?",
        ) + countAndTell
        aids && op == Op.SUB -> txt(
            "There ${if (a == 1) "is" else "are"} ${Stick.of(a).en}. Take away $b. How many sticks are left?",
            "${Stick.of(a).hi} ${if (a == 1) "है" else "हैं"}, उनमें से $b हटा दो। कितनी तीलियाँ बचीं?",
        ) + countAndTell
        aids -> sum(a, op, b) + txt("Count the dots or work it out!", "बिंदु गिनो या सोचकर बताओ!")
        else -> sum(a, op, b) + workItOut
    }

    fun makeAngle(kind: AngleKind): Txt = txt("Make ${a(kind.title.en)}!", "${kind.title.hi} बनाओ!")

    fun whichColour(colour: Colour): Txt = txt("Which one is ${colour.name[Lang.EN]}?", "${colour.name[Lang.HI]} वाला कौन सा है?")

    fun whichColourSay(colour: Colour): Txt =
        whichColour(colour) + txt("Find ${colour.name.en} and tap it!", "${colour.name.hi} रंग ढूँढकर उसे छुओ!")

    fun yesColour(colour: Colour): Txt = txt("Yes! That is ${colour.name[Lang.EN]}!", "हाँ! यह ${colour.name[Lang.HI]} है!")

    fun whichShape(shape: ShapeInfo): Txt =
        txt("Which one is the ${shape.name[Lang.EN].lowercase()}?", "इनमें ${shape.name[Lang.HI]} कौन सा है?")

    fun whichSides(sides: Int): Txt =
        txt("Which shape has $sides sides?", "किस आकार की $sides भुजाएँ हैं?").simpler("Which shape has $sides corners?", "किस आकार के $sides कोने हैं?")

    fun whichSidesSay(sides: Int): Txt = whichSides(sides) +
        txt("Count the sides and tap it!", "भुजाएँ गिनकर उसे छुओ!").simpler("Count the corners and tap it!", "कोने गिनकर उसे छुओ!")

    fun whoSays(animal: Animal): Txt = txt("Who says “${animal.sound[Lang.EN]}”?", "कौन बोलता है “${animal.sound[Lang.HI]}”?")

    fun whoSaysSay(animal: Animal): Txt = whoSays(animal) + txt("Find the animal and tap it!", "उस जानवर को ढूँढकर छुओ!")

    val tapItsHome = txt("Tap its home!", "उसका घर छुओ!")

    fun startsWith(letter: Letter): Txt {
        val hi = "${letter.word} किस अक्षर से शुरू ${if (letter.feminine) "होती" else "होता"} है?"
        return when (letter.lang) {
            Lang.EN -> txt("${letter.word} starts with which letter?", hi)
            Lang.HI -> txt("Which letter does ${letter.word} start with?", hi)
        }
    }

    fun startsWithSay(letter: Letter): Txt = startsWith(letter) + txt("Find the letter and tap it!", "वो अक्षर ढूँढकर छुओ!")

    fun notInGroup(group: Category): Txt = txt("All the others are ${group.name[Lang.EN]}!", "बाकी सब ${group.name[Lang.HI]} हैं!")

    fun oddNumber(odd: Int, rule: Txt): Txt = txt("$odd is different: the others are ${rule[Lang.EN]}!", "$odd अलग है: बाकी सब ${rule[Lang.HI]} हैं!")

    fun sum(a: Int, op: Op, b: Int): Txt = when (op) {
        Op.ADD -> txt("What is $a plus $b?", "$a और $b कितने होते हैं?").simpler("$a and $b make how many?", "$a और $b कितने होते हैं?")
        Op.SUB -> txt("What is $a minus $b?", "$a में से $b गए तो कितने बचे?").simpler("$a take away $b leaves how many?", "$a में से $b गए तो कितने बचे?")
        Op.MUL -> txt("What is $a times $b?", "$a गुणा $b कितना होता है?")
    }

    fun sumAnswer(a: Int, op: Op, b: Int, c: Int): Txt = when (op) {
        Op.ADD -> txt("$a plus $b is $c!", "$a और $b, $c होते हैं!").simpler("$a and $b make $c!", "$a और $b, $c होते हैं!")
        Op.SUB -> txt("$a minus $b is $c!", "$a में से $b गए, $c बचे!").simpler("$a take away $b leaves $c!", "$a में से $b गए, $c बचे!")
        Op.MUL -> txt("$a times $b is $c!", "$a गुणा $b, $c होता है!")
    }

    val addThem = txt("Add them up!", "जोड़ो!")
    val takeAway = txt("Take away!", "घटाओ!")
    val multiply = txt("Multiply!", "गुणा करो!")

    fun more(a: Int, b: Int): Txt = txt("$a is more than $b!", "$a, $b से ज़्यादा है!")

    fun less(a: Int, b: Int): Txt = txt("$a is less than $b!", "$a, $b से कम है!")

    fun time(hour: Int, minute: Int): Txt =
        if (minute == 0) txt("It is $hour o'clock!", "$hour बजे हैं!")
        else txt("It is $hour:${minute.toString().padStart(2, '0')}!", "$hour बजकर $minute मिनट हुए हैं!")

    /*
     * Why a tapped choice is not the answer, said instead of a bare "try again": what the
     * child tapped, and what tells it apart. Where the question already names the answer
     * (a colour, a shape, a letter) the line names it too: "That is green, not blue. Find
     * blue!". Where finding it is the puzzle (counting, sums, patterns) it gives a hint,
     * not the answer: "4 is too few: there are more apples than that."
     */

    fun notColour(tapped: Colour, wanted: Colour): Txt = txt(
        "That is ${tapped.name.en}, not ${wanted.name.en}. Find ${wanted.name.en} and tap it!",
        "यह ${tapped.name.hi} है, ${wanted.name.hi} नहीं। ${wanted.name.hi} वाला ढूँढकर छुओ!",
    )

    fun notShape(tapped: ShapeInfo, wanted: ShapeInfo): Txt = txt(
        "That is ${a(tapped.name.en)}, not ${a(wanted.name.en)}. Find the ${wanted.name.en.lowercase()}!",
        "यह ${tapped.name.hi} है, ${wanted.name.hi} नहीं। ${wanted.name.hi} ढूँढो!",
    )

    fun notSides(tapped: ShapeInfo, sides: Int): Txt = txt(
        "That one has ${tapped.sides} sides. Find the one with $sides sides!",
        "इसकी ${tapped.sides} भुजाएँ हैं। $sides भुजाओं वाला ढूँढो!",
    ).simpler("That one has ${tapped.sides} corners. Find the one with $sides corners!", "इसके ${tapped.sides} कोने हैं। $sides कोनों वाला ढूँढो!")

    fun notLetter(tapped: String, wanted: Letter): Txt {
        val starts = "से शुरू ${if (wanted.feminine) "होती" else "होता"} है"
        return txt(
            "That is $tapped. ${wanted.word} starts with ${wanted.glyph}. Find ${wanted.glyph}!",
            "यह $tapped है। ${wanted.word}, ${wanted.glyph} $starts। ${wanted.glyph} ढूँढो!",
        )
    }

    fun notAnimal(tapped: Animal, wanted: Animal): Txt = tapped.reveal + whoSays(wanted)

    fun notHome(dweller: Dweller, tapped: Home): Txt = dweller.notAt(tapped) + txt("Look again!", "फिर से देखो!")

    /** [big]: the child was asked for the big one. [rank]: the tapped one's size, 0 smallest. */
    fun notSize(t: Thing, big: Boolean, ofThree: Boolean, rank: Int): Txt {
        val bigHi = t.agree("बड़ा", "बड़ी")
        val smallHi = t.agree("छोटा", "छोटी")
        val one = t.agree("वाला", "वाली")
        val wantedEn = if (ofThree) (if (big) "the biggest" else "the smallest") else (if (big) "the big one" else "the small one")
        val wantedHi = (if (ofThree) "सबसे " else "") + (if (big) bigHi else smallHi) + " $one"
        val isEn: String
        val isHi: String
        when {
            ofThree && rank == 1 -> {
                isEn = if (big) "big, but not the biggest" else "small, but not the smallest"
                isHi = if (big) "$bigHi है, पर सबसे $bigHi नहीं" else "$smallHi है, पर सबसे $smallHi नहीं"
            }
            ofThree -> {
                isEn = if (rank == 0) "the smallest" else "the biggest"
                isHi = "सबसे " + (if (rank == 0) smallHi else bigHi) + " है"
            }
            else -> {
                isEn = if (rank == 0) "small" else "big"
                isHi = (if (rank == 0) smallHi else bigHi) + " है"
            }
        }
        return txt("That ${t.one.en} is $isEn. Find $wantedEn!", "यह ${t.one.hi} $isHi। $wantedHi ढूँढो!")
    }

    /** A count that is off: [tapped] against the real [n]. */
    fun notCount(tapped: Int, n: Int, t: Thing): Txt =
        if (tapped < n) txt("$tapped is too few: there are more ${t.many.en} than that. Count them again, one by one!", "$tapped कम है, ${t.many.hi} इससे ज़्यादा हैं। एक-एक करके फिर से गिनो!")
        else txt("$tapped is too many: there are fewer ${t.many.en} than that. Count them again, one by one!", "$tapped ज़्यादा है, ${t.many.hi} इससे कम हैं। एक-एक करके फिर से गिनो!")

    fun notFewer(tapped: Int, t: Thing): Txt = txt(
        "This one has ${t.of(tapped).en}. The other one has more! Tap it.",
        "इसमें ${t.of(tapped).hi} ${if (tapped == 1) "है" else "हैं"}। दूसरे में ज़्यादा हैं! उसे छुओ।",
    )

    fun notBigger(tapped: Int, other: Int): Txt = less(tapped, other) +
        txt("Tap the bigger number!", "बड़ी संख्या वाली को छुओ!").simpler("Tap the bigger number!", "बड़े नंबर वाले को छुओ!")

    fun notSmaller(tapped: Int, other: Int): Txt = more(tapped, other) +
        txt("Tap the smaller number!", "छोटी संख्या वाली को छुओ!").simpler("Tap the smaller number!", "छोटे नंबर वाले को छुओ!")

    /** "5 is too small: 3 and 4 make more than that." The sum's answer stays unsaid. */
    fun notSum(tapped: Int, a: Int, op: Op, b: Int, c: Int, aids: Boolean): Txt {
        val more = tapped < c
        val lead = if (more) txt("$tapped is too small!", "$tapped कम है!") else txt("$tapped is too big!", "$tapped ज़्यादा है!")
        val enCmp = if (more) "more" else "less"
        val hiCmp = if (more) "ज़्यादा" else "कम"
        val why = when (op) {
            Op.ADD -> txt("$a and $b make $enCmp than that.", "$a और $b मिलकर इससे $hiCmp होते हैं।")
            Op.SUB -> txt("$a take away $b leaves $enCmp than that.", "$a में से $b गए, तो इससे $hiCmp बचते हैं।")
            Op.MUL -> txt("$a times $b is $enCmp than that.", "$a गुणा $b इससे $hiCmp होता है।")
        }
        val tail = if (aids) txt("Count again!", "फिर से गिनो!") else txt("Try again!", "फिर से सोचो!")
        return lead + why + tail
    }

    /** [step]: the gap between numbers, when it is the same each time; null for 1, 2, 4, 8. */
    fun notInRow(tapped: Int, step: Int?): Txt {
        val lead = txt("$tapped does not fit.", "$tapped यहाँ ठीक नहीं बैठता।")
        return lead + when {
            step == null -> txt("Look at how the numbers grow!", "देखो, नंबर कैसे बढ़ रहे हैं!")
            step > 0 -> txt("Each number is $step more than the one before!", "हर नंबर पिछले वाले से $step ज़्यादा है!")
            else -> txt("Each number is ${-step} less than the one before!", "हर नंबर पिछले वाले से ${-step} कम है!")
        }
    }

    /** [unit]: one repeat of the pattern, by name. */
    fun notInPattern(tapped: Thing, unit: List<Thing>): Txt {
        if (tapped !in unit) return txt("There is no ${tapped.one.en} in this pattern at all! Look again.", "इसमें तो ${tapped.one.hi} है ही नहीं! फिर से देखो।")
        val en = unit.joinToString(", ") { it.one.en }
        val hi = unit.joinToString(", ") { it.one.hi }
        return txt("Not ${tapped.one.en}. Look: it goes $en, then $en again.", "${tapped.one.hi} नहीं। देखो: $hi, फिर से $hi।") + whatNext
    }

    /** [hourWrong]: the tapped time is a different hour, not just different minutes. */
    fun notTime(hour: Int, minute: Int, hourWrong: Boolean): Txt = txt("That one says ${clockName(hour, minute).en}.", "उसमें ${clockName(hour, minute).hi} हैं।") +
        if (hourWrong) txt("The short hand shows the hour: see where it points!", "छोटी सुई घंटा बताती है: देखो वो कहाँ है!")
        else txt("The long hand shows the minutes: see where it points!", "बड़ी सुई मिनट बताती है: देखो वो कहाँ है!")

    private fun clockName(hour: Int, minute: Int): Txt =
        if (minute == 0) txt("$hour o'clock", "$hour बजे") else txt("$hour:${minute.toString().padStart(2, '0')}", "$hour बजकर $minute मिनट")

    fun notOddNumber(tapped: Int, rule: Txt): Txt = txt(
        "$tapped is like the others: they are all ${rule.en}. Find the one that is different!",
        "$tapped भी बाकियों जैसा है: ये सब ${rule.hi} हैं। जो अलग है, उसे ढूँढो!",
    )

    fun notOddItem(group: Category): Txt = txt(
        "That one is like the others: they are all ${group.name.en}. Find the one that is different!",
        "यह भी बाकियों जैसा है: ये सब ${group.name.hi} हैं। जो अलग है, उसे ढूँढो!",
    )

    /** A wrong sudoku value is always already in the blank's row. */
    fun alreadyInRow(symbol: Txt): Txt = txt("There is already ${a(symbol.en)} in this row!", "इस पंक्ति में ${symbol.hi} पहले से है!")
        .simpler("There is already ${a(symbol.en)} in this line!", "इस लाइन में ${symbol.hi} पहले से है!") +
        txt("Find the one that is not there yet!", "जो अभी नहीं है, वो ढूँढो!")

    val allDone = txt("All done for today!", "आज के लिए बस!")
    val allDoneBody = txt("Time to eat, play and rest. See you tomorrow!", "अब खाना, खेलना और आराम। कल मिलते हैं!")
    val almostDone = txt("Almost done", "बस थोड़ा और")

    fun <T> pick(list: List<T>, random: Random): T = list[random.nextInt(list.size)]

    private val Txt.en: String get() = this[Lang.EN]
    private val Txt.hi: String get() = this[Lang.HI]

    /** "a triangle", "an oval". */
    private fun a(name: String): String = name.lowercase().let { (if (it.first() in "aeiou") "an " else "a ") + it }
}
