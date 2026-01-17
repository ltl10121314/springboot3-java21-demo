package org.example.springboot3java21demo.exercise.stack;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Administrator
 */
public class GetListFile {
    private static final Logger log = LoggerFactory.getLogger(GetListFile.class);

    public static void main(String[] args) throws IOException {
//        showFileName("D:\\downloads\\合并\\合并\\lib.zip");
//        File currentDir = new File("D:\\downloads\\合并\\合并\\lib.zip");
//        listDir(currentDir.getCanonicalFile());
//        mkDir("D:\\downloads\\合并\\合并\\hahah\\test.txt");
    }

    private static void showFileName(String path) {

        File file = new File(path);
        //判断文件是否存在
        if (file.exists()) {
            //判断file是否是文件夹
            if (file.isDirectory()) {
                //获取文件下的子文件
                File[] listFiles = file.listFiles();
                // 目录下文件
                if (listFiles.length == 0) {
                    System.out.println("该文件夹下没有文件");
                }
                for (File f : listFiles) {
                    //判断file是否是文件夹
                    if (f.isDirectory()) {
                        System.out.println("文件夹：" + f.getName());
                        //文件夹就继续遍历下的子文件
                        showFileName(f.getAbsolutePath());
                    } else if (f.isFile()) {
                        System.out.println("文件：" + f.getName());
                    } else {
                        System.err.println("未知错误");
                    }

                }
            }
        }
    }

    static void listDir(File dir) {
        // TODO: 递归打印所有文件和子文件夹的内容
        File[] fs = dir.listFiles();
        System.out.println(fs[0].getName());
        if (fs != null) {
            for (File f : fs) {
                System.out.println(f.getName());
            }
        }
    }

    public static void mkDir(String toFileName) {
        File file = new File(toFileName);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                boolean flag = file.createNewFile();
                if (!flag) {
                    log.info("createNewFile failed!");
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    @Test
    public void mkDirTest() {
        mkDir("D:\\downloads\\合并\\合并\\hahah\\test.txt");
    }
}
