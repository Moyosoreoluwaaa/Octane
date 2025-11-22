plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ktorfit)
}

android {
    namespace = "com.octane"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.octane"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts += "META-INF/LICENSE.md"
            pickFirsts += "META-INF/LICENSE-notice.md"
        }
    }
}

sqldelight {
    databases {
        create("OctaneDatabase") {
            packageName.set("com.octane.db")
        }
    }
}

dependencies {
    // Core Android
    coreLibraryDesugaring(libs.android.desugarJdkLibs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.compose.animation)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room Core and Kotlin Extensions
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // For Flow and Coroutines support
    // Room Paging (Required for LimitOffsetPagingSource error)
    implementation(libs.androidx.room.paging)

    // Room Compiler using KSP
    ksp(libs.androidx.room.compiler)

    // Dependency Injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose)

    // Navigation
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.screenmodel)
    implementation(libs.voyager.transitions)

    // Networking & Serialization
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Ktorfit
    implementation(libs.ktorfit.lib)
    implementation(libs.ktor.client.logging)
    ksp(libs.ktorfit.ksp)

    // Utilities & Logging
    implementation(libs.napier)
    implementation(libs.androidx.biometric)

    // Image Loading
    implementation(libs.coil.compose)

    // Database
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.coroutines.extensions)

    // Immutable Collections
    implementation(libs.kotlinx.collections.immutable)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.ui.tooling.preview)

    // AndroidX Additions
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.work.runtime.ktx)

    // Firebase (Optional - comment out if not using Firebase)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.remote.config.ktx)

    // Security & Compliance
    implementation(libs.scottyab.rootbeer.lib)  // Now using correct JitPack coordinate
    implementation(libs.play.integrity)
    debugImplementation(libs.leakcanary.android)
    implementation(libs.androidx.security.crypto)

    // Preferences Storage
    implementation(libs.androidx.datastore.preferences)

    // Benchmarking
    androidTestImplementation(libs.androidx.benchmark.macro)

    /* TEMPORARILY COMMENTED OUT - Dependencies with Repository Issues
     *
     * WalletConnect and Solana Mobile Wallet Adapter require additional setup:
     *
     * 1. WalletConnect: The 'android-core' artifact has transitive dependencies on:
     *    - Scarlet libraries (custom fork, may need JitPack or custom Maven repo)
     *    - Kethereum libraries (available on JitPack as com.github.komputing)
     *    - java-multibase (available on JitPack as com.github.multiformats)
     *
     * 2. Solana Mobile Wallet Adapter: Requires authentication to GitHub Packages
     *
     * To re-enable these:
     * A. For WalletConnect:
     *    - Verify the correct Maven repository for Scarlet libraries
     *    - Or implement WalletConnect integration manually using WebSockets
     *
     * B. For Solana Mobile Wallet Adapter:
     *    - Add GitHub token to gradle.properties:
     *      gpr.user=your_github_username
     *      gpr.key=your_github_personal_access_token
     *    - Or use alternative Solana SDK that doesn't require authentication
     *
     * Uncomment when ready:
     * implementation(libs.walletconnect.core)
     * implementation(libs.solana.mobile.wallet.adapter)
     */
}