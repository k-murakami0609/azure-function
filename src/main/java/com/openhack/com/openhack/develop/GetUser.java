package com.openhack.com.openhack.develop;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.FeedResponse;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.openhack.Error;
import com.openhack.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GetUser {

    private static final String DATABASE_NAME = "OpenHack";
    private static final String COLLECTION_NAME = "users";
    private Gson gson = new Gson();
    private DocumentClient documentClient;
    private FeedOptions queryOptions;

    public GetUser() {
        this.documentClient = new DocumentClient(
                System.getenv("serviceEndpoint"),
                System.getenv("masterKey"),
                ConnectionPolicy.GetDefault(),
                ConsistencyLevel.Session
        );
        FeedOptions queryOptions = new FeedOptions();
        queryOptions.setPageSize(-1);
        queryOptions.setEnableCrossPartitionQuery(true);
        this.queryOptions = queryOptions;
    }

    @FunctionName("GetUser")
    public HttpResponseMessage getUser(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        // Parse query parameter
        String query = request.getQueryParameters().get("userId");
        String userId = request.getBody().orElse(query);

        if (userId == null || userId.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).header("Content-Type", "application/json").body(Error.builder().code(HttpStatus.BAD_REQUEST.value()).message("Query parameter userId is must not be null or empty")).build();
        }

        String collectionLink = String.format("/dbs/%s/colls/%s", DATABASE_NAME, COLLECTION_NAME);
        FeedResponse<Document> queryResults = documentClient.queryDocuments(collectionLink, "SELECT * FROM users r where r.userId='" + userId + "'", queryOptions);

        List<User> userList = new ArrayList<>();
        queryResults.getQueryIterable().forEach(document -> {
            userList.add(gson.fromJson(String.format("%s", document), User.class));
        });

        if (userList.size() == 0) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(Error.builder().code(HttpStatus.NOT_FOUND.value()).message("ID " + userId + " is not found.")).header("Content-Type", "application/json").build();
        }
        return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json").body(userList.get(0)).build();
    }
}
