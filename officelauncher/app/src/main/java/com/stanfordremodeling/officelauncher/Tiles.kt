package com.stanfordremodeling.officelauncher

/**
 * The app tiles shown on the home screen, in order.
 *
 * Each entry lists candidate package names; the first one that's actually
 * installed on the TV is used. To add or remove an app, edit this list.
 * Accent is the color the tile glows when focused.
 */
data class AppTile(val label: String, val packages: List<String>, val accent: Int)

object Tiles {
    val APPS: List<AppTile> = listOf(
        AppTile("Netflix",     listOf("com.netflix.ninja", "com.netflix.mediaclient"),                        0xFFE50914.toInt()),
        AppTile("Hulu",        listOf("com.hulu.livingroomplus", "com.hulu.plus"),                             0xFF1CE783.toInt()),
        AppTile("Prime Video", listOf("com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient"), 0xFF00A8E1.toInt()),
        AppTile("ESPN",        listOf("com.espn.score_center", "com.espn.androidtv"),                          0xFFD62828.toInt()),
        AppTile("YouTube",     listOf("com.google.android.youtube.tv", "com.google.android.youtube"),          0xFFFF0000.toInt()),
    )

    // Package names of the stock Google TV launcher, listed here only for the README.
    const val STOCK_LAUNCHER = "com.google.android.apps.tv.launcherx"
    const val STOCK_LAUNCHER_FALLBACK = "com.google.android.tungsten.setupwraith"
}
