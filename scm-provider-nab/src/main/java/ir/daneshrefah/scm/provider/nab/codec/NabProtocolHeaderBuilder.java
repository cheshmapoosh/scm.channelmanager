package ir.daneshrefah.scm.provider.nab.codec;

import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.domain.NabHeaderValues;
import ir.daneshrefah.scm.provider.nab.domain.NabProtocol;
import ir.daneshrefah.scm.provider.nab.domain.NabWireRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NabProtocolHeaderBuilder {
    private final FixedLengthEncoder encoder;

    public NabWireRequest build(NabHeaderValues headerValues, NabProtocol protocol, NabResolvedConfig config, String requestBody) {
        var headerFields = config.headerFieldsByProtocol().get(protocol.name());
        if (headerFields == null || headerFields.isEmpty()) {
            throw new IllegalArgumentException("NAB header fields are not configured for protocol " + protocol);
        }
        String header = encoder.encode(headerValues.data(), headerFields);
        if (!header.startsWith(headerValues.protocol())) {
            throw new IllegalArgumentException("NAB header for protocol " + protocol
                    + " must start with the protocol field value " + headerValues.protocol());
        }
        String fullMessage = header + requestBody;
        if (log.isTraceEnabled()) {
            log.trace("NAB header built protocol={} headerLength={} bodyLength={}",
                    headerValues.protocol(), header.length(), requestBody.length());
        }
        return new NabWireRequest(headerValues.protocol(), fullMessage);
    }
}
