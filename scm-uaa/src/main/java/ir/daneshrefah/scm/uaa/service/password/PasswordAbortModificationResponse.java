package ir.daneshrefah.scm.uaa.service.password;

import java.time.LocalDate;

public record PasswordAbortModificationResponse(boolean aborted, LocalDate reactionDate) {
}
