package com.slalom.cloudperks.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CreateCard implements RequestHandler<Map<String,Object>, GatewayResponse>{
    
    @Override
    public GatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("inside lambda "+input.getClass()+ " data:"+input);

        String body = (String)input.get("body");
        logger.log("Body is:"+body);

        String output = createCard(body);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new GatewayResponse(output, headers, 200);
    }

    private String createCard(String body) {
        DynamoDbClient ddb = DynamoDbClient.create();
        String tableName= "cards";
        String primaryKey = "cardNumber";
        Map<String, AttributeValue> item = new HashMap<>();


        JsonParser parser =  new JsonParser();
        JsonElement element = parser.parse(body);
        JsonObject jsonObject = element.getAsJsonObject();

        String cardNumber = jsonObject.get("cardNumber").getAsString();


        item.put(primaryKey, AttributeValue.builder().s(cardNumber).build());

        jsonObject.addProperty("cardRegisteredDateTime", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));

        Set<String> keys = jsonObject.keySet();
        for (String key: keys) {
            item.put(key, AttributeValue.builder().s(jsonObject.get(key).getAsString()).build());
        }

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
        PutItemResponse response = ddb.putItem(putItemRequest);
        return jsonObject.toString();

    }
}
