package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParameterDeleteRequest implements RequestData {

    private String id;
    private LocalDateTime lastEditDate;
}
