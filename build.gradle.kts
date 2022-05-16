// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("android") apply false
}

buildscript {
    dependencies {
        classpath(Android.tools.build.gradlePlugin)
        classpath(AndroidX.navigation.safeArgsGradlePlugin)
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:_")
    }
}

val clean by tasks.creating(Delete::class) {
    group = "build"
    delete(rootProject.buildDir)
}
