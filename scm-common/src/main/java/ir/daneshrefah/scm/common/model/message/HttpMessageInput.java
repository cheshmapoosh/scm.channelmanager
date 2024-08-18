package ir.daneshrefah.scm.common.model.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
@SuperBuilder
@Getter
@NoArgsConstructor
public class HttpMessageInput extends AbstractExternalMessageInput<String> {

    private String httpUrl;
    private String httpMethod;
    private String clientAgent;
    private String requestBody;
    private String contentType;

}
