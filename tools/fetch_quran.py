"""
Downloads the complete Quran (Arabic text) from the Tanzil project
and converts it to the JSON format expected by GlobalAdhan's
QuranRepository.importFromAsset().

Data source: Tanzil.net (public domain Uthmani script)
This script is run once during development to produce app/src/main/assets/quran.json.

Usage:
    python tools/fetch_quran.py [--output app/src/main/assets/quran.json]

Requirements:
    pip install requests
"""

import argparse
import json
import sys
import urllib.request

TANZIL_BASE = "https://tanzil.net/trans/?type=simple"
# Quran.com's AlQuran Cloud API provides per-ayah juz/page metadata for free
API_BASE = "https://api.alquran.cloud/v1/surah/{number}?offset=0&limit=999"

# Surah names (Arabic, transliterated, translation) - metadata only
SURAH_NAMES = [
    ("الفاتحة", "Al-Fatihah", "The Opening"),
    ("البقرة", "Al-Baqarah", "The Cow"),
    ("آل عمران", "Aal-Imran", "The Family of Imran"),
    ("النساء", "An-Nisa", "The Women"),
    ("المائدة", "Al-Ma'idah", "The Table Spread"),
    ("الأنعام", "Al-An'am", "The Cattle"),
    ("الأعراف", "Al-A'raf", "The Heights"),
    ("الأنفال", "Al-Anfal", "The Spoils of War"),
    ("التوبة", "At-Tawbah", "The Repentance"),
    ("يونس", "Yunus", "Jonah"),
    ("هود", "Hud", "Hud"),
    ("يوسف", "Yusuf", "Joseph"),
    ("الرعد", "Ar-Ra'd", "The Thunder"),
    ("إبراهيم", "Ibrahim", "Abraham"),
    ("الحجر", "Al-Hijr", "The Rocky Tract"),
    ("النحل", "An-Nahl", "The Bee"),
    ("الإسراء", "Al-Isra", "The Night Journey"),
    ("الكهف", "Al-Kahf", "The Cave"),
    ("مريم", "Maryam", "Mary"),
    ("طه", "Ta-Ha", "Ta-Ha"),
    ("الأنبياء", "Al-Anbiya", "The Prophets"),
    ("الحج", "Al-Hajj", "The Pilgrimage"),
    ("المؤمنون", "Al-Mu'minun", "The Believers"),
    ("النور", "An-Nur", "The Light"),
    ("الفرقان", "Al-Furqan", "The Criterion"),
    ("الشعراء", "Ash-Shu'ara", "The Poets"),
    ("النمل", "An-Naml", "The Ant"),
    ("القصص", "Al-Qasas", "The Stories"),
    ("العنكبوت", "Al-Ankabut", "The Spider"),
    ("الروم", "Ar-Rum", "The Romans"),
    ("لقمان", "Luqman", "Luqman"),
    ("السجدة", "As-Sajdah", "The Prostration"),
    ("الأحزاب", "Al-Ahzab", "The Combined Forces"),
    ("سبأ", "Saba", "Sheba"),
    ("فاطر", "Fatir", "The Originator"),
    ("يس", "Ya-Sin", "Ya-Sin"),
    ("الصافات", "As-Saffat", "Those Who Set the Ranks"),
    ("ص", "Sad", "The Letter Sad"),
    ("الزمر", "Az-Zumar", "The Groups"),
    ("غافر", "Ghafir", "The Forgiver"),
    ("فصلت", "Fussilat", "Explained in Detail"),
    ("الشورى", "Ash-Shura", "The Consultation"),
    ("الزخرف", "Az-Zukhruf", "The Ornaments of Gold"),
    ("الدخان", "Ad-Dukhan", "The Smoke"),
    ("الجاثية", "Al-Jathiyah", "The Crouching"),
    ("الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills"),
    ("محمد", "Muhammad", "Muhammad"),
    ("الفتح", "Al-Fath", "The Victory"),
    ("الحجرات", "Al-Hujurat", "The Rooms"),
    ("ق", "Qaf", "The Letter Qaf"),
    ("الذاريات", "Adh-Dhariyat", "The Winnowing Winds"),
    ("الطور", "At-Tur", "The Mount"),
    ("النجم", "An-Najm", "The Star"),
    ("القمر", "Al-Qamar", "The Moon"),
    ("الرحمن", "Ar-Rahman", "The Beneficent"),
    ("الواقعة", "Al-Waqi'ah", "The Inevitable"),
    ("الحديد", "Al-Hadid", "The Iron"),
    ("المجادلة", "Al-Mujadila", "The Pleading Woman"),
    ("الحشر", "Al-Hashr", "The Exile"),
    ("الممتحنة", "Al-Mumtahanah", "She That Is to Be Examined"),
    ("الصف", "As-Saff", "The Ranks"),
    ("الجمعة", "Al-Jumu'ah", "The Congregation"),
    ("المنافقون", "Al-Munafiqun", "The Hypocrites"),
    ("التغابن", "At-Taghabun", "The Mutual Disillusion"),
    ("الطلاق", "At-Talaq", "The Divorce"),
    ("التحريم", "At-Tahrim", "The Prohibition"),
    ("الملك", "Al-Mulk", "The Sovereignty"),
    ("القلم", "Al-Qalam", "The Pen"),
    ("الحاقة", "Al-Haqqah", "The Reality"),
    ("المعارج", "Al-Ma'arij", "The Ascending Stairways"),
    ("نوح", "Nuh", "Noah"),
    ("الجن", "Al-Jinn", "The Jinn"),
    ("المزمل", "Al-Muzzammil", "The Enshrouded One"),
    ("المدثر", "Al-Muddaththir", "The Cloaked One"),
    ("القيامة", "Al-Qiyamah", "The Resurrection"),
    ("الإنسان", "Al-Insan", "The Man"),
    ("المرسلات", "Al-Mursalat", "The Emissaries"),
    ("النبأ", "An-Naba", "The Tidings"),
    ("النازعات", "An-Nazi'at", "Those Who Drag Forth"),
    ("عبس", "Abasa", "He Frowned"),
    ("التكوير", "At-Takwir", "The Overthrowing"),
    ("الانفطار", "Al-Infitar", "The Cleaving"),
    ("المطففين", "Al-Mutaffifin", "The Defrauding"),
    ("الانشقاق", "Al-Inshiqaq", "The Sundering"),
    ("البروج", "Al-Buruj", "The Mansions of the Stars"),
    ("الطارق", "At-Tariq", "The Nightcomer"),
    ("الأعلى", "Al-A'la", "The Most High"),
    ("الغاشية", "Al-Ghashiyah", "The Overwhelming"),
    ("الفجر", "Al-Fajr", "The Dawn"),
    ("البلد", "Al-Balad", "The City"),
    ("الشمس", "Ash-Shams", "The Sun"),
    ("الليل", "Al-Layl", "The Night"),
    ("الضحى", "Ad-Duha", "The Morning Hours"),
    ("الشرح", "Ash-Sharh", "The Relief"),
    ("التين", "At-Tin", "The Fig"),
    ("العلق", "Al-Alaq", "The Clot"),
    ("القدر", "Al-Qadr", "The Power"),
    ("البينة", "Al-Bayyinah", "The Clear Proof"),
    ("الزلزلة", "Az-Zalzalah", "The Earthquake"),
    ("العاديات", "Al-Adiyat", "The Courser"),
    ("القارعة", "Al-Qari'ah", "The Calamity"),
    ("التكاثر", "At-Takathur", "The Rivalry in World Increase"),
    ("العصر", "Al-Asr", "The Declining Day"),
    ("الهمزة", "Al-Humazah", "The Traducer"),
    ("الفيل", "Al-Fil", "The Elephant"),
    ("قريش", "Quraysh", "Quraysh"),
    ("الماعون", "Al-Ma'un", "The Small Kindnesses"),
    ("الكوثر", "Al-Kawthar", "The Abundance"),
    ("الكافرون", "Al-Kafirun", "The Disbelievers"),
    ("النصر", "An-Nasr", "The Divine Support"),
    ("المسد", "Al-Masad", "The Palm Fiber"),
    ("الإخلاص", "Al-Ikhlas", "The Sincerity"),
    ("الفلق", "Al-Falaq", "The Daybreak"),
    ("الناس", "An-Nas", "Mankind"),
]

# Revelation types per surah (M for Meccan, M2 for Medinan)
REVELATION = [
    "M", "M2", "M2", "M2", "M2", "M", "M", "M2", "M2", "M",
    "M", "M", "M2", "M", "M", "M", "M", "M", "M", "M",
    "M", "M2", "M", "M2", "M", "M", "M", "M", "M", "M",
    "M", "M", "M2", "M", "M", "M", "M", "M", "M", "M",
    "M", "M", "M", "M", "M", "M", "M2", "M2", "M2", "M",
    "M", "M", "M", "M", "M", "M", "M2", "M2", "M2", "M2",
    "M2", "M2", "M2", "M2", "M2", "M2", "M", "M", "M", "M",
    "M", "M", "M", "M", "M", "M", "M", "M", "M", "M",
    "M", "M", "M", "M", "M", "M", "M", "M", "M", "M",
    "M", "M", "M", "M", "M", "M", "M", "M", "M2", "M2",
    "M", "M", "M", "M", "M", "M", "M", "M", "M", "M",
    "M", "M", "M2", "M2", "M2", "M2", "M", "M", "M", "M",
]


def fetch_surah(number: int) -> dict:
    """Fetch a single surah's ayahs with metadata from the AlQuran Cloud API."""
    url = API_BASE.format(number=number)
    with urllib.request.urlopen(url, timeout=30) as resp:
        data = json.loads(resp.read().decode("utf-8"))
    return data["data"]


def build_quran() -> dict:
    surahs = []
    for number, (arabic, translit, translation) in enumerate(SURAH_NAMES, start=1):
        print(f"Fetching surah {number}...")
        data = fetch_surah(number)
        ayahs = []
        for ayah in data["ayahs"]:
            ayahs.append({
                "number": ayah["numberInSurah"],
                "text": ayah["text"],
                "juz": ayah["juz"],
                "hizbQuarter": ayah["hizbQuarter"],
                "page": ayah["page"],
            })
        surahs.append({
            "number": number,
            "name": arabic,
            "englishName": translit,
            "englishNameTranslation": translation,
            "revelationType": "Meccan" if REVELATION[number - 1] == "M" else "Medinan",
            "ayahs": ayahs,
        })
    return {"surahs": surahs}


def main():
    parser = argparse.ArgumentParser(description="Fetch the complete Quran from a licensed public source")
    parser.add_argument("--output", default="app/src/main/assets/quran.json")
    args = parser.parse_args()

    quran = build_quran()

    import os
    os.makedirs(os.path.dirname(args.output) or ".", exist_ok=True)
    with open(args.output, "w", encoding="utf-8") as f:
        json.dump(quran, f, ensure_ascii=False)

    total_ayahs = sum(len(s["ayahs"]) for s in quran["surahs"])
    print(f"Done. {len(quran['surahs'])} surahs, {total_ayahs} ayahs written to {args.output}")


if __name__ == "__main__":
    main()
