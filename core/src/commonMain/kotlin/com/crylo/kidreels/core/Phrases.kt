package com.crylo.kidreels.core

import kotlin.random.Random

/** Everything the reels say that is not part of one reel's content. */
object Phrases {
    val praise: List<Txt> = listOf(
        txt("Well done!", "शाबाश!"),
        txt("Super!", "बहुत बढ़िया!"),
        txt("You got it!", "सही जवाब!"),
        txt("Yay!", "वाह!"),
        txt("Great job!", "बहुत अच्छे!"),
    )

    /** Never "wrong": a pre-schooler hears that as "you are bad". */
    val tryAgain: List<Txt> = listOf(
        txt("Try again!", "फिर से कोशिश करो!"),
        txt("Almost! Try another one.", "लगभग! दूसरा देखो।"),
        txt("Have another go!", "एक बार और!"),
    )

    /** Shown, not spoken, when a puzzle is swiped past: the next reel is already talking. */
    val skipped = txt("Aww, you didn't try that one!", "अरे, वो तो किया ही नहीं!")

    val swipeUp = txt("Swipe up", "ऊपर सरकाओ")
    val letsCount = txt("Let's count! Tap each one.", "चलो गिनें! हर एक को छुओ।")

    fun counted(n: Int): Txt = txt("$n! You counted them all!", "$n! तुमने सब गिन लिए!")

    val howMany = txt("How many are there?", "कितने हैं?")
    val whatNext = txt("What comes next?", "आगे क्या आएगा?")
    val whatMissing = txt("What is missing?", "बीच में क्या छूटा है?")
    val oddOneOut = txt("Which one does not belong?", "इनमें से अलग कौन है?")
    val whatTime = txt("What time is it?", "घड़ी में कितने बजे हैं?")
    val whichMore = txt("Which has more?", "किसमें ज़्यादा हैं?")
    val whichBigger = txt("Which number is bigger?", "कौन सी संख्या बड़ी है?")
    val whichSmaller = txt("Which number is smaller?", "कौन सी संख्या छोटी है?")
    val whichBig = txt("Which one is big?", "बड़ा वाला कौन सा है?")
    val whichSmall = txt("Which one is small?", "छोटा वाला कौन सा है?")
    val whichBiggest = txt("Which one is the biggest?", "सबसे बड़ा कौन सा है?")
    val whichSmallest = txt("Which one is the smallest?", "सबसे छोटा कौन सा है?")
    val sudokuNumber = txt("Which number goes in the empty box?", "खाली खाने में कौन सी संख्या आएगी?")
    val sudokuPicture = txt("Which one goes in the empty box?", "खाली खाने में कौन सा आएगा?")
    val sudokuRule = txt("Each row, column and box has each one once.", "हर पंक्ति, स्तंभ और खंड में हर एक बस एक बार।")
    val dragAngle = txt("Drag the arm to make an angle!", "भुजा को खींचकर कोण बनाओ!")
    val stretchIt = txt("Slide to change it", "बदलने के लिए सरकाओ")

    fun makeAngle(kind: AngleKind): Txt {
        val name = kind.title[Lang.EN].lowercase()
        val article = if (name.first() in "aeiou") "an" else "a"
        return txt("Make $article $name!", "${kind.title[Lang.HI]} बनाओ!")
    }

    fun whichColour(colour: Colour): Txt = txt("Which one is ${colour.name[Lang.EN]}?", "इनमें ${colour.name[Lang.HI]} कौन सा है?")

    fun yesColour(colour: Colour): Txt = txt("Yes! That is ${colour.name[Lang.EN]}!", "हाँ! यह ${colour.name[Lang.HI]} है!")

    fun whichShape(shape: ShapeInfo): Txt =
        txt("Which one is the ${shape.name[Lang.EN].lowercase()}?", "इनमें ${shape.name[Lang.HI]} कौन सा है?")

    fun whichSides(sides: Int): Txt = txt("Which shape has $sides sides?", "किस आकार की $sides भुजाएँ हैं?")

    fun whoSays(animal: Animal): Txt = txt("Who says “${animal.sound[Lang.EN]}”?", "कौन बोलता है “${animal.sound[Lang.HI]}”?")

    fun startsWith(letter: Letter): Txt = when (letter.lang) {
        Lang.EN -> txt("${letter.word} starts with which letter?", "${letter.word} किस अक्षर से शुरू होता है?")
        Lang.HI -> txt("Which letter does ${letter.word} start with?", "${letter.word} किस अक्षर से शुरू होता है?")
    }

    fun notInGroup(group: Category): Txt = txt("All the others are ${group.name[Lang.EN]}!", "बाकी सब ${group.name[Lang.HI]} हैं!")

    fun oddNumber(odd: Int, rule: Txt): Txt = txt("$odd is different: the others are ${rule[Lang.EN]}!", "$odd अलग है: बाकी सब ${rule[Lang.HI]} हैं!")

    fun sum(a: Int, op: Op, b: Int): Txt = when (op) {
        Op.ADD -> txt("What is $a plus $b?", "$a और $b कितने होते हैं?")
        Op.SUB -> txt("What is $a minus $b?", "$a में से $b गए तो कितने बचे?")
        Op.MUL -> txt("What is $a times $b?", "$a गुणा $b कितना होता है?")
    }

    fun sumAnswer(a: Int, op: Op, b: Int, c: Int): Txt = when (op) {
        Op.ADD -> txt("$a plus $b is $c!", "$a और $b, $c होते हैं!")
        Op.SUB -> txt("$a minus $b is $c!", "$a में से $b गए, $c बचे!")
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

    val allDone = txt("All done for today!", "आज के लिए बस!")
    val allDoneBody = txt("Time to eat, play and rest. See you tomorrow!", "अब खाना, खेलना और आराम। कल मिलते हैं!")
    val almostDone = txt("Almost done", "बस थोड़ा और")

    fun <T> pick(list: List<T>, random: Random): T = list[random.nextInt(list.size)]
}
