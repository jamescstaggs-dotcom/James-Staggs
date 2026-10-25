#!/bin/bash
# Push a folder of job photos to the office TV for the launcher background.
#   ./push-photos.sh ~/Pictures/StanfordJobs            (TV already connected via adb)
#   ./push-photos.sh ~/Pictures/StanfordJobs 192.168.1.47 (connect first)
# Photos are resized to 1920px wide on the Mac before sending. Re-run anytime;
# the launcher picks up changes the next time it comes to the foreground.
set -e
SRC="${1:?usage: push-photos.sh <photo-folder> [tv-ip]}"
[ -n "$2" ] && adb connect "$2:5555"
DEST=/sdcard/Android/data/com.stanfordremodeling.officelauncher/files/wallpapers
TMP=$(mktemp -d)
n=0
for f in "$SRC"/*.{jpg,jpeg,JPG,JPEG,png,PNG}; do
  [ -e "$f" ] || continue
  out="$TMP/$(basename "${f%.*}").jpg"
  sips -s format jpeg -s formatOptions 85 -Z 1920 "$f" --out "$out" >/dev/null
  n=$((n+1))
done
[ "$n" -gt 0 ] || { echo "no photos found in $SRC"; exit 1; }
adb shell mkdir -p "$DEST"
adb shell rm -f "$DEST/*"
adb push "$TMP"/*.jpg "$DEST/"
rm -rf "$TMP"
echo "pushed $n photos. Press Home on the remote to see them."
