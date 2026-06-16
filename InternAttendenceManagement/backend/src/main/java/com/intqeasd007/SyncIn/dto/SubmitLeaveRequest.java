package com.intqeasd007.SyncIn.dto;

import lombok.Data;

@Data
public class SubmitLeaveRequest {
    private String reason;
    private String description;
    // Optional ISO date (yyyy-MM-dd). If omitted, backend uses current date.
    private String date;
}

