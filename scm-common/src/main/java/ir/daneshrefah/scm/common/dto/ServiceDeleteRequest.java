package ir.daneshrefah.scm.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceDeleteRequest {
    private String id;
    private LocalDateTime lastEditDate;
}
