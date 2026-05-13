package ir.daneshrefah.scm.provider.shetab.lease;

public interface ShetabPortLease extends AutoCloseable {
    int port();

    @Override
    void close();

    static ShetabPortLease none() {
        return new ShetabPortLease() {
            @Override
            public int port() {
                return 0;
            }

            @Override
            public void close() {
            }
        };
    }
}
