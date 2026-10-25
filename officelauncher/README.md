# Office Launcher — Stanford Remodeling

A minimal home screen for the TCL 55Q651G (Google TV) in the office.
One screen, two rows:

- **Watch:** Netflix · Hulu · Prime Video · ESPN · YouTube (only the ones installed show up)
- **Inputs & tools:** each HDMI port (uses the name you gave it in TV settings) · Audio out · Bluetooth · All apps · Settings

The Stanford Remodeling logo sits top-center over rotating job photos.
Back does nothing on the home screen. Mac mini mirroring (AirPlay / Chromecast)
takes over the screen on its own, so it needs no tile.

## 1. Logo and rotating job photos

The Stanford Remodeling logo is baked in at `app/src/main/res/drawable/logo.png`, and the
color scheme (slate, gold) comes from it.

Behind the logo and tiles, the launcher rotates through your job photos — a new one every
20 seconds with a crossfade and a slow drift. Two ways to supply them:

- **Push to the TV (no rebuild):** put photos in a folder on the Mac, then
  `./push-photos.sh ~/Pictures/StanfordJobs` (add the TV's IP as a second argument if
  you're not already connected). The script resizes them and sends them over ADB.
  Re-run whenever you want to swap the set.
- **Bake into the APK:** drop JPGs into `app/src/main/assets/wallpapers/` before building.
  Pushed photos take priority over bundled ones.

With no photos in either place, the flat slate gradient shows. Landscape photos work best;
portrait ones are center-cropped. The scrim darkness is in `drawable/bg_scrim.xml`, the
rotation speed is the `intervalMs` default in `Wallpapers.kt`.

## 2. Build the APK (Android Studio on the Mac)

1. Install Android Studio (free) and open this folder as a project.
   If it asks about the Gradle wrapper, let it generate/download it.
2. Let it sync, then **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
3. The file lands at `app/build/outputs/apk/debug/app-debug.apk`.
   (A debug build is fine for sideloading to your own TV.)

## 3. Put the TV in debug mode

1. Settings → System → About → scroll to **Android TV OS build** and click it 7 times
   until it says you're a developer.
2. Settings → System → **Developer options** → turn on **USB debugging** and
   **Network debugging** (names vary slightly by firmware).
3. Note the TV's IP: Settings → Network & Internet → tap your WiFi network.

## 4. Install from the Mac

```bash
brew install android-platform-tools      # one time
adb connect 192.168.x.x:5555             # TV's IP — accept the prompt on the TV
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 5. Make it the home screen

Google TV won't let you pick a different home screen from a menu, so you disable the
stock one. This is fully reversible.

```bash
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith
```

Press **Home** on the remote. If it asks which launcher to use, pick Office Launcher
→ Always.

To go back to stock Google TV:

```bash
adb shell pm enable com.google.android.apps.tv.launcherx
adb shell pm enable com.google.android.tungsten.setupwraith
```

## Customizing

- **Add / remove / reorder apps:** edit the list in `Tiles.kt`. Each entry is a
  label, one or more package names (first installed one wins), and an accent color.
  To find a package name: `adb shell pm list packages | grep -i <name>`.
- **Tile size, colors, row labels:** `MainActivity.makeTile`, `values/colors.xml`,
  `values/strings.xml`.
- **Title text** (top-left): `app_title` in `values/strings.xml`.

## Notes

- The HDMI tiles use Android's TV-input passthrough. On most TCL firmware this
  switches inputs directly; if a firmware build doesn't expose it, the tile falls
  back to a toast telling you to use the Input button on the remote.
- Google TV's Home button occasionally re-enables the stock launcher after a major
  firmware update. If that happens, rerun the two `pm disable-user` commands.
