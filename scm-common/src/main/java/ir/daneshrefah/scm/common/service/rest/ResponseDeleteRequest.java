package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.RequestData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseDeleteRequest implements RequestData {

    private Long id;
    private LocalDateTime lastEditDate;
}
