plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")

    alias(libs.plugins.hilt.android) //
    alias(libs.plugins.navigation.safe.args)  // Add this line for Navigation SafeArgs

}

android {
    namespace = "com.devbrian.osebo"
    compileSdk = 36
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.devbrian.osebo"
        minSdk = 24
        targetSdk = 34
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

    sourceSets {
        getByName("main") {
            java.srcDirs("build/generated/source/kapt/main")
        }
    }
}


    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.swiperefreshlayout)
        implementation("com.google.code.gson:gson:2.10.1")
        implementation("com.jakewharton.timber:timber:5.0.1")


        // Navigation
        implementation(libs.navigation.fragment.ktx)
        implementation(libs.navigation.ui.ktx)

        // ViewModel & LiveData
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

        // Glide
        implementation("com.github.bumptech.glide:glide:4.15.1")

     // Hilt
    implementation(libs.hilt.android)
     kapt(libs.hilt.compiler)

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

        // Retrofit
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// OkHttp
        implementation("com.squareup.okhttp3:okhttp:4.11.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
// Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
// Lifecycle
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

        // CardView
        implementation("androidx.cardview:cardview:1.0.0")
// Or if using Material Components:
        implementation("com.google.android.material:material:1.10.0")
// ViewBinding is included in AndroidX
        implementation("androidx.viewpager2:viewpager2:1.0.0")







    }




