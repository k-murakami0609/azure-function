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

        RatingDto inputDto = request.getBody();
        if (inputDto == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(Error.builder().code(HttpStatus.BAD_REQUEST.value())
                .message("request body must not empty"))
                .build();
        }

        try {
            RatingDao ratingDao = new RatingDao();
            ratingDao.create(inputDto);
        } catch(RuntimeException e) {
            e.printStackTrace();
            context.getLogger().warning(e.toString());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return request.createResponseBuilder(HttpStatus.OK).build();
    }
}
