package com.amazonaws.lambda.java.infrastructure;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigatewayv2.alpha.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.alpha.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.HttpLambdaIntegration;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.nio.file.Path;
import java.util.Map;

import static java.util.Collections.singletonList;

public class InfrastructureStack extends Stack {

    private static final String HANDLER_PACKAGE = "com.amazonaws.lambda.java.incrementalimprovements.handlers.";
    private static final Path APP_PATH = Path.of("../app/build");
    private static final AssetCode SDK_OPTIMIZED_CODE = AssetCode.fromAsset("../app-sdk-optimized/build/zipped-uber.zip");
    private static final AssetCode GRAALVM_CODE = AssetCode.fromAsset("../app-graalvm/build/function.zip");
    private static final AssetCode KOTLIN_CODE = AssetCode.fromAsset("../app-kotlin/build/zipped-uber.zip");

    public InfrastructureStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        HttpApi httpApi = new HttpApi(this, "ExampleApi", HttpApiProps.builder()
                .apiName("ExampleApi")
                .build());

        Table exampleTable = new Table(this, "ExampleTable", TableProps.builder()
                .partitionKey(Attribute.builder()
                        .type(AttributeType.STRING)
                        .name("id").build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());

        Bucket exampleBucket = new Bucket(this, "ExampleBucket");

        Function[] functions = new Function[]{
                //region Config only changes
                new Function(this, "Baseline", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "BaselineHandler")
                        .code(Code.fromAsset(APP_PATH.resolve("app-lib.zip").toString()))
                        .build()
                ),
                new Function(this, "MemoryOptimized", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "BaselineHandler")
                        .code(Code.fromAsset(APP_PATH.resolve("app-lib.zip").toString()))
                        .memorySize(2048)
                        .build()
                ),
                new Function(this, "UberJar", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "BaselineHandler")
                        .code(Code.fromAsset(APP_PATH.resolve("app-uber.jar").toString()))
                        .memorySize(2048)
                        .build()
                ),
                new Function(this, "UberJarZipped", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "BaselineHandler")
                        .code(Code.fromAsset(APP_PATH.resolve("zipped-uber.zip").toString()))
                        .memorySize(2048)
                        .build()
                ),
                new Function(this, "TieredCompilation", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "BaselineHandler")
                        .code(Code.fromAsset(APP_PATH.resolve("zipped-uber.zip").toString()))
                        .memorySize(2048)
                        .environment(
                                Map.of("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1")
                        )
                        .build()
                ),
                //endregion
                //region Code changes
                new Function(this, "EagerInit", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "EagerInitHandler")
                        .code(Code.fromAsset(APP_PATH.resolve("zipped-uber.zip").toString()))
                        .memorySize(2048)
                        .environment(
                                Map.of("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1")
                        )
                        .build()
                ),
                new Function(this, "SdkOptimizations", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "SdkOptimizationsHandler")
                        .code(SDK_OPTIMIZED_CODE)
                        .memorySize(2048)
                        .environment(
                                Map.of("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1")
                        )
                        .build()
                ),
                new Function(this, "Multithreading", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "MultithreadingHandler")
                        .code(SDK_OPTIMIZED_CODE)
                        .memorySize(2048)
                        .environment(
                                Map.of("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1")
                        )
                        .build()
                ),
                new Function(this, "KotlinSdk", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "KotlinHandler")
                        .code(KOTLIN_CODE)
                        .memorySize(2048)
                        .environment(
                                Map.of("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1")
                        )
                        .build()
                ),
                new Function(this, "CrtHttpClient", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "CrtHttpClientHandler")
                        .code(SDK_OPTIMIZED_CODE)
                        .memorySize(2048)
                        .environment(
                                Map.of("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1")
                        )
                        .build()
                ),
                //endregion
                //region Advanced
                new Function(this, "SdkInitCall", getBaseFunctionProps()
                        .handler(HANDLER_PACKAGE + "SdkInitCallHandler")
                        .code(SDK_OPTIMIZED_CODE)
                        .memorySize(2048)
                        .environment(
                                Map.of("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1")
                        )
                        .build()
                ),
                new Function(this, "GraalVM", getBaseFunctionProps()
                        .runtime(Runtime.PROVIDED_AL2)
                        .handler(HANDLER_PACKAGE + "GraalVmHandler")
                        .code(GRAALVM_CODE)
                        .environment(
                                Map.of("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1")
                        )
                        .memorySize(256)
                        .build()
                )
                //endregion
        };


        int number = 0;
        for (Function function : functions) {

            String constructId = function.getNode().getId();
            Tags.of(function).add("OptimizeSteps", String.format("Step %d", number), TagProps.builder()
                    .priority(300).build());

            function.addEnvironment("TABLE_NAME", exampleTable.getTableName());
            function.addEnvironment("BUCKET_NAME", exampleBucket.getBucketName());

            exampleBucket.grantPut(function);
            exampleTable.grantWriteData(function);

            httpApi.addRoutes(AddRoutesOptions.builder()
                    .path(String.format("/%s", constructId.replaceAll("([^A-Z])([A-Z])", "$1-$2").toLowerCase()))
                    .methods(singletonList(HttpMethod.PUT))
                    .integration(
                            new HttpLambdaIntegration(String.format("%sLambdaIntegration", id), function))
                    .build());

            number++;
        }

        new CfnOutput(this, "ApiEndpoint", CfnOutputProps.builder()
                .value(httpApi.getApiEndpoint())
                .build());
    }

    private FunctionProps.Builder getBaseFunctionProps() {
        return FunctionProps.builder()
                .memorySize(512)
                .timeout(Duration.seconds(60))
                .logRetention(RetentionDays.ONE_WEEK)
                .runtime(Runtime.JAVA_11);
    }
}
