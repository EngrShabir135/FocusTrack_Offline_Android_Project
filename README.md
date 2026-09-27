# FocusTrack Offline — Android APK

A mobile-first study tracker designed to work with Wi-Fi/mobile data turned OFF.

## Included
- Dark blue + green attractive UI
- Fully offline: no login, no cloud, no network calls
- Python, FastAPI, Django, Machine Learning, Deep Learning, Mathematics, Physics, Chemistry, Excel, Power BI, Tableau, SQL
- Add your own skills
- Optional topic for every study session
- Start / Pause / Resume / Finish timer
- Daily goals per skill
- Today's focused time
- Skill progress bars
- Recent session history
- Local streak counter
- Portrait mobile layout

## Important
The app does **not** turn Wi-Fi/mobile data off automatically. Android normally restricts ordinary apps from changing those network settings. You can switch Wi-Fi/data off yourself and FocusTrack will continue working normally.

## Build the APK
This repository includes `.github/workflows/build-apk.yml` so GitHub can build the APK using its Android SDK.

1. Create a GitHub repository.
2. Upload this entire project.
3. Push to the `main` branch, or open GitHub → Actions → **Build FocusTrack APK** → **Run workflow**.
4. After the workflow completes, open the workflow run and download the artifact named **FocusTrack-Offline-debug**.
5. Extract it and install `app-debug.apk` on your Android phone.

No internet is needed when the installed app is being used.
