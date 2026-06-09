package ir.daneshrefah.scm.uaa.service.shahkar.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShahkarInquiryResponse(
        String doneDateTime,
        boolean done,
        ShahkarResult result
) {}

