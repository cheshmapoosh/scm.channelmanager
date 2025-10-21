package ir.daneshrefah.scm.uaa.domain.pwa;

import lombok.Data;

import java.io.Serializable;

@Data
public class WhiteList implements Serializable {

    private Long id;
    private String phoneNumber;
    private String username;
}
