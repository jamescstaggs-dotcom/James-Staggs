# James Staggs — projects

- **officelauncher/** — custom Android TV home screen for the Stanford Remodeling office TV.
  Every push to that folder builds the APK on GitHub Actions and commits it to `apk/OfficeLauncher.apk`.
  Install: `adb connect <tv-ip>:5555 && adb install -r apk/OfficeLauncher.apk` — full steps in `officelauncher/README.md`.
