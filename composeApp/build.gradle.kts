import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.aboutLibraries)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
            implementation(libs.koin.core)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.coil)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.androidx.compose.adaptive.navigation.suite)
            implementation(libs.androidx.compose.window.size)
            implementation(libs.androidx.browser)
            implementation(libs.aboutlibraries.compose)
            implementation(libs.reorderable)
            implementation(libs.compose.markdown)
            implementation(libs.google.fonts)
            
            implementation(libs.kmp.logger)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.androidx.compose.material3.alpha)
            implementation(libs.kotlinx.datetime)
            implementation(libs.cloudy)

            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)

            implementation(libs.aboutlibraries)
            
            implementation(libs.kmp.logger)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.dnfapps.arrmatey"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    val bugReportFile = file("../.github/ISSUE_TEMPLATE/bug_report.md")
    val bugReportContent = if (bugReportFile.exists()) {
        bugReportFile.readText()
            .replace(Regex("---[\\s\\S]*?---"), "")
            .trim()
            .replace("\n", "\\n")
            .replace("\"", "\\\"")
    } else {
        ""
    }

    defaultConfig {
        applicationId = "com.dnfapps.arrmatey"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 17
        versionName = "0.3.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles (for Google Play)
        includeInBundle = false
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

aboutLibraries {
    export {
        outputFile = file(layout.projectDirectory.file("../shared/src/commonMain/resources/aboutLibraries.json"))
        prettyPrint = true
    }
}