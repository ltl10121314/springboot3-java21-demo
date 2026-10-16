package org.example.springboot3java21demo.exercise.io;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class JavaIOTest {
    public static void main(String[] args) {

    }

    @Test
    public void inputStreamReaderTest() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream("tmp/test/test.txt"), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void fileTest() {
        File dir = new File("tmp/test");
        if (!dir.exists()) {
            boolean mkdir = dir.mkdirs();
            log.info(String.valueOf(mkdir));
        }
        File file = new File("tmp/test/test.txt");
        log.info(String.valueOf(file.exists()));
        log.info(file.getName());
        log.info(String.valueOf(file.length()));
        log.info(file.getAbsolutePath());
    }

    @Test
    public void inputStreamTest() {
        File file = new File("tmp/test/test.txt");
        try (BufferedInputStream in =
                     new BufferedInputStream(new FileInputStream(file))
        ) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                System.out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void outputStreamTest() {
        File file = new File("tmp/test/test2.txt");
        try (BufferedOutputStream out =
                     new BufferedOutputStream(new FileOutputStream(file, true))
        ) {
            String s = "Hello Java" + System.lineSeparator() + "Hello World";
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            out.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void fileReaderTest() {
        File file = new File("tmp/test/test.txt");
        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))
        ) {
            int c;
            while ((c = reader.read()) != -1) {
                System.out.print((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void fileWriterTest() {
        File file = new File("tmp/test/test3.txt");
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file))
        ) {
            writer.write("你好 Java");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void fileNioTest() {
        Path source = Path.of("tmp/test/source.txt");
        Path target = Path.of("tmp/test/target.txt");
        try (FileChannel in = FileChannel.open(source, StandardOpenOption.READ);
             FileChannel out = FileChannel.open(target, StandardOpenOption.CREATE,
                     StandardOpenOption.WRITE,
                     StandardOpenOption.TRUNCATE_EXISTING
             )
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            while (in.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    out.write(buffer);
                }
                buffer.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("NIO 文件复制完成");
    }
}
