package ir.daneshrefah.scm.plugin.nab.transformer;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-22
 */
@RequiredArgsConstructor
@Data
public class NabRequestMessage {

    private final NabMessageHeader header;
}
