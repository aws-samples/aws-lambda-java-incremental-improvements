package com.amazonaws.lambda.java.incrementalimprovements.handlers

import aws.sdk.kotlin.runtime.auth.credentials.EnvironmentCredentialsProvider
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.content.ByteStream
import com.amazonaws.lambda.java.incrementalimprovements.handlers.store.CoroutineProductStore
import com.amazonaws.lambda.java.incrementalimprovements.mappers.GsonProductMapper
import com.amazonaws.lambda.java.incrementalimprovements.model.Product
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import java.util.*

class KotlinHandler :
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private val envRegion = System.getenv("AWS_REGION")
    private val envCredentials = EnvironmentCredentialsProvider()

    private val dynamodbClient = DynamoDbClient {
        region = envRegion
        credentialsProvider = envCredentials
    }

    private val s3Client = S3Client {
        region = envRegion
        credentialsProvider = envCredentials
    }

    private val gson = GsonBuilder().create()
    private val productMapper = GsonProductMapper(gson)
    private val productStore = CoroutineProductStore(productMapper, dynamodbClient, s3Client)

    init {
        warmupSdk()
    }

    private fun warmupSdk() {
        runBlocking {
            runCatching {
                dynamodbClient.putItem {
                    tableName = "dummy"
                    item = mapOf()
                }
            }
            runCatching {
                s3Client.putObject {
                    key = "dummy"
                    bucket = "dummy"
                    body = ByteStream.fromString("")
                }
            }
        }
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val response = APIGatewayProxyResponseEvent()

        return try {
            val product: Product = productMapper.mapFromJson(input.body)
            product.id = UUID.randomUUID().toString()
            productStore.saveProduct(product)
            response
                .withBody(productMapper.mapToJson(product))
                .withStatusCode(200)
        } catch (e: Exception) {
            e.printStackTrace()
            context.logger.log(e.message)
            response.withBody("error").withStatusCode(500)
        }
    }
}