package ir.daneshrefah.scm.common.service.terminal;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.terminal.TerminalStatus;
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
public class TerminalDeleteRequest implements RequestData {

    private String id;
    private LocalDateTime lastEditDate;

}
