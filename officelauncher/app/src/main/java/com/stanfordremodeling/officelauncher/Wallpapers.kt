package com.stanfordremodeling.officelauncher

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import java.io.File

/**
 * Rotating photo background. Crossfades between two ImageViews with a slow
 * Ken Burns drift so the screen never sits fully static.
 *
 * Photos come from two places, in this order:
 *   1. The TV's app-private folder, which you can fill over ADB with no rebuild:
 *        /sdcard/Android/data/com.stanfordremodeling.officelauncher/files/wallpapers/
 *      (see push-photos.sh in the project root)
 *   2. Photos bundled into the APK under app/src/main/assets/wallpapers/
 * If neither has any photos, the views are hidden and the flat gradient shows.
 */
class Wallpapers(
    private val ctx: Context,
    private val viewA: ImageView,
    private val viewB: ImageView,
    private val intervalMs: Long = 20_000L,
    private val fadeMs: Long = 1_800L,
) {
    private sealed class Pic {
        data class OnDisk(val file: File) : Pic()
        data class InAssets(val name: String) : Pic()
    }

    private val handler = Handler(Looper.getMainLooper())
    private var pics: List<Pic> = emptyList()
    private var idx = 0
    private var showingA = true
    private var drift: AnimatorSet? = null

    private val tick = object : Runnable {
        override fun run() {
            next()
            handler.postDelayed(this, intervalMs)
        }
    }

    fun start() {
        pics = load()
        if (pics.isEmpty()) {
            viewA.visibility = View.GONE
            viewB.visibility = View.GONE
            return
        }
        viewA.visibility = View.VISIBLE
        viewB.visibility = View.VISIBLE
        viewB.alpha = 0f
        viewA.alpha = 1f
        viewA.setImageBitmap(decode(pics[0]))
        showingA = true
        idx = if (pics.size > 1) 1 else 0
        startDrift(viewA)
        handler.removeCallbacks(tick)
        if (pics.size > 1) handler.postDelayed(tick, intervalMs)
    }

    fun stop() {
        handler.removeCallbacks(tick)
        drift?.cancel()
    }

    /** The folder you push photos into over ADB. Created on first run. */
    fun folder(): File? = ctx.getExternalFilesDir("wallpapers")

    // ------------------------------------------------------------------

    private fun next() {
        val incoming = if (showingA) viewB else viewA
        val outgoing = if (showingA) viewA else viewB
        val bmp = decode(pics[idx])
        idx = (idx + 1) % pics.size
        if (bmp == null) return

        incoming.setImageBitmap(bmp)
        incoming.alpha = 0f
        incoming.animate().alpha(1f).setDuration(fadeMs).start()
        outgoing.animate().alpha(0f).setDuration(fadeMs)
            .withEndAction { outgoing.setImageDrawable(null) }
            .start()
        startDrift(incoming)
        showingA = !showingA
    }

    /** Slow 1.00 → 1.07 zoom with a slight pan over the full display time. */
    private fun startDrift(v: View) {
        drift?.cancel()
        v.scaleX = 1f; v.scaleY = 1f; v.translationX = 0f; v.translationY = 0f
        val dur = intervalMs + fadeMs
        val panX = if ((idx and 1) == 0) 18f else -18f
        val panY = if ((idx and 2) == 0) 10f else -10f
        drift = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(v, View.SCALE_X, 1f, 1.07f),
                ObjectAnimator.ofFloat(v, View.SCALE_Y, 1f, 1.07f),
                ObjectAnimator.ofFloat(v, View.TRANSLATION_X, 0f, panX),
                ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, 0f, panY),
            )
            duration = dur
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun load(): List<Pic> {
        val exts = setOf("jpg", "jpeg", "png", "webp")
        val onDisk = folder()?.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in exts }
            ?.sortedBy { it.name }
            ?.map { Pic.OnDisk(it) }
            .orEmpty()
        if (onDisk.isNotEmpty()) return onDisk.shuffled()

        val bundled = runCatching { ctx.assets.list("wallpapers")?.toList() }.getOrNull()
            ?.filter { it.substringAfterLast('.', "").lowercase() in exts }
            ?.sorted()
            ?.map { Pic.InAssets(it) }
            .orEmpty()
        return bundled.shuffled()
    }

    /** Decode scaled down to roughly the screen size so a 12 MP photo doesn't blow the heap. */
    private fun decode(p: Pic): Bitmap? {
        val target = 1920
        fun opts(sample: Int) = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            open(p) { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= target) sample *= 2
            open(p) { BitmapFactory.decodeStream(it, null, opts(sample)) }
        }.getOrNull()
    }

    private fun <T> open(p: Pic, block: (java.io.InputStream) -> T): T = when (p) {
        is Pic.OnDisk -> p.file.inputStream().use(block)
        is Pic.InAssets -> ctx.assets.open("wallpapers/${p.name}").use(block)
    }
}
