package ir.daneshrefah.scm.common.model.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-06
 */
@SuperBuilder
@Getter
@NoArgsConstructor
public abstract class AbstractExternalMessageInput<T> extends MessageInput<T> {

    private String clientRemoteAddress;
}
