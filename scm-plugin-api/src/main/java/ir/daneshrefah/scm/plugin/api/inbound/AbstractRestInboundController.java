package ir.daneshrefah.scm.plugin.api.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public abstract class AbstractRestInboundController {

    private HttpInboundExecutor executor;

    protected final Message executeService(HttpServletRequest request, String serviceCode, JsonNode payload) {
        return executor.executeService(request, serviceCode, payload);
    }

    protected final Message executeService(HttpServletRequest request, String serviceCode) {
        return executeService(request, serviceCode, null);
    }

    public void setExecutor(HttpInboundExecutor executor) {
        this.executor = executor;
    }

}
