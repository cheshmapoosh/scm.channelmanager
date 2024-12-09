package ir.daneshrefah.scm.common.dto.channel;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.terminal.ChannelProtocol;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-03
 */
@Data
public class ChannelCreateRequest implements RequestData {

    @NotBlank
    private String code;
    @NotBlank
    private String title;
    @NotBlank
    private String terminalCode;
    @NotNull
    private ChannelProtocol protocol;
    @NotBlankIfPresent
    private String channelClassName;
    @NotBlankIfPresent
    private JsonNode metadata;

}
