package com.slalom.cloudperks.cdk;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CorsLambdaCrudDynamodbStack CDK example for Java!
 */
class CloudPerksStack extends Stack {
    public CloudPerksStack(final Construct parent, final String name) {
        super(parent, name);

        Table dynamodbTable = createDynamoDbTable("items", "itemId");
        Table membersTable = createDynamoDbTable("members", "memberId");
        Table cardsTable = createDynamoDbTable("cards", "cardNumber");
        Table pointsTable = createDynamoDbTable("points", "cardNumber");
        Table transactionsTable = createDynamoDbTable("transactions", "transactionId");

        Map<String, String> lambdaEnvMap = new HashMap<>();
        lambdaEnvMap.put("GOLD_THRESHOLD", "150");

        Function createCardFunction = new Function(this, "createCardFunction",
                getLambdaFunctionProps(lambdaEnvMap, "com.slalom.cloudperks.lambda.CreateCard"));
        Function createMemberFunction = new Function(this, "createMemberFunction",
                getLambdaFunctionProps(lambdaEnvMap, "com.slalom.cloudperks.lambda.CreateMember"));
        Function createTransactionFunction = new Function(this, "createTransactionFunction",
                getLambdaFunctionProps(lambdaEnvMap, "com.slalom.cloudperks.lambda.CreateTransaction"));
        Function getMemberFunction = new Function(this, "getMemberFunction",
                getLambdaFunctionProps(lambdaEnvMap, "com.slalom.cloudperks.lambda.GetMember"));
        Function getPointsFunction = new Function(this, "getPointsFunction",
                getLambdaFunctionProps(lambdaEnvMap, "com.slalom.cloudperks.lambda.GetPoints"));
        Function redeemPointsFunction = new Function(this, "redeemPointsFunction",
                getLambdaFunctionProps(lambdaEnvMap, "com.slalom.cloudperks.lambda.RedeemPoints"));


        cardsTable.grantReadWriteData(createCardFunction);
        membersTable.grantReadWriteData(createMemberFunction);

        RestApi api = new RestApi(this, "cloudPerksApi",
                RestApiProps.builder().restApiName("CloudPerks Service").build());

        IResource members = api.getRoot().addResource("members");
        IResource cards = api.getRoot().addResource("cards");
        IResource points = api.getRoot().addResource("points");
        IResource transactions = api.getRoot().addResource("transactions");

        addCorsOptions(members);
        addCorsOptions(cards);
        addCorsOptions(points);
        addCorsOptions(transactions);

        addMethod(members, createMemberFunction,"POST");
        addMethod(cards, createCardFunction,"POST");
        addMethod(transactions, createTransactionFunction,"POST");
        addMethod(members, getMemberFunction,"GET");
        addMethod(points, redeemPointsFunction,"POST");
        addMethod(points, getPointsFunction,"GET");
    }

    private void addMethod(IResource resource, Function function, String method)
    {
        Integration integration = new LambdaIntegration(function);
        resource.addMethod(method, integration);
    }

    private Table createDynamoDbTable(String tableName, String partitionKeyName) {

        TableProps tableProps;
        Attribute partitionKey = Attribute.builder()
                .name(partitionKeyName)
                .type(AttributeType.STRING)
                .build();
        tableProps = TableProps.builder()
                .tableName(tableName)
                .partitionKey(partitionKey)
                // The default removal policy is RETAIN, which means that cdk destroy will not attempt to delete
                // the new table, and it will remain in your account until manually deleted. By setting the policy to
                // DESTROY, cdk destroy will delete the table (even if it has data in it)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        return new Table(this, tableName, tableProps);
    }


    private void addCorsOptions(IResource item) {
        List<MethodResponse> methoedResponses = new ArrayList<>();

        Map<String, Boolean> responseParameters = new HashMap<>();
        responseParameters.put("method.response.header.Access-Control-Allow-Headers", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Methods", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Credentials", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Origin", Boolean.TRUE);
        methoedResponses.add(MethodResponse.builder()
                .responseParameters(responseParameters)
                .statusCode("200")
                .build());
        MethodOptions methodOptions = MethodOptions.builder()
                .methodResponses(methoedResponses)
                .build()
                ;

        Map<String, String> requestTemplate = new HashMap<>();
        requestTemplate.put("application/json","{\"statusCode\": 200}");
        List<IntegrationResponse> integrationResponses = new ArrayList<>();

        Map<String, String> integrationResponseParameters = new HashMap<>();
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Headers","'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,X-Amz-User-Agent'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Origin","'*'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Credentials","'false'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Methods","'OPTIONS,GET,PUT,POST,DELETE'");
        integrationResponses.add(IntegrationResponse.builder()
                .responseParameters(integrationResponseParameters)
                .statusCode("200")
                .build());
        Integration methodIntegration = MockIntegration.Builder.create()
                .integrationResponses(integrationResponses)
                .passthroughBehavior(PassthroughBehavior.NEVER)
                .requestTemplates(requestTemplate)
                .build();

        item.addMethod("OPTIONS", methodIntegration, methodOptions);
    }

    private FunctionProps getLambdaFunctionProps(Map<String, String> lambdaEnvMap, String handler) {
        return FunctionProps.builder()
                    .code(Code.fromAsset("./lambda/target/lambda-1.0.0-jar-with-dependencies.jar"))
                    .handler(handler)
                    .runtime(Runtime.JAVA_8)
                    .environment(lambdaEnvMap)
                    .timeout(Duration.seconds(30))
                    .memorySize(512)
                    .build();
    }
}
