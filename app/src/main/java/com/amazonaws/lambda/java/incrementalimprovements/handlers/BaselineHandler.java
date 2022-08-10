package com.amazonaws.lambda.java.incrementalimprovements.handlers;

import com.amazonaws.lambda.java.incrementalimprovements.handlers.store.ProductStoreImpl;
import com.amazonaws.lambda.java.incrementalimprovements.mappers.GsonProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.mappers.ProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.model.Product;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.UUID;

public class BaselineHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    public APIGatewayV2HTTPResponse handleRequest(final APIGatewayV2HTTPEvent input, final Context context) {

        final AmazonDynamoDB dynamoDbClient = AmazonDynamoDBClientBuilder.defaultClient();
        final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        final Gson gson = new GsonBuilder().create();
        final ProductMapper productMapper = new GsonProductMapper(gson);
        final ProductStoreImpl productStore = new ProductStoreImpl(productMapper, dynamoDbClient, s3Client);

        APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder response = APIGatewayV2HTTPResponse.builder();

        try {
            Product product = productMapper.mapFromJson(input.getBody());
            product.setId(UUID.randomUUID().toString());
            productStore.saveProduct(product);
            response.withBody(productMapper.mapToJson(product));
        } catch (Exception e) {
            e.printStackTrace();
            context.getLogger().log(e.getMessage());
            response.withBody("error").withStatusCode(500);
        }
        return response.build();
    }
}
