package org.example.springboot3java21demo.exercise.filter;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 
 * @date 2020/7/19 16:23
 * @description
 */
public class FilterTest {
    private static final Logger log = LoggerFactory.getLogger(FilterTest.class);

    @Test
    public void infoTest() throws Exception {
        byte[] data = "hello, world!".getBytes(StandardCharsets.UTF_8);
        log.info(String.valueOf(data));
        try (CountInputStream input = new CountInputStream(new ByteArrayInputStream(data))) {
            int n;
            while ((n = input.read()) != -1) {
                System.out.println((char) n);
            }
            System.out.println("Total read " + input.getBytesRead() + " bytes");
        }
    }
}


class CountInputStream extends FilterInputStream {
    private int count = 0;

    CountInputStream(ByteArrayInputStream in) {
        super(in);
    }

    public int getBytesRead() {
        return this.count;
    }

    @Override
    public int read() throws IOException {
        int n = in.read();
        if (n != -1) {
            this.count++;
        }
        return n;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = in.read(b, off, len);
        this.count += n;
        return n;
    }
}
