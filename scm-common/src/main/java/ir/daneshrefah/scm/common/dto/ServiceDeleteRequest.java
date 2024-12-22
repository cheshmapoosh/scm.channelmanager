package ir.daneshrefah.scm.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceDeleteRequest {
    @NotNull
    @NotBlank
    private String id;
    @NotNull
    private LocalDateTime lastEditDate;
}
