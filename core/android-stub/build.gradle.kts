plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "space.xiaoxiao.databasemanager.core.android.stub"
    compileSdk = 35

    defaultConfig {
        minSdk = 27
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}
