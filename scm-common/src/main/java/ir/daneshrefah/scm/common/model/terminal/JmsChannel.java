package ir.daneshrefah.scm.common.model.terminal;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class JmsChannel extends Channel {

    private String host;
    private Integer port;
    private String queueManager;
    private String requestQueue;
    private String responseQueue;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getQueueManager() {
        return queueManager;
    }

    public void setQueueManager(String queueManager) {
        this.queueManager = queueManager;
    }

    public String getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(String requestQueue) {
        this.requestQueue = requestQueue;
    }

    public String getResponseQueue() {
        return responseQueue;
    }

    public void setResponseQueue(String responseQueue) {
        this.responseQueue = responseQueue;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.JMS;
    }
}
