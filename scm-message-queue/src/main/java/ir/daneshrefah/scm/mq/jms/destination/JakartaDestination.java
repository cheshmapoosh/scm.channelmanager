package ir.daneshrefah.scm.mq.jms.destination;

import jakarta.jms.Destination;
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
public abstract class JakartaDestination<T extends javax.jms.Destination> implements Destination {

    @Getter
    private final T destination;

}
