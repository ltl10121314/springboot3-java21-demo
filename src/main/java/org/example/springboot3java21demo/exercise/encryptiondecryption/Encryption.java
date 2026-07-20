package org.example.springboot3java21demo.exercise.encryptiondecryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Encryption {
    private static final Logger log = LoggerFactory.getLogger(Encryption.class);

    public static void main(String[] args) {
        String temp = "j80zc0cv-dataenc";
        log.info("hashCode={}", temp.hashCode());
        int hash = temp.hashCode();
        String sss;
        if (hash < 0) {
            sss = (hash + "").replaceFirst("-", "");
            log.info("-sss={}", sss);
        } else {
            sss = hash + "";
            log.info("sss={}", sss);
        }
    }
}
