package ir.daneshrefah.scm.provider.shetab.lease;

public interface ShetabEndpointLease extends AutoCloseable {
    String endpoint();

    String remoteHost();

    int remotePort();

    @Override
    void close();

    static ShetabEndpointLease none() {
        return new ShetabEndpointLease() {
            @Override
            public String endpoint() {
                return "";
            }

            @Override
            public String remoteHost() {
                return "";
            }

            @Override
            public int remotePort() {
                return 0;
            }

            @Override
            public void close() {
            }
        };
    }
}
