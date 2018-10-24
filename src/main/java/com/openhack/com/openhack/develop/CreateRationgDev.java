package com.openhack.com.openhack.develop;

import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.RequestOptions;
import com.openhack.DocumentDbUtils;
import com.openhack.RatingDao;
import com.openhack.RatingDto;

/**
 * Created by grachro on 2018/10/24.
 */
public class CreateRationgDev {
    public static void main(String... args) {
        RatingDto inputDto = new RatingDto();
        inputDto.productId = "test";
        inputDto.userId = "testUser";
        inputDto.id = "testId2";


        RatingDao dao = new RatingDao();
        dao.create(inputDto);
    }
}
