package ir.daneshrefah.scm.core.service.constant;

import com.hazelcast.map.listener.MapListener;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@RequiredArgsConstructor
public class ConstantCacheListener implements MapListener {

    private final ConstantCache constantCache;
}
