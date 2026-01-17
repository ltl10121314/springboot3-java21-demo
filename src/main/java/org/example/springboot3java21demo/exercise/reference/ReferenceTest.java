package org.example.springboot3java21demo.exercise.reference;

import org.junit.Test;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 引用测试类
 */
public class ReferenceTest {
    /**
     * 1KB
     */
    private static final int KB_1 = 1024;
    private static final int MB_1 = 1024 * KB_1;
    private static final int MB_5 = 5 * MB_1;
    private static final int MB_50 = 50 * MB_1;

    @Test
    public void strongReference() {
//        List<byte[]> list = new ArrayList<>();
//        for (int i = 0; i < 20; i++) {
//            list.add(new byte[MB_5]);
//        }
    }

    @Test
    public void softReference() throws InterruptedException {
        //创建一个软引用，引用50mb的byte数据
        SoftReference<byte[]> sr1 = new SoftReference<>(new byte[MB_50]);
        //获取软引用中的数据
        System.out.println(Arrays.toString(sr1.get()));
        //来个强引用的list
        List<byte[]> list = new ArrayList<>();
        //选好向list中添加数据，慢慢内存不足，会触发弱引用sr1中引用的50MB byte数据被回收
        for (int i = 0; i < 10; i++) {
            list.add(new byte[MB_5]);
            //获取弱引用中引用的数据
            byte[] bytes = sr1.get();
            System.out.println(list.size() + ":" + Arrays.toString(bytes));
            TimeUnit.SECONDS.sleep(1);
            if (bytes == null) {
                break;
            }
        }
    }

    @Test
    public void weakReference() throws InterruptedException {
        //创建一个弱引用，引用50mb的byte数据
        WeakReference<byte[]> sr1 = new WeakReference<>(new byte[MB_50]);
        //获取软引用中的数据
        System.out.println(sr1.get());
        System.out.println("触发gc");
        System.gc();//触发gc，会导致弱引用中的数据被回收，即sr1中引用的50mb byte被回收
        System.out.println("gc完毕");
        System.out.println(sr1.get());
    }

    @Test
    public void phantomReference() throws InterruptedException {
        //创建引用队列，当Reference对象引用的数据被回收的时候，Reference对象会被加入到这个队列中
        ReferenceQueue<byte[]> referenceQueue = new ReferenceQueue<>();
        PhantomReference<byte[]> sr1 = new PhantomReference<>(new byte[MB_50], referenceQueue);
        System.out.println(sr1);
        //获取软引用中的数据
        System.out.println(sr1.get());
        System.out.println(referenceQueue.poll());
        System.out.println("触发gc");
        System.gc();//触发gc
        System.out.println("gc完毕");
        System.out.println(sr1.get());
        System.out.println(referenceQueue.poll());
    }
}
