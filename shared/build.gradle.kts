plugins {
    java
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.code.gson:gson:${Dependencies.gsonVersion}")
    api("com.amazonaws:aws-lambda-java-core:${Dependencies.awsLambdaJavaCoreVersion}")
    api("com.amazonaws:aws-lambda-java-events:${Dependencies.awsLambdaJavaEventsVersion}")
}