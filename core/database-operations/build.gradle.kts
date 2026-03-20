import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

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
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        // Android 和 JVM 共享的基础实现
        val jvmAndroidMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                // MySQL Driver (通用)
                implementation("com.mysql:mysql-connector-j:9.1.0")
                // Jedis for Redis - Android 使用 4.x（兼容 Commons Pool2）
                implementation("redis.clients:jedis:4.4.8")
            }
        }

        // Android 专用
        val androidMain by getting {
            dependsOn(jvmAndroidMain)
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                // PostgreSQL Driver
                implementation("org.postgresql:postgresql:42.7.4")
                // Android Stub (提供缺失的类：java.lang.management, com.mysql.cj 等)
                implementation(project(":core:android-stub"))
            }
        }

        // JVM 专用：支持 MySQL 和 PostgreSQL
        val jvmMain by getting {
            dependsOn(jvmAndroidMain)
            dependencies {
                // PostgreSQL Driver
                implementation("org.postgresql:postgresql:42.7.4")

            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
                // JUnit 5
                implementation("org.junit.jupiter:junit-jupiter:5.10.1")
                implementation("org.junit.platform:junit-platform-launcher:1.10.1")
                // H2 Database for testing
                implementation("com.h2database:h2:2.2.224")
                // Jedis for testing
                implementation("redis.clients:jedis:4.4.8")
            }
        }
    }
}

android {
    namespace = "space.xiaoxiao.databasemanager.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 27  // Android 8.1+ required for PostgreSQL JDBC driver (MethodHandle API)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
