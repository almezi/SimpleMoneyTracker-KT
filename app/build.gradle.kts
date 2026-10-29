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
        versionCode = 6
        versionName = "2.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Deliberate, not an oversight: Saku is an automation-test target,
            // so release bytecode stays unshrunk and unobfuscated — no R8
            // keep-rules to drift, and stack traces map 1:1 to sources. The
            // cost is APK size (~27 MiB). Revisit if this ever ships as a
            // product; note that testTagsAsResourceId does NOT require this.
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

val currentVersionName = android.defaultConfig.versionName ?: "2.4"

// Matches the flow documented in README.md: an assembleRelease artifact
// (unsigned), named for the release directory. This task used to copy the
// *debug* APK under a release name — the `SimpleMoneyTrackerKt-V2.3.apk`
// checked in on 2026-10-26 is such a mislabeled debug build.
tasks.register<Copy>("copyReleaseApk") {
    description = "Copies the release APK to the release directory with version name"
    dependsOn("assembleRelease")
    from(layout.buildDirectory.file("outputs/apk/release/app-release-unsigned.apk"))
    into(layout.projectDirectory.dir("../release"))
    rename("app-release-unsigned\\.apk", "SimpleMoneyTrackerKT-v$currentVersionName.apk")
}

dependencies {
    // Shared Kotlin: entities, DAOs, repositories, Clock, seeds, AppContainer.
    // Room and KSP now live in :composeApp.
    implementation(project(":composeApp"))

    // MainActivity is the only Android-side UI: a semantics root and
    // setContent. Material3, the icon pack, AppCompat, Material Components and
    // the second (older) Compose BOM were all removed — every pixel renders
    // from :composeApp, and a second Compose toolchain in one APK is pure skew.
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
}