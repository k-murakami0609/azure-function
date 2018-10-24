package com.openhack;

import com.google.gson.Gson;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.FeedResponse;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GetRating {

    private static final String DATABASE_NAME = "OpenHack";
    private static final String COLLECTION_NAME = "ratings";
    private Gson gson = new Gson();
    private DocumentClient documentClient;
    private FeedOptions queryOptions;

    public GetRating() {
        this.documentClient = DocumentDbUtils.createClient();

        FeedOptions queryOptions = new FeedOptions();
        queryOptions.setPageSize(-1);
        queryOptions.setEnableCrossPartitionQuery(true);
        this.queryOptions = queryOptions;
    }

    @FunctionName("GetRating")
    public HttpResponseMessage getRating(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        // Parse query parameter
        String query = request.getQueryParameters().get("ratingId");
        String ratingId = request.getBody().orElse(query);

        if (ratingId == null || ratingId.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).header("Content-Type", "application/json").body(Error.builder().code(HttpStatus.BAD_REQUEST.value()).message("Query parameter ratingId is must not be null or empty")).build();
        }

        String collectionLink = String.format("/dbs/%s/colls/%s", DATABASE_NAME, COLLECTION_NAME);
        FeedResponse<Document> queryResults = documentClient.queryDocuments(collectionLink, "SELECT * FROM ratings r where r.id='" + ratingId + "'", queryOptions);

        List<Rating> ratingList = new ArrayList<>();
        queryResults.getQueryIterable().forEach(document -> {
            ratingList.add(gson.fromJson(String.format("%s", document), Rating.class));
        });

        if (ratingList.size() == 0) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(Error.builder().code(HttpStatus.NOT_FOUND.value()).message("ID " + ratingId + " is not found.")).header("Content-Type", "application/json").build();
        }
        return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json").body(ratingList.get(0)).build();
    }

    @FunctionName("GetRatings")
    public HttpResponseMessage getRatings(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        // Parse query parameter
        String query = request.getQueryParameters().get("userId");
        String userId = request.getBody().orElse(query);

        if (userId == null || userId.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).header("Content-Type", "application/json").body(Error.builder().code(HttpStatus.BAD_REQUEST.value()).message("Query parameter userId is must not be null or empty")).build();
        }

        String collectionLink = String.format("/dbs/%s/colls/%s", DATABASE_NAME, COLLECTION_NAME);
        FeedResponse<Document> queryResults = documentClient.queryDocuments(collectionLink, "SELECT * FROM ratings r where r.userId='" + userId + "'", queryOptions);

        List<Rating> ratingList = new ArrayList<>();
        queryResults.getQueryIterable().forEach(document -> {
            ratingList.add(gson.fromJson(String.format("%s", document), Rating.class));
        });

        if (ratingList.size() == 0) {
            return request.createResponseBuilder(HttpStatus.NO_CONTENT).header("Content-Type", "application/json").build();
        }
        return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json").body(ratingList).build();
    }
}
