package ir.daneshrefah.scm.uaa.domain.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-31
 */
@Getter
@Setter
@AllArgsConstructor
public class ClientVersion {

    private String version;
    private String signature;
    private ClientVersionStatus status;
    private String url;

}
