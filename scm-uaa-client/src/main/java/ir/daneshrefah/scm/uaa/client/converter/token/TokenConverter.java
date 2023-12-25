package ir.daneshrefah.scm.uaa.client.converter.token;

import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.core.convert.converter.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-21
 */
public interface TokenConverter<S> extends Converter<S, UserAuthentication> {

    public UserAuthentication convert(S token);

}
