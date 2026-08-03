package ir.daneshrefah.scm.provider.shetab.lease;

public interface ShetabEndpointLease extends AutoCloseable {
    String endpoint();

    String remoteHost();

    int remotePort();

    boolean leasingRequired();

    boolean isValid();

    default void onInvalidated(Runnable listener) {
    }

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
            public boolean leasingRequired() {
                return false;
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public void close() {
            }
        };
    }
}
