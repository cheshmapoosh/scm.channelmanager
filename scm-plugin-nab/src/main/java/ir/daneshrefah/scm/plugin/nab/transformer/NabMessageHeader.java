package ir.daneshrefah.scm.plugin.nab.transformer;

import lombok.Builder;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-22
 */
@Builder
@Data
public class NabMessageHeader {

    private String serviceCode;
    private String rqUID;
    private String userId;
    private String password;

}
