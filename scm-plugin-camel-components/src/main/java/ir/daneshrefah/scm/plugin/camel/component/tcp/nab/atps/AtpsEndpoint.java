package ir.daneshrefah.scm.plugin.camel.component.tcp.nab.atps;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.*;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultEndpoint;

@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "atps",
        title = "ATPS",
        syntax = "atps:tcp://host:port",
        producerOnly = true,
        category = { Category.NETWORKING, Category.TRANSFORMATION }
)
public class AtpsEndpoint extends DefaultEndpoint {

    // URI نتتی که زیر پوست استفاده می‌کنیم
    private final String nettyUri;

    // گزینه‌ها (با @UriParam تا از application.yml هم ست شوند)
    @Setter
    @Getter
    @UriParam(defaultValue = "3000", label = "common")
    private int connectTimeout = 3000;

    @Setter
    @Getter
    @UriParam(defaultValue = "5000", label = "common")
    private int requestTimeout = 5000;

    @Setter
    @Getter
    @UriParam(label = "advanced", defaultValue = "true")
    private boolean tcpNoDelay = true;

    @Setter
    @Getter
    @UriParam(label = "advanced", defaultValue = "true")
    private boolean keepAlive = true;

    // اگر بخواهید ack را بررسی کنید
    @Setter
    @Getter
    @UriParam(label = "advanced", defaultValue = "false")
    private boolean validateAck;

    @Setter
    @Getter
    @UriParam(label = "advanced")
    private String ackEquals; // مثلا "00000"

    public AtpsEndpoint(String endpointUri, Component component, String nettyUri) {
        super(endpointUri, component);
        this.nettyUri = nettyUri;
    }


    @Override
    public Producer createProducer() {
        return new AtpsProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) {
        throw new UnsupportedOperationException("WebClientEndpoint doesn't support consumers");
    }

    // ----- getters/setters -----
    public String getNettyUriBase() { return nettyUri; }
    public int getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }
    public int getRequestTimeout() { return requestTimeout; }
    public void setRequestTimeout(int requestTimeout) { this.requestTimeout = requestTimeout; }
    public boolean isTcpNoDelay() { return tcpNoDelay; }
    public void setTcpNoDelay(boolean tcpNoDelay) { this.tcpNoDelay = tcpNoDelay; }
    public boolean isKeepAlive() { return keepAlive; }
    public void setKeepAlive(boolean keepAlive) { this.keepAlive = keepAlive; }
    public boolean isValidateAck() { return validateAck; }
    public void setValidateAck(boolean validateAck) { this.validateAck = validateAck; }
    public String getAckEquals() { return ackEquals; }
    public void setAckEquals(String ackEquals) { this.ackEquals = ackEquals; }
}
