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
            freeCompilerArgs.add("-Xno-param-names")
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
            implementation(libs.androidx.adaptive)
            implementation(libs.androidx.adaptive.layout)
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

    defaultConfig {
        applicationId = "com.dnfapps.arrmatey"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 20
        versionName = "0.4.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "**/baseline.prof"
            excludes += "**/baseline.profm"
            excludes += "/META-INF/version-control-info.textproto"
            excludes += "**/META-INF/*.version"
            excludes += "**/META-INF/com.android.tools/**"
            excludes += "**/META-INF/androidx.**"
            excludes += "**/META-INF/*.kotlin_module"
            excludes += "**/META-INF/proguard/**"
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
        outputFile = layout.buildDirectory.file("generated/aboutLibraries/aboutLibraries.json")
        prettyPrint = true
    }
}
