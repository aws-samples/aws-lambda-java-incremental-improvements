package com.amazonaws.lambda.java.incrementalimprovements.handlers;


import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
import java.util.concurrent.*;

import static com.amazonaws.lambda.java.incrementalimprovements.Utils.ignoreException;

public class SdkInitCallHandler extends SdkOptimizationsHandler {

    //do dummy calls to use init boost
    public SdkInitCallHandler() {
        super();
        warmupSdk();
    }

    private void warmupSdk() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        List<Callable<Void>> tasks = List.of(() -> {
            ignoreException(() -> dynamoDbClient.putItem(PutItemRequest.builder().build()));
            return null;
        }, () -> {
            ignoreException(() -> s3Client.putObject(PutObjectRequest.builder().build(), RequestBody.empty()));
            return null;
        });

        try {
            for (Future<Void> task : executor.invokeAll(tasks)) {
                task.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            //ignore
        }

    }


}
