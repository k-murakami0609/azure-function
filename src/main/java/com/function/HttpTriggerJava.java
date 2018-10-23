package com.function;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.*;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;

/**
 * Azure Functions with HTTP Trigger.
 */
public class HttpTriggerJava {
    /**
     * This function listens at endpoint "/api/HttpTriggerJava". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTriggerJava
     * 2. curl {your host}/api/HttpTriggerJava?name=HTTP%20Query
     */
    @FunctionName("HttpTriggerJava")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        FeedOptions queryOptions = new FeedOptions();
        queryOptions.setPageSize(-1);
        queryOptions.setEnableCrossPartitionQuery(true);

        DocumentClient documentClient = new DocumentClient(
            "", 
            "", 
            ConnectionPolicy.GetDefault(), 
            ConsistencyLevel.Session
        );

        String databaseName = "OpenHack";
        String collectionName = "ratings";

        String collectionLink = String.format("/dbs/%s/colls/%s", databaseName, collectionName);

        FeedResponse<Document> queryResults = documentClient.queryDocuments(
            collectionLink,
            "SELECT * FROM c",
            queryOptions
        );

        for (Document family : queryResults.getQueryIterable()) {
            System.out.println(String.format("\tRead %s", family));
        }

        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }
}
