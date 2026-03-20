import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore/keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
val hasReleaseKeystore = keystorePropertiesFile.exists()

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:database-operations"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            // Ktor for HTTP client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
        }

        commonTest.dependencies {
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation("androidx.security:security-crypto:1.1.0-alpha06")
            // Coil for SVG support on Android
            implementation(libs.coil.compose)
            implementation(libs.coil.svg)
            // Apache POI for Excel export
            implementation("org.apache.poi:poi:5.2.5")
            implementation("org.apache.poi:poi-ooxml:5.2.5")
            // Ktor Android engine
            implementation(libs.ktor.client.android)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.0")
            // Apache POI for Excel export (JVM only)
            implementation("org.apache.poi:poi:5.2.5")
            implementation("org.apache.poi:poi-ooxml:5.2.5")
            // Ktor JVM engine
            implementation(libs.ktor.client.okhttp)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.uiTestJUnit4)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
        }
    }
}

android {
    namespace = "space.xiaoxiao.databasemanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "space.xiaoxiao.databasemanager"
        minSdk = 27  // Android 8.1+ required for PostgreSQL JDBC driver (MethodHandle API)
        targetSdk = 35
        versionCode = 4
        versionName = "1.3.0"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                val storeFilePath = (keystoreProperties["storeFile"] as String?)?.trim().orEmpty()
                require(storeFilePath.isNotBlank()) { "Missing storeFile in ${keystorePropertiesFile.path}" }

                storeFile = rootProject.file(storeFilePath)
                storePassword = (keystoreProperties["storePassword"] as String?)?.trim()
                keyAlias = (keystoreProperties["keyAlias"] as String?)?.trim()
                keyPassword = (keystoreProperties["keyPassword"] as String?)?.trim()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/*.properties"
        }
    }
}

compose.desktop {
    application {
        mainClass = "space.xiaoxiao.databasemanager.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb)
            packageName = "database-manager"
            packageVersion = "1.0.0"
        }
    }
}

afterEvaluate {
    val requireKeystoreForRelease: (String) -> Unit = { taskName ->
        tasks.matching { it.name == taskName }.configureEach {
            doFirst {
                if (!keystorePropertiesFile.exists()) {
                    throw GradleException(
                        "Release signing config missing. Create ${keystorePropertiesFile.path} (run scripts/android/generate-release-keystore.sh)."
                    )
                }
            }
        }
    }
    requireKeystoreForRelease("assembleRelease")
    requireKeystoreForRelease("bundleRelease")
    requireKeystoreForRelease("packageRelease")
}
