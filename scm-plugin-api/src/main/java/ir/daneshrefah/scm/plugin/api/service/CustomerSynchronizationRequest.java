package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2022-01-24
 */
@Data
public class CustomerSynchronizationRequest implements RequestData {

    private String providerId;
    private Long personId;

}
