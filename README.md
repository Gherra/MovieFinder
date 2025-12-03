# Movie Finder App

<img width="207" height="450" alt="LoginPage" src="https://github.com/user-attachments/assets/387ab0f6-fc58-457e-869d-9ac1a719c229" />
<img width="207" height="450" alt="MovieFinderSearchPage" src="https://github.com/user-attachments/assets/4cf87d30-d0b6-4903-9ef4-acf82ee69a57" />
<img width="207" height="450" alt="MovieFinderExplorePage" src="https://github.com/user-attachments/assets/69fdccb2-0d1d-4d6c-a8a3-c4b80d6b29dd" />
<img width="207" height="450" alt="MovieFinderSwipePage" src="https://github.com/user-attachments/assets/736c9d60-5be1-48c3-8435-0d135f1bba55" />

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
