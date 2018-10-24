package com.openhack;

import lombok.Builder;
import lombok.Data;

import java.security.Timestamp;

@Data
@Builder
public class Rating {
    private String id;
    private String userId;
    private String productId;
    private String timestamp;
    private String locationName;
    private int rating;
    private String userNotes;
}
