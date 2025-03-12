package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.uaa.domain.client.Client;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientResponse extends Client {
    private String nickname;
    private String title;
    private String titleFa;
}
