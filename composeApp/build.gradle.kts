// Generates a Kotlin object of string constants into commonMain from
// `shared-strings/strings.xml`.
//
// WHY THIS EXISTS
//
// Compose Multiplatform 1.12.1 (and 1.11.x, 1.10.x) generates resource
// accessors for the Android target only from `src/androidMain/composeResources`.
// Resources placed in `commonMain` produce accessors wired into the iOS/native
// compilations but never into the Android one, so `Res.string.*` fails with
// "Unresolved reference" even inside the module. Verified across CMP 1.10.3,
// 1.11.1 and 1.12.1, so it is not a regression. Putting the same files in both
// source sets makes the accessors resolve but then fails with an unconfigurable
// duplicate error in the plugin's CopyResourcesToAndroidAssetsTask.
//
// See docs/09-ios-migration-plan.md section 6.5 for the full investigation.
//
// This keeps `strings.xml` as the single source of truth and exposes it to
// commonMain code as plain Kotlin constants, which need no codegen support from
// the plugin and therefore compile for every target.
//
// REVISIT: if Compose Multiplatform fixes Android accessor generation for
// commonMain resources, delete this task and the generated file, then switch
// call sites back to `stringResource(Res.string.*)`.

import java.util.Base64
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose") version "1.12.1"
    id("com.google.devtools.ksp") version "2.3.12"
    id("androidx.room3") version "3.0.3"
    // Required by the CMP navigation typed routes (@Serializable route objects).
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.20"
}

kotlin {
    // Per the official JetBrains convention for an existing Android app:
    // https://kotlinlang.org/docs/multiplatform/multiplatform-integrate-in-existing-app.html
    androidLibrary {
        namespace = "id.almezi.simplemoneytracker_kt.shared"
        compileSdk = 37

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        androidResources {
            enable = true
        }

        // Tests are disabled by default on this plugin. Saku has 26 unit tests.
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room3.runtime)
            // Bundles the same SQLite build for every platform so iOS and
            // Android cannot drift apart. Preferred over the OS SQLite.
            implementation(libs.androidx.sqlite.bundled)

            implementation(libs.kotlinx.datetime)

            // Compose DSL accessors only. Never pin
            // org.jetbrains.compose.material3 — it has no stable release past
            // 1.9.0; the Compose Gradle plugin resolves it internally.
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            // Only iosMain reads Res.font.* — Android loads the same TTFs from
            // its own src/androidMain/res/font. Compose Multiplatform does not
            // generate working accessors for the Android target, so Android must
            // not consume this. See docs/09 section 6.5.
            implementation("org.jetbrains.compose.components:components-resources:1.12.1")
            // JetBrains multiplatform navigation. androidx.navigation's
            // navigation-compose is Android-only.
            //
            // `api`, not `implementation`: NavHostController appears in the
            // signature of the shared NavGraph, so consuming apps (including
            // the Android :app module) need it on their compile classpath.
            api(libs.androidx.navigation.compose)
            // `api` for the same reason: Screen routes are @Serializable.
            api(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.savedstate)
        }

        // Shared tests run on every enabled target, so they use kotlin.test
        // rather than JUnit: `:composeApp:allTests` covers both Android unit
        // tests and the iOS simulator test tasks.
        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        // Compose UI tests only for the iOS simulator tree. Android host tests
        // cannot run `runComposeUiTest` without Robolectric, so keeping this
        // out of commonTest leaves `:composeApp:allTests` green.
        //
        // Coordinates spelled out rather than `compose.uiTest`: that accessor is
        // deprecated AND requires opting into ExperimentalComposeLibrary at the
        // build-script level, which then makes every source set carry the
        // opt-in. Same 1.12.1 as the `org.jetbrains.compose` plugin above.
        iosTest.dependencies {
            implementation("org.jetbrains.compose.ui:ui-test:1.12.1")
        }
    }
}

// NOTE: the extension is `room3`, not `room`, for Room 3.x. The plugin refuses
// to configure without a schema location.
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler.get().toString())
    add("kspIosArm64", libs.androidx.room3.compiler.get().toString())
    add("kspIosSimulatorArm64", libs.androidx.room3.compiler.get().toString())
}

// ---------------------------------------------------------------------------
// String constants generated for commonMain
// ---------------------------------------------------------------------------

val sharedStringsFile = layout.projectDirectory.file("shared-strings/strings.xml")
val generatedStringsDir = layout.buildDirectory.dir("generated/sakuStrings/kotlin")

val generateSakuStrings by tasks.registering {
    description = "Generates commonMain string constants from shared-strings/strings.xml"
    group = "build"

    val source = sharedStringsFile.asFile
    val outputDir = generatedStringsDir
    inputs.file(source)
    outputs.dir(outputDir)

    doLast {
        // Both helpers are defined inside the task action: a configuration-cache
        // serializable task may not capture references to Gradle script objects.
        // Escape for a Kotlin string literal. Three characters matter:
        //   backslash, quote, and `$` — the last because `%1$s` style format
        //   placeholders would otherwise be read as a string template.
        fun esc(s: String) =
            s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("$", "\\$")

        // Android string-resource escaping. &amp; must be handled last so that
        // "&amp;lt;" does not become "<".
        fun xmlUnescape(v: String): String = v
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")

        val strings = StringBuilder()
        val arrays = StringBuilder()
        var stringCount = 0
        var arrayCount = 0

        source.readLines().forEach { raw ->
            val line = raw.trim()
            when {
                line.startsWith("<string name=\"") -> {
                    val name = line.substringAfter("name=\"").substringBefore("\"")
                    val body = line.substringAfter(">", "").substringBeforeLast("<")
                    strings.append("    const val $name: String = \"${esc(xmlUnescape(body))}\"\n")
                    stringCount++
                }
                line.startsWith("<string-array name=\"") -> {
                    val name = line.substringAfter("name=\"").substringBefore("\"")
                    arrays.append("    val $name: List<String> = listOf(\n")
                    arrayCount++
                }
                line.startsWith("<item>") -> {
                    val body = line.removePrefix("<item>").removeSuffix("</item>")
                    arrays.append("        \"${esc(xmlUnescape(body))}\",\n")
                }
                line.startsWith("</string-array>") -> {
                    arrays.append("    )\n")
                }
            }
        }

        val body = buildString {
            append("// GENERATED FILE - do not edit by hand.\n")
            append("// Source: composeApp/shared-strings/strings.xml\n")
            append("// Regenerated by the :composeApp:generateSakuStrings Gradle task.\n")
            append("package id.almezi.simplemoneytracker_kt\n\n")
            append("/**\n")
            append(" * Every user-facing string in Saku, generated from `shared-strings/strings.xml`.\n")
            append(" *\n")
            append(" * Indonesian by design: `AGENTS.md` requires Indonesian-first copy.\n")
            append(" *\n")
            append(" * Compile-time constants, so the string-resource plumbing disappears and the\n")
            append(" * same values are available on every target including Kotlin/Native.\n")
            append(" */\n")
            append("object SakuStrings {\n")
            append(strings)
            append("\n")
            append(arrays)
            append("}\n")
        }

        val pkgDir = outputDir.get().asFile.resolve("id/almezi/simplemoneytracker_kt")
        pkgDir.mkdirs()
        pkgDir.resolve("SakuStrings.kt").writeText(body)
        logger.lifecycle("Generated SakuStrings.kt ($stringCount strings, $arrayCount arrays)")
    }
}

// Register the generated directory as a source dir, passing the TASK PROVIDER
// rather than a plain path. That makes Gradle infer the dependency (and the
// KSP task in particular errors with "implicit dependency" otherwise).
kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(generateSakuStrings)

compose.resources {
    publicResClass = true
    packageOfResClass = "id.almezi.simplemoneytracker_kt"
}
