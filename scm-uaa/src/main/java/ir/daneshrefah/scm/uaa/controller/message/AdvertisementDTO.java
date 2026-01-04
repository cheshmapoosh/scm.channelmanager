package ir.daneshrefah.scm.uaa.controller.message;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
public class AdvertisementDTO implements Serializable {

    private String url;
    private String text;
    private String icon;
}
