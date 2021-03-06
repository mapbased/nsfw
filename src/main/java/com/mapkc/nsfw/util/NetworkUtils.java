package com.mapkc.nsfw.util;

import com.mapkc.nsfw.util.logging.ESLogger;
import com.mapkc.nsfw.util.logging.Loggers;

import java.net.*;
import java.util.*;

public class NetworkUtils {
    public static final String IPv4_SETTING = "java.net.preferIPv4Stack";
    public static final String IPv6_SETTING = "java.net.preferIPv6Addresses";
    public static final String NON_LOOPBACK_ADDRESS = "non_loopback_address";
    private final static ESLogger logger = Loggers
            .getLogger(NetworkUtils.class);
    private final static InetAddress localAddress;

    static {
        InetAddress localAddressX = null;
        try {
            localAddressX = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.warn("Failed to find local host", e);
        }
        localAddress = localAddressX;
    }

    private NetworkUtils() {

    }

    public static Boolean defaultReuseAddress() {
        return true;
        // return OsUtils.WINDOWS ? null : true;
    }

    public static boolean isIPv4() {
        return System.getProperty("java.net.preferIPv4Stack") != null
                && System.getProperty("java.net.preferIPv4Stack")
                .equals("true");
    }

    public static InetAddress getIPv4Localhost() throws UnknownHostException {
        return getLocalhost(StackType.IPv4);
    }

    public static InetAddress getIPv6Localhost() throws UnknownHostException {
        return getLocalhost(StackType.IPv6);
    }

    public static InetAddress getLocalAddress() {
        return localAddress;
    }

    public static InetAddress getLocalhost(StackType ip_version)
            throws UnknownHostException {
        if (ip_version == StackType.IPv4)
            return InetAddress.getByName("127.0.0.1");
        else
            return InetAddress.getByName("::1");
    }

    public static boolean canBindToMcastAddress() {
        return true;
        // return OsUtils.LINUX || OsUtils.SOLARIS || OsUtils.HP;
    }

    /**
     * Returns the first non-loopback address on any interface on the current
     * host.
     *
     * @param ip_version Constraint on IP version of address to be returned, 4 or 6
     */
    public static InetAddress getFirstNonLoopbackAddress(StackType ip_version)
            throws SocketException {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // InetAddress address = null;
        //
        // Enumeration intfs = NetworkInterface.getNetworkInterfaces();
        // while (intfs.hasMoreElements()) {
        // NetworkInterface intf = (NetworkInterface)
        // intfs.nextElement();
        // if(intf.isVirtual() || !intf.isUp() || intf.isLoopback())
        // continue;
        // address = getFirstNonLoopbackAddress(intf, ip_version);
        // if (address != null) {
        // return address;
        // }
        // }
        // return null;
    }

    /**
     * Returns the first non-loopback address on the given interface on the
     * current host.
     *
     * @param intf      the interface to be checked
     * @param ipVersion Constraint on IP version of address to be returned, 4 or 6
     */
    public static InetAddress getFirstNonLoopbackAddress(NetworkInterface intf,
                                                         StackType ipVersion) throws SocketException {
        if (intf == null)
            throw new IllegalArgumentException(
                    "Network interface pointer is null");

        for (Enumeration addresses = intf.getInetAddresses(); addresses
                .hasMoreElements(); ) {
            InetAddress address = (InetAddress) addresses.nextElement();
            if (!address.isLoopbackAddress()) {
                if ((address instanceof Inet4Address && ipVersion == StackType.IPv4)
                        || (address instanceof Inet6Address && ipVersion == StackType.IPv6))
                    return address;
            }
        }
        return null;
    }

    /**
     * A function to check if an interface supports an IP version (i.e has
     * addresses defined for that IP version).
     *
     * @param intf
     * @return
     */
    public static boolean interfaceHasIPAddresses(NetworkInterface intf,
                                                  StackType ipVersion) throws SocketException, UnknownHostException {
        boolean supportsVersion = false;
        if (intf != null) {
            // getTarget all the InetAddresses defined on the interface
            Enumeration addresses = intf.getInetAddresses();
            while (addresses != null && addresses.hasMoreElements()) {
                // getTarget the next InetAddress for the current
                // interface
                InetAddress address = (InetAddress) addresses.nextElement();

                // check if we find an address of correct
                // version
                if ((address instanceof Inet4Address && (ipVersion == StackType.IPv4))
                        || (address instanceof Inet6Address && (ipVersion == StackType.IPv6))) {
                    supportsVersion = true;
                    break;
                }
            }
        } else {
            throw new UnknownHostException("network interface " + intf
                    + " not found");
        }
        return supportsVersion;
    }

    /**
     * Tries to determine the type of IP stack from the available interfaces and
     * their addresses and from the system properties (java.net.preferIPv4Stack
     * and java.net.preferIPv6Addresses)
     *
     * @return StackType.IPv4 for an IPv4 only stack, StackYTypeIPv6 for an IPv6
     * only stack, and StackType.Unknown if the type cannot be detected
     */
    public static StackType getIpStackType() {
        boolean isIPv4StackAvailable = isStackAvailable(true);
        boolean isIPv6StackAvailable = isStackAvailable(false);

        // if only IPv4 stack available
        if (isIPv4StackAvailable && !isIPv6StackAvailable) {
            return StackType.IPv4;
        }
        // if only IPv6 stack available
        else if (isIPv6StackAvailable && !isIPv4StackAvailable) {
            return StackType.IPv6;
        }
        // if dual stack
        else if (isIPv4StackAvailable && isIPv6StackAvailable) {
            // getTarget the System property which records user preference
            // for a stack
            // on a dual stack machine
            if (Boolean.getBoolean(IPv4_SETTING)) // has preference
                // over
                // java.net.preferIPv6Addresses
                return StackType.IPv4;
            if (Boolean.getBoolean(IPv6_SETTING))
                return StackType.IPv6;
            return StackType.IPv6;
        }
        return StackType.Unknown;
    }

    public static boolean isStackAvailable(boolean ipv4) {
        Collection<InetAddress> allAddrs = getAllAvailableAddresses();
        for (InetAddress addr : allAddrs)
            if (ipv4 && addr instanceof Inet4Address
                    || (!ipv4 && addr instanceof Inet6Address))
                return true;
        return false;
    }

    public static List<NetworkInterface> getAllAvailableInterfaces()
            throws SocketException {
        List<NetworkInterface> allInterfaces = new ArrayList<NetworkInterface>(
                10);
        NetworkInterface intf;
        for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en
                .hasMoreElements(); ) {
            intf = (NetworkInterface) en.nextElement();
            allInterfaces.add(intf);
        }
        return allInterfaces;
    }

    public static Collection<InetAddress> getAllAvailableAddresses() {
        Set<InetAddress> retval = new HashSet<InetAddress>();
        Enumeration en;

        try {
            en = NetworkInterface.getNetworkInterfaces();
            if (en == null)
                return retval;
            while (en.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                Enumeration<InetAddress> addrs = intf.getInetAddresses();
                while (addrs.hasMoreElements())
                    retval.add(addrs.nextElement());
            }
        } catch (SocketException e) {
            logger.warn("Failed to derive all available interfaces", e);
        }

        return retval;
    }

    static public int getNextAliablePort(String address, int startport)
            throws UnknownHostException {
        InetAddress a = InetAddress.getByName(address);
        return getNextAliablePort(a, startport);
    }

    static public int getNextAliablePort(InetAddress address, int startport) {
        int port = startport;
        while (port < 65535) {
            try {
                Socket socket = new Socket(address, port);
                socket.close();

                port++;
            } catch (Exception ex) {

                return port;

            }
        }

        return -1;

    }

    public static void main(String[] ss) {
        try {
            System.out.println(getNextAliablePort("192.168.62.45", 10000));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum StackType {
        IPv4, IPv6, Unknown
    }
}
