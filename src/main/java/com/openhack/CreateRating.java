package com.openhack;

import com.microsoft.azure.documentdb.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.text.SimpleDateFormat;
import java.util.UUID;

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
                .message("request body is must not be null or empty"))
                .build();
        }

        if (inputDto.rating < 0 || inputDto.rating > 5) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(Error.builder().code(HttpStatus.BAD_REQUEST.value())
                            .message("request body is invalid value"))
                    .build();
        }

        try {
            String uuid = UUID.randomUUID().toString();
            inputDto.id = uuid;

            RatingDao ratingDao = new RatingDao();
            Document doc = ratingDao.create(inputDto);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            inputDto.timestamp = sdf.format(doc.getTimestamp());

            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json").body(inputDto).build();
        } catch(RuntimeException e) {
            e.printStackTrace();
            context.getLogger().warning(e.toString());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
