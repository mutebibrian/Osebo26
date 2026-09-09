import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safe.args)
    id("kotlin-parcelize")
}

compose.resources {
    packageOfResClass = "com.devbrian.osebo.resources"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "OseboShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(compose.preview)

            // Core AndroidX
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.material)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.androidx.swiperefreshlayout)
            implementation(libs.androidx.preference.ktx)

            // Navigation (kept version-catalog entries only — removed duplicate direct aliases)
            implementation(libs.navigation.fragment.ktx)
            implementation(libs.navigation.ui.ktx)

            // Charts
            implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

            // Google Play Services (kept only the newer pair — removed the older duplicate)
            implementation("com.google.android.gms:play-services-auth:21.2.0")
            implementation("com.google.android.gms:play-services-auth-api-phone:18.1.0")
            implementation(libs.play.services.games)

            // Misc utilities
            implementation("com.google.code.gson:gson:2.10.1")
            implementation("com.facebook.shimmer:shimmer:0.5.0")
            implementation("com.jakewharton.timber:timber:5.0.1")

            // Lifecycle
            implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
            implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

            // Glide
            implementation("com.github.bumptech.glide:glide:4.15.1")

            // Koin (DI)
            implementation(libs.koin.android)

            // Networking
            implementation("com.squareup.retrofit2:retrofit:2.9.0")
            implementation("com.squareup.retrofit2:converter-gson:2.9.0")
            implementation("com.squareup.okhttp3:okhttp:4.11.0")
            implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

            // Coroutines
            implementation(libs.kotlinx.coroutines.android)

            // UI components
            implementation("androidx.cardview:cardview:1.0.0")
            implementation("androidx.viewpager2:viewpager2:1.0.0")

            // Country code picker
            implementation("com.hbb20:ccp:2.7.3")

            // Room
            implementation("androidx.room:room-runtime:2.8.4")
            implementation("androidx.room:room-ktx:2.8.4")

            // CameraX + ML Kit barcode scanning
            implementation("androidx.camera:camera-camera2:1.3.0")
            implementation("androidx.camera:camera-lifecycle:1.3.0")
            implementation("androidx.camera:camera-view:1.3.0")
            implementation("com.google.mlkit:barcode-scanning:17.2.0")

            // WorkManager
            implementation("androidx.work:work-runtime-ktx:2.9.0")
        }

        androidUnitTest.dependencies {
            implementation(libs.junit)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
        }
    }
}

android {
    namespace = "com.devbrian.osebo"
    compileSdk = 36

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }
}

// KSP processors run for the Android target only (Room, Glide) — KMP
// projects use per-target configuration names (kspAndroid) instead of the
// deprecated catch-all ksp(...) that plain single-platform apps use.
dependencies {
    add("kspAndroid", "androidx.room:room-compiler:2.8.4")
    add("kspAndroid", "com.github.bumptech.glide:ksp:4.15.1")
    debugImplementation(compose.uiTooling)
}

// Force consistent Room version
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.room") {
            useVersion("2.8.4")
        }
    }
}
