buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.2.10-2.0.2")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}
