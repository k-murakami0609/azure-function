package com.openhack;

import com.microsoft.azure.documentdb.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CreateRating {
    /**
     * This function listens at endpoint "/api/CreateRating". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/CreateRating
     * 2. curl {your host}/api/CreateRating?name=HTTP%20Query
     */
    @FunctionName("CreateRating")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<RatingDto> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        RatingDto inputDto = request.getBody();
        context.getLogger().info("00000000000");
        context.getLogger().info(inputDto.id);
        context.getLogger().info(inputDto.locationName);
        context.getLogger().info("00000000000");

        FeedOptions queryOptions = new FeedOptions();
        queryOptions.setPageSize(-1);
        queryOptions.setEnableCrossPartitionQuery(true);

        DocumentClient documentClient = DocumentDbUtils.createClient();

        String databaseName = "OpenHack";
        String collectionName = "ratings";

        String collectionLink = String.format("/dbs/%s/colls/%s", databaseName, collectionName);
        
        try {
            documentClient.createDocument(collectionLink, inputDto, new RequestOptions(), true);
        } catch(Exception e) {
            System.out.println(e);
        }

        return request.createResponseBuilder(HttpStatus.OK).body("Hello, ").build();
    }
}
