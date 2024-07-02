package ir.daneshrefah.scm.utils.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Optional;

public class NetworkUtils {
    private NetworkUtils() {
    }

    public static Optional<InetAddress> findCurrentInet4Address() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress instanceof Inet4Address inet4Address) {
                        return Optional.of(inet4Address);
                    }
                }
            }
        } catch (SocketException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

}
