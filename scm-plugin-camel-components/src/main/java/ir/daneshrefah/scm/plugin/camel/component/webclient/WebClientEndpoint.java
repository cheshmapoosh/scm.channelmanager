package ir.daneshrefah.scm.plugin.camel.component.webclient;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultComponent;
import org.apache.camel.support.DefaultEndpoint;

public class WebClientEndpoint extends DefaultEndpoint {

    @UriPath(description = "The target URI")
    private String rawUri;

    @UriParam(defaultValue = "GET", description = "HTTP method to use")
    private String method = "GET";

    @UriParam(defaultValue = "5000", description = "Connection timeout in milliseconds")
    private int connectTimeout = 5000;

    @UriParam(defaultValue = "5000", description = "Write timeout in milliseconds")
    private int writeTimeout = 5000;

    @UriParam(defaultValue = "5000", description = "Response timeout in ms")
    private int responseTimeout = 5000;

    @UriParam(defaultValue = "true", description = "Enable compression")
    private boolean compress = true;

    @UriParam(description = "Enable wiretap debugging")
    private boolean wiretap = false;

    @UriParam(defaultValue = "100", description = "Maximum connections in pool")
    private int maxConnections = 100;

    @UriParam(defaultValue = "60", description = "Maximum time to wait to acquire a connection in seconds")
    private int acquireTimeoutSeconds = 60;

    @UriParam(defaultValue = "30000", description = "Maximum idle time in milliseconds for pooled connections")
    private long maxIdleTime = 30000L;

    @UriParam(defaultValue = "262144", description = "Maximum in-memory buffer size for codecs in bytes")
    private int maxInMemorySize = 262144;

    @UriParam(description = "Proxy hostname")
    private String proxyHost;

    @UriParam(description = "Proxy port")
    private Integer proxyPort;

    @UriParam(description = "Proxy username for authentication")
    private String proxyUsername;

    @UriParam(description = "Proxy password for authentication")
    private String proxyPassword;

    @UriParam(description = "Enable insecure (trust all) SSL")
    private boolean insecureSsl = false;

    @UriParam(description = "Enable retry with circuit breaker")
    private boolean retryEnabled = false;

    @UriParam(defaultValue = "3", description = "Number of retry attempts")
    private int maxAttempts = 3;

    @UriParam(defaultValue = "1000", description = "Backoff delay in milliseconds between retries")
    private int minBackoff = 1000;

    public WebClientEndpoint(String endpointUri, DefaultComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new WebClientProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) {
        throw new UnsupportedOperationException("WebClientEndpoint doesn't support consumers");
    }

    public String getRawUri() {
        return rawUri;
    }

    public void setRawUri(String rawUri) {
        this.rawUri = rawUri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean isWiretap() {
        return wiretap;
    }

    public void setWiretap(boolean wiretap) {
        this.wiretap = wiretap;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getAcquireTimeoutSeconds() {
        return acquireTimeoutSeconds;
    }

    public void setAcquireTimeoutSeconds(int acquireTimeoutSeconds) {
        this.acquireTimeoutSeconds = acquireTimeoutSeconds;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public int getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public boolean isInsecureSsl() {
        return insecureSsl;
    }

    public void setInsecureSsl(boolean insecureSsl) {
        this.insecureSsl = insecureSsl;
    }

    public boolean isRetryEnabled() {
        return retryEnabled;
    }

    public void setRetryEnabled(boolean retryEnabled) {
        this.retryEnabled = retryEnabled;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getMinBackoff() {
        return minBackoff;
    }

    public void setMinBackoff(int minBackoff) {
        this.minBackoff = minBackoff;
    }

    @Override
    public boolean isLenientProperties() {
        return true;
    }
}
