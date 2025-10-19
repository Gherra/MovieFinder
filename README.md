# Movie Finder App

<img width="221" height="480" alt="image" src="https://github.com/user-attachments/assets/14648177-e380-4dc0-8103-3e6d27d30cfd" />

<img width="221" height="480" alt="image" src="https://github.com/user-attachments/assets/fec078a5-067e-45f5-adb7-4e33b667c8f8" />

<img width="221" height="480" alt="image" src="https://github.com/user-attachments/assets/5aff21a4-1670-44ad-bdeb-ee25cc80b0ea" />



Android movie discovery app using The Movie Database (TMDb) API.

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/Gherra/MovieFinder.git
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
5. Click Run

## Features
Search movies by title  
Browse popular movies  
View detailed movie information  
Save movies to favorites  
Material Design dark theme  

## Tech Stack
- **Language:** Kotlin
- **Networking:** Retrofit + Gson
- **Image Loading:** Glide
- **UI:** Material Design
- **API:** TMDb

## Team
CMPT 362 - Fall 2025
