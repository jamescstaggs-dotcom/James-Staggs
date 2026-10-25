package com.stanfordremodeling.officelauncher

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import android.view.animation.DecelerateInterpolator
import java.text.SimpleDateFormat
import java.util.Locale
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Date

class MainActivity : Activity() {


    private lateinit var appRow: LinearLayout
    private lateinit var inputRow: LinearLayout
    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var wallpapers: Wallpapers

    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            val now = Date()
            clock.text = DateFormat.getTimeFormat(this@MainActivity).format(now)
            date.text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now)
            handler.postDelayed(this, 15_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appRow = findViewById(R.id.app_row)
        inputRow = findViewById(R.id.input_row)
        clock = findViewById(R.id.clock)
        date = findViewById(R.id.date)
        wallpapers = Wallpapers(this, findViewById(R.id.bg_a), findViewById(R.id.bg_b))
    }

    override fun onResume() {
        super.onResume()
        buildTiles()
        handler.post(tick)
        wallpapers.start()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
        wallpapers.stop()
    }

    /** Back does nothing on the home screen — there's nowhere to go. */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { /* intentionally empty */ }

    // ---------------------------------------------------------------- tiles

    private fun buildTiles() {
        val hadFocus = currentFocus != null
        appRow.removeAllViews()
        inputRow.removeAllViews()
        val pm = packageManager

        // Row 1: streaming apps
        for (app in Tiles.APPS) {
            val pkg = app.packages.firstOrNull { isInstalled(pm, it) } ?: continue
            val art = runCatching { pm.getApplicationBanner(pkg) }.getOrNull()
                ?: runCatching { pm.getApplicationIcon(pkg) }.getOrNull()
            appRow.addView(makeTile(app.label, art, app.accent) { launchApp(pkg) })
        }

        // Row 2: HDMI inputs, then utilities
        for (input in hdmiInputs()) {
            val label = input.loadCustomLabel(this)?.toString()
                ?: input.loadLabel(this)?.toString()
                ?: "HDMI"
            inputRow.addView(makeTile(label, getDrawable(R.drawable.ic_hdmi), 0xFF4C8DFF.toInt()) {
                switchToInput(input)
            })
        }
        inputRow.addView(makeTile("Audio out", getDrawable(R.drawable.ic_sound), 0xFF8B5CF6.toInt()) {
            openSettings(Settings.ACTION_SOUND_SETTINGS)
        })
        inputRow.addView(makeTile("Bluetooth", getDrawable(R.drawable.ic_bluetooth), 0xFF2563EB.toInt()) {
            openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
        })
        inputRow.addView(makeTile("All apps", getDrawable(R.drawable.ic_apps), 0xFF6B7280.toInt()) {
            showAllApps()
        })
        inputRow.addView(makeTile("Settings", getDrawable(R.drawable.ic_settings), 0xFF6B7280.toInt()) {
            openSettings(Settings.ACTION_SETTINGS)
        })

        if (!hadFocus) (appRow.getChildAt(0) ?: inputRow.getChildAt(0))?.requestFocus()
    }

    private fun makeTile(label: String, art: Drawable?, accent: Int, onClick: () -> Unit): View {
        val gold = getColor(R.color.gold)
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isFocusable = true
            isClickable = true
            background = tileBackground(accent)
            // weight 1 => every tile in a row shares the width equally
            layoutParams = LinearLayout.LayoutParams(0, dp(112), 1f).apply {
                marginStart = dp(6); marginEnd = dp(6)
            }
            outlineSpotShadowColor = gold
            outlineAmbientShadowColor = gold
            setOnClickListener { onClick() }
            setOnFocusChangeListener { v, focused ->
                val s = if (focused) 1.06f else 1f
                v.animate()
                    .scaleX(s).scaleY(s)
                    .translationZ(if (focused) dp(14).toFloat() else 0f)   // gold glow via shadow
                    .setDuration(160)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
                v.alpha = 1f
            }
        }
        val img = ImageView(this).apply {
            setImageDrawable(art)
            layoutParams = LinearLayout.LayoutParams(dp(108), dp(52))
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val txt = TextView(this).apply {
            text = label
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(0, dp(8), 0, 0)
            maxLines = 1
        }
        tile.addView(img)
        tile.addView(txt)
        return tile
    }

    private fun tileBackground(accent: Int): Drawable {
        val base = getColor(R.color.tile)
        fun shape(stroke: Int, strokeW: Int, fill: Int) = GradientDrawable().apply {
            cornerRadius = dp(14).toFloat()
            setColor(fill)
            setStroke(strokeW, stroke)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused),
                shape(getColor(R.color.gold), dp(2), blend(base, accent, 0.45f)))
            addState(intArrayOf(), shape(getColor(R.color.tile_stroke), dp(1), base))
        }
    }

    // -------------------------------------------------------------- actions

    private fun launchApp(pkg: String) {
        val pm = packageManager
        val intent = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg)
        if (intent == null) {
            Toast.makeText(this, "Can't open that app", Toast.LENGTH_SHORT).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun switchToInput(input: TvInputInfo) {
        val uri = TvContract.buildChannelUriForPassthroughInput(input.id)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            // This TV build doesn't expose a passthrough viewer; fall back to the Inputs settings page.
            Toast.makeText(this, "Use the Input button on the remote", Toast.LENGTH_SHORT).show()
            openSettings(Settings.ACTION_SETTINGS)
        }
    }

    private fun openSettings(action: String) {
        try {
            startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun showAllApps() {
        val pm = packageManager
        val probe = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val apps = pm.queryIntentActivities(probe, 0)
            .filter { it.activityInfo.packageName != packageName }
            .map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
            .distinctBy { it.second }
            .sortedBy { it.first.lowercase() }
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("All apps")
            .setItems(apps.map { it.first }.toTypedArray()) { _, i -> launchApp(apps[i].second) }
            .show()
    }

    // -------------------------------------------------------------- helpers

    private fun hdmiInputs(): List<TvInputInfo> {
        val tim = getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager ?: return emptyList()
        return runCatching { tim.tvInputList }.getOrDefault(emptyList())
            .filter { it.type == TvInputInfo.TYPE_HDMI && it.isPassthroughInput }
            .sortedBy { it.loadLabel(this)?.toString() ?: "" }
    }

    private fun isInstalled(pm: PackageManager, pkg: String) =
        runCatching { pm.getApplicationInfo(pkg, 0) }.isSuccess

    private fun blend(a: Int, b: Int, t: Float): Int {
        fun ch(x: Int, y: Int) = (x + (y - x) * t).toInt()
        return Color.rgb(
            ch(Color.red(a), Color.red(b)),
            ch(Color.green(a), Color.green(b)),
            ch(Color.blue(a), Color.blue(b)),
        )
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
