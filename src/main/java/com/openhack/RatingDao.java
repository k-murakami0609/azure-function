package com.openhack;

import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.ResourceResponse;

/**
 * Created by grachro on 2018/10/24.
 */
public class RatingDao {

    public Document create(RatingDto dto) {

        DocumentClient documentClient = DocumentDbUtils.createClient();

        String databaseName = "OpenHack";
        String collectionName = "ratings";

        String collectionLink = String.format("/dbs/%s/colls/%s", databaseName, collectionName);

        try {
            ResourceResponse<Document> response = documentClient.createDocument(collectionLink, dto, new RequestOptions(), true);

            return response.getResource();
        } catch(RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
