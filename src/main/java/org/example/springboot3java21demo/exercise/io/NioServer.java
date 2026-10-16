package org.example.springboot3java21demo.exercise.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Slf4j
public class NioServer {

    public static void main(String[] args) throws IOException {
        // 1. 创建 Selector
        try (Selector selector = Selector.open();
             ServerSocketChannel server = ServerSocketChannel.open()
        ) {
            // 2. 配置服务端非阻塞模式
            server.configureBlocking(false);
            // 3. 绑定端口
            server.bind(new InetSocketAddress(8080));
            // 4. 注册连接接入事件
            server.register(selector, SelectionKey.OP_ACCEPT);
            log.info("NIO 服务端启动，端口：8080");
            // 5. 事件循环
            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    try {
                        if (key.isAcceptable()) {
                            // 有新客户端连接
                            accept(selector, server);
                        } else if (key.isReadable()) {
                            // 客户端发送了数据
                            read(key);
                        } else if (key.isWritable()) {
                            // Socket 可以继续写入数据
                            write(key);
                        }
                    } catch (IOException e) {
                        closeKey(key);
                        log.info("连接已关闭：{}", e.getMessage());
                    }
                }
            }
        }
    }

    // 接收客户端连接
    private static void accept(Selector selector, ServerSocketChannel server) throws IOException {
        SocketChannel client = server.accept();
        if (client == null) {
            return;
        }
        client.configureBlocking(false);
        // 每个客户端拥有独立的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        client.register(selector, SelectionKey.OP_READ, buffer);
        log.info("客户端连接：{}", client.getRemoteAddress());
    }

    // 读取客户端数据
    private static void read(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        int len = client.read(buffer);
        if (len == -1) {
            closeKey(key);
            return;
        }
        if (len == 0) {
            return;
        }
        // 从写模式切换到读模式
        buffer.flip();
        // 将收到的字节解码为 UTF-8 文本用于日志
        String message = StandardCharsets.UTF_8.decode(buffer.asReadOnlyBuffer()).toString();
        log.info("收到客户端消息：{}", message);
        // 切换到写事件，处理可能未写完的数据
        key.interestOps(SelectionKey.OP_WRITE);
//        write(key);
    }

    // 将数据回写给客户端
    private static void write(SelectionKey key) throws IOException {
        ByteBuffer buffer;
        try (SocketChannel client = (SocketChannel) key.channel()) {
            buffer = (ByteBuffer) key.attachment();
            client.write(buffer);
            if (buffer.hasRemaining()) {
                // 非阻塞写入可能只写入部分数据
                key.interestOps(SelectionKey.OP_WRITE);
            } else {
                // 数据全部写完，重置缓冲区，继续监听读取
                buffer.clear();
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    // 关闭连接并取消注册
    private static void closeKey(SelectionKey key) {
        key.cancel();
        try {
            key.channel().close();
        } catch (IOException ignored) {
            // 忽略关闭时的异常
        }
    }
}
