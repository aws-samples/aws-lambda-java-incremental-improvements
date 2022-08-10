package com.amazonaws.lambda.java.incrementalimprovements.handlers;

import com.amazonaws.lambda.java.incrementalimprovements.mappers.GsonProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.mappers.ProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.model.Product;
import com.amazonaws.lambda.java.incrementalimprovements.store.BasicProductStore;
import com.amazonaws.lambda.java.incrementalimprovements.store.ProductStore;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.UUID;

public class SdkOptimizationsHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private final Gson gson = new GsonBuilder().create();
    private final ProductMapper productMapper = new GsonProductMapper(gson);
    private final EnvironmentVariableCredentialsProvider credentialsProvider = EnvironmentVariableCredentialsProvider.create();
    private final Region region = Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable()));
    private final SdkHttpClient httpClient = UrlConnectionHttpClient.create();
    protected final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .httpClient(httpClient)
            .build();
    protected final S3Client s3Client = S3Client.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .httpClient(httpClient)
            .build();
    protected ProductStore productStore = new BasicProductStore(productMapper, dynamoDbClient, s3Client);

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
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
