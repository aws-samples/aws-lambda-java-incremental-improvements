@file:Suppress("ObjectLiteralToLambda")

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
    implementation("software.amazon.awssdk:aws-crt-client:${Dependencies.awsSdkV2Version}-PREVIEW")
    implementation("org.slf4j:slf4j-simple:${Dependencies.slf4jSimpleVersion}")
    implementation("com.amazonaws:aws-lambda-java-runtime-interface-client:${Dependencies.awsLambdaRuntimeInterfaceClientVersion}")
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

val createArtifactTask = tasks.register("createArtifact", Zip::class.java) {
    from(projectDir.resolve("bootstrap"))
    from(project.buildDir.resolve("application"))
    archiveFileName.set("function.zip")
    destinationDirectory.set(project.buildDir)
}

val dockerTagName = "${rootProject.name}/${project.name}-${project.version}"

val generateNativeImageTask = tasks.register("generateNativeImage", Exec::class.java) {
    workingDir(projectDir)
    argumentProviders.add(object : CommandLineArgumentProvider {
        override fun asArguments() = listOf(
            "-v",
            "${project.buildDir}:/build",
            dockerTagName,
            "-jar",
            "app.jar",
            "/build/application",
            "--verbose",
            "--no-fallback",
            "--initialize-at-build-time=org.slf4j",
            "--enable-url-protocols=http",
            "-J--add-opens=java.base/java.util=ALL-UNNAMED",
            "-H:+AllowIncompleteClasspath"
        )
    })
    commandLine(
        "docker",
        "run"
    )
    outputs.file(project.buildDir.resolve("application"))
    finalizedBy(createArtifactTask)
}

val buildDockerImageTask = tasks.register("buildDockerImage", Exec::class.java) {
    workingDir(projectDir)
    argumentProviders.add(object : CommandLineArgumentProvider {
        override fun asArguments() = listOf(
            "--build-arg",
            "JAR_FILE=${tasks.shadowJar.get().outputs.files.singleFile.relativeTo(projectDir)}",
            "-f",
            project.relativePath("Dockerfile"),
            "-t",
            dockerTagName,
            "."
        )
    })
    commandLine(
        "docker",
        "build"
    )
    finalizedBy(generateNativeImageTask)
}


val shadowJar = tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveBaseName.set("app")
    destinationDirectory.set(project.buildDir.absoluteFile.resolve("lib"))
    finalizedBy(buildDockerImageTask)
    manifest {
        attributes["Main-Class"] = "com.amazonaws.services.lambda.runtime.api.client.AWSLambda"
    }
}

tasks {
    build {
        this.finalizedBy(shadowJar.get())
    }
}

/*graalvmNative {
    binaries {
        named("main") {
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(11))
                vendor.set(JvmVendorSpec.matching("GraalVM Community"))
            })
            verbose.set(true)
            fallback.set(false)
            imageName.set("application")
            mainClass.set("com.amazonaws.services.lambda.runtime.api.client.AWSLambda")
            buildArgs.add("--initialize-at-build-time=org.slf4j")
            buildArgs.add("--enable-url-protocols=http")
            buildArgs.add("-H:+AllowIncompleteClasspath")
        }
    }
}*/

