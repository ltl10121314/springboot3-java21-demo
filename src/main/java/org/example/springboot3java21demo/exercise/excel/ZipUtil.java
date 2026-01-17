package org.example.springboot3java21demo.exercise.excel;//package org.example.exercise.excel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 压缩文本文档工具类
 */
@Slf4j
public class ZipUtil {

    @Test
    public void test4() {
        String[] strings = {"id", "code", "bank"};
    }

    @Test
    public void test1() {
        String str = "{\"a\":\"1\",\"b\": \"2\"}\n";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.readValue(str, new TypeReference<Map<String, Object>>() {
            });
            System.out.println(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test2() {
        // 文本文件所在目录
        String sourceFolder = "/Users/liutianlong/Downloads";
        // 输出ZIP文件名
        String zipFileName = sourceFolder + "/" + "output.zip";

        ArrayList<File> textFiles = new ArrayList<>();
        File sourceDir = new File(sourceFolder);
        String path = sourceDir.getPath();
        System.out.println(path);
        String absolutePath = sourceDir.getAbsolutePath();
        System.out.println(absolutePath);
        // 获取所有.txt文件
        File[] files = sourceDir.listFiles();
        for (File file : files) {
            System.out.println(file.getPath());
            if (file.isFile() && file.getName().endsWith(".txt")) {
                textFiles.add(file);
            }
        }
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(Paths.get(zipFileName)))) {
            for (File file : textFiles) {
                zipOut.putNextEntry(new ZipEntry(file.getName()));
                try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, bytesRead);
                    }
                }
                zipOut.closeEntry();
            }
            zipOut.flush();
            zipOut.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Compressing Done.");
    }

    @Test
    public void test3() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("名称", "a");
        map.put("编码", 1);
        map.put("身份证号码", "c");
        list.add(map);
        list.add(map);
        list.add(map);
        list.add(map);
        List<String> title = new ArrayList<>();
        title.add("名称");
        title.add("编码");
        title.add("身份证号码");

        // 文件路径
        String filePath = "/Users/liutianlong/Downloads";
        // TXT文件输出路径
        String txtFilePath = filePath + "/" + "output.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilePath))) {
            for (int k = 0; k < title.size(); k++) {
                writer.write(title.get(k));
                if (k == title.size() - 1) {
                    continue;
                }
                writer.write(" ");
            }
            writer.newLine();
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> data = list.get(0);
                Set<String> keySet = data.keySet();
                for (int j = 0; j < title.size(); j++) {
                    Object value = data.get(title.get(j));
                    if (value instanceof String) {
                        writer.write(value.toString());
                    } else if (value instanceof Number) {
                        writer.write(String.valueOf(value));
                    }
                    if (j == title.size() - 1) {
                        continue;
                    }
                    // 使用制表符分隔不同单元格的内容
                    writer.write(" ");
                }
                if (i == list.size() - 1) {
                    continue;
                }
                // 写入一个新行
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

