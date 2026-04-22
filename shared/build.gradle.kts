import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import dev.icerock.gradle.MultiplatformResourcesPluginExtension

buildscript {
    dependencies {
        classpath(libs.resources.generator)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.skie)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.aboutLibraries)
    kotlin("plugin.serialization") version libs.versions.kotlin
}

apply(plugin = "dev.icerock.mobile.multiplatform-resources")

skie {
    features {
        enableSwiftUIObservingPreview = true
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(libs.moko.resources)
            linkerOpts("-linker-option", "-ObjC")
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        androidMain.dependencies {
            // Android-specific
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.androidx.core.ktx)

            // Koin Compose
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.cloudy)
        }

        commonMain.dependencies {
            // Koin DI
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            // Networking
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Coroutines & Utilities
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Database
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // DataStore
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            // MOKO Resources
            api(libs.moko.resources)
            api(libs.moko.resources.compose)

            implementation(libs.kmp.logger)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.dnfapps.arrmatey.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    buildFeatures {
        buildConfig = true
    }

    sourceSets {
        named("main") {
            res.srcDirs("build/generated/moko/androidMain/res")
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

configure<MultiplatformResourcesPluginExtension> {
    resourcesPackage.set("com.dnfapps.arrmatey.shared")
    resourcesClassName.set("MR")
    iosBaseLocalizationRegion.set("en")
}

aboutLibraries {
    export {
        outputFile = layout.buildDirectory.file("generated/aboutLibraries/aboutLibraries.json")
        prettyPrint = true
        excludeFields = setOf("funding")
    }
}

// Copy libraries JSON to iOS - Configuration cache compatible
abstract class CopyLibrariesToIOSTask : DefaultTask() {
    @get:InputFile
    abstract val sourceFile: RegularFileProperty

    @get:OutputFile
    abstract val targetFile: RegularFileProperty

    @TaskAction
    fun copy() {
        val source = sourceFile.get().asFile
        val target = targetFile.get().asFile

        if (!source.exists()) {
            logger.warn("Source file does not exist: ${source.absolutePath}")
            return
        }

        target.parentFile.mkdirs()
        source.copyTo(target, overwrite = true)

        logger.lifecycle("Exported aboutLibraries.json (${target.length()} bytes) to iOS")
    }
}

val exportLibrariesToIOS = tasks.register<CopyLibrariesToIOSTask>("exportLibrariesToIOS") {
    group = "build"
    description = "Export AboutLibraries JSON to iOS"

    dependsOn("exportLibraryDefinitions")

    sourceFile.set(layout.buildDirectory.file("generated/aboutLibraries/aboutLibraries.json"))
    targetFile.set(layout.projectDirectory.file("../iosApp/iosApp/Resources/aboutLibraries.json"))
}

afterEvaluate {
    tasks.matching {
        it.name.contains("compileKotlin") && it.name.contains("Ios")
    }.configureEach {
        finalizedBy(exportLibrariesToIOS)
    }
}