# Movie Finder App

<img width="207" height="450" alt="image" src="https://github.com/user-attachments/assets/14648177-e380-4dc0-8103-3e6d27d30cfd" />

<img width="207" height="450" alt="image" src="https://github.com/user-attachments/assets/fec078a5-067e-45f5-adb7-4e33b667c8f8" />

<img width="207" height="450" alt="image" src="https://github.com/user-attachments/assets/5aff21a4-1670-44ad-bdeb-ee25cc80b0ea" />

<img width="207" height="450" alt="image" src="https://github.com/user-attachments/assets/5654591e-7e83-4fd9-b1e4-de75619cf90a" />



Android movie discovery app using The Movie Database (TMDb) API.

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/Gherra/MovieFinder.git
cd MovieFinder
```
### Required API Keys & Files

**This project requires three keys/files:**
- TMDb API Key
- Gemini API Key (for AI-powered movie search and keyword extraction)
- Firebase google-services.json (for Authentication)
- Follow the instructions below to configure all required keys.

### 2. Get Your TMDb API Key
1. Sign up at [TMDb](https://www.themoviedb.org/signup) (free)
2. Go to **Settings → API**
3. Request an API Key (choose "Developer")
4. Copy your API key

### 3. Get Your Gemini API Key
1. Get Your Gemini API Key
2. Go to [Google AI Studio](https://aistudio.google.com)
3. Sign in with your Google account
4. Go to API Keys → Create API Key


### 4. Get Your Google-Services.json 
1. Go to the Firebase Console: https://console.firebase.google.com
2. Create a new Firebase project
3. Add a new Android App
4. Use the package name:
```
com.ramankumar.moviefinder
```
5. Download your **google-services.json**
6. Paste into `app/` directory:
```
app/google-services.json
```


### 5. Configure API Keys
1. Navigate to: `app/src/main/java/com/ramankumar/moviefinder/api/`
2. Copy `ApiConfig.example.kt` and rename it to `ApiConfig.kt`
3. Open `ApiConfig.kt` and replace `PASTE_YOUR_TMDB_API_KEY_HERE` with your actual respective API keys (TMdb and Gemini)

### 6. Build and Run
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
