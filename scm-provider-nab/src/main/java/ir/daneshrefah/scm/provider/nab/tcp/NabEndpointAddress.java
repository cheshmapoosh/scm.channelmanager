package ir.daneshrefah.scm.provider.nab.tcp;

record NabEndpointAddress(String value, String host, int port) {

    static NabEndpointAddress parse(String endpoint) {
        int separator = endpoint == null ? -1 : endpoint.lastIndexOf(':');
        if (separator <= 0 || separator == endpoint.length() - 1) {
            throw new IllegalArgumentException("Invalid NAB endpoint: " + endpoint);
        }
        return new NabEndpointAddress(
                endpoint,
                endpoint.substring(0, separator),
                Integer.parseInt(endpoint.substring(separator + 1))
        );
    }
}
