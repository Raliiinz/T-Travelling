plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.secrets)
    alias(libs.plugins.hilt)
    id("kotlin-parcelize")
    id("kotlin-kapt")
    alias(libs.plugins.detekt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "ru.itis.travelling"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.itis.travelling"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        defaultConfig {
            buildConfigField("String", "API_URL", "\"http://141.105.71.181:8080\"")
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            vectorDrawables.useSupportLibrary = true
        }

        buildFeatures{
            buildConfig = true
        }
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    implementation("com.google.android.material:material:1.10.0")
    implementation(libs.shimmer)

    implementation(libs.vbpd) // для vbpd

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.fragment)

    // DataStore Preferences
    implementation(libs.datastore.preferences)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    // Network
    implementation(libs.bundles.network.deps)

    //EncryptedSharedPreferences
    implementation(libs.security.crypto)

    //Annotation
    implementation(libs.annotation)

    //Room
    implementation(libs.room)
    implementation(libs.room.ktx)
    ksp(libs.room.ksp)

    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    implementation("androidx.core:core-splashscreen:1.0.1")
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}