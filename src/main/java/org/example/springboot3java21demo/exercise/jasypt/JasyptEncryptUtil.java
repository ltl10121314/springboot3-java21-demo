package org.example.springboot3java21demo.exercise.jasypt;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 * 加密/解密
 */
@Slf4j
public class JasyptEncryptUtil {

    public static void main(String[] args) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        //加密所需的salt(盐)，注意要与 配置文件中设置jasypt.encryptor.password相同
        String salt = "salt";
        //需要加密的数据
        String data = "password";
        log.info("加密的数据:{}", data);
        textEncryptor.setPassword(salt);
        //加密数据
        String encryptValue = textEncryptor.encrypt(data);
        log.info("加密结果：{}", encryptValue);
        //解密数据
        String decryptValue = textEncryptor.decrypt(encryptValue);
        log.info("解密结果：{}", decryptValue);
    }
}
