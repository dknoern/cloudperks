package com.slalom.cloudperks.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.nio.ByteBuffer;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;

import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.services.kinesis.model.KinesisResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

public class CreateTransaction implements RequestHandler<Map<String,Object>, GatewayResponse>{
    
    @Override
    public GatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("inside lambda "+input.getClass()+ " data:"+input);

        String body = (String)input.get("body");
        logger.log("Body is:"+body);

        String output = "{'foo':'bar'}";

        KinesisClient kinesisClient = KinesisClient.create();

        PutRecordRequest request = PutRecordRequest.builder().streamName("card-transactions").partitionKey("thecardnumber").data(SdkBytes.fromUtf8String(body)).build();
        KinesisResponse response = kinesisClient.putRecord(request);

        logger.log("put record response = " + response.toString());


        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new GatewayResponse(output, headers, 200);
    }
}
