package com.crylo.kidreels.ui

import com.crylo.kidreels.core.Look
import com.crylo.kidreels.core.Txt
import com.crylo.kidreels.core.txt

/**
 * The parent screens' text, shown in the main language. Kept with the shared UI rather
 * than in Android resources, because a reel shows several languages at once and every
 * platform needs the same words.
 */
object S {
    val appName = txt("Kid Reels", "किड रील्स")
    val tagline = txt(
        "Reels that teach. Every swipe is a little game: counting, letters, shapes and puzzles.",
        "सिखाने वाली रील्स। हर स्वाइप एक छोटा खेल है: गिनती, अक्षर, आकार और पहेलियाँ।",
    )
    val promiseNoAds = txt("No ads, no internet, nothing leaves this phone", "कोई विज्ञापन नहीं, इंटरनेट नहीं, कुछ भी फ़ोन से बाहर नहीं जाता")
    val promiseLimit = txt("You set a daily time limit", "रोज़ का समय आप तय करते हैं")
    val promiseGate = txt("Settings open only with a grown-up's long press", "सेटिंग्स सिर्फ़ बड़ों के देर तक दबाने से खुलती हैं")
    val setUp = txt("Set up", "शुरू करें")
    val next = txt("Next", "आगे")
    val back = txt("Back", "पीछे")
    val start = txt("Start reels", "रील्स शुरू करें")
    val done = txt("Done", "हो गया")

    val langsTitle = txt("Which languages does your child hear at home?", "बच्चा घर में कौन सी भाषाएँ सुनता है?")
    val langsBody = txt(
        "Pick up to 3. The first one is the main language: reels speak it. Alphabet reels of the other languages speak their own.",
        "3 तक चुनें। पहली मुख्य भाषा है: रील्स उसी में बोलती हैं। दूसरी भाषाओं की वर्णमाला वाली रील्स अपनी भाषा में बोलती हैं।",
    )
    val langsMain = txt("Main", "मुख्य")
    val makeMain = txt("Make main", "मुख्य बनाएँ")
    val langsMore = txt("More languages are coming.", "और भाषाएँ जल्द आएँगी।")

    val ageTitle = txt("How old is your child?", "बच्चे की उम्र कितनी है?")
    val ageBody = txt(
        "Reels are made for this age, with a few easier and harder ones mixed in.",
        "रील्स इसी उम्र के लिए बनती हैं, बीच-बीच में कुछ आसान और कुछ थोड़ी कठिन।",
    )

    fun years(n: Int): Txt = txt(if (n == 1) "1 year" else "$n years", "$n साल")

    val lookTitle = txt("Pick the look", "रूप चुनें")
    val lookBody = txt("You can change it any time in settings.", "इसे सेटिंग्स में कभी भी बदल सकते हैं।")

    fun lookName(look: Look): Txt = when (look) {
        Look.CLASSIC -> txt("Classic", "क्लासिक")
        Look.CANDY -> txt("Candy", "कैंडी")
        Look.CALM -> txt("Calm", "शांत")
    }

    fun lookAbout(look: Look): Txt = when (look) {
        Look.CLASSIC -> txt("Dark, like the reels apps they know", "गहरा, जानी-पहचानी रील्स ऐप जैसा")
        Look.CANDY -> txt("Bright, bubbly and bouncy", "चमकीला और उछलता हुआ")
        Look.CALM -> txt("Soft colours, nothing extra", "हल्के रंग, कुछ भी फ़ालतू नहीं")
    }

    val limitTitle = txt("Daily time limit", "रोज़ का समय")
    val limitBody = txt(
        "When time is up, a goodbye reel says it is time to eat and play. A grown-up can add 10 minutes from there.",
        "समय पूरा होने पर एक आख़िरी रील कहती है कि अब खाने और खेलने का समय है। बड़े वहाँ से 10 मिनट और दे सकते हैं।",
    )
    val limitAdvice = txt(
        "The WHO suggests no more than one hour of screen time a day for ages 2 to 4.",
        "WHO के अनुसार 2 से 4 साल के बच्चों के लिए स्क्रीन का समय दिन में एक घंटे से ज़्यादा नहीं होना चाहिए।",
    )
    val noLimit = txt("No limit", "कोई सीमा नहीं")

    fun minutes(n: Int): Txt = txt("$n min", "$n मिनट")

    fun leftToday(minutes: Long): Txt = txt("$minutes min left today", "आज $minutes मिनट बाकी")

    val settings = txt("Settings", "सेटिंग्स")

    val showOnly = txt("Show only", "सिर्फ़ यह दिखाएँ")
    val showOnlyBody = txt(
        "Pick a topic to practise just that, in order: the alphabet from A, counting from 1, puzzles from easy to hard. It lasts until the app is closed; then all topics come back.",
        "सिर्फ़ एक विषय का अभ्यास कराने के लिए उसे चुनें। वह क्रम से चलेगा: वर्णमाला शुरू से, गिनती 1 से, पहेलियाँ आसान से कठिन तक। यह ऐप बंद होने तक रहेगा, फिर सभी विषय वापस आ जाएँगे।",
    )
    val allTopics = txt("All topics", "सभी विषय")

    fun forAges(ages: IntRange): Txt = txt("ages ${ages.first}–${ages.last}", "${ages.first}–${ages.last} साल")

    fun onlyTopic(title: Txt): Txt = txt("Only: ${title[com.crylo.kidreels.core.Lang.EN]}", "सिर्फ़: ${title[com.crylo.kidreels.core.Lang.HI]}")
    val checkInTitle = txt("Age check-in", "उम्र की जाँच")
    val checkInBody = txt(
        "Every few months we ask whether your child is ready for the next age's reels.",
        "हर कुछ महीनों में हम पूछेंगे कि क्या बच्चा अगली उम्र की रील्स के लिए तैयार है।",
    )

    fun everyMonths(n: Int): Txt = when (n) {
        0 -> txt("Never", "कभी नहीं")
        1 -> txt("Every month", "हर महीने")
        else -> txt("Every $n months", "हर $n महीने")
    }

    val speech = txt("Speech", "आवाज़")
    val slowSpeech = txt("Speak slowly", "धीरे बोलें")
    val voiceReady = txt("Voice ready", "आवाज़ तैयार है")
    val voiceMissing = txt("Voice not installed: reels in it will be silent", "आवाज़ इंस्टॉल नहीं है: इसकी रील्स चुप रहेंगी")
    val noEngine = txt("This phone has no text-to-speech engine, so reels will be silent.", "इस फ़ोन में बोलने वाला इंजन नहीं है, इसलिए रील्स चुप रहेंगी।")
    val installVoice = txt("Install voice", "आवाज़ इंस्टॉल करें")

    val progress = txt("Progress", "प्रगति")
    val noProgress = txt("No puzzles yet.", "अभी तक कोई पहेली नहीं।")

    fun levelLine(level: Int, tried: Int, percent: Int): Txt =
        txt("Age $level reels: $tried puzzles, $percent% right first time", "$level साल की रील्स: $tried पहेलियाँ, $percent% पहली बार में सही")

    val about = txt("About", "जानकारी")

    fun aboutBody(version: String): Txt = txt(
        "Kid Reels $version. Free and open source (MIT licence). No ads, no tracking and no internet access: nothing leaves this phone.",
        "किड रील्स $version। मुफ़्त और ओपन सोर्स (MIT लाइसेंस)। कोई विज्ञापन नहीं, कोई ट्रैकिंग नहीं, इंटरनेट नहीं: कुछ भी फ़ोन से बाहर नहीं जाता।",
    )

    val privacyPolicy = txt("Privacy policy", "गोपनीयता नीति")

    /** Linked from Google Play too, so the two can never say different things. */
    const val PRIVACY_URL = "https://github.com/vivekg7/kidreels/blob/main/PRIVACY.md"

    val gateTitle = txt("For grown-ups", "बड़ों के लिए")
    val gateBody = txt("Press and hold the button for 3 seconds.", "बटन को 3 सेकंड तक दबाकर रखें।")
    val hold = txt("Hold", "दबाए रखें")
    val cancel = txt("Cancel", "रद्द करें")

    val checkInBanner = txt("For grown-ups: age check-in", "बड़ों के लिए: उम्र की जाँच")
    val checkInQuestion = txt("Is your child ready for harder reels?", "क्या बच्चा कठिन रील्स के लिए तैयार है?")

    fun checkInSince(months: Int, age: Int): Txt = txt(
        "It has been $months months since you chose age $age.",
        "आपने $months महीने पहले $age साल चुना था।",
    )

    fun moveUp(age: Int): Txt = txt("Move up to age $age", "$age साल पर जाएँ")

    fun keep(age: Int): Txt = txt("Stay at age $age", "$age साल पर ही रहें")

    val later = txt("Ask me in a week", "एक हफ़्ते बाद पूछें")

    val moreTime = txt("Grown-ups: hold for 10 more minutes", "बड़े: 10 मिनट और के लिए दबाए रखें")
    val like = txt("Like", "पसंद")
    val again = txt("Again", "फिर से")
}
