plugins {
    java
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

dependencyManagement {
    imports {
        mavenBom("software.amazon.awssdk:bom:${Dependencies.awsSdkV2Version}")
    }
}

dependencies {
    implementation(project(":shared"))

    implementation("software.amazon.awssdk:dynamodb")
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:url-connection-client")
    implementation("software.amazon.awssdk:aws-crt-client:${Dependencies.awsSdkV2Version}-PREVIEW")
    implementation("org.slf4j:slf4j-simple:${Dependencies.slf4jSimpleVersion}")
}


configurations {
    runtimeClasspath {
        exclude("software.amazon.awssdk", "apache-client")
        exclude("software.amazon.awssdk", "netty-nio-client")
        exclude("org.eclipse.jetty.websocket")
        exclude("org.eclipse.jetty")
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("app-uber.jar")
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
