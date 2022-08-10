plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

subprojects {

    repositories {
        mavenCentral()
    }

    apply {
        plugin("org.gradle.java")
        plugin("com.github.johnrengelman.shadow")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11;
        targetCompatibility = JavaVersion.VERSION_11;
    }
}