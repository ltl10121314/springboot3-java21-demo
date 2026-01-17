package org.example.springboot3java21demo.exercise.filter;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 *
 */
public class EncodeTest {
    private static final Logger log = LoggerFactory.getLogger(EncodeTest.class);

    @Test
    public void encodingTest() throws Exception {
        byte[] b_gbk = "深".getBytes("GBK");
        byte[] b_utf8 = "深".getBytes(StandardCharsets.UTF_8);
        byte[] b_iso88591 = "深".getBytes(StandardCharsets.ISO_8859_1);
        byte[] b_unicode = "深".getBytes("unicode");
        String s_gbk = new String(b_gbk, "GBK");
        log.info(s_gbk);
        String s_utf8 = new String(b_utf8, StandardCharsets.UTF_8);
        log.info(s_utf8);
        String s_iso88591 = new String(b_iso88591, StandardCharsets.ISO_8859_1);
        log.info(s_iso88591);
        String s_unicode = new String(b_unicode, "unicode");
        log.info(s_unicode);
    }
}
