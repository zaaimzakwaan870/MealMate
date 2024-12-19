// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google() // Google's Maven repository
        mavenCentral() // Maven Central repository
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2") // Android Gradle plugin version
        classpath("com.google.gms:google-services:4.3.14") // Firebase Google Services plugin
    }
}

allprojects {
    repositories {
        google() // Google's Maven repository
        mavenCentral() // Maven Central repository
    }
}
