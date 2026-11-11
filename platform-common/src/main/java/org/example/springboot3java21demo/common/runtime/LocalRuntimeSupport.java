package org.example.springboot3java21demo.common.runtime;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

/**
 * 本地多服务运行时适配。
 *
 * <p>macOS 上的系统代理和 VPN 网卡会干扰服务之间的内网直连。这里在 JVM 启动早期
 * 移除系统代理，并为 Dubbo 选择真实局域网 IPv4 地址，避免把 127.0.0.1 或 utun
 * 的 fake-ip 注册到 Nacos。生产环境仍可通过 DUBBO_IP_TO_REGISTRY 显式覆盖。</p>
 */
public final class LocalRuntimeSupport {

    private LocalRuntimeSupport() {
    }

    public static void configure() {
        clearSystemProxy();
        configureDubboRegistryAddress();
    }

    private static void clearSystemProxy() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("ftp.proxyHost");
        System.clearProperty("ftp.proxyPort");
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");
    }

    private static void configureDubboRegistryAddress() {
        if (System.getProperty("DUBBO_IP_TO_REGISTRY") != null
                || System.getenv("DUBBO_IP_TO_REGISTRY") != null) {
            return;
        }
        String address = findSiteLocalIpv4();
        if (address != null) {
            System.setProperty("DUBBO_IP_TO_REGISTRY", address);
        }
    }

    private static String findSiteLocalIpv4() {
        String fallback = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (!network.isUp() || network.isLoopback() || network.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!(address instanceof Inet4Address) || !address.isSiteLocalAddress()) {
                        continue;
                    }
                    String hostAddress = address.getHostAddress();
                    String name = network.getName().toLowerCase(Locale.ROOT);
                    if (name.startsWith("en") || name.startsWith("eth")) {
                        return hostAddress;
                    }
                    if (fallback == null) {
                        fallback = hostAddress;
                    }
                }
            }
        } catch (SocketException ignored) {
            // 不阻断应用启动；调用方仍可通过环境变量显式指定地址。
        }
        return fallback;
    }
}
