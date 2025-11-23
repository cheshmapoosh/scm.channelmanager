package ir.daneshrefah.scm.uaa.controller.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-27
 */
@Data
@Builder
public class PublicKeyResponse {

    @JsonProperty("alg")
    private String algorithm;
    private String value;

}