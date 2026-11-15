package com.crylo.kidreels.ui

import com.crylo.kidreels.core.Lang
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

    val childTitle = txt("Who is this for?", "यह किसके लिए है?")
    val childBody = txt(
        "A name and a picture. If more children use this phone, each finds their own reels by their picture. The name can be left out.",
        "एक नाम और एक चित्र। अगर इस फ़ोन पर और बच्चे भी खेलें, तो हर बच्चा अपने चित्र से अपनी रील्स ढूँढ लेगा। नाम छोड़ भी सकते हैं।",
    )
    val name = txt("Name", "नाम")
    val nameHint = txt("Optional", "ज़रूरी नहीं")
    val picture = txt("Picture", "चित्र")
    val colour = txt("Colour", "रंग")

    val children = txt("Children", "बच्चे")
    val childrenBody = txt(
        "Each child has their own age, languages, time limit and progress: tap a child to see or change them. With more than one, the app asks who is playing when it opens.",
        "हर बच्चे की अपनी उम्र, भाषाएँ, समय और प्रगति होती है: देखने या बदलने के लिए बच्चे को छुएँ। एक से ज़्यादा बच्चे हों तो ऐप खुलते ही पूछता है कि कौन खेलेगा।",
    )
    val addChild = txt("Add a child", "बच्चा जोड़ें")
    val playingNow = txt("Playing now", "अभी इनकी बारी")
    val checkInDue = txt("Age check-in waiting", "उम्र की जाँच बाकी है")
    val profile = txt("Name, picture and age", "नाम, चित्र और उम्र")
    val timeGroup = txt("Time", "समय")
    val reelsGroup = txt("Reels", "रील्स")
    val languages = txt("Languages", "भाषाएँ")
    val look = txt("Look", "रूप")

    fun childLine(age: Int, minutes: Int): Txt =
        if (minutes == 0) txt("Age $age · no limit", "$age साल · कोई सीमा नहीं") else perDay(minutes).let { txt("Age $age · ${it[Lang.EN]}", "$age साल · ${it[Lang.HI]}") }

    fun newChildBody(name: String): Txt = txt(
        "Languages, look and time limit start the same as $name's. You can change them on the next page.",
        "भाषाएँ, रूप और समय $name जैसे ही रहेंगे। इन्हें अगले पेज पर बदल सकते हैं।",
    )

    val save = txt("Save", "सहेजें")

    fun removeChild(name: String): Txt = txt("Remove $name", "$name को हटाएँ")

    fun removeBody(name: String): Txt = txt(
        "Everything $name has done on this phone will be gone: progress, likes and today's time.",
        "इस फ़ोन पर $name का सब कुछ मिट जाएगा: प्रगति, पसंद और आज का समय।",
    )

    val remove = txt("Remove", "हटाएँ")

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

    val bedtimeTitle = txt("Bedtime", "सोने का समय")
    val bedtimeBody = txt(
        "No reels from this time until 6 in the morning, however many minutes are left. A good-night reel says it is time to sleep; a grown-up can add 10 minutes from there.",
        "इस समय से सुबह 6 बजे तक कोई रील नहीं, चाहे कितने भी मिनट बचे हों। एक रील कहती है कि अब सोने का समय है; बड़े वहाँ से 10 मिनट और दे सकते हैं।",
    )
    val off = txt("Off", "बंद")

    /** An evening time: "8:30 pm" / "रात 8:30". */
    fun clock(minuteOfDay: Int): Txt {
        val h = minuteOfDay / 60
        val m = (minuteOfDay % 60).toString().padStart(2, '0')
        val h12 = (h + 11) % 12 + 1
        return txt("$h12:$m ${if (h < 12) "am" else "pm"}", "${if (h < 20) "शाम" else "रात"} $h12:$m")
    }

    /** "45 min", "1 h", "1 h 30 min" / "45 मिनट", "1 घंटा", "2 घंटे 30 मिनट". */
    fun duration(minutes: Int): Txt {
        val h = minutes / 60
        val m = minutes % 60
        val en = listOfNotNull(if (h > 0) "$h h" else null, if (m > 0 || h == 0) "$m min" else null)
        val hi = listOfNotNull(if (h > 0) "$h ${if (h == 1) "घंटा" else "घंटे"}" else null, if (m > 0 || h == 0) "$m मिनट" else null)
        return txt(en.joinToString(" "), hi.joinToString(" "))
    }

    fun perDay(minutes: Int): Txt = duration(minutes).let { txt("${it[Lang.EN]} a day", "रोज़ ${it[Lang.HI]}") }

    fun leftToday(minutes: Long): Txt = duration(minutes.toInt()).let { txt("${it[Lang.EN]} left today", "आज ${it[Lang.HI]} बाकी") }

    val custom = txt("Custom", "अपना चुनें")
    val hours = txt("Hours", "घंटे")
    val hour = txt("Hour", "घंटा")
    val minutesLabel = txt("Minutes", "मिनट")
    val limitUpTo = txt("Up to 6 hours a day.", "दिन में ज़्यादा से ज़्यादा 6 घंटे।")
    val bedtimeRange = txt("Any time from 4 pm to 11:55 pm.", "शाम 4 बजे से रात 11:55 तक कोई भी समय।")

    fun from(time: Txt): Txt = txt("From ${time[Lang.EN]}", "${time[Lang.HI]} से")

    val settings = txt("Settings", "सेटिंग्स")

    val showOnly = txt("Show only", "सिर्फ़ यह दिखाएँ")
    val showOnlyBody = txt(
        "Pick a topic to practise just that, in order: the alphabet from A, counting from 1, puzzles from easy to hard. It lasts until the app is closed; then all topics come back.",
        "सिर्फ़ एक विषय का अभ्यास कराने के लिए उसे चुनें। वह क्रम से चलेगा: वर्णमाला शुरू से, गिनती 1 से, पहेलियाँ आसान से कठिन तक। यह ऐप बंद होने तक रहेगा, फिर सभी विषय वापस आ जाएँगे।",
    )
    val allTopics = txt("All topics", "सभी विषय")
    val goToReels = txt("Go to reels", "रील्स पर जाएँ")
    val otherAges = txt("Also show topics for other ages", "दूसरी उम्र के विषय भी दिखाएँ")

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

    val voices = txt("Voices on this phone", "इस फ़ोन की आवाज़ें")
    val slowSpeech = txt("Speak slowly", "धीरे बोलें")
    val voiceReady = txt("Voice ready", "आवाज़ तैयार है")
    val voiceMissing = txt("Voice not installed: reels in it will be silent", "आवाज़ इंस्टॉल नहीं है: इसकी रील्स चुप रहेंगी")
    val noEngine = txt("This phone has no text-to-speech engine, so reels will be silent.", "इस फ़ोन में बोलने वाला इंजन नहीं है, इसलिए रील्स चुप रहेंगी।")
    val installVoice = txt("Install voice", "आवाज़ इंस्टॉल करें")

    fun langName(lang: Lang): Txt = when (lang) {
        Lang.EN -> txt("English", "अंग्रेज़ी")
        Lang.HI -> txt("Hindi", "हिन्दी")
    }

    fun voiceBanner(lang: Lang): Txt =
        txt("For grown-ups: no ${langName(lang)[Lang.EN]} voice", "बड़ों के लिए: ${langName(lang)[Lang.HI]} की आवाज़ नहीं है")

    fun voiceTitle(lang: Lang): Txt = txt("${langName(lang)[Lang.EN]} reels are silent", "${langName(lang)[Lang.HI]} रील्स चुप हैं")

    fun voiceBody(lang: Lang): Txt = txt(
        "This phone has no ${langName(lang)[Lang.EN]} voice, so reels in ${langName(lang)[Lang.EN]} say nothing. Installing one is free and takes a minute on Wi-Fi.",
        "इस फ़ोन में ${langName(lang)[Lang.HI]} की आवाज़ नहीं है, इसलिए ${langName(lang)[Lang.HI]} की रील्स कुछ नहीं बोलतीं। आवाज़ इंस्टॉल करना मुफ़्त है और Wi-Fi पर एक मिनट लगता है।",
    )

    val notNow = txt("Not now (ask again in a week)", "अभी नहीं (एक हफ़्ते बाद पूछें)")

    val progress = txt("Progress", "प्रगति")
    val noProgress = txt("No puzzles yet.", "अभी तक कोई पहेली नहीं।")
    val thisWeek = txt("The last 7 days", "पिछले 7 दिन")
    val nothingThisWeek = txt("Nothing played yet.", "अभी तक कुछ नहीं खेला।")

    fun weekTime(minutes: Int, days: Int): Txt = txt(
        "$minutes min on the feed, on $days ${if (days == 1) "day" else "days"}",
        "$minutes मिनट, $days ${if (days == 1) "दिन" else "दिनों"} में",
    )

    fun weekPuzzles(tried: Int, percent: Int): Txt = txt(
        "$tried ${if (tried == 1) "puzzle" else "puzzles"}, $percent% right first time",
        "$tried ${if (tried == 1) "पहेली" else "पहेलियाँ"}, $percent% पहली बार में सही",
    )

    val doingWell = txt("Doing well at:", "इनमें अच्छा कर रहे हैं:")
    val findsHard = txt("Finds hard (try one together):", "इनमें मुश्किल हो रही है (साथ में करके देखें):")
    val likesMost = txt("Likes most:", "सबसे ज़्यादा पसंद:")

    fun levelLine(level: Int, tried: Int, percent: Int): Txt =
        txt("Age $level reels: $tried ${if (tried == 1) "puzzle" else "puzzles"}, $percent% right first time", "$level साल की रील्स: $tried ${if (tried == 1) "पहेली" else "पहेलियाँ"}, $percent% पहली बार में सही")

    val about = txt("About", "जानकारी")

    fun aboutBody(version: String): Txt = txt(
        "Kid Reels $version. Free and open source (MIT licence). No ads, no tracking and no internet access: nothing leaves this phone. " +
            "The peas, ginger, beetroot and kidney beans pictures are from Noto Emoji, © Google LLC, under the SIL Open Font License 1.1.",
        "किड रील्स $version। मुफ़्त और ओपन सोर्स (MIT लाइसेंस)। कोई विज्ञापन नहीं, कोई ट्रैकिंग नहीं, इंटरनेट नहीं: कुछ भी फ़ोन से बाहर नहीं जाता। " +
            "मटर, अदरक, चुकंदर और राजमा की तस्वीरें Noto Emoji से हैं, © Google LLC, SIL Open Font License 1.1 के तहत।",
    )

    val privacyPolicy = txt("Privacy policy", "गोपनीयता नीति")

    val crashTitle = txt("The app closed unexpectedly", "ऐप अचानक बंद हो गया था")
    val crashBody = txt(
        "A report was kept on this phone. The app cannot send it (it has no internet access), but you can copy it and paste it into an email to vivek@ivy.homes or an issue on GitHub. It holds the app and Android versions, the phone model and where the app went wrong; nothing about your child.",
        "एक रिपोर्ट इस फ़ोन पर रखी गई है। ऐप इसे भेज नहीं सकता (उसके पास इंटरनेट नहीं है), पर आप इसे कॉपी करके vivek@ivy.homes पर ईमेल या GitHub पर issue में चिपका सकते हैं। इसमें ऐप और Android का वर्ज़न, फ़ोन का मॉडल और ऐप में गड़बड़ी की जगह है; बच्चे के बारे में कुछ नहीं।",
    )
    val copyReport = txt("Copy report", "रिपोर्ट कॉपी करें")
    val copied = txt("Copied", "कॉपी हो गई")
    val deleteReport = txt("Delete", "मिटाएँ")

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
