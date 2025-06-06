plugins {
    id("com.google.devtools.ksp") version(Version.getKSPVersion())
}

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        classpath(BuildPlugin.kotlin)
        classpath(BuildPlugin.android)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}