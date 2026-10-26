plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "id.almezi.simplemoneytracker_kt"
    compileSdk = 37

    defaultConfig {
        applicationId = "id.almezi.simplemoneytracker_kt"
        minSdk = 24
        targetSdk = 37
        versionCode = 5
        versionName = "2.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

val currentVersionName = android.defaultConfig.versionName ?: "2.2"

tasks.register<Copy>("copyReleaseApk") {
    description = "Copies debug APK to release directory with version name"
    dependsOn("assembleDebug")
    from(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
    into(layout.projectDirectory.dir("../release"))
    rename("app-debug\\.apk", "SimpleMoneyTrackerKt-V$currentVersionName.apk")
}

dependencies {
    // Shared Kotlin: entities, DAOs, repositories, Clock, seeds, AppContainer.
    // Room and KSP now live in :composeApp.
    implementation(project(":composeApp"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.activity.compose)
    // Navigation comes from :composeApp, which uses the multiplatform
    // org.jetbrains.androidx.navigation build. AndroidX's navigation-compose is
    // Android-only and shadows the typed-route API the shared NavGraph needs.
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation(libs.kotlinx.datetime)
    implementation(libs.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}