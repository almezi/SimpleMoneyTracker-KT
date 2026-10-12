// STAGE 1 SPIKE — throwaway.
//
// Deliberately MINIMAL: no Room, no string-resource migration, no navigation.
// The only question this answers is whether testTag surfaces as an iOS
// accessibilityIdentifier. Adding anything else would conflate risks.
//
// NOTE: this module has NO Android target. AGP 9.0+ refuses to combine
// com.android.application with org.jetbrains.kotlin.multiplatform ("There
// isn't a direct replacement... extract your Android application to a separate
// Gradle module"). The spike only needs iOS, so Android is omitted entirely.

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Use the Gradle DSL accessors, never literal coordinates.
            // org.jetbrains.compose.material3 has no 1.12.x stable release —
            // the Compose plugin resolves it internally.
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
    }
}
compose.resources {
    // Generate a public Res class in the `saku` package so the design system
    // can use the same Res.string.* / Res.font.* accessors on iOS that the
    // Android app uses via R.string.* / R.font.*.
    publicResClass = true
    packageOfResClass = "saku"
    generateResClass = always
}
