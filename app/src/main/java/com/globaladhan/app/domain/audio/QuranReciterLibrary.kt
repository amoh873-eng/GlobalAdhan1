package com.globaladhan.app.domain.audio

/**
 * Quran recitation library.
 *
 * LEGAL STATUS: No Quran recitation audio is bundled or downloaded. Famous
 * reciters are listed for selection but marked isAvailable=false until a
 * legally licensed source is configured — the UI must never claim an
 * unlicensed reciter is installed (spec: do not package unauthorized audio).
 *
 * The architecture supports adding legally licensed recordings later:
 * set isAvailable = true and provide a local file path / licensed CDN source.
 */
data class QuranReciter(
    val id: String,
    val name: String,
    val style: String,
    val audioSource: String,
    val license: String,
    val isAvailable: Boolean,
    val availableSurahs: IntRange? = null,
    val quality: String = "MP3 128kbps"
)

object QuranReciterLibrary {

    /**
     * Famous reciters. All currently isAvailable=false because no legally
     * redistributable recordings are configured. When a licensed source is
     * obtained, flip the flag and set audioSource to the licensed file/CDN.
     */
    val reciters: List<QuranReciter> = listOf(
        QuranReciter(
            id = "husary",
            name = "محمود خليل الحصري",
            style = "Murattal",
            audioSource = "",
            license = "غير مرخص بعد — لا يتوفر صوت",
            isAvailable = false
        ),
        QuranReciter(
            id = "sudais",
            name = "عبد الرحمن السديس",
            style = "Murattal",
            audioSource = "",
            license = "غير مرخص بعد — لا يتوفر صوت",
            isAvailable = false
        ),
        QuranReciter(
            id = "muaiqly",
            name = "ماهر المعيقلي",
            style = "Murattal",
            audioSource = "",
            license = "غير مرخص بعد — لا يتوفر صوت",
            isAvailable = false
        ),
        QuranReciter(
            id = "ghamdi",
            name = "سعد الغامدي",
            style = "Murattal",
            audioSource = "",
            license = "غير مرخص بعد — لا يتوفر صوت",
            isAvailable = false
        ),
        QuranReciter(
            id = "dosari",
            name = "ياسر الدوسري",
            style = "Murattal",
            audioSource = "",
            license = "غير مرخص بعد — لا يتوفر صوت",
            isAvailable = false
        ),
        QuranReciter(
            id = "afasy",
            name = "مشاري راشد العفاسي",
            style = "Murattal",
            audioSource = "",
            license = "غير مرخص بعد — لا يتوفر صوت",
            isAvailable = false
        )
    )

    fun byId(id: String): QuranReciter? = reciters.firstOrNull { it.id == id }

    /** Reciters the user can actually play right now (legally licensed). */
    fun available(): List<QuranReciter> = reciters.filter { it.isAvailable }
}
