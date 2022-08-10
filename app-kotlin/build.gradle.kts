plugins {
    java
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.7.0"
}

dependencyManagement {
    imports {
        mavenBom("software.amazon.awssdk:bom:${Dependencies.awsSdkV2Version}")
    }
}

dependencies {
    implementation(project(":shared"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Dependencies.kotlinxCoroutinesVersion}")

    implementation("aws.sdk.kotlin:s3:${Dependencies.awsKotlinSdkVersion}")
    implementation("aws.sdk.kotlin:dynamodb:${Dependencies.awsKotlinSdkVersion}")

    //implementation("software.amazon.awssdk:url-connection-client")
    implementation("org.slf4j:slf4j-simple:${Dependencies.slf4jSimpleVersion}")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("app-uber.jar")
    exclude("**/netty-nio-client*", "**/apache-client*", "**/*netty")
    destinationDirectory.set(project.buildDir)
}

val zipUberTask = tasks.register("zipUber", Zip::class.java) {
    from(tasks.shadowJar) {
        into("lib")
    }
    archiveBaseName.set("zipped-uber")
    destinationDirectory.set(project.buildDir)
}

tasks.build {
    finalizedBy(tasks.shadowJar, zipUberTask)
}
