package ir.daneshrefah.scm.uaa.controller.message;

import ir.daneshrefah.scm.uaa.repository.activation.domain.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
@Getter
@Setter
public class LoginMessageResponseDTO implements Serializable {


    private Long id;
    private String body;
    private long createdDate;
    private long expirationDate;
    private Long client;
    private MessageType messageType;
    private AdvertisementDTO advertisement;

    private boolean isActive;

}
