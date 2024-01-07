package ir.daneshrefah.scm.logging.domain.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-07
 */
@Getter
@SuperBuilder
public abstract class DurableEvent<T, S> extends Event<T> {

    private Instant endTimestamp;
    private Long durationMillis;
    private S output;
    private Exception error;

}
