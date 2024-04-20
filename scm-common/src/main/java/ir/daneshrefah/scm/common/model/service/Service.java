package ir.daneshrefah.scm.common.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Getter
@Setter

public abstract class Service extends BaseModel<String> {

    private String code;
    private String title;
    private String alias;
    private Integer version;
    private Boolean isSystemic;
    private JsonNode metadata;
    private ServiceType type;
    private ServiceStatus status;
    private Service parent;
    private ServiceImplementationType implementationType;
    private String requestJsonSchema;
    private String responseJsonSchema;
    private Boolean checkAccessFirstAuthentication;
    private Boolean checkAccessSecondAuthentication;
    private Boolean checkAccessService;
    private Boolean checkAccessAsset;
    private String customerProperty;
    private String amountProperty;
    private String assetProperty;

}
