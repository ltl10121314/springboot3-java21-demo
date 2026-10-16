package org.example.springboot3java21demo.exercise.ip;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class IPAddress {

    public static void main(String[] args) throws UnknownHostException {
        byte[] ipBytes = InetAddress.getByName("192.168.8.3").getAddress();
        byte[] maskBytes = InetAddress.getByName("255.255.255.0").getAddress();
        byte[] networkBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            byte ipByte = ipBytes[i];
            byte maskByte = maskBytes[i];
            networkBytes[i] = (byte) (ipByte & maskByte);
        }
        String networkAddress = InetAddress.getByAddress(networkBytes).getHostAddress();
        log.info("网络地址：{}", networkAddress);
    }
}
