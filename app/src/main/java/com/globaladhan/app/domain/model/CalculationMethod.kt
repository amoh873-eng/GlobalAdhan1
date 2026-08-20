package com.globaladhan.app.domain.model

/**
 * Recognized prayer calculation methods.
 * Each method defines Fajr and Isha angles (or fixed minutes after sunset for Isha).
 */
enum class CalculationMethod(
    val displayName: String,
    val fajrAngle: Double?,
    val ishaAngle: Double?,
    val ishaMinutes: Int?
) {
    MUSLIM_WORLD_LEAGUE("Muslim World League", 18.0, 17.0, null),
    EGYPTIAN("Egyptian General Authority of Survey", 19.5, 17.5, null),
    KARACHI("University of Islamic Sciences, Karachi", 18.0, 18.0, null),
    UMM_AL_QURA("Umm al-Qura, Makkah", 18.5, null, 90),
    DUBAI("Dubai", 18.2, 18.2, null),
    QATAR("Qatar", 18.0, null, 90),
    KUWAIT("Kuwait", 18.0, 17.5, null),
    SINGAPORE("Singapore", 20.0, 18.0, null),
    NORTH_AMERICA("North America (ISNA)", 15.0, 15.0, null),
    CUSTOM("Custom", 18.0, 17.0, null);

    companion object {
        val default = MUSLIM_WORLD_LEAGUE
    }
}

enum class AsrMethod(val displayName: String) {
    STANDARD("Standard (Shafi'i, Maliki, Hanbali)"),
    HANAFI("Hanafi");

    companion object {
        val default = STANDARD
    }
}

enum class HighLatitudeMethod(val displayName: String) {
    MIDDLE_OF_THE_NIGHT("Middle of the Night"),
    SEVENTH_OF_THE_NIGHT("Seventh of the Night"),
    ANGLE_BASED("Angle-Based"),
    ONE_SEVENTH("One-Seventh"),
    NONE("None");

    companion object {
        val default = MIDDLE_OF_THE_NIGHT
    }
}

enum class TimeAdjustmentType {
    FAJR_ANGLE,
    ISHA_ANGLE,
    MANUAL_MINUTES
}
