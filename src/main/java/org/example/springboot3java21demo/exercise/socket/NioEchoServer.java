package org.example.springboot3java21demo.exercise.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NioEchoServer {
    private static final int PORT = 8088;
    private static final int BUFFER_SIZE = 1024;
    /**
     * 单个连接待发送数据的上限，防止对端一直不读导致内存无限增长
     */
    private static final int MAX_PENDING_BYTES = 1024 * 1024;

    private volatile boolean running = true;
    private Selector selector;
    private ServerSocketChannel serverChannel;

    /**
     * 事件循环退出并完成资源清理后倒计数，供 shutdown hook 等待优雅停机
     */
    private final CountDownLatch terminated = new CountDownLatch(1);

    public static void main(String[] args) throws IOException {
        NioEchoServer server = new NioEchoServer();
        server.start();

        // Ctrl+C / kill(SIGTERM) 时触发：唤醒 select() 让事件循环退出，并等待清理完成
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("NIO Echo Server did not stop within 5s");
            }
        }, "nio-echo-shutdown"));

        server.loop();
    }

    public void start() throws IOException {
        // 1. 打开 ServerSocketChannel
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(PORT));

        // 2. 打开 Selector
        selector = Selector.open();

        // 3. 把 ServerSocketChannel 注册到 Selector，监听 OP_ACCEPT
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        log.info("NIO Echo Server started on port {}", PORT);
    }

    /**
     * 停止服务：设置退出标志并唤醒可能阻塞在 select() 上的事件循环
     */
    public void stop() {
        running = false;
        Selector s = selector;
        if (s != null) {
            s.wakeup();
        }
    }

    /**
     * 等待事件循环退出并完成资源清理
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        try {
            return terminated.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void loop() {
        try {
            while (running) {
                selector.select(); // 阻塞等待就绪事件，stop() 里的 wakeup() 可以唤醒它
                if (!running) {
                    break;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove(); // 必须手动移除，防止重复处理

                    // 同一批次里前面的处理可能已经让 key 失效
                    if (!key.isValid()) {
                        continue;
                    }

                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key, selector);
                        } else {
                            if (key.isReadable()) {
                                handleRead(key);
                            }
                            // handleRead 可能已经把连接关掉了
                            if (key.isValid() && key.isWritable()) {
                                handleWrite(key);
                            }
                        }
                    } catch (IOException | RuntimeException e) {
                        // 单个连接出错只影响它自己，不能拖垮整个事件循环
                        log.warn("Closing connection due to error: {}", e.toString());
                        closeKey(key);
                    }
                }
            }
        } catch (ClosedSelectorException e) {
            log.info("Selector closed, event loop exits");
        } catch (IOException e) {
            log.error("Event loop terminated abnormally", e);
        } finally {
            shutdown();
            terminated.countDown();
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) {
            return;
        }

        try {
            clientChannel.configureBlocking(false);
            // 注册读事件，并给每个连接挂一个状态对象，保存没写完的数据
            clientChannel.register(selector, SelectionKey.OP_READ, new Connection());
            log.info("Client connected: {}", clientChannel.getRemoteAddress());
        } catch (IOException | RuntimeException e) {
            // 注册失败时必须在这里关闭刚接入的连接，外层 catch 只会关掉 serverChannel
            closeQuietly(clientChannel);
            throw e;
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        // 使用 Buffer 接收数据
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            // 先取地址再关闭：close() 之后调 getRemoteAddress() 会抛 ClosedChannelException
            SocketAddress remote = null;
            try {
                remote = clientChannel.getRemoteAddress();
            } catch (IOException ignored) {
                // 拿不到地址不影响断开
            }
            closeKey(key);
            log.info("Client disconnected: {}", remote);
            return;
        }

        if (bytesRead > 0) {
            // 切换为读模式
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data, StandardCharsets.UTF_8);
            log.info("Received: {}", message.trim());

            // 原样回写
            writeOrQueue(key, clientChannel, ByteBuffer.wrap(("Echo: " + message).getBytes(StandardCharsets.UTF_8)));
        }
    }

    /**
     * 非阻塞写：能写完就直接写完；写不完的挂到连接上并注册 OP_WRITE，
     * 等 socket 再次可写时由 handleWrite 继续，避免丢数据。
     */
    private void writeOrQueue(SelectionKey key, SocketChannel clientChannel, ByteBuffer buffer) throws IOException {
        clientChannel.write(buffer);
        if (!buffer.hasRemaining()) {
            return;
        }

        Connection connection = (Connection) key.attachment();
        connection.pendingWrites.add(buffer);
        connection.pendingBytes += buffer.remaining();

        // 对端一直不读时队列会无限增长，超过上限直接断开
        if (connection.pendingBytes > MAX_PENDING_BYTES) {
            log.warn("Pending write data exceeds {} bytes, closing connection", MAX_PENDING_BYTES);
            closeKey(key);
            return;
        }

        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Connection connection = (Connection) key.attachment();

        while (!connection.pendingWrites.isEmpty()) {
            ByteBuffer buffer = connection.pendingWrites.peek();
            int remainingBefore = buffer.remaining();
            clientChannel.write(buffer);
            connection.pendingBytes -= remainingBefore - buffer.remaining();

            if (buffer.hasRemaining()) {
                // 发送缓冲区又满了，保留 OP_WRITE，等下一次可写事件
                return;
            }
            connection.pendingWrites.poll();
        }

        // 队列清空后必须注销 OP_WRITE，否则 socket 一直可写、select() 会立即返回导致空转
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }

    /**
     * 每个客户端连接的状态：待发送的数据。
     */
    private static final class Connection {
        private final Deque<ByteBuffer> pendingWrites = new ArrayDeque<>();
        private long pendingBytes;
    }

    /**
     * 取消并关闭 key 对应的 channel，异常不外抛
     */
    private static void closeKey(SelectionKey key) {
        key.cancel();
        closeQuietly(key.channel());
    }

    /**
     * 释放所有资源。注意 Selector.close() 并不会关闭已注册的 channel，必须逐个关闭。
     */
    private void shutdown() {
        log.info("Closing all connections and releasing resources...");
        if (selector != null) {
            // 先拷贝一份，避免关闭 channel 时修改 keys 集合导致 ConcurrentModificationException
            List<SelectionKey> keys = new ArrayList<>(selector.keys());
            for (SelectionKey key : keys) {
                key.cancel();
                closeQuietly(key.channel());
            }
            closeQuietly(selector);
        }
        closeQuietly(serverChannel);
        log.info("NIO Echo Server stopped");
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            log.debug("Failed to close {}", closeable.getClass().getSimpleName(), e);
        }
    }
}
