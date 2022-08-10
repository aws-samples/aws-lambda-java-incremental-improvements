package com.amazonaws.lambda.java.incrementalimprovements.handlers.store;

import com.amazonaws.lambda.java.incrementalimprovements.mappers.ProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.model.Product;
import com.amazonaws.lambda.java.incrementalimprovements.store.ProductStore;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.s3.AmazonS3;

import java.util.HashMap;
import java.util.Map;

public class ProductStoreImpl implements ProductStore {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");

    private final AmazonDynamoDB dynamoDbClient;
    private final AmazonS3 s3Client;
    private final ProductMapper productMapper;

    public ProductStoreImpl(ProductMapper productMapper, AmazonDynamoDB dynamoDbClient, AmazonS3 s3Client) {
        this.productMapper = productMapper;
        this.dynamoDbClient = dynamoDbClient;
        this.s3Client = s3Client;
    }

    public void saveProduct(Product product) {
        saveProductInDynamo(product);
        saveProductInS3(product);
    }

    public void saveProductInDynamo(Product product) {
        dynamoDbClient.putItem(
                new PutItemRequest()
                        .withTableName(TABLE_NAME)
                        .withItem(toAttributeMap(product))
        );
    }

    private Map<String, AttributeValue> toAttributeMap(Product product) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(Product.ID, new AttributeValue(product.getId()));
        item.put(Product.NAME, new AttributeValue(product.getName()));
        item.put(Product.PRICE, new AttributeValue().withN(String.valueOf(product.getPrice())));
        return item;
    }

    public void saveProductInS3(Product product) {
        s3Client.putObject(BUCKET_NAME, product.getId(), productMapper.mapToJson(product));
    }
}
