package com.amazonaws.lambda.java.incrementalimprovements.handlers;

import com.amazonaws.lambda.java.incrementalimprovements.mappers.GsonProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.mappers.ProductMapper;
import com.amazonaws.lambda.java.incrementalimprovements.model.Product;
import com.amazonaws.lambda.java.incrementalimprovements.store.AsyncProductStore;
import com.amazonaws.lambda.java.incrementalimprovements.store.ProductStore;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.util.UUID;

public class CrtHttpClientHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private final Gson gson = new GsonBuilder().create();
    private final ProductMapper productMapper = new GsonProductMapper(gson);
    private final EnvironmentVariableCredentialsProvider credentialsProvider = EnvironmentVariableCredentialsProvider.create();
    private final Region region = Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable()));
    private final SdkAsyncHttpClient httpClient = AwsCrtAsyncHttpClient.create();
    protected final DynamoDbAsyncClient dynamoDbAsyncClient = DynamoDbAsyncClient.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .httpClient(httpClient)
            .build();
    protected final S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .httpClient(httpClient)
            .build();
    protected ProductStore productStore = new AsyncProductStore(productMapper, dynamoDbAsyncClient, s3AsyncClient);

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
