package ir.daneshrefah.scm.observation.servlet;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scm.observation.http")
public class ServletObservationProperties {
    private ServerProperties server = new ServerProperties();

    public ServerProperties getServer() {
        return server;
    }

    public void setServer(ServerProperties server) {
        this.server = server == null ? new ServerProperties() : server;
    }

    public static class ServerProperties {
        private boolean enabled;
        private String mode = "channel-only";
        private String spanName = "http.server.request";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getSpanName() {
            return spanName;
        }

        public void setSpanName(String spanName) {
            this.spanName = spanName;
        }
    }
}
