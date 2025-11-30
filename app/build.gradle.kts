plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"

    // google-service for firebase
    id("com.google.gms.google-services")


}

android {
    namespace = "com.ramankumar.moviefinder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ramankumar.moviefinder"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }



}

dependencies {

    // ========== CORE ANDROID ==========
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // ========== TESTING ==========
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ========== RETROFIT ==========
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ========== GLIDE (XML use only) ==========
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ========== ROOM ==========
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ========== LIFECYCLE ==========
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // ========== COROUTINES ==========
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")



    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))

    // Core Compose libraries (NO versions!)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Material 3 + Icons
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Compose Activity
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coil (Compose Image Loading)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // Debugging
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Pull to Refresh
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")

    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:13.0.0")

    // ExoPlayer for fast trailer playback
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-common:1.2.1")

    // Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Firebase
    //BOM for Firebase service version sync
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    // Firebase auth
    implementation("com.google.firebase:firebase-auth")
}