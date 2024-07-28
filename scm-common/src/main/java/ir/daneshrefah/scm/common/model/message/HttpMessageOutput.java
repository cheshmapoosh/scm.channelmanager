package ir.daneshrefah.scm.common.model.message;

import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-21
 */
@SuperBuilder
@Getter
public class HttpMessageOutput extends MessageOutput {

    @Override
    public ServiceProviderProtocol getProtocol() {
        return ServiceProviderProtocol.REST;
    }

}
