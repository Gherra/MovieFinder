# Movie Finder App 🎬

Android movie discovery app using The Movie Database (TMDb) API.

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/gherra/MovieFinder.git
cd MovieFinder
```

### 2. Get Your TMDb API Key
1. Sign up at [TMDb](https://www.themoviedb.org/signup) (free)
2. Go to **Settings → API**
3. Request an API Key (choose "Developer")
4. Copy your API key

### 3. Configure API Key
1. Navigate to: `app/src/main/java/com/ramankumar/moviefinder/api/`
2. Copy `ApiConfig.example.kt` and rename it to `ApiConfig.kt`
3. Open `ApiConfig.kt` and replace `PASTE_YOUR_TMDB_API_KEY_HERE` with your actual API key

### 4. Build and Run
1. Open the project in Android Studio
2. File → Sync Project with Gradle Files
3. Build → Clean Project
4. Build → Rebuild Project
5. Click Run ▶️

## Features
✅ Search movies by title  
✅ Browse popular movies  
✅ View detailed movie information  
✅ Save movies to favorites  
✅ Material Design dark theme  

## Tech Stack
- **Language:** Kotlin
- **Networking:** Retrofit + Gson
- **Image Loading:** Glide
- **UI:** Material Design
- **API:** TMDb

## Team
CMPT 362 - Fall 2025
