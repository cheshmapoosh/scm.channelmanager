package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-30
 */
@Data
public class ServiceFindRequest extends PagedRequestData {

    private String code;
    private String title;
    private Boolean isSystemic;
    private ServiceType type;
    private ServiceStatus status;
    private String parentId;
    private ServiceImplementationType implementationType;

}
