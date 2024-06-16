package ir.daneshrefah.scm.mq.jms.message;

import jakarta.jms.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-15
 */
@RequiredArgsConstructor
public class JakartaMessage extends JakartaAbstractMessage implements Message {

    @Getter
    private final javax.jms.Message message;

}
