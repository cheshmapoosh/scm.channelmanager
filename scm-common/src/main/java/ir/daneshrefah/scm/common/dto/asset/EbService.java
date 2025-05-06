package ir.daneshrefah.scm.common.dto.asset;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class EbService implements Serializable {
    private Integer id;
    private Boolean publish;
    private String name;
    private String code;
    private String abbreviation;
    private Integer serviceCategoryId;
}
