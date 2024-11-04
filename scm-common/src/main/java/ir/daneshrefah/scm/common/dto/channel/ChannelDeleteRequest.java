package ir.daneshrefah.scm.common.dto.channel;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-03
 */
@Data
public class ChannelDeleteRequest implements RequestData {

    private String id;
    private LocalDateTime lastEditDate;

}
