import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    android {
        namespace = "com.crylo.kidreels.core"
        compileSdk = 37
        minSdk = 31
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    // The desktop JVM target is what the unit tests run on, and it keeps core honest:
    // anything Android-only would fail to compile here.
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
