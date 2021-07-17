package com.slalom.cloudperks.cdk;

import software.amazon.awscdk.core.App;

public class CloudPerksApp {
    public static void main(final String[] args) {
        App app = new App();

        new CloudPerksStack(app, "cdk-cors-lambda-crud-dynamodb-example");

        app.synth();
    }
}
