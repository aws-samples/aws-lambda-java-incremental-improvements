package com.amazonaws.lambda.java.incrementalimprovements.handlers.store

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.content.ByteStream
import com.amazonaws.lambda.java.incrementalimprovements.mappers.ProductMapper
import com.amazonaws.lambda.java.incrementalimprovements.model.Product
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking


class CoroutineProductStore(
    private val productMapper: ProductMapper,
    private val dynamodbClient: DynamoDbClient,
    private val s3Client: S3Client,
) {

    private val tableNameEnv = System.getenv("TABLE_NAME")
    private val bucketNameEnv = System.getenv("BUCKET_NAME")

    fun saveProduct(product: Product) {
        runBlocking {
            awaitAll(
                async {
                    saveProductInDynamo(product)
                },
                async {
                    saveProductInS3(product)
                }
            )
        }
    }

    private suspend fun saveProductInDynamo(product: Product) {
        dynamodbClient.putItem {
            tableName = tableNameEnv
            item = toAttributeMap(product)
        }
    }

    private fun toAttributeMap(product: Product) = mapOf(
        Product.ID to AttributeValue.S(product.id),
        Product.NAME to AttributeValue.S(product.name),
        Product.PRICE to AttributeValue.N(product.price.toString())
    )

    private suspend fun saveProductInS3(product: Product) {
        s3Client.putObject {
            bucket = bucketNameEnv
            key = product.id
            body = ByteStream.fromString(productMapper.mapToJson(product))
        }
    }

}