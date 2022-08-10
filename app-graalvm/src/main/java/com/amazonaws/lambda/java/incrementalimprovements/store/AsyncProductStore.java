package com.amazonaws.lambda.java.incrementalimprovements.store;

import com.amazonaws.lambda.java.incrementalimprovements.mappers.ProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.model.Product;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AsyncProductStore implements ProductStore {

    public static final String TABLE_NAME = System.getenv("TABLE_NAME");
    public static final String BUCKET_NAME = System.getenv("BUCKET_NAME");

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
                .tableName(TABLE_NAME)
                .item(toAttributeMap(product))
                .build()
        );
    }

    public CompletableFuture<PutObjectResponse> saveProductInS3(Product product) {
        return s3AsyncClient.putObject(
                PutObjectRequest.builder().bucket(BUCKET_NAME).key(product.getId()).build(),
                AsyncRequestBody.fromString(productMapper.mapToJson(product))
        );
    }

    public static Map<String, AttributeValue> toAttributeMap(Product product) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(Product.ID, AttributeValue.builder().s(product.getId()).build());
        item.put(Product.NAME, AttributeValue.builder().s(product.getName()).build());
        item.put(Product.PRICE, AttributeValue.builder().n(String.valueOf(product.getPrice())).build());
        return item;
    }
}
