package org.example.springboot3java21demo.exercise.constructor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 生成器测试
 */
public class ComputerInstance {
    private static final Logger Logger = LoggerFactory.getLogger(ComputerInstance.class);

    public static void main(String[] args) {
        Computer computer = new Computer.Builder()
                .setCpu("英特尔")
                .setRam("三星")
                .setDisplay("制作芯片")
                .setKeyboard("好用")
                .setUsbCount(12)
                .build();
        System.out.println(computer);
        Logger.info("{}", computer);
        System.out.println(computer);
        System.out.println(ThreadLocalRandom.current().nextInt(10));
        ArrayList<Object> objects = new ArrayList<>();
        objects.add("hehe");
        System.out.println(objects.contains("he"));

        String hehe = "hshshs";
        System.out.println(hehe.contains("s"));
    }
//    public static <F,T> T newAndCopy(F fromEntity, Class<T> toClass){
//        return newAndCopyOpt(fromEntity,toClass);
//    }
}
