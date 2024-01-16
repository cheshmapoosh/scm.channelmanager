package ir.daneshrefah.scm.plugin.api.model.message;

import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;

import java.time.Instant;

import static ir.daneshrefah.scm.utils.constant.Constants.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-16
 */
//@Builder
public class DefaultHttpMessageRequestBuilder {

    private final HttpServletRequest request;
    private final Channel channel;

    public DefaultHttpMessageRequestBuilder(HttpServletRequest request, Channel channel) {
        this.request = request;
        this.channel = channel;
    }

    public MessageBuildRequest build() {
        return MessageBuildRequest.builder()
                .terminalCode(request.getHeader(SCM_PARAMETER_TERMINAL))
                .channelCode(channel.getCode())
                .contentType(request.getContentType())
                .clientCorrelationId(request.getHeader(SCM_PARAMETER_CLIENT_CORRELATION_ID))
                .clientTimestamp(null != request.getHeader(SCM_PARAMETER_CLIENT_TIMESTAMP) ?
                        Instant.parse(request.getHeader(SCM_PARAMETER_CLIENT_TIMESTAMP)) : null)
                .receiveTimestamp(Instant.now())
                .accessParameter(request.getHeader(SCM_PARAMETER_ACCESS_PARAMETER))
                .clientAgent(request.getHeader(HttpConstants.HTTP_HEADER_USER_AGENT))
                .serverHost(request.getHeader(HttpConstants.HTTP_HEADER_HOST))
                .clientAddress(request.getRemoteAddr())
                .payload(null)
                .build();
    }

    @Builder
    public static DefaultHttpMessageRequestBuilder builder(HttpServletRequest request, Channel channel) {
        return new DefaultHttpMessageRequestBuilder(request, channel);
    }

}
