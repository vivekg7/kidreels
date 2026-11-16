package com.crylo.kidreels.core

/**
 * What a picture is called, so tapping any picture can say "This is an elephant" / "यह
 * हाथी है". One table for every emoji the app shows, so a new picture cannot be silent:
 * `ContentTest.everyPictureHasAName` fails if one is missing.
 *
 * [name] is lowercase English ("elephant"); [title] is what a card shows ("Elephant").
 * [fact] is the line a picture card adds from age 4. [plural] and [pluralHi] are separate
 * because the languages disagree: "glasses" is plural, "चश्मा" is not. [article] is null
 * for "a" or "an" by the first letter, or the word itself ("the Earth", "" for "This is
 * corn").
 */
data class Word(
    val emoji: String,
    val name: Txt,
    val fact: Txt? = null,
    val plural: Boolean = false,
    val pluralHi: Boolean = plural,
    val article: String? = null,
    val colour: Boolean = false,
) {
    val title: Txt get() = txt(name[Lang.EN].replaceFirstChar { it.uppercase() }, name[Lang.HI])

    /** "This is an elephant!" / "यह हाथी है!"; "These are grapes!"; "This is the colour red!". */
    fun thisIs(): Txt = Phrases.thisIs(name, plural, pluralHi, article, colour)
}

/**
 * Picture cards: one topic per group, in teaching order (the everyday ones first). Every
 * emoji is from Emoji 13.1 or earlier, the newest set Android 12 can draw.
 */
object Words {
    private fun w(emoji: String, en: String, hi: String, factEn: String, factHi: String, plural: Boolean = false, pluralHi: Boolean = plural, article: String? = null) =
        Word(emoji, txt(en, hi), txt(factEn, factHi), plural, pluralHi, article)

    private fun n(emoji: String, en: String, hi: String, plural: Boolean = false, pluralHi: Boolean = plural, article: String? = null) =
        Word(emoji, txt(en, hi), null, plural, pluralHi, article)

    private fun colour(emoji: String, c: Colour) = Word(emoji, c.name, colour = true)

    val animals = listOf(
        w("🐄", "cow", "गाय", "The cow gives us milk.", "गाय हमें दूध देती है।"),
        w("🐕", "dog", "कुत्ता", "The dog wags its tail when it is happy.", "ख़ुश होने पर कुत्ता पूँछ हिलाता है।"),
        w("🐈", "cat", "बिल्ली", "The cat loves to drink milk.", "बिल्ली को दूध पीना अच्छा लगता है।"),
        w("🐘", "elephant", "हाथी", "The elephant has a long trunk.", "हाथी की सूँड लंबी होती है।"),
        w("🐐", "goat", "बकरी", "The goat eats leaves and grass.", "बकरी पत्ते और घास खाती है।"),
        w("🐃", "buffalo", "भैंस", "The buffalo gives us milk too.", "भैंस भी हमें दूध देती है।"),
        w("🐎", "horse", "घोड़ा", "The horse runs very fast.", "घोड़ा बहुत तेज़ दौड़ता है।"),
        w("🐒", "monkey", "बंदर", "The monkey jumps from tree to tree.", "बंदर एक पेड़ से दूसरे पेड़ पर कूदता है।"),
        w("🐑", "sheep", "भेड़", "Wool comes from the sheep.", "भेड़ से ऊन मिलती है।"),
        w("🐁", "mouse", "चूहा", "The mouse is small and quick.", "चूहा छोटा और फुर्तीला होता है।"),
        w("🐇", "rabbit", "खरगोश", "The rabbit has long ears.", "खरगोश के कान लंबे होते हैं।"),
        w("🐿️", "squirrel", "गिलहरी", "The squirrel climbs trees very quickly.", "गिलहरी पेड़ पर बहुत तेज़ी से चढ़ती है।"),
        w("🦁", "lion", "शेर", "The lion is called the king of the jungle.", "शेर को जंगल का राजा कहते हैं।"),
        w("🐅", "tiger", "बाघ", "The tiger has black stripes.", "बाघ के शरीर पर काली धारियाँ होती हैं।"),
        w("🐻", "bear", "भालू", "The bear loves honey.", "भालू को शहद बहुत पसंद है।"),
        w("🦌", "deer", "हिरण", "The deer can jump very high.", "हिरण बहुत ऊँची छलाँग लगाता है।"),
        w("🦒", "giraffe", "जिराफ़", "The giraffe has a very long neck.", "जिराफ़ की गर्दन बहुत लंबी होती है।"),
        w("🦓", "zebra", "ज़ेबरा", "The zebra has black and white stripes.", "ज़ेबरा पर काली और सफ़ेद धारियाँ होती हैं।"),
        w("🐪", "camel", "ऊँट", "The camel can walk in the hot desert.", "ऊँट गरम रेगिस्तान में चल सकता है।"),
        w("🐖", "pig", "सूअर", "The pig likes to roll in the mud.", "सूअर को कीचड़ में लोटना अच्छा लगता है।"),
    )

    val birds = listOf(
        w("🐦", "bird", "चिड़िया", "The bird flies in the sky.", "चिड़िया आसमान में उड़ती है।"),
        w("🦚", "peacock", "मोर", "The peacock dances in the rain.", "मोर बारिश में नाचता है।"),
        w("🦜", "parrot", "तोता", "The parrot can learn to say words.", "तोता बोलना सीख सकता है।"),
        w("🐓", "rooster", "मुर्गा", "The rooster wakes everyone up in the morning.", "मुर्गा सुबह सबको जगाता है।"),
        w("🐔", "hen", "मुर्गी", "The hen lays eggs.", "मुर्गी अंडे देती है।"),
        w("🐥", "chick", "चूज़ा", "The chick comes out of an egg.", "चूज़ा अंडे से निकलता है।"),
        w("🦆", "duck", "बतख", "The duck swims in the pond.", "बतख तालाब में तैरती है।"),
        w("🕊️", "dove", "कबूतर", "The white dove is a sign of peace.", "सफ़ेद कबूतर शांति का निशान है।"),
        w("🦉", "owl", "उल्लू", "The owl stays awake at night.", "उल्लू रात में जागता है।"),
        w("🦅", "eagle", "चील", "The eagle flies very high.", "चील बहुत ऊँचा उड़ती है।"),
        w("🦢", "swan", "हंस", "The swan has a long neck.", "हंस की गर्दन लंबी होती है।"),
        w("🦩", "flamingo", "राजहंस", "The flamingo is pink and stands on one leg.", "राजहंस गुलाबी होता है और एक पैर पर खड़ा होता है।"),
        w("🐧", "penguin", "पेंगुइन", "The penguin cannot fly, but it swims very well.", "पेंगुइन उड़ नहीं सकता, पर बहुत अच्छा तैरता है।"),
    )

    val vehicles = listOf(
        w("🚗", "car", "कार", "A car has four wheels.", "कार के चार पहिए होते हैं।"),
        w("🚌", "bus", "बस", "A bus carries lots of people.", "बस में बहुत सारे लोग बैठते हैं।"),
        w("🛺", "auto rickshaw", "ऑटो रिक्शा", "An auto rickshaw has three wheels.", "ऑटो रिक्शा के तीन पहिए होते हैं।"),
        w("🏍️", "motorbike", "मोटरसाइकिल", "A motorbike has two wheels.", "मोटरसाइकिल के दो पहिए होते हैं।"),
        w("🛵", "scooter", "स्कूटर", "A scooter has two small wheels.", "स्कूटर के दो छोटे पहिए होते हैं।"),
        w("🚲", "bicycle", "साइकिल", "We pedal a bicycle with our feet.", "साइकिल पैरों से चलाते हैं।"),
        w("🚚", "truck", "ट्रक", "A truck carries heavy things.", "ट्रक भारी सामान ले जाता है।"),
        w("🚜", "tractor", "ट्रैक्टर", "A tractor works on the farm.", "ट्रैक्टर खेत में काम करता है।"),
        w("🚆", "train", "रेलगाड़ी", "A train runs on tracks.", "रेलगाड़ी पटरी पर चलती है।"),
        w("✈️", "aeroplane", "हवाई जहाज़", "An aeroplane flies in the sky.", "हवाई जहाज़ आसमान में उड़ता है।"),
        w("🚁", "helicopter", "हेलीकॉप्टर", "A helicopter has a big fan on top.", "हेलीकॉप्टर के ऊपर बड़ा पंखा होता है।"),
        w("⛵", "boat", "नाव", "A boat floats on water.", "नाव पानी पर तैरती है।"),
        w("🚢", "ship", "जहाज़", "A ship sails on the sea.", "जहाज़ समुद्र में चलता है।"),
        w("🚑", "ambulance", "एम्बुलेंस", "An ambulance takes sick people to hospital.", "एम्बुलेंस बीमार लोगों को अस्पताल ले जाती है।"),
        w("🚒", "fire engine", "दमकल", "A fire engine puts out fires.", "दमकल आग बुझाती है।"),
        w("🚓", "police car", "पुलिस की गाड़ी", "A police car has a loud siren.", "पुलिस की गाड़ी में तेज़ सायरन होता है।"),
        w("🚀", "rocket", "रॉकेट", "A rocket goes up into space.", "रॉकेट अंतरिक्ष में जाता है।"),
    )

    val body = listOf(
        w("👁️", "eye", "आँख", "We see with our eyes.", "हम आँखों से देखते हैं।", article = "an"),
        w("👂", "ear", "कान", "We hear with our ears.", "हम कानों से सुनते हैं।"),
        w("👃", "nose", "नाक", "We smell with our nose.", "हम नाक से सूँघते हैं।"),
        w("👄", "mouth", "मुँह", "We eat with our mouth.", "हम मुँह से खाते हैं।"),
        w("👅", "tongue", "जीभ", "We taste with our tongue.", "हम जीभ से स्वाद लेते हैं।"),
        w("🦷", "tooth", "दाँत", "Brush your teeth every day.", "रोज़ दाँत साफ़ करो।"),
        w("✋", "hand", "हाथ", "We hold things with our hands.", "हम हाथों से चीज़ें पकड़ते हैं।"),
        w("☝️", "finger", "उँगली", "We have ten fingers.", "हमारी दस उँगलियाँ होती हैं।"),
        w("💪", "arm", "बाँह", "Strong arms can lift things.", "मज़बूत बाँहें चीज़ें उठा सकती हैं।", article = "an"),
        w("🦵", "leg", "टाँग", "We run with our legs.", "हम टाँगों से दौड़ते हैं।"),
        w("🦶", "foot", "पैर", "We walk on our feet.", "हम पैरों से चलते हैं।"),
        w("🧠", "brain", "दिमाग़", "We think with our brain.", "हम दिमाग़ से सोचते हैं।"),
    )

    val fruits = listOf(
        w("🍎", "apple", "सेब", "The apple is red and sweet.", "सेब लाल और मीठा होता है।"),
        w("🍌", "banana", "केला", "We peel a banana before we eat it.", "केला छीलकर खाते हैं।"),
        w("🥭", "mango", "आम", "The mango is called the king of fruits.", "आम को फलों का राजा कहते हैं।"),
        w("🍇", "grapes", "अंगूर", "Grapes grow in bunches.", "अंगूर गुच्छों में लगते हैं।", plural = true),
        w("🍊", "orange", "संतरा", "The orange is full of juice.", "संतरा रस से भरा होता है।"),
        w("🍉", "watermelon", "तरबूज़", "The watermelon is green outside and red inside.", "तरबूज़ बाहर से हरा और अंदर से लाल होता है।"),
        w("🍍", "pineapple", "अनानास", "The pineapple has a crown of leaves.", "अनानास के ऊपर पत्तों का ताज होता है।"),
        w("🥥", "coconut", "नारियल", "There is water inside a coconut.", "नारियल के अंदर पानी होता है।"),
        w("🍋", "lemon", "नींबू", "The lemon tastes sour.", "नींबू खट्टा होता है।"),
        w("🍐", "pear", "नाशपाती", "The pear is sweet and crunchy.", "नाशपाती मीठी और कुरकुरी होती है।"),
        w("🍓", "strawberry", "स्ट्रॉबेरी", "The strawberry has tiny seeds on the outside.", "स्ट्रॉबेरी के बाहर छोटे-छोटे बीज होते हैं।"),
        w("🍒", "cherries", "चेरी", "Cherries are small and red.", "चेरी छोटी और लाल होती हैं।", plural = true),
        w("🍑", "peach", "आड़ू", "The peach is soft and juicy.", "आड़ू मुलायम और रसीला होता है।"),
        w("🥝", "kiwi", "कीवी", "The kiwi is brown outside and green inside.", "कीवी बाहर से भूरा और अंदर से हरा होता है।"),
        w("🍈", "melon", "ख़रबूज़ा", "The melon smells sweet when it is ripe.", "पका ख़रबूज़ा मीठी ख़ुशबू देता है।"),
        w("🫐", "blueberries", "ब्लूबेरी", "Blueberries are tiny and blue.", "ब्लूबेरी छोटी और नीली होती हैं।", plural = true),
        w("🥑", "avocado", "एवोकाडो", "The avocado has one big seed inside.", "एवोकाडो के अंदर एक बड़ा बीज होता है।", article = "an"),
        w("🫒", "olive", "जैतून", "Oil is made from olives.", "जैतून से तेल बनता है।", article = "an"),
    )

    val vegetables = listOf(
        w("🥔", "potato", "आलू", "Chips are made from potatoes.", "आलू से चिप्स बनते हैं।"),
        w("🍅", "tomato", "टमाटर", "The tomato is red and juicy.", "टमाटर लाल और रसीला होता है।"),
        w("🧅", "onion", "प्याज़", "Cutting an onion can make you cry.", "प्याज़ काटने से आँसू आते हैं।"),
        w("🥕", "carrot", "गाजर", "The carrot grows under the ground.", "गाजर ज़मीन के नीचे उगती है।"),
        w("🍆", "brinjal", "बैंगन", "The brinjal is purple.", "बैंगन बैंगनी रंग का होता है।"),
        w("🌽", "corn", "भुट्टा", "Roasted corn is yummy in the rain.", "बारिश में भुना भुट्टा बहुत अच्छा लगता है।", article = ""),
        w("🥒", "cucumber", "खीरा", "The cucumber is cool and crunchy.", "खीरा ठंडा और कुरकुरा होता है।"),
        w("🌶️", "chilli", "मिर्च", "The chilli is very hot!", "मिर्च बहुत तीखी होती है!"),
        w("🫑", "capsicum", "शिमला मिर्च", "The capsicum is not hot like a chilli.", "शिमला मिर्च तीखी नहीं होती।"),
        w("🧄", "garlic", "लहसुन", "Garlic has a strong smell.", "लहसुन की गंध तेज़ होती है।", article = ""),
        w("🥦", "broccoli", "ब्रोकली", "Broccoli looks like a little tree.", "ब्रोकली छोटे पेड़ जैसी दिखती है।", article = ""),
        w("🍄", "mushroom", "मशरूम", "The mushroom looks like a little umbrella.", "मशरूम छोटे छाते जैसा दिखता है।"),
        w("🍠", "sweet potato", "शकरकंद", "The sweet potato is sweet when it is roasted.", "भुना शकरकंद मीठा लगता है।"),
        // No emoji shows these well, or at all, so they are drawn: see art/README.md.
        w("art:okra", "okra", "भिंडी", "Okra has sticky seeds inside.", "भिंडी के अंदर चिपचिपे बीज होते हैं।", article = "", pluralHi = false),
        w("art:bottle-gourd", "bottle gourd", "लौकी", "A bottle gourd is long and full of water.", "लौकी लंबी होती है और उसमें बहुत पानी होता है।"),
        w("art:bitter-gourd", "bitter gourd", "करेला", "Bitter gourd tastes bitter, but it is good for you.", "करेला कड़वा होता है, पर सेहत के लिए अच्छा है।"),
        w("art:cauliflower", "cauliflower", "फूलगोभी", "A cauliflower is made of little white florets.", "फूलगोभी छोटे-छोटे सफ़ेद फूलों से बनी होती है।"),
        w("art:spinach", "spinach", "पालक", "Spinach leaves make us strong.", "पालक के पत्ते हमें ताक़त देते हैं।", article = ""),
        w("art:radish", "radish", "मूली", "A radish is a white root that grows under the ground.", "मूली ज़मीन के नीचे उगने वाली सफ़ेद जड़ है।"),
        w("art:pumpkin", "pumpkin", "कद्दू", "A pumpkin can grow very, very big.", "कद्दू बहुत बड़ा हो सकता है।"),
        w("art:peas", "peas", "मटर", "Peas grow inside a pod.", "मटर के दाने फली के अंदर होते हैं।", plural = true, pluralHi = false),
        w("art:ginger", "ginger", "अदरक", "We put ginger in tea.", "चाय में अदरक डालते हैं।", article = ""),
        w("art:beetroot", "beetroot", "चुकंदर", "Beetroot turns everything pink!", "चुकंदर हर चीज़ को गुलाबी कर देता है!", article = ""),
        w("art:kidney-beans", "kidney beans", "राजमा", "Rajma and rice make a yummy meal.", "राजमा-चावल बहुत स्वादिष्ट होते हैं।", plural = true, pluralHi = false),
    )

    val home = listOf(
        w("🛏️", "bed", "बिस्तर", "We sleep on a bed.", "हम बिस्तर पर सोते हैं।"),
        w("🪑", "chair", "कुर्सी", "We sit on a chair.", "हम कुर्सी पर बैठते हैं।"),
        w("🚪", "door", "दरवाज़ा", "Close the door gently.", "दरवाज़ा धीरे से बंद करो।"),
        w("🪟", "window", "खिड़की", "Light comes in through the window.", "खिड़की से रोशनी आती है।"),
        w("🥄", "spoon", "चम्मच", "We eat with a spoon.", "हम चम्मच से खाते हैं।"),
        w("🍽️", "plate", "थाली", "We eat our food from a plate.", "हम थाली में खाना खाते हैं।"),
        w("🪥", "toothbrush", "टूथब्रश", "Brush your teeth morning and night.", "सुबह और रात को ब्रश करो।"),
        w("🧼", "soap", "साबुन", "Wash your hands with soap.", "साबुन से हाथ धोओ।", article = ""),
        w("🪣", "bucket", "बाल्टी", "We fill the bucket with water.", "बाल्टी में पानी भरते हैं।"),
        w("🧹", "broom", "झाड़ू", "We sweep the floor with a broom.", "झाड़ू से फ़र्श साफ़ करते हैं।"),
        w("☂️", "umbrella", "छाता", "An umbrella keeps us dry in the rain.", "छाता बारिश में हमें भीगने से बचाता है।"),
        w("🔑", "key", "चाबी", "A key opens a lock.", "चाबी से ताला खुलता है।"),
        w("💡", "bulb", "बल्ब", "A bulb lights up the room.", "बल्ब से कमरे में रोशनी होती है।"),
        w("⏰", "clock", "घड़ी", "The clock tells us the time.", "घड़ी हमें समय बताती है।"),
        w("📱", "phone", "फ़ोन", "We talk to Grandma on the phone.", "हम फ़ोन पर दादी से बात करते हैं।"),
        w("📺", "TV", "टीवी", "A TV has a big screen.", "टीवी की स्क्रीन बड़ी होती है।"),
        w("📚", "books", "किताबें", "Books are full of stories.", "किताबों में कहानियाँ होती हैं।", plural = true),
        w("🧸", "teddy bear", "टेडी बियर", "A teddy bear is soft to hug.", "टेडी बियर को गले लगाना अच्छा लगता है।"),
    )

    val clothes = listOf(
        w("👕", "T-shirt", "टी-शर्ट", "A T-shirt has short sleeves.", "टी-शर्ट की बाँहें छोटी होती हैं।"),
        w("👖", "jeans", "जींस", "Jeans are made of thick cloth.", "जींस मोटे कपड़े की होती है।", plural = true, pluralHi = false),
        w("🩳", "shorts", "निक्कर", "We wear shorts when it is hot.", "गर्मी में निक्कर पहनते हैं।", plural = true, pluralHi = false),
        w("👗", "dress", "फ़्रॉक", "We wear a pretty dress to a party.", "पार्टी में सुंदर फ़्रॉक पहनते हैं।"),
        w("🥻", "sari", "साड़ी", "A sari is very, very long.", "साड़ी बहुत लंबी होती है।"),
        w("🧢", "cap", "टोपी", "A cap keeps the sun off our face.", "टोपी चेहरे को धूप से बचाती है।"),
        w("👟", "shoe", "जूता", "We wear shoes when we go out.", "बाहर जाते समय जूते पहनते हैं।"),
        w("🩴", "slipper", "चप्पल", "We wear slippers at home.", "घर में चप्पल पहनते हैं।"),
        w("🧦", "socks", "मोज़े", "We wear socks inside our shoes.", "जूतों के अंदर मोज़े पहनते हैं।", plural = true),
        w("🧥", "coat", "कोट", "A coat keeps us warm in winter.", "कोट सर्दी में हमें गरम रखता है।"),
        w("🧣", "scarf", "मफ़लर", "A scarf keeps our neck warm.", "मफ़लर गले को गरम रखता है।"),
        w("🧤", "gloves", "दस्ताने", "Gloves keep our hands warm.", "दस्ताने हाथों को गरम रखते हैं।", plural = true),
        w("👓", "glasses", "चश्मा", "Glasses help us see clearly.", "चश्मे से साफ़ दिखता है।", plural = true, pluralHi = false),
    )

    /**
     * Computer parts, for children who see a grown-up's computer every day. From age 4,
     * so the cards come with their fact, and up to 8.
     */
    val computer = listOf(
        w("🖥️", "computer", "कंप्यूटर", "The screen of a computer is called a monitor.", "कंप्यूटर की स्क्रीन को मॉनिटर कहते हैं।"),
        w("⌨️", "keyboard", "कीबोर्ड", "We type letters with a keyboard.", "कीबोर्ड से अक्षर टाइप करते हैं।"),
        w("🖱️", "mouse", "माउस", "A mouse moves the arrow on the screen.", "माउस से स्क्रीन पर तीर चलता है।"),
        w("💻", "laptop", "लैपटॉप", "A laptop folds shut and goes in a bag.", "लैपटॉप बंद करके बैग में रख सकते हैं।"),
        w("🖨️", "printer", "प्रिंटर", "A printer puts what is on the screen onto paper.", "प्रिंटर स्क्रीन की चीज़ काग़ज़ पर छाप देता है।"),
        w("🎧", "headphones", "हेडफ़ोन", "With headphones, only you hear the sound.", "हेडफ़ोन से आवाज़ सिर्फ़ तुम्हें सुनाई देती है।", plural = true, pluralHi = false),
        w("🔈", "speaker", "स्पीकर", "A speaker plays the sound out loud.", "स्पीकर से आवाज़ ज़ोर से आती है।"),
        w("🎤", "microphone", "माइक", "A microphone lets the computer hear you.", "माइक से कंप्यूटर तुम्हारी आवाज़ सुनता है।"),
        w("📷", "camera", "कैमरा", "A camera takes photos.", "कैमरा फ़ोटो खींचता है।"),
        w("🎮", "game controller", "गेम कंट्रोलर", "We play video games with a game controller.", "गेम कंट्रोलर से वीडियो गेम खेलते हैं।"),
        w("🔌", "plug", "प्लग", "A plug brings electricity from the wall. Never touch it with wet hands!", "प्लग दीवार से बिजली लाता है। गीले हाथों से कभी मत छूना!"),
        w("🔋", "battery", "बैटरी", "A battery keeps electricity inside it.", "बैटरी के अंदर बिजली भरी रहती है।"),
    )

    /** What each computer part is for, asked the other way round: "Which one do we type with?". */
    val computerUses: Map<String, Txt> = mapOf(
        "🖥️" to txt("Which one is a computer with a screen?", "स्क्रीन वाला कंप्यूटर कौन-सा है?"),
        "⌨️" to txt("Which one do we type letters with?", "अक्षर किससे टाइप करते हैं?"),
        "🖱️" to txt("Which one moves the arrow on the screen?", "स्क्रीन पर तीर किससे चलाते हैं?"),
        "💻" to txt("Which computer folds shut and goes in a bag?", "कौन-सा कंप्यूटर बंद करके बैग में जाता है?"),
        "🖨️" to txt("Which one puts pictures onto paper?", "तस्वीर काग़ज़ पर कौन छापता है?"),
        "🎧" to txt("Which do we wear to listen on our own?", "अकेले सुनने के लिए क्या पहनते हैं?"),
        "🔈" to txt("Which one plays the sound out loud?", "आवाज़ ज़ोर से कौन सुनाता है?"),
        "🎤" to txt("Which one lets the computer hear you?", "कंप्यूटर तुम्हारी आवाज़ किससे सुनता है?"),
        "📷" to txt("Which one takes photos?", "फ़ोटो किससे खींचते हैं?"),
        "🎮" to txt("Which one do we play video games with?", "वीडियो गेम किससे खेलते हैं?"),
        "🔌" to txt("Which one brings electricity from the wall?", "दीवार से बिजली कौन लाता है?"),
        "🔋" to txt("Which one keeps electricity inside it?", "किसके अंदर बिजली भरी रहती है?"),
    )

    /** The desktop and the laptop: when "What is it for?" asks for one, the other is not a choice. */
    val twoComputers = setOf("🖥️", "💻")

    /** Which list each picture-card topic shows. */
    val byTopic: Map<Topic, List<Word>> = mapOf(
        Topic.ANIMALS to animals,
        Topic.BIRDS to birds,
        Topic.VEHICLES to vehicles,
        Topic.BODY to body,
        Topic.FRUITS to fruits,
        Topic.VEGETABLES to vegetables,
        Topic.HOME_THINGS to home,
        Topic.CLOTHES to clothes,
        Topic.COMPUTER to computer,
    )

    /** Picture cards are for ages 2 to 5, except where a topic says otherwise. */
    fun agesOf(topic: Topic): IntRange = if (topic == Topic.COMPUTER) 4..8 else 2..5

    /** Every other picture in the app: in quizzes, as examples, in patterns. Names only. */
    private val others = listOf(
        n("🐶", "dog", "कुत्ता"), n("🐱", "cat", "बिल्ली"), n("🐰", "rabbit", "खरगोश"), n("🐭", "mouse", "चूहा"),
        n("🦊", "fox", "लोमड़ी"), n("🐼", "panda", "पांडा"), n("🐯", "tiger", "बाघ"), n("🐮", "cow", "गाय"),
        n("🐷", "pig", "सूअर"), n("🐵", "monkey", "बंदर"), n("🐴", "horse", "घोड़ा"), n("🐍", "snake", "साँप"),
        n("🐻‍❄️", "polar bear", "सफ़ेद भालू"), n("🐫", "camel", "ऊँट"), n("🐸", "frog", "मेंढक"),
        n("🐟", "fish", "मछली"), n("🐠", "fish", "मछली"), n("🐡", "pufferfish", "फुग्गा मछली"), n("🐙", "octopus", "ऑक्टोपस", article = "an"),
        n("🦀", "crab", "केकड़ा"), n("🐬", "dolphin", "डॉल्फ़िन"), n("🐳", "whale", "व्हेल"), n("🦈", "shark", "शार्क"),
        n("🐝", "bee", "मधुमक्खी"), n("🐞", "ladybird", "लाल कीड़ा"), n("🦋", "butterfly", "तितली"), n("🐜", "ant", "चींटी"),
        n("🐛", "caterpillar", "इल्ली"), n("🦗", "cricket", "झींगुर"),
        n("🚕", "taxi", "टैक्सी"), n("🚂", "train engine", "इंजन"), n("🚐", "van", "वैन"),
        n("⚽", "football", "फ़ुटबॉल"), n("🏀", "basketball", "बास्केटबॉल"), n("🏈", "rugby ball", "रग्बी बॉल"), n("🏉", "rugby ball", "रग्बी बॉल"),
        n("⚾", "baseball", "बेसबॉल"), n("🎾", "tennis ball", "टेनिस बॉल"), n("🏐", "volleyball", "वॉलीबॉल"),
        n("⭐", "star", "तारा"), n("🌟", "star", "तारा"), n("🌙", "moon", "चाँद", article = "the"), n("☀️", "sun", "सूरज", article = "the"),
        n("🌞", "sun", "सूरज", article = "the"), n("☁️", "cloud", "बादल"), n("🌍", "Earth", "पृथ्वी", article = "the"),
        n("🪐", "planet Saturn", "शनि ग्रह", article = "the"), n("💧", "drop of water", "पानी की बूँद"),
        n("🌸", "flower", "फूल"), n("🌻", "sunflower", "सूरजमुखी"), n("🌳", "tree", "पेड़"), n("🌴", "palm tree", "ताड़ का पेड़"),
        n("🌊", "sea", "समुद्र", article = "the"), n("🏜️", "desert", "रेगिस्तान"), n("🧊", "ice", "बर्फ़", article = ""),
        n("🌾", "farm", "खेत"), n("🏠", "house", "घर"), n("🏫", "school", "स्कूल"),
n("🦴", "bone", "हड्डी"), n("🥛", "milk", "दूध", article = ""), n("🧀", "cheese", "चीज़", article = ""),
        n("🍯", "honey", "शहद", article = ""), n("🌿", "grass", "घास", article = ""), n("🎋", "bamboo", "बाँस", article = ""),
        n("🧺", "basket", "टोकरी"), n("🐨", "koala", "कोआला"),
        n("🦄", "unicorn", "यूनिकॉर्न", article = "a"), n("🐹", "hamster", "हैम्स्टर"), n("👸", "queen", "रानी"),
                n("🎈", "balloon", "गुब्बारा"), n("🧁", "cupcake", "केक"), n("🍪", "biscuit", "बिस्कुट"), n("🍩", "doughnut", "डोनट"),
        n("🧇", "waffle", "वफ़ल"), n("🍕", "slice of pizza", "पिज़्ज़ा"), n("🥐", "croissant", "क्रोसाँ"), n("🍦", "ice cream", "आइसक्रीम", article = "an"),
        n("🥚", "egg", "अंडा", article = "an"), n("🥫", "tin", "डिब्बा"), n("🍏", "green apple", "हरा सेब"),
        n("🕐", "clock", "घड़ी"), n("⌚", "watch", "घड़ी"), n("⛺", "tent", "तंबू"),
        n("⚠️", "warning sign", "ख़तरे का निशान"), n("🛑", "stop sign", "रुकने का निशान"), n("🎄", "Christmas tree", "क्रिसमस ट्री"),
        n("💌", "love letter", "प्यार भरी चिट्ठी"), n("💎", "diamond", "हीरा"), n("🪁", "kite", "पतंग"), n("🔩", "nut and bolt", "नट-बोल्ट"),
        n("🎲", "dice", "पासा"), n("🎉", "party popper", "पार्टी पॉपर"), n("📦", "box", "डिब्बा"),
n("🌵", "cactus", "नागफनी"), n("🪨", "stone", "पत्थर"),
        n("🧱", "brick", "ईंट"), n("🎡", "big wheel", "बड़ा झूला"), n("🪀", "yo-yo", "यो-यो"), n("🥁", "drum", "ढोल"),
        n("🏹", "bow and arrow", "धनुष-बाण"), n("🚰", "tap", "नल"), n("👜", "bag", "थैला"), n("🐚", "conch", "शंख"),
        n("🇮🇳", "flag of India", "भारत का झंडा", article = "the"), n("🔱", "trident", "त्रिशूल"), n("☔", "umbrella", "छाता", article = "an"),
        colour("🔴", Colours.all[0]), colour("🔵", Colours.all[1]), colour("🟡", Colours.all[2]), colour("🟢", Colours.all[3]),
        colour("🟥", Colours.all[0]), colour("🟦", Colours.all[1]), colour("🟨", Colours.all[2]), colour("🟩", Colours.all[3]),
    )

    private val table: Map<String, Word> =
        (others + byTopic.values.flatten()).associateBy { key(it.emoji) }

    /** The name for [emoji], or null if the app has none. Variation selectors do not matter. */
    fun of(emoji: String): Word? = table[key(emoji)]

    private fun key(emoji: String) = emoji.replace("️", "")
}
