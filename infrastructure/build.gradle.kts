plugins {
    application
}

repositories {
    mavenCentral()
}

val cdkVersion = "2.29.1"
val cdkAlphaVersion = "$cdkVersion-alpha.0"
val cdkConstructsVersion = "10.1.42"

dependencies {
    dependencies {
        implementation("software.amazon.awscdk:aws-cdk-lib:$cdkVersion")
        implementation("software.amazon.awscdk:apigatewayv2-alpha:$cdkAlphaVersion")
        implementation("software.amazon.awscdk:apigatewayv2-integrations-alpha:$cdkAlphaVersion")
        implementation("software.constructs:constructs:$cdkConstructsVersion")
    }
}


application {
    mainClass.set("com.amazonaws.lambda.java.infrastructure.InfrastructureApp")
}
