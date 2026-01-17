package org.example.springboot3java21demo.exercise.exception;

import com.jcraft.jsch.ChannelSftp;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 异常测试类
 */
public class ExceptionTest {
    ChannelSftp sftp = null;
    private final Logger Logger = LoggerFactory.getLogger(ExceptionTest.class);

    @Test
    public void exception1Test() {
        String a = "";
        if (a == null) {
            throw new RuntimeException("抛出异常");
        }
    }

    @Test
    public void inputStream() {
        File file = new File("hello.txt");
        try {
            InputStream fileInputStream = new FileInputStream(file);
            sftp.put(fileInputStream, "hello.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void exceptionTest() {
        try {
            int a = 1 / 0;
        } catch (Exception e) {
            Logger.info(e.getMessage());
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//            System.out.println("==========");
//            System.out.println(e.getLocalizedMessage());
        }
    }
}
