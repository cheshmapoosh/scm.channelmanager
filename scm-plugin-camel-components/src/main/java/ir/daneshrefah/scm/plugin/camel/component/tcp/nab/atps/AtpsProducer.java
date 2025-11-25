package ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.component.netty.NettyConstants;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.support.DefaultProducer;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Producer for handling ATPS protocol messages in a Camel route.
 */
public class AtpsProducer extends DefaultProducer {
    Logger LOG = Logger.getLogger(AtpsProducer.class.getName());
    public static final String SERVICE_TIMEOUT = "SERVICE_TIMEOUT";
    public static final String SEND_MESSAGE_TO_CORE_TIME = "SEND_MESSAGE_TO_CORE_TIME";
    public static final String RECEIVED_MESSAGE_FROM_CORE_TIME = "RECEIVED_MESSAGE_FROM_CORE_TIME";

    private static final Charset CP1256 = Charset.forName("Cp1256");
    private static final String ATPS = "ATPS";
    private String command;
    private final AtpsEndpoint atpsEndpoint;

    private Endpoint nettyEndpoint;
    private Producer nettyProducer;

    public AtpsProducer(AtpsEndpoint endpoint) {
        super(endpoint);
        this.atpsEndpoint = endpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        String nettyUri = buildNettyUri();
        this.nettyEndpoint = getEndpoint().getCamelContext().getEndpoint(nettyUri);
        command = atpsEndpoint.getCommand();
        this.nettyProducer = nettyEndpoint.createProducer();
        this.nettyProducer.start();
    }

    private String buildNettyUri() {
        return String.format("%s?sync=true" +
                        "&reuseChannel=true" +
                        "&disconnect=false" +
                        "&allowDefaultCodec=false" +
                        "&decoders=#atpsResponseBodyDecoder" +
                        "&tcpNoDelay=%b" +
                        "&keepAlive=%b&connectTimeout=%d",
                atpsEndpoint.getNettyUriBase(),
                atpsEndpoint.isTcpNoDelay(),
                atpsEndpoint.isKeepAlive(),
                atpsEndpoint.getConnectTimeout());
    }

    @Override
    protected void doStop() throws Exception {
        try {
            Optional.ofNullable(nettyProducer).ifPresent(Producer::stop);
        } finally {
            nettyProducer = null;
            nettyEndpoint = null;
            super.doStop();
        }
    }



    @Override
    public void process(Exchange exchange) throws Exception {
        Object inBody = exchange.getMessage().getBody();

        if (inBody == null) {
            throw new IllegalArgumentException("ATPS body must not be null");
        }
        String userPart = enricherHeader(inBody);

        // Process the message: header and payload preparation
        ByteBuf header = Unpooled.wrappedBuffer(ATPS.getBytes(CP1256));

        ByteBuf payload = preparePayload(userPart);



        // Request timeout logic
        int requestTimeout = Optional.ofNullable(exchange.getMessage().getHeader(SERVICE_TIMEOUT))
                .map(Object::toString)
                .map(Integer::parseInt)
                .orElse(atpsEndpoint.getRequestTimeout());

      LOG.log(java.util.logging.Level.INFO, "[ATPS] Using request timeout: " + requestTimeout + " ms");

        // Step 1: Send header, get ack without closing the channel
        prepareAndProcessHeader(exchange, header, requestTimeout);

        // Step 2: Validate ack if needed
        validateAck(exchange);

        // Step 3: Send payload and close the channel for the final response
        prepareAndProcessPayload(exchange, payload, requestTimeout);

        // Step 4: Handle final response
        handleFinalResponse(exchange);
    }

    private String enricherHeader(Object inBody) {
        String headerPart = AtpsHelper.enrichRequestBody(command);
        String userPart = AtpsHelper.toString(inBody, CP1256);
        return headerPart + userPart;
    }

    private ByteBuf preparePayload(Object inBody) {
        return AtpsHelper.toByteBuf(inBody, CP1256);
    }



    private void prepareAndProcessHeader(Exchange exchange, ByteBuf header, long requestTimeout) throws Exception {
        exchange.getIn().setHeader(NettyConstants.NETTY_REQUEST_TIMEOUT, requestTimeout);
        exchange.getIn().setHeader(NettyConstants.NETTY_CLOSE_CHANNEL_WHEN_COMPLETE, false);
        exchange.getIn().setBody(header);

        exchange.getIn().setHeader(SEND_MESSAGE_TO_CORE_TIME, new Date());
        nettyProducer.process(exchange);

        if (exchange.isFailed()) {
            throw new RuntimeException("ATPS start has failed", exchange.getException());
        }
    }

    private void validateAck(Exchange exchange) {
        if (atpsEndpoint.isValidateAck()) {
            String ack = exchange.getMessage().getBody(String.class);
            if (ack == null) {
                throw new RuntimeException("ATPS ack is null");
            }
            if (!ack.equals(Optional.ofNullable(atpsEndpoint.getAckEquals()).orElse(""))) {
                throw new RuntimeException("ATPS dispatcher not ready (ack mismatch)");
            }
        }
    }

    private void prepareAndProcessPayload(Exchange exchange, ByteBuf payload, long requestTimeout) throws Exception {
        exchange.getIn().setHeader(NettyConstants.NETTY_REQUEST_TIMEOUT, requestTimeout);
        exchange.getIn().setHeader(NettyConstants.NETTY_CLOSE_CHANNEL_WHEN_COMPLETE, true);
        exchange.getIn().setBody(payload);

        nettyProducer.process(exchange);
        if (exchange.isFailed()) {
            throw new RuntimeException("ATPS response has failed", exchange.getException());
        }
    }

    private void handleFinalResponse(Exchange exchange) {
        byte[] responseBytes = exchange.getMessage().getBody(byte[].class);
        Message responseMessage = new DefaultMessage(exchange);

        Object parsedResponse = parseResponse(responseBytes);
        responseMessage.setBody(parsedResponse);


        exchange.setMessage(responseMessage);

        exchange.getMessage().setHeader(RECEIVED_MESSAGE_FROM_CORE_TIME, new Date());
    }

    /**
     * Parses raw byte[] response into either:
     * - String[] if multiline (contains '\n')
     * - String if single line
     */
    private Object parseResponse(byte[] bytes) {
        if (bytes == null || bytes.length < 5) {
            throw new RuntimeException("Received message is invalid");
        }

        String responseString = new String(bytes, CP1256).trim();

        if (responseString.contains("\n")) {
            // Multi-line response → split to array
            return responseString.split("\\r?\\n");
        } else {
            // Single-line response → single object
            return responseString;
        }
    }
}
