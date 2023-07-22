package ir.daneshrefah.scm.common.model.terminal;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class RestChannel extends Channel {
    private String context;
    private Integer port;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.REST;
    }
}
