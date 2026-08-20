package com.globaladhan.app.domain.model

/** Kaaba coordinates used for Qibla calculations. */
object Kaaba {
    const val LATITUDE = 21.422487
    const val LONGITUDE = 39.826206
    const val NAME = "Kaaba, Makkah"
}

data class QiblaResult(
    val bearingDegrees: Double,
    val distanceKm: Double,
    val deviceHeadingDegrees: Float? = null
)

data class IslamicDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val monthName: String
)

enum class IslamicMonth(val arabicName: String) {
    MUHARRAM("محرم"),
    SAFAR("صفر"),
    RABI_AL_AWWAL("ربيع الأول"),
    RABI_AL_THANI("ربيع الثاني"),
    JUMADA_AL_AWWAL("جمادى الأولى"),
    JUMADA_AL_THANI("جمادى الآخرة"),
    RAJAB("رجب"),
    SHABAN("شعبان"),
    RAMADAN("رمضان"),
    SHAWWAL("شوال"),
    DHUL_QADAH("ذو القعدة"),
    DHUL_HIJJAH("ذو الحجة");

    companion object {
        fun byIndex(index: Int): IslamicMonth = entries[index % entries.size]
    }
}
