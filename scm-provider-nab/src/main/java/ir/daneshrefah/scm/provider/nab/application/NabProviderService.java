package ir.daneshrefah.scm.provider.nab.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.provider.nab.codec.FixedLengthEncoder;
import ir.daneshrefah.scm.provider.nab.codec.JsonFieldSpecReader;
import ir.daneshrefah.scm.provider.nab.codec.NabCommandSpecReader;
import ir.daneshrefah.scm.provider.nab.codec.NabHeaderResolver;
import ir.daneshrefah.scm.provider.nab.codec.NabProtocolHeaderBuilder;
import ir.daneshrefah.scm.provider.nab.codec.NabResponseParser;
import ir.daneshrefah.scm.provider.nab.codec.NabResponseSpecReader;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.domain.NabCommandSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabHeaderValues;
import ir.daneshrefah.scm.provider.nab.domain.NabResponseSpec;
import ir.daneshrefah.scm.provider.nab.domain.NabWireRequest;
import ir.daneshrefah.scm.provider.nab.tcp.NabTcpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NabProviderService {
    private final NabCommandSpecReader commandSpecReader;
    private final JsonFieldSpecReader fieldSpecReader;
    private final NabResponseSpecReader responseSpecReader;
    private final NabHeaderResolver headerResolver;
    private final FixedLengthEncoder encoder;
    private final NabProtocolHeaderBuilder headerBuilder;
    private final NabTcpClient tcpClient;
    private final NabResponseParser responseParser;

    public ObjectNode execute(JsonNode input, NabResolvedConfig config) {
        if (input == null || !input.isObject()) {
            throw new IllegalArgumentException("NAB provider input must be a JSON object");
        }

        NabCommandSpec commandSpec = commandSpecReader.read(input);
        validateProviderProtocol(config, commandSpec);
        List<NabFieldSpec> requestFields = fieldSpecReader.readFields(input.path("request").path("fields"), "request");
        NabResponseSpec responseSpec = responseSpecReader.read(input);
        JsonNode data = input.path("data");
        NabHeaderValues headerValues = headerResolver.resolve(input, config, commandSpec);

        String requestBody = encoder.encode(data, requestFields);
        NabWireRequest wireRequest = headerBuilder.build(headerValues, commandSpec.protocol(), config, requestBody);
        log.info("Sending NAB request provider={} protocol={} command={} requestFields={} responseFields={}",
                config.provider(), commandSpec.protocol(), commandSpec.code(),
                requestFields.size(), responseSpec.fields().size());

        String wireResponse = tcpClient.request(config, wireRequest);
        ObjectNode response = responseParser.parse(wireResponse, responseSpec);
        response.put("rqUid", headerValues.rqUid());
        response.put("command", commandSpec.code());
        response.put("protocol", commandSpec.protocol().name());
        return response;
    }

    private void validateProviderProtocol(NabResolvedConfig config, NabCommandSpec commandSpec) {
        String fixedProtocol = config.protocol();
        if (fixedProtocol == null || fixedProtocol.isBlank()) {
            return;
        }
        String requestProtocol = commandSpec.protocol().name();
        if (!fixedProtocol.equalsIgnoreCase(requestProtocol)) {
            throw new IllegalArgumentException("NAB provider " + config.provider()
                    + " is configured for protocol " + fixedProtocol
                    + " but request protocol is " + requestProtocol);
        }
    }
}
