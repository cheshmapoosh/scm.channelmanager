package ir.daneshrefah.scm.uaa.controller.message;

import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.repository.activation.domain.MessageType;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
public class LoginMessageRequestDTO {
    private Instant createdDate;
    private boolean getExpiredMessage;
    @Max(30)
    private Integer fetchSize = 10;
    private ClientVersion clientDTO;
    private MessageType messageType = MessageType.INFORMATION;

    public Integer getFetchSize() {
        if (fetchSize == null)
            fetchSize = 1;
        return fetchSize;
    }
}
