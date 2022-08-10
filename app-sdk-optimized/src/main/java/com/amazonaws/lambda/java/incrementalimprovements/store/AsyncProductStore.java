package com.amazonaws.lambda.java.incrementalimprovements.store;

import com.amazonaws.lambda.java.incrementalimprovements.mappers.ProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.model.Product;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.concurrent.CompletableFuture;

import static com.amazonaws.lambda.java.incrementalimprovements.store.BasicProductStore.toAttributeMap;

public class AsyncProductStore implements ProductStore {

    private final ProductMapper productMapper;
    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private final S3AsyncClient s3AsyncClient;

    public AsyncProductStore(ProductMapper productMapper, DynamoDbAsyncClient dynamoDbAsyncClient, S3AsyncClient s3AsyncClient) {
        this.productMapper = productMapper;
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
        this.s3AsyncClient = s3AsyncClient;
    }


    public void saveProduct(Product product) {
        CompletableFuture.allOf(
                CompletableFuture.supplyAsync(() -> saveProductInDynamo(product)),
                CompletableFuture.supplyAsync(() -> saveProductInS3(product))
        ).join();
    }

    public CompletableFuture<PutItemResponse> saveProductInDynamo(Product product) {
        return dynamoDbAsyncClient.putItem(PutItemRequest.builder()
                .tableName(BasicProductStore.TABLE_NAME)
                .item(toAttributeMap(product))
                .build()
        );
    }

    public CompletableFuture<PutObjectResponse> saveProductInS3(Product product) {
        return s3AsyncClient.putObject(
                PutObjectRequest.builder().bucket(BasicProductStore.BUCKET_NAME).key(product.getId()).build(),
                AsyncRequestBody.fromString(productMapper.mapToJson(product))
        );
    }
}
