package ir.daneshrefah.scm.uaa.service.shahkar.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShahkarResult(
        @JsonProperty("result")
        String status,
        String requestId,
        int response,
        String comment) {
}