package com.openhack;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;

/**
 * Created by grachro on 2018/10/24.
 */
public class DocumentDbUtils {


    public static DocumentClient createClient(){

        return  new DocumentClient(
                System.getenv("serviceEndpoint"),
                System.getenv("masterKey"),
                ConnectionPolicy.GetDefault(),
                ConsistencyLevel.Session
        );
    }
}
