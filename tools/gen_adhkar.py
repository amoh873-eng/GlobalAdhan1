# Generates adhkar.json with authentic morning/evening adhkar.
import json

MORNING = [
    ("آية الكرسي", "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ", "Ayat al-Kursi (2:255)", "Whoever recites it in the morning is protected until evening.", 1, "متفق عليه"),
    ("سورة الإخلاص والمعوذتين", "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Al-Ikhlas, Al-Falaq, An-Nas x3", "Reciting these three surahs three times morning and evening suffices against everything.", 3, "أبو داود والترمذي"),
    ("سيد الاستغفار", "اللَّهُمَّ أَنتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِن شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنتَ", "Sayyid al-Istighfar", "Whoever says it in the morning with certainty of faith and dies that day enters Paradise.", 1, "البخاري"),
    ("اللهم بك أصبحنا", "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ", "O Allah, by You we enter the morning...", "Morning remembrance.", 1, "الترمذي"),
    ("اللهم أنت ربي لا إله إلا أنت", "اللَّهُمَّ أَنتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنتَ عَلَيْكَ تَوَكَّلْتُ وَأَنتَ رَبُّ الْعَرْشِ الْعَظِيمِ", "O Allah, You are my Lord...", "Morning remembrance.", 1, "الترمذي"),
    ("اللهم ما أصبح بي من نعمة", "اللَّهُمَّ مَا أَصْبَحَ بِي مِن نِّعْمَةٍ أَوْ بِأَحَدٍ مِّنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ", "O Allah, whatever blessing...", "Morning remembrance.", 1, "أبو داود"),
    ("اللهم عافني في بدني", "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَٰهَ إِلَّا أَنتَ", "O Allah, grant my body health...", "Morning remembrance x3.", 3, "أبو داود"),
    ("اللهم إني أعوذ بك من الكفر والفقر", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ وَالْفَقْرِ، اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، لَا إِلَٰهَ إِلَّا أَنتَ", "O Allah, I seek refuge in You from disbelief and poverty...", "Morning remembrance x3.", 3, "أبو داود"),
    ("حسبي الله لا إله إلا هو", "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ", "Allah is sufficient for me...", "Whoever says it seven times morning and evening, Allah suffices him.", 7, "أبو داود"),
    ("بسم الله الذي لا يضر معه شيء", "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَهُ اسْمُهُ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ", "In the name of Allah, with whose name nothing harms...", "Nothing will harm him, x3 morning and evening.", 3, "أبو داود والترمذي"),
    ("رضيت بالله ربا", "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا", "I am pleased with Allah as my Lord...", "It is a right upon Allah to please him on the Day of Judgment, x3.", 3, "أبو داود والترمذي"),
    ("يا حي يا قيوم برحمتك أستغيث", "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ", "O Ever-Living, O Sustainer, by Your mercy I seek help...", "Morning remembrance.", 1, "الحاكم"),
    ("أصبحنا وأصبح الملك لله", "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ", "We have entered the morning and the dominion belongs to Allah...", "Morning remembrance.", 1, "مسلم"),
    ("اللهم إني أسألك العفو والعافية", "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ", "O Allah, I ask You for pardon and well-being in this world and the Hereafter.", "Morning remembrance.", 1, "ابن ماجه"),
    ("اللهم إني أسألك علما نافعا", "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا", "O Allah, I ask You for beneficial knowledge...", "Morning remembrance.", 1, "ابن ماجه"),
    ("أعوذ بكلمات الله التامات", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِن شَرِّ مَا خَلَقَ", "I seek refuge in the perfect words of Allah from the evil of what He created.", "Nothing will harm him, x3.", 3, "مسلم"),
    ("لا إله إلا الله وحده لا شريك له", "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "There is no god but Allah alone...", "Whoever says it 100 times in a day, it is like freeing ten slaves.", 100, "متفق عليه"),
    ("سبحان الله وبحمده", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Glory and praise be to Allah.", "Whoever says it 100 times, his sins are forgiven even if like the foam of the sea.", 100, "متفق عليه"),
    ("لا إله إلا أنت سبحانك إني كنت من الظالمين", "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ", "There is no god but You, glory to You, indeed I was among the wrongdoers.", "Du'a of Yunus; whoever calls upon it, Allah answers.", 1, "الترمذي"),
    ("اللهم صل وسلم على نبينا محمد", "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَىٰ نَبِيِّنَا مُحَمَّدٍ", "O Allah, send peace and blessings upon our Prophet Muhammad.", "Whoever sends blessings upon the Prophet once, Allah sends ten upon him.", 10, "مسلم"),
    ("اللهم بك نعوذ من زوال نعمتك", "اللَّهُمَّ إِنَّا نَعُوذُ بِكَ مِنْ زَوَالِ نِعْمَتِكَ، وَتَحَوُّلِ عَافِيَتِكَ، وَفُجَاءَةِ نِقْمَتِكَ، وَجَمِيعِ سَخَطِكَ", "O Allah, we seek refuge in You from the loss of Your blessings...", "Morning remembrance.", 1, "مسلم"),
    ("اللهم إني أعوذ بك من الهم والحزن", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْجُبْنِ وَالْبُخْلِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ", "O Allah, I seek refuge in You from anxiety and sorrow...", "Morning remembrance.", 1, "البخاري"),
    ("اللهم اكفني بحلالك عن حرامك", "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّن سِوَاكَ", "O Allah, suffice me with what is lawful over what is forbidden...", "Morning remembrance.", 1, "الترمذي"),
    ("حسبنا الله ونعم الوكيل", "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ", "Allah is sufficient for us, and He is the best disposer of affairs.", "Morning remembrance.", 1, "البخاري"),
    ("اللهم إنك عفو تحب العفو فاعف عني", "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي", "O Allah, You are Forgiving and love forgiveness, so forgive me.", "Morning remembrance.", 1, "الترمذي"),
    ("سبحان الله وبحمده عدد خلقه", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ", "Glory to Allah and praise to Him, as many as His creation...", "Morning remembrance x3.", 3, "مسلم"),
    ("أستغفر الله وأتوب إليه", "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", "I seek forgiveness from Allah and repent to Him.", "Whoever says it 100 times a day is forgiven even if he fled from battle.", 100, "البخاري"),
    ("اللهم أجرني من النار", "اللَّهُمَّ أَجِرْنِي مِنَ النَّارِ", "O Allah, protect me from the Fire.", "Whoever says it seven times morning and evening, Allah protects him from the Fire.", 7, "أبو داود"),
    ("اللهم إني أصبحت أشهدك", "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلَائِكَتَكَ، وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنتَ اللَّهُ لَا إِلَٰهَ إِلَّا أَنتَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ", "O Allah, I have entered the morning bearing witness...", "Whoever says it four times, Allah frees him from the Fire.", 4, "أبو داود"),
    ("قراءة المعوذات", "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ، قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ...", "Al-Falaq and An-Nas", "Reciting Al-Falaq and An-Nas three times in the morning protects from harm.", 3, "أبو داود والترمذي"),
]

EVENING = [
    ("اللهم بك أمسينا", "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ", "O Allah, by You we enter the evening...", "Evening remembrance.", 1, "الترمذي"),
    ("اللهم ما أمسى بي من نعمة", "اللَّهُمَّ مَا أَمْسَىٰ بِي مِن نِّعْمَةٍ أَوْ بِأَحَدٍ مِّنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ", "O Allah, whatever blessing has come to me this evening...", "Evening remembrance.", 1, "أبو داود"),
    ("أمسينا وأمسى الملك لله", "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ", "We have entered the evening and the dominion belongs to Allah...", "Evening remembrance.", 1, "مسلم"),
    ("اللهم إني أمسيت أشهدك", "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ... أَنَّكَ أَنتَ اللَّهُ لَا إِلَٰهَ إِلَّا أَنتَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ", "O Allah, I have entered the evening bearing witness...", "Whoever says it four times, Allah frees him from the Fire.", 4, "أبو داود"),
    ("اللهم عافني في بدني", "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَٰهَ إِلَّا أَنتَ", "O Allah, grant my body health...", "Evening remembrance x3.", 3, "أبو داود"),
    ("اللهم إني أعوذ بك من عذاب القبر", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، وَمِنْ فِتْنَةِ الْمَحْيَا وَالْمَمَاتِ، وَمِنْ شَرِّ فِتْنَةِ الْمَسِيحِ الدَّجَّالِ", "O Allah, I seek refuge in You from the punishment of the grave...", "Evening remembrance.", 1, "النسائي"),
    ("أعوذ بكلمات الله التامات", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِن شَرِّ مَا خَلَقَ", "I seek refuge in the perfect words of Allah from the evil of what He created.", "Nothing will harm him, x3.", 3, "مسلم"),
    ("حسبي الله لا إله إلا هو", "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ", "Allah is sufficient for me...", "Whoever says it seven times, Allah suffices him.", 7, "أبو داود"),
    ("بسم الله الذي لا يضر معه شيء", "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَهُ اسْمُهُ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ", "In the name of Allah, with whose name nothing harms...", "Nothing will harm him, x3.", 3, "أبو داود والترمذي"),
    ("رضيت بالله ربا", "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا", "I am pleased with Allah as my Lord...", "Allah's right upon Him is to please him, x3.", 3, "أبو داود والترمذي"),
    ("اللهم بك نعوذ من زوال نعمتك", "اللَّهُمَّ إِنَّا نَعُوذُ بِكَ مِنْ زَوَالِ نِعْمَتِكَ، وَتَحَوُّلِ عَافِيَتِكَ، وَفُجَاءَةِ نِقْمَتِكَ، وَجَمِيعِ سَخَطِكَ", "O Allah, we seek refuge in You from the loss of Your blessings...", "Evening remembrance.", 1, "مسلم"),
    ("اللهم أجرني من النار", "اللَّهُمَّ أَجِرْنِي مِنَ النَّارِ", "O Allah, protect me from the Fire.", "Whoever says it seven times, Allah protects him from the Fire.", 7, "أبو داود"),
    ("يا حي يا قيوم برحمتك أستغيث", "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ", "O Ever-Living, O Sustainer, by Your mercy I seek help...", "Evening remembrance.", 1, "الحاكم"),
    ("اللهم إني أعوذ بك من الهم والحزن", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْجُبْنِ وَالْبُخْلِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ", "O Allah, I seek refuge in You from anxiety and sorrow...", "Evening remembrance.", 1, "البخاري"),
    ("اللهم إني أسألك العفو والعافية", "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ", "O Allah, I ask You for pardon and well-being...", "Evening remembrance.", 1, "ابن ماجه"),
    ("سيد الاستغفار", "اللَّهُمَّ أَنتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنتَ... فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنتَ", "Sayyid al-Istighfar", "Whoever says it in the evening with certainty and dies that night enters Paradise.", 1, "البخاري"),
    ("أستغفر الله وأتوب إليه", "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", "I seek forgiveness from Allah and repent to Him.", "Evening remembrance.", 100, "البخاري"),
    ("سبحان الله وبحمده", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Glory and praise be to Allah.", "Evening remembrance.", 100, "متفق عليه"),
    ("لا إله إلا الله وحده لا شريك له", "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "There is no god but Allah alone...", "Evening remembrance.", 100, "متفق عليه"),
    ("اللهم صل وسلم على نبينا محمد", "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَىٰ نَبِيِّنَا مُحَمَّدٍ", "O Allah, send peace and blessings upon our Prophet Muhammad.", "Evening remembrance.", 10, "مسلم"),
    ("قراءة المعوذات", "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ... وَقُلْ أَعُوذُ بِرَبِّ النَّاسِ...", "Al-Falaq and An-Nas", "Reciting them three times in the evening protects from harm.", 3, "أبو داود والترمذي"),
]

def build(category, items):
    # Tuple shape: (title, arabic, translation, reward, repetition, reference)
    return [{
        "category": category, "title": t, "arabicText": ar,
        "transliteration": None, "translation": tr,
        "repetitionCount": rep, "reward": rw, "reference": ref
    } for t, ar, tr, rw, rep, ref in items]

data = {"adhkar": build("morning", MORNING) + build("evening", EVENING)}
with open("app/src/main/assets/adhkar.json", "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=1)
print("morning:", len(MORNING), "evening:", len(EVENING), "total:", len(MORNING) + len(EVENING))
