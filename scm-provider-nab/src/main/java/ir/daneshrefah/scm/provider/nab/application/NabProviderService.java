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
        List<NabFieldSpec> requestFields = fieldSpecReader.readFields(input.path("request").path("fields"), "request");
        NabResponseSpec responseSpec = responseSpecReader.read(input);
        JsonNode data = input.path("data");
        NabHeaderValues headerValues = headerResolver.resolve(input, config, commandSpec);

        String requestBody = encoder.encode(data, requestFields);
        NabWireRequest wireRequest = headerBuilder.build(headerValues, commandSpec.protocol(), config, requestBody);
        log.info("Sending NAB request provider={} protocol={} command={} rqUid={} requestFields={} responseFields={}",
                config.provider(), commandSpec.protocol(), commandSpec.code(), headerValues.rqUid(),
                requestFields.size(), responseSpec.fields().size());
        log.debug("NAB request fullMessage provider={} content=[{}]", config.provider(), wireRequest.fullMessage());

        String wireResponse = tcpClient.request(config, wireRequest);
        log.debug("NAB response message provider={} content=[{}]", config.provider(), wireResponse);
        ObjectNode response = responseParser.parse(wireResponse, responseSpec);
        response.put("rqUid", headerValues.rqUid());
        response.put("command", commandSpec.code());
        response.put("protocol", commandSpec.protocol().name());
        return response;
    }
}
