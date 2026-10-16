package org.example.springboot3java21demo.exercise.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class NioClient {

    public static void main(String[] args) throws IOException {
        try (SocketChannel client = SocketChannel.open()) {
            // 连接服务端
            client.connect(new InetSocketAddress("127.0.0.1", 8080));
            String message = "Hello NIO";
            // 发送数据
            ByteBuffer sendBuffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
            while (sendBuffer.hasRemaining()) {
                client.write(sendBuffer);
            }
            log.info("客户端发送：{}", message);
            // 接收服务端回显
            ByteBuffer receiveBuffer = ByteBuffer.allocate(4096);
            while (true) {
                int len = client.read(receiveBuffer);
                if (len == -1) {
                    break;
                }
                if (len == 0) {
                    continue;
                }
                receiveBuffer.flip();
                String response = StandardCharsets.UTF_8.decode(receiveBuffer).toString();
                log.info("客户端收到：{}", response);
                break;
            }
        }
    }
}