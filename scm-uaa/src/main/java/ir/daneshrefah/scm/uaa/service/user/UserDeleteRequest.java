package ir.daneshrefah.scm.uaa.service.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDeleteRequest {
    private LocalDateTime lastEditDate;
}
