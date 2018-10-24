package com.openhack;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GetProduct {

    private static final String DATABASE_NAME = "OpenHack";
    private static final String COLLECTION_NAME = "products";
    private Gson gson = new Gson();
    private DocumentClient documentClient;
    private FeedOptions queryOptions;

    public GetProduct() {
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

    @FunctionName("GetProduct")
    public HttpResponseMessage getProduct(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        // Parse query parameter
        String query = request.getQueryParameters().get("productId");
        String productId = request.getBody().orElse(query);

        if (productId == null || productId.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).header("Content-Type", "application/json").body(Error.builder().code(HttpStatus.BAD_REQUEST.value()).message("Query parameter productId is must not be null or empty")).build();
        }

        String collectionLink = String.format("/dbs/%s/colls/%s", DATABASE_NAME, COLLECTION_NAME);
        FeedResponse<Document> queryResults = documentClient.queryDocuments(collectionLink, "SELECT * FROM products p where p.productId='" + productId + "'", queryOptions);

        List<Product> productList = new ArrayList<>();
        queryResults.getQueryIterable().forEach(document -> {
            productList.add(gson.fromJson(String.format("%s", document), Product.class));
        });

        if (productList.size() == 0) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(Error.builder().code(HttpStatus.NOT_FOUND.value()).message("ID " + productId + " is not found.")).header("Content-Type", "application/json").build();
        }
        return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json").body(productList.get(0)).build();
    }
}
