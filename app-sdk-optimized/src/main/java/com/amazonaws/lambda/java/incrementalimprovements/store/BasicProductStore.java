package com.amazonaws.lambda.java.incrementalimprovements.store;

import com.amazonaws.lambda.java.incrementalimprovements.mappers.ProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.model.Product;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;

public class BasicProductStore implements ProductStore {

    public static final String TABLE_NAME = System.getenv("TABLE_NAME");
    public static final String BUCKET_NAME = System.getenv("BUCKET_NAME");

    protected final DynamoDbClient dynamoDbClient;
    protected final S3Client s3Client;
    protected final ProductMapper productMapper;

    public BasicProductStore(ProductMapper productMapper, DynamoDbClient dynamoDbClient, S3Client s3Client) {
        this.productMapper = productMapper;
        this.dynamoDbClient = dynamoDbClient;
        this.s3Client = s3Client;
    }

    public static Map<String, AttributeValue> toAttributeMap(Product product) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(Product.ID, AttributeValue.builder().s(product.getId()).build());
        item.put(Product.NAME, AttributeValue.builder().s(product.getName()).build());
        item.put(Product.PRICE, AttributeValue.builder().n(String.valueOf(product.getPrice())).build());
        return item;
    }

    @Override
    public void saveProduct(Product product) {
        saveProductInDynamo(product);
        saveProductInS3(product);
    }

    protected void saveProductInDynamo(Product product) {
        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(toAttributeMap(product))
                .build()
        );
    }

    protected void saveProductInS3(Product product) {
        s3Client.putObject(
                PutObjectRequest.builder().bucket(BUCKET_NAME).key(product.getId()).build(),
                RequestBody.fromString(productMapper.mapToJson(product))
        );
    }
}
