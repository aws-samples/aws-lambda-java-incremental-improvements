plugins {
    java
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

dependencyManagement {
    imports {
        mavenBom("com.amazonaws:aws-java-sdk-bom:${Dependencies.awsSdkV1Version}")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("com.amazonaws:aws-java-sdk-s3")
    implementation("com.amazonaws:aws-java-sdk-dynamodb")
    implementation("javax.xml.bind:jaxb-api:${Dependencies.jaxbApiVersion}")
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

val zipTask = tasks.register("zipLibs", Zip::class.java) {
    from(tasks.compileJava)
    from(tasks.processResources)
    from(configurations.runtimeClasspath.get()) {
        into("lib")
    }
    archiveBaseName.set("app-lib")
    destinationDirectory.set(project.buildDir)
}


tasks.build {
    finalizedBy(zipTask, tasks.shadowJar, zipUberTask)
}
